package dev.ahmedmourad.githubsurfer.users.fakes

import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.simplify
import dev.ahmedmourad.githubsurfer.users.repo.RemoteDataSource
import dev.ahmedmourad.githubsurfer.users.repo.RemoteResult

class FakeRemoteDataSource : RemoteDataSource {

    var error: RemoteResult<Nothing>? = null
    var pageSize = 30
    var data = emptyList<User>()

    override suspend fun findAllUsers(since: Long): RemoteResult<List<SimpleUser>> {
        if (error != null) {
            return error!!
        }
        return RemoteResult.Success(
            data.sortedBy { it.id.value }
                .filter { it.id.value > since }
                .take(pageSize)
                .map(User::simplify)
        )
    }

    override suspend fun findUser(user: SimpleUser): RemoteResult<User> {
        if (error != null) {
            return error!!
        }
        return RemoteResult.Success(data.first { it.id == user.id })
    }
}
