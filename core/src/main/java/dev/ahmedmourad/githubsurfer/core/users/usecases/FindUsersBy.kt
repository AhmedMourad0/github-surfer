package dev.ahmedmourad.githubsurfer.core.users.usecases

import dev.ahmedmourad.githubsurfer.core.users.UsersRepository
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import javax.inject.Inject

interface FindUsersBy {
    suspend fun execute(query: String): FindUsersByResult
    suspend fun findUser(id: UserId): SimpleUser?
}

@Reusable
internal class FindUsersByImpl @Inject constructor(
    private val repository: UsersRepository
) : FindUsersBy {
    override suspend fun execute(query: String): FindUsersByResult {
        return repository.findUsersBy(query)
    }
    override suspend fun findUser(id: UserId): SimpleUser? {
        return repository.findSimpleUser(id)
    }
}

sealed class FindUsersByResult {
    data class Data(val v: List<SimpleUser>) : FindUsersByResult()
    data class Error(val e: Throwable) : FindUsersByResult()
}
