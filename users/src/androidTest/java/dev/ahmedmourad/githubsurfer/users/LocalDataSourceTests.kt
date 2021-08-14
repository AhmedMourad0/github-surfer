package dev.ahmedmourad.githubsurfer.users

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.simplify
import dev.ahmedmourad.githubsurfer.users.local.LocalDataSourceImpl
import dev.ahmedmourad.githubsurfer.users.local.UsersDatabase
import dev.ahmedmourad.githubsurfer.users.local.entities.UserEntity
import dev.ahmedmourad.githubsurfer.users.local.toUser
import dev.ahmedmourad.githubsurfer.users.repo.LocalDataSource
import dev.ahmedmourad.githubsurfer.users.repo.LocalResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import java.util.*

@RunWith(AndroidJUnit4::class)
class LocalDataSourceTests {

    private lateinit var db: UsersDatabase
    private lateinit var source: LocalDataSource

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, UsersDatabase::class.java).build()
        source = LocalDataSourceImpl(db.usersDao())
    }

    @Test
    @Throws(Exception::class)
    fun findAllUsers_returnsAPageSizeOfUsersStartingAfterIdOfSince() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomUsers(5, 25).onEach {
            assertEquals(LocalResult.Success(it), source.insertOrUpdate(it))
        }

        suspend fun go(since: Long, pageSize: Int) {
            val retrieved = source.findAllUsers(since, pageSize)
            assert(retrieved is LocalResult.Success)
            retrieved as LocalResult.Success
            assertTrue(retrieved.v.all { it.id.value > since })
            assertEquals(
                users.filter { it.id.value > since }
                    .take(pageSize)
                    .sortedBy { it.id.value }
                    .map { it.simplify() },
                retrieved.v.sortedBy { it.id.value }
            )
        }

        go(3, 30)
        go(7, 15)
        go(4, 21)
    }

    @Test
    @Throws(Exception::class)
    fun insertOrUpdate_insert_insertsTheUserIfNotExistentOtherwiseItUpdatesIt() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomUsers(5, 25).onEach {
            assertEquals(LocalResult.Success(it), source.insertOrUpdate(it))
        }

        suspend fun go(user: User, attempt: Int) {
            assert(source.insertOrUpdate(user) is LocalResult.Success)
            val retrieved = db.usersDao().findAllUsersForTesting().first().map(UserEntity::toUser)
            assertTrue(retrieved.size - users.size == attempt)
            assertTrue(retrieved.contains(user))
        }

        go(randomUser(50), 1)
        go(randomUser(77), 2)
        go(randomUser(64), 3)
    }

    @Test
    @Throws(Exception::class)
    fun insertOrUpdate_bulkInsert_insertsTheUserIfNotExistentOtherwiseItUpdatesIt() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomUsers(5, 25).onEach {
            assertEquals(LocalResult.Success(it), source.insertOrUpdate(it))
        }

        suspend fun go(u: List<SimpleUser>, extra: Int) {
            assert(source.insertOrUpdate(u) is LocalResult.Success)
            val retrieved = db.usersDao().findAllUsersForTesting().first()
                .map(UserEntity::toUser)
                .map(User::simplify)
            assertTrue((retrieved.size - users.size) == (extra + u.size))
            assertTrue(containsIgnoringNotes(retrieved, u))
        }

        go(randomSimpleUsers(30, 40), 0)
        go(randomSimpleUsers(53, 62), 10)
        go(randomSimpleUsers(75, 80), 19)
    }

    @Test
    @Throws(Exception::class)
    fun insertOrUpdate_update_insertsTheUserIfNotExistentOtherwiseItUpdatesIt() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomUsers(5, 25).onEach {
            assertEquals(LocalResult.Success(it), source.insertOrUpdate(it))
        }

        suspend fun go(user: User) {
            assert(source.insertOrUpdate(user) is LocalResult.Success)
            val retrieved = db.usersDao().findAllUsersForTesting().first().map(UserEntity::toUser)
            assertEquals(retrieved.size, users.size)
            val notes = retrieved.first { it.id == user.id }.notes
            assertTrue(retrieved.contains(user.copy(notes = notes)))
        }

        go(randomUser(7))
        go(randomUser(14))
        go(randomUser(23))
    }

    @Test
    @Throws(Exception::class)
    fun insertOrUpdate_bulkUpdate_insertsTheUserIfNotExistentOtherwiseItUpdatesIt() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomUsers(5, 25).onEach {
            assertEquals(LocalResult.Success(it), source.insertOrUpdate(it))
        }

        suspend fun go(u: List<SimpleUser>) {
            assert(source.insertOrUpdate(u) is LocalResult.Success)
            val retrieved = db.usersDao()
                .findAllUsersForTesting()
                .first()
                .map(UserEntity::toUser)
                .map(User::simplify)
            assertEquals(retrieved.size, users.size)
            assertTrue(containsIgnoringNotes(retrieved, u))
        }

        go(randomSimpleUsers(7, 10))
        go(randomSimpleUsers(9, 15))
        go(randomSimpleUsers(5, 10))
    }

    @Test
    @Throws(Exception::class)
    fun findUser_returnsTheUserOfTheGivenIdOrNullOtherwise() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomUsers(5, 25).onEach {
            assertEquals(LocalResult.Success(it), source.insertOrUpdate(it))
        }

        suspend fun go(user: User, exists: Boolean) {
            val retrieved = source.findUser(user.id)
            assert(retrieved is LocalResult.Success)
            retrieved as LocalResult.Success
            if (exists) {
                assertEquals(user, retrieved.v)
            } else {
                assertEquals(null, retrieved.v)
            }
        }

        go(users.random(), true)
        go(randomUser(100), false)
        go(users.random(), true)
    }

    @Test
    @Throws(Exception::class)
    fun findSimpleUser_returnsTheUserOfTheGivenIdOrNullOtherwise() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomSimpleUsers(5, 25).map { it.copy(hasNotes = false) }
        assertEquals(LocalResult.Success(users), source.insertOrUpdate(users))

        suspend fun go(user: SimpleUser, exists: Boolean) {
            val retrieved = source.findSimpleUser(user.id)
            assert(retrieved is LocalResult.Success)
            retrieved as LocalResult.Success
            if (exists) {
                assertEquals(user, retrieved.v)
            } else {
                assertEquals(null, retrieved.v)
            }
        }

        go(users.random(), true)
        go(randomUser(100).simplify(), false)
        go(users.random(), true)
    }

    @Test
    @Throws(Exception::class)
    fun findUsersBy_returnsTheUsersMatchingOrLikeTheGivenQuery() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomUsers(5, 25).onEach {
            assertEquals(LocalResult.Success(it), source.insertOrUpdate(it))
        }

        suspend fun go(query: String) {
            val retrieved = source.findUsersBy(query)
            assert(retrieved is LocalResult.Success)
            retrieved as LocalResult.Success
            assertEquals(
                users.filter { it.login.contains(query) || it.notes?.contains(query) == true }
                    .sortedBy { it.id.value }
                    .map(User::simplify),
                retrieved.v.sortedBy { it.id.value }
            )
        }

        go(users.random().login)
        go(users.random().login.random().toString())
        go(users.filter { it.notes != null }.random().notes!!)
        go(users.filter { it.notes != null }.random().notes!!.random().toString())
    }

    @Test
    @Throws(Exception::class)
    fun updateNotes_updatesTheNotesOfTheGivenUser() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomUsers(5, 25).onEach {
            assertEquals(LocalResult.Success(it), source.insertOrUpdate(it))
        }

        suspend fun go(user: User) {
            assert(source.updateNotes(user) is LocalResult.Success)
            val retrieved = db.usersDao()
                .findAllUsersForTesting()
                .first()
                .first { it.id == user.id.value }
            assertEquals(
                user.notes,
                retrieved.notes
            )
        }

        go(users.random().copy(notes = UUID.randomUUID().toString()))
        go(users.random().copy(notes = null))
        go(users.random())
    }

    @Test
    @Throws(Exception::class)
    fun deleteAllNoteless_deletesAllTheUsersThatHaveNoNotes() = runBlocking {

        assertEquals(0, db.usersDao().findAllUsersForTesting().first().size)
        val users = randomUsers(5, 25).onEach {
            assertEquals(LocalResult.Success(it), source.insertOrUpdate(it))
        }

        assert(source.deleteAllNoteless() is LocalResult.Success)
        val retrieved = db.usersDao()
            .findAllUsersForTesting()
            .first()
            .map(UserEntity::toUser)
        assertEquals(
            users.filter { it.notes != null }.sortedBy { it.id.value },
            retrieved.sortedBy { it.id.value }
        )
    }

    private fun containsIgnoringNotes(
        all: List<SimpleUser>,
        part: List<SimpleUser>
    ): Boolean {
        return part.all { p -> all.any { p.copy(hasNotes = it.hasNotes) == it } }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
