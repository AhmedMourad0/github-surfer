package dev.ahmedmourad.githubsurfer.core.users

import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindAllUsersResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUserResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUsersByResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.UpdateNotesResult
import kotlinx.coroutines.flow.Flow

interface UsersRepository {
    fun findAllUsers(since: Long, useCache: Boolean): Flow<FindAllUsersResult>
    suspend fun findUsersBy(query: String): FindUsersByResult
    suspend fun findSimpleUser(id: UserId): SimpleUser?
    fun findUser(user: SimpleUser, useCache: Boolean): Flow<FindUserResult>
    suspend fun updateNotes(user: User): UpdateNotesResult
}
