package dev.ahmedmourad.githubsurfer.users.remote

import dagger.Reusable
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.users.remote.services.UsersService
import dev.ahmedmourad.githubsurfer.users.repo.RemoteDataSource
import dev.ahmedmourad.githubsurfer.users.repo.RemoteResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@Reusable
internal class RemoteDataSourceImpl @Inject constructor(
    private val service: UsersService
) : RemoteDataSource {

    override suspend fun findAllUsers(since: Long) = withContext(Dispatchers.IO) {
        exec { service.getUsers(since).map(RemoteSimpleUser::toSimpleUser) }
    }

    override suspend fun findUser(user: SimpleUser) = withContext(Dispatchers.IO) {
        exec { service.getUser(user.login).toUser() }
    }
}

private suspend fun <T : Any> exec(block: suspend () -> T): RemoteResult<T> {
    return try {
        RemoteResult.Success(block())
    } catch (e: Exception) {
        when (e) {
            is CancellationException -> throw e
            is SocketTimeoutException,
            is SocketException,
            is IOException,
            is UnknownHostException -> RemoteResult.NoConnection
            else -> RemoteResult.Error(e)
        }
    }
}
