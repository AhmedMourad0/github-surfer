package dev.ahmedmourad.githubsurfer.users.local

import dev.ahmedmourad.githubsurfer.users.local.daos.UsersDao
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import dev.ahmedmourad.githubsurfer.users.local.entities.SimpleUserEntity
import dev.ahmedmourad.githubsurfer.users.local.entities.toUserNotesUpdateEntity
import dev.ahmedmourad.githubsurfer.users.repo.LocalDataSource
import dev.ahmedmourad.githubsurfer.users.repo.LocalResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Reusable
internal class LocalDataSourceImpl @Inject constructor(
    private val dao: UsersDao
) : LocalDataSource {

    override suspend fun findAllUsers(
        since: Long,
        pageSize: Int
    ) = withContext(Dispatchers.IO) {
        exec {
            dao.findUsers(
                since = since,
                limit = pageSize
            ).map(SimpleUserEntity::toSimpleUser)
        }
    }

    override suspend fun insertOrUpdate(
        users: List<SimpleUser>
    ) = withContext(Dispatchers.IO) {
        exec {
            dao.bulkUpsert(users.map(SimpleUser::toUserEntity))
            users
        }
    }

    override suspend fun insertOrUpdate(
        user: User
    ) = withContext(Dispatchers.IO) {
        exec {
            dao.upsert(user.toUserEntity())
            user
        }
    }

    override suspend fun findUser(
        userId: UserId
    ) = withContext(Dispatchers.IO) {
        exec { dao.findUser(userId.value)?.toUser() }
    }


    override suspend fun findSimpleUser(
        userId: UserId
    ) = withContext(Dispatchers.IO) {
        exec { dao.findSimpleUser(userId.value)?.toSimpleUser() }
    }

    override suspend fun findUsersBy(query: String) = withContext(Dispatchers.IO) {
        exec { dao.findUsersBy(sanitizeQuery(query)).map(SimpleUserEntity::toSimpleUser) }
    }

    private fun sanitizeQuery(query: String): String {
        return "%${query.trim()}%"
    }

    override suspend fun updateNotes(user: User) = withContext(Dispatchers.IO) {
        exec {
            dao.updateNotes(user.toUserEntity().toUserNotesUpdateEntity())
        }
    }

    override suspend fun deleteAllNoteless() = withContext(Dispatchers.IO) {
        exec {
            dao.deleteAllNoteless()
            Unit
        }
    }
}

private suspend fun <T> exec(block: suspend () -> T): LocalResult<T> {
    return try {
        LocalResult.Success(block())
    } catch (e: Exception) {
        when (e) {
            is CancellationException -> throw e
            else -> LocalResult.Error(e)
        }
    }
}
