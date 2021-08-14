package dev.ahmedmourad.githubsurfer.users.fakes

import dev.ahmedmourad.githubsurfer.users.remote.RemoteSimpleUser
import dev.ahmedmourad.githubsurfer.users.remote.RemoteUser
import dev.ahmedmourad.githubsurfer.users.remote.services.UsersService
import dev.ahmedmourad.githubsurfer.users.remote.simplify

internal class FakeUsersService : UsersService {

    var pageSize = 30
    var data = emptyList<RemoteUser>()

    override suspend fun getUsers(since: Long): List<RemoteSimpleUser> {
        return data.filter { it.id > since }.take(pageSize).map(RemoteUser::simplify)
    }

    override suspend fun getUser(login: String): RemoteUser {
        return data.first { it.login == login }
    }
}
