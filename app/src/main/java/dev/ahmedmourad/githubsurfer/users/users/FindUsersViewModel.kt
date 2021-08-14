package dev.ahmedmourad.githubsurfer.users.users

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.common.AssistedViewModelFactory
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindAllUsers
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindAllUsersResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

//By combining these values into a single immutable class, we assert
// that changes to them are done atomically, triggering only a single
// event instead of one per property
data class PagingState(
    val since: Long,
    //This becomes true after the user clicks our fab or swipes to refresh, and it stays true
    val enforceUpToDate: Boolean,
    //A random value to bypasses the distinctUntilChanged factor of state flows,
    // enforcing a refresh everytime it changes
    val refresh: String
)

//We start by looking for cached data, while simultaneously fetching
// data from the backend, if there's data in cache it is displayed, then,
// when the remote call finished, if it's successful and there's no cache
// data displayed, the remote data is displayed, if there is already data being
// displayed, we show the user a fab indicating that newer data is available
@OptIn(ExperimentalCoroutinesApi::class)
class FindUsersViewModel(
    findAllUsers: FindAllUsers
) : ViewModel() {

    // If the returned result is the same as the one already stored in "state",
    // no items are emitted (since state flows are distinctUntilChanged), so we
    // use this flow to signal that the loading has finished, no matter the result
    val endRefresh = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val refreshLastVisitedUser = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    //This's used to refresh the visited users' note indicator after they've been visited
    val lastVisitedUserId = MutableStateFlow<UserId?>(null)
    val lastVisitedUser = lastVisitedUserId.flatMapLatest { id ->
        refreshLastVisitedUser.mapLatest {
            if (id != null) {
                findAllUsers.findUser(id)
            } else {
                null
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    val pagingState = MutableStateFlow(
        PagingState(
            since = INITIAL_SINCE,
            enforceUpToDate = false,
            refresh = UUID.randomUUID().toString()
        )
    )

    //This initiates with page zero, and never changes until the user asks for up-to-date data
    private val upToDate = pagingState.filter { s ->
        //We only continue if enforceUpToDate is true or if this is just the first page
        s.since <= INITIAL_SINCE || s.enforceUpToDate
    }.flatMapLatest { s ->
        findAllUsers.execute(s.since, false)
    }.onEach {
        endRefresh.tryEmit(Unit)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    private val cached = pagingState.flatMapLatest { s ->
        //We use an inner flatmap to ensure both calls receive the same since value
        findAllUsers.execute(s.since, true).flatMapLatest { result ->
            //If we receive an empty list it means we are out of cached data, and
            // need to continue using remote data
            if (result == FindAllUsersResult.Cached(emptyList(), s.since)) {
                findAllUsers.execute(s.since, false)
            } else {
                flowOf(result)
            }
        }
    }.onEach {
        endRefresh.tryEmit(Unit)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    //Indicates whether up-to-date data are ready to be served
    val hasUpToDate = pagingState.distinctUntilChangedBy {
        it.enforceUpToDate //This stream only cares about changes in enforceUpToDate
    }.flatMapLatest { s ->
        if (s.enforceUpToDate) {
            //If enforce is changed to true, this becomes obsolete
            flowOf(false)
        } else {
            upToDate.map { result ->
                result is FindAllUsersResult.UpToDate && result.v.isNotEmpty()
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    val state = pagingState.distinctUntilChangedBy {
        it.enforceUpToDate //This stream only cares about changes in enforceUpToDate
    }.flatMapLatest { s ->
        if (s.enforceUpToDate) upToDate else cached
    }.map { result ->
        endRefresh.tryEmit(Unit)
        when (result) {
            is FindAllUsersResult.Cached -> State.Cached(result.v, result.since)
            is FindAllUsersResult.UpToDate -> State.UpToDate(result.v, result.since)
            is FindAllUsersResult.Error -> State.Error(result.e)
            FindAllUsersResult.NoConnection -> State.NoConnection
            null -> State.Loading
        }
    }.catch { e ->
        emit(State.Error(e))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = State.Loading
    )

    //This's me regretting not using Google's paging library
    val items = MutableStateFlow(emptyList<SimpleUser>())

    fun enforceUpToDate() {
        items.value = emptyList()
        pagingState.value = pagingState.value.copy(
            since = INITIAL_SINCE,
            enforceUpToDate = true
        )
    }

    fun onRefresh() {
        pagingState.value = pagingState.value.copy(
            since = INITIAL_SINCE,
            enforceUpToDate = true,
            refresh = UUID.randomUUID().toString()
        )
    }

    fun onRefreshLastVisitedUser() {
        refreshLastVisitedUser.tryEmit(Unit)
    }

    sealed class State {
        data class Cached(val data: List<SimpleUser>, val since: Long) : State()
        data class UpToDate(val data: List<SimpleUser>, val since: Long) : State()
        object Loading : State()
        object NoConnection : State()
        data class Error(val e: Throwable) : State()
    }

    @Reusable
    class Factory @Inject constructor(
        private val findAllUsers: FindAllUsers
    ) : AssistedViewModelFactory<FindUsersViewModel> {
        override fun invoke(handle: SavedStateHandle): FindUsersViewModel {
            return FindUsersViewModel(findAllUsers)
        }
    }

    companion object {
        const val INITIAL_SINCE = 0L
        fun defaultArgs(): Bundle? = null
    }
}
