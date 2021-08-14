package dev.ahmedmourad.githubsurfer.users.profile

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.common.AssistedViewModelFactory
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUserDetails
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUserResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.UpdateNotes
import dev.ahmedmourad.githubsurfer.core.users.usecases.UpdateNotesResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModel(
    savedStateHandle: SavedStateHandle,
    findUserDetails: FindUserDetails,
    private val updateNotes: UpdateNotes
) : ViewModel() {

    private val simpleUser = savedStateHandle
        .get<ParcelableSimpleUser>(ARG_SIMPLE_USER)!!
        .unparcel()

    private val refresh = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val notes = MutableStateFlow<String?>(null)

    val state = refresh.flatMapLatest {
        findUserDetails.execute(simpleUser, true)
    }.map { result ->
        when (result) {
            is FindUserResult.Success -> {
                if (result.v != null) {
                    State.Data(result.v!!)
                } else {
                    State.NoData
                }
            }
            is FindUserResult.Error -> State.Error(result.e)
            FindUserResult.NoConnection -> State.NoConnection
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = State.Loading
    )

    //If we receive Data then NoConnection, we want to keep the data displayed
    private val displayedUser = state.filter { s ->
        s is State.Data
    }.map { s ->
        (s as State.Data).item
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val isUserDisplayed = displayedUser.map { user ->
        user != null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    val updateNotesState = MutableStateFlow<UpdateNotesState?>(null)

    init {
        refresh.tryEmit(Unit)
    }

    fun onNotesChanged(new: String?) {
        notes.value = new.takeUnless(String?::isNullOrBlank)
    }

    fun hasNotesChanged(notes: String?): Boolean {
        return displayedUser.value
            ?.notes
            ?.takeUnless(String::isNullOrBlank) !=
                notes.takeUnless(String?::isNullOrBlank)
    }

    fun onUpdateNotes() {
        viewModelScope.launch {
            val user = displayedUser.value?.copy(notes = notes.value)
            if (user == null) {
                updateNotesState.value = UpdateNotesState.Error(IllegalArgumentException("User is null!"))
                return@launch
            }
            updateNotesState.value = when (val result = updateNotes.execute(user)) {
                is UpdateNotesResult.Success -> {
                    refresh.tryEmit(Unit)
                    UpdateNotesState.Success(result.v)
                }
                is UpdateNotesResult.Error -> UpdateNotesState.Error(result.e)
            }
        }
    }

    sealed class State {
        data class Data(val item: User) : State()
        object NoData : State()
        object Loading : State()
        object NoConnection : State()
        data class Error(val e: Throwable) : State()
    }

    sealed class UpdateNotesState {
        data class Success(val item: User) : UpdateNotesState()
        data class Error(val e: Throwable) : UpdateNotesState()
    }

    @Reusable
    class Factory @Inject constructor(
        private val findUserDetails: FindUserDetails,
        private val updateNotes: UpdateNotes
    ) : AssistedViewModelFactory<UserProfileViewModel> {
        override fun invoke(handle: SavedStateHandle): UserProfileViewModel {
            return UserProfileViewModel(handle, findUserDetails, updateNotes)
        }
    }

    companion object {

        private const val ARG_SIMPLE_USER =
            "dev.ahmedmourad.githubsurfer.users.profile.arg.SIMPLE_USER"

        fun defaultArgs(user: ParcelableSimpleUser): Bundle {
            return Bundle(1).apply {
                putParcelable(ARG_SIMPLE_USER, user)
            }
        }
    }
}
