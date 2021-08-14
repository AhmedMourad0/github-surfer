package dev.ahmedmourad.githubsurfer.users

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.ahmedmourad.githubsurfer.core.users.UsersRepository
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.simplify
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindAllUsersResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUserResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUsersByResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.UpdateNotesResult
import dev.ahmedmourad.githubsurfer.users.fakes.FakeLocalDataSource
import dev.ahmedmourad.githubsurfer.users.fakes.FakePreferencesManager
import dev.ahmedmourad.githubsurfer.users.fakes.FakeRemoteDataSource
import dev.ahmedmourad.githubsurfer.users.repo.LocalResult
import dev.ahmedmourad.githubsurfer.users.repo.UsersRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.lang.RuntimeException
import java.util.*

@RunWith(AndroidJUnit4::class)
class UsersRepositoryTest {

    private lateinit var local: FakeLocalDataSource
    private lateinit var remote: FakeRemoteDataSource
    private lateinit var preferences: FakePreferencesManager
    private lateinit var repo: UsersRepository

    @Before
    fun setup() {
        local = FakeLocalDataSource()
        remote = FakeRemoteDataSource()
        preferences = FakePreferencesManager()
        repo = UsersRepositoryImpl(
            local,
            remote,
            preferences
        )
    }

    @Test
    fun findAllUsers_returnAllTheUsersAfterSince_prioritizingCacheIfUseCacheIsTrue() = runBlocking {

        val data = randomUsers(5, 50, false)
        local.data = data.take(20).toMutableList()
        remote.data = data

        val pageSize = 10
        remote.pageSize = pageSize
        preferences.latestPageSize = pageSize

        val usesCacheFirst = repo.findAllUsers(4, true)
            .first()
        assertEquals(
            FindAllUsersResult.Cached(data.map(User::simplify).take(pageSize), 4),
            usesCacheFirst
        )

        val usesRemote = repo.findAllUsers(4, false)
            .first()
        assertEquals(
            FindAllUsersResult.UpToDate(data.map(User::simplify).take(pageSize), 4),
            usesRemote
        )

        local.data = mutableListOf()
        val usesRemoteIfEmptyCache = repo.findAllUsers(4, true).first()
        assertEquals(
            FindAllUsersResult.UpToDate(data.map { it.simplify().copy(hasNotes = false) }.take(pageSize), 4),
            usesRemoteIfEmptyCache
        )
    }

    @Test
    fun findUsersBy_returnsTheUsersMatchingOrLikeTheGivenQuery() = runBlocking {

        val data = randomUsers(5, 50, false)
        local.data = data.take(20).toMutableList()
        remote.data = data

        val pageSize = 10
        remote.pageSize = pageSize
        preferences.latestPageSize = pageSize

        suspend fun go(query: String) {
            val retrieved = repo.findUsersBy(query)
            assert(retrieved is FindUsersByResult.Data)
            retrieved as FindUsersByResult.Data
            assertEquals(
                data.take(20)
                    .filter { it.login.contains(query) || it.notes?.contains(query) == true }
                    .sortedBy { it.id.value }
                    .map(User::simplify),
                retrieved.v.sortedBy { it.id.value }
            )
        }

        go(data.random().login)
        go(data.random().login.random().toString())
        go(data.filter { it.notes != null }.random().notes!!)
        go(data.filter { it.notes != null }.random().notes!!.random().toString())
    }

    @Test
    fun findSimpleUser_returnsTheUserOfTheGivenIdOrNullOtherwise() = runBlocking {

        val data = randomUsers(5, 50, false)
        local.data = data.take(20).toMutableList()
        remote.data = data

        val pageSize = 10
        remote.pageSize = pageSize
        preferences.latestPageSize = pageSize

        suspend fun go(user: SimpleUser, exists: Boolean) {
            val retrieved = repo.findSimpleUser(user.id)
            if (exists) {
                assertEquals(user, retrieved)
            } else {
                assertEquals(null, retrieved)
            }
        }

        go(data[14].simplify(), true)
        go(randomUser(100).simplify(), false)
        go(data[19].simplify(), true)
    }

    @Test
    fun findUser_returnsTheUserOfTheGivenIdOrNullOtherwise() = runBlocking {

        val data = randomUsers(5, 50, false)
        local.data = data.take(20).toMutableList()
        remote.data = data

        val pageSize = 10
        remote.pageSize = pageSize
        preferences.latestPageSize = pageSize

        suspend fun go(user: User, exists: Boolean, useCache: Boolean) {
            val retrieved = repo.findUser(user.simplify(), useCache).first()
            assertTrue(retrieved is FindUserResult.Success)
            retrieved as FindUserResult.Success
            if (exists) {
                assertEquals(user, retrieved.v)
            } else {
                assertEquals(null, retrieved.v)
            }
        }

        go(data[14], exists = true, useCache = true)
        go(data[40], exists = true, useCache = true)
        go(data[19], exists = true, useCache = true)

        go(data[14], exists = true, useCache = false)
        go(data[40], exists = true, useCache = false)
        go(data[19], exists = true, useCache = false)
    }

    @Test
    fun updateNotes_updatesTheNotesOfTheGivenUser() = runBlocking {

        val data = randomUsers(5, 50, false)
        local.data = data.take(20).toMutableList()
        remote.data = data

        val pageSize = 10
        remote.pageSize = pageSize
        preferences.latestPageSize = pageSize

        suspend fun go(user: User) {
            assert(repo.updateNotes(user) is UpdateNotesResult.Success)
            val retrieved = local.data.first { it.id == user.id }
            assertEquals(
                user.notes,
                retrieved.notes
            )
        }

        go(data[14].copy(notes = UUID.randomUUID().toString()))
        go(data[19])
    }
}
