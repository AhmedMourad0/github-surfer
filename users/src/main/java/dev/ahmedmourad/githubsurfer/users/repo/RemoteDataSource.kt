package dev.ahmedmourad.githubsurfer.users.repo

import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User

internal interface RemoteDataSource {
    suspend fun findAllUsers(since: Long): RemoteResult<List<SimpleUser>>
    suspend fun findUser(user: SimpleUser): RemoteResult<User>
}

sealed class RemoteResult<out T : Any> {
    data class Success<out T : Any>(val v: T) : RemoteResult<T>()
    object NoConnection : RemoteResult<Nothing>()
    data class Error(val e: Throwable) : RemoteResult<Nothing>()
}
