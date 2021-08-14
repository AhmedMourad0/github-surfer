package dev.ahmedmourad.githubsurfer.core.users.usecases

import dev.ahmedmourad.githubsurfer.core.users.UsersRepository
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface FindUserDetails {
    fun execute(user: SimpleUser, useCache: Boolean): Flow<FindUserResult>
}

@Reusable
internal class FindUserDetailsImpl @Inject constructor(
    private val repository: UsersRepository
) : FindUserDetails {
    override fun execute(user: SimpleUser, useCache: Boolean): Flow<FindUserResult> {
        return repository.findUser(user, useCache)
    }
}

sealed class FindUserResult {
    data class Success(val v: User?) : FindUserResult()
    object NoConnection : FindUserResult()
    data class Error(val e: Throwable) : FindUserResult()
}
