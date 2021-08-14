package dev.ahmedmourad.githubsurfer.core.users.usecases

import dev.ahmedmourad.githubsurfer.core.users.UsersRepository
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.core.users.model.User
import javax.inject.Inject

interface UpdateNotes {
    suspend fun execute(user: User): UpdateNotesResult
}

@Reusable
internal class UpdateNotesImpl @Inject constructor(
    private val repository: UsersRepository
) : UpdateNotes {
    override suspend fun execute(user: User): UpdateNotesResult {
        return repository.updateNotes(user)
    }
}

sealed class UpdateNotesResult {
    data class Success(val v:User) : UpdateNotesResult()
    data class Error(val e: Throwable) : UpdateNotesResult()
}
