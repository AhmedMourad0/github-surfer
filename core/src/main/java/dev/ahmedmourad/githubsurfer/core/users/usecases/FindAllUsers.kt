package dev.ahmedmourad.githubsurfer.core.users.usecases

import dev.ahmedmourad.githubsurfer.core.users.UsersRepository
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface FindAllUsers {
    fun execute(since: Long, useCache: Boolean): Flow<FindAllUsersResult>
    suspend fun findUser(id: UserId): SimpleUser?
}

@Reusable
internal class FindAllUsersImpl @Inject constructor(
    private val repository: UsersRepository
) : FindAllUsers {
    override fun execute(since: Long, useCache: Boolean): Flow<FindAllUsersResult> {
        return repository.findAllUsers(since, useCache)
    }
    override suspend fun findUser(id: UserId): SimpleUser? {
        return repository.findSimpleUser(id)
    }
}

sealed class FindAllUsersResult {
    data class UpToDate(val v: List<SimpleUser>, val since: Long) : FindAllUsersResult()
    data class Cached(val v: List<SimpleUser>, val since: Long) : FindAllUsersResult()
    object NoConnection : FindAllUsersResult()
    data class Error(val e: Throwable) : FindAllUsersResult()
}
