package dev.ahmedmourad.githubsurfer.users.search

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.common.AssistedViewModelFactory
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUsersBy
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUsersByResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class SearchResultsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val findUsersBy: FindUsersBy,
) : ViewModel() {

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
                findUsersBy.findUser(id)
            } else {
                null
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    val query = MutableStateFlow(savedStateHandle.get<String>(ARG_QUERY)!!)
    val state = MutableStateFlow<State>(State.Loading)

    init {
        refreshState()
    }

    fun onQueryChanged(query: String) {
        savedStateHandle[ARG_QUERY] = query
        this.query.value = query
        refreshState()
    }

    fun onRefreshLastVisitedUser() {
        refreshLastVisitedUser.tryEmit(Unit)
    }

    private fun refreshState() {
        viewModelScope.launch {
            state.value = when (val result = findUsersBy.execute(query.value)) {
                is FindUsersByResult.Data -> State.Data(result.v)
                is FindUsersByResult.Error -> State.Error(result.e)
            }
        }
    }

    sealed class State {
        data class Data(val data: List<SimpleUser>) : State()
        object Loading : State()
        data class Error(val e: Throwable) : State()
    }

    @Reusable
    class Factory @Inject constructor(
        private val findUsersBy: FindUsersBy
    ) : AssistedViewModelFactory<SearchResultsViewModel> {
        override fun invoke(handle: SavedStateHandle): SearchResultsViewModel {
            return SearchResultsViewModel(handle, findUsersBy)
        }
    }

    companion object {
        private const val ARG_QUERY = "dev.ahmedmourad.githubsurfer.users.search.arg.ARG_QUERY"
        fun defaultArgs(query: String): Bundle = Bundle(1).apply {
            putString(ARG_QUERY, query)
        }
    }
}
