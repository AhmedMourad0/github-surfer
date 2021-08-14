package dev.ahmedmourad.githubsurfer.users.repo

import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.UserId

internal interface LocalDataSource {
    suspend fun findAllUsers(since: Long, pageSize: Int): LocalResult<List<SimpleUser>>
    suspend fun insertOrUpdate(users: List<SimpleUser>): LocalResult<List<SimpleUser>>
    suspend fun insertOrUpdate(user: User): LocalResult<User>
    suspend fun findUser(userId: UserId): LocalResult<User?>
    suspend fun findSimpleUser(userId: UserId): LocalResult<SimpleUser?>
    suspend fun findUsersBy(query: String): LocalResult<List<SimpleUser>>
    suspend fun updateNotes(user: User): LocalResult<Unit>
    suspend fun deleteAllNoteless(): LocalResult<Unit>
}

sealed class LocalResult<out T> {
    data class Success<out T>(val v: T) : LocalResult<T>()
    data class Error(val e: Throwable) : LocalResult<Nothing>()
}
