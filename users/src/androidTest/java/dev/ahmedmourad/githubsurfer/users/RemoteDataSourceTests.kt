package dev.ahmedmourad.githubsurfer.users

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.simplify
import dev.ahmedmourad.githubsurfer.users.fakes.FakeUsersService
import dev.ahmedmourad.githubsurfer.users.remote.RemoteDataSourceImpl
import dev.ahmedmourad.githubsurfer.users.remote.RemoteUser
import dev.ahmedmourad.githubsurfer.users.remote.services.UsersService
import dev.ahmedmourad.githubsurfer.users.remote.toUser
import dev.ahmedmourad.githubsurfer.users.repo.RemoteDataSource
import dev.ahmedmourad.githubsurfer.users.repo.RemoteResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import java.util.*

@RunWith(AndroidJUnit4::class)
class RemoteDataSourceTests {

    private lateinit var service: FakeUsersService
    private lateinit var source: RemoteDataSource

    @Before
    fun setup() {
        service = FakeUsersService()
        source = RemoteDataSourceImpl(service)
    }

    @Test
    fun findAllUsers_returnsAllUsersStartingAfterSince() = runBlocking {
        val data = randomUsers(5, 25, false)
            .map(User::toRemoteUser)
        service.data = data

        suspend fun go(since: Long) {
            val fetched = source.findAllUsers(since)
            assertTrue(fetched is RemoteResult.Success)
            fetched as RemoteResult.Success
            assertEquals(
                data.filter { it.id > since }.map { it.toUser().simplify() },
                fetched.v
            )
        }

        go(4)
        go(5)
        go(7)
        go(15)
        go(25)
        go(30)
    }

    @Test
    fun findUser_returnsTheUserOfTheGivenLogin() = runBlocking {
        val data = randomUsers(5, 25, false)
            .map(User::toRemoteUser)
        service.data = data

        suspend fun go(user: SimpleUser) {
            val fetched = source.findUser(user)
            assertTrue(fetched is RemoteResult.Success)
            fetched as RemoteResult.Success
            assertEquals(
                data.first { it.login == user.login }.toUser(),
                fetched.v
            )
        }

        go(data.random().toUser().simplify())
        go(data.random().toUser().simplify())
        go(data.random().toUser().simplify())
    }
}

private fun User.toRemoteUser() = RemoteUser(
    id = id.value,
    name = name!!,
    login = login,
    avatarUrl = avatarUrl,
    followersCount = followersCount!!,
    followingCount = followingCount!!,
    reposCount = reposCount!!,
    gistsCount = gistsCount!!,
    email = email,
    location = location,
    bio = bio,
    company = company,
    blog = blog
)
