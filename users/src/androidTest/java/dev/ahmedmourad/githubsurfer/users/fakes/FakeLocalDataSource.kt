package dev.ahmedmourad.githubsurfer.users.fakes

import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import dev.ahmedmourad.githubsurfer.core.users.model.simplify
import dev.ahmedmourad.githubsurfer.users.local.toUserEntity
import dev.ahmedmourad.githubsurfer.users.local.toUser
import dev.ahmedmourad.githubsurfer.users.repo.LocalDataSource
import dev.ahmedmourad.githubsurfer.users.repo.LocalResult
import kotlinx.coroutines.CancellationException
import java.lang.Exception

class FakeLocalDataSource : LocalDataSource {

    var error: LocalResult<Nothing>? = null
    var data = mutableListOf<User>()

    override suspend fun findAllUsers(since: Long, pageSize: Int): LocalResult<List<SimpleUser>> {
        if (error != null) {
            return error!!
        }
        return exec {
            data.sortedBy { it.id.value }
                .filter { it.id.value > since }
                .take(pageSize)
                .map(User::simplify)
        }
    }

    override suspend fun insertOrUpdate(users: List<SimpleUser>): LocalResult<List<SimpleUser>> {
        if (error != null) {
            return error!!
        }
        val currentIds = data.map { it.id }
        val (toUpdate, toInsert) = users.partition { it.id in currentIds }
        val updated = data.map { user ->
            val new = toUpdate.firstOrNull { it.id == user.id } ?: return@map user
            user.copy(login = new.login, avatarUrl = new.avatarUrl)
        }
        data.clear()

        data.addAll(updated + toInsert.map { it.toUserEntity().toUser() })
        return LocalResult.Success(users)
    }

    override suspend fun insertOrUpdate(user: User): LocalResult<User> {
        if (error != null) {
            return error!!
        }
        val existing = data.firstOrNull { it.id == user.id }
        if (existing != null) {
            val index = data.indexOfFirst { it.id == user.id }
            data[index] = user.copy(notes = existing.notes)
        } else {
            data.add(user)
        }
        return LocalResult.Success(user)
    }

    override suspend fun findUser(userId: UserId): LocalResult<User?> {
        if (error != null) {
            return error!!
        }
        return LocalResult.Success(data.firstOrNull { it.id == userId })
    }

    override suspend fun findSimpleUser(userId: UserId): LocalResult<SimpleUser?> {
        if (error != null) {
            return error!!
        }
        return LocalResult.Success(data.firstOrNull { it.id == userId }?.simplify())
    }

    override suspend fun findUsersBy(query: String): LocalResult<List<SimpleUser>> {
        if (error != null) {
            return error!!
        }
        return LocalResult.Success(
            data.filter { it.login.contains(query) || (it.notes?.contains(query) == true) }.map(User::simplify)
        )
    }

    override suspend fun updateNotes(user: User): LocalResult<Unit> {
        if (error != null) {
            return error!!
        }
        val index = data.indexOfFirst { it.id == user.id }
        data[index] = data[index].copy(notes = user.notes)
        return LocalResult.Success(Unit)
    }

    override suspend fun deleteAllNoteless(): LocalResult<Unit> {
        if (error != null) {
            return error!!
        }
        data.removeIf { it.notes == null || it.notes.isNullOrEmpty() }
        return LocalResult.Success(Unit)
    }
}

private suspend fun <T> exec(block: suspend () -> T): LocalResult<T> {
    return try {
        LocalResult.Success(block())
    } catch (e: Exception) {
        if (e is CancellationException) {
            throw e
        } else {
            LocalResult.Error(e)
        }
    }
}
