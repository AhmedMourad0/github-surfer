package dev.ahmedmourad.githubsurfer.users.repo

import dagger.Reusable
import dev.ahmedmourad.githubsurfer.core.users.UsersRepository
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindAllUsersResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUserResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindUsersByResult
import dev.ahmedmourad.githubsurfer.core.users.usecases.UpdateNotesResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
internal class UsersRepositoryImpl @Inject constructor(
    private val local: LocalDataSource,
    private val remote: RemoteDataSource,
    private val preferences: PreferencesManager
) : UsersRepository {

    override fun findAllUsers(
        since: Long,
        useCache: Boolean
    ): Flow<FindAllUsersResult> {
        return withExponentialBackoff(
            fromCache = { enforce ->
                // cache is only checked if useCache is true of if it's enforced
                local.takeIf { useCache || enforce }
                    ?.findAllUsers(since, preferences.latestPageSize() ?: 20)
                    ?.takeIf { result ->
                        //An empty v list is translated into a null result
                        when (result) {
                            is LocalResult.Error -> {
                                Timber.e(result.e)
                                true
                            }
                            is LocalResult.Success -> result.v.isNotEmpty()
                        }
                    }
            }, remoteCall = {
                remote.findAllUsers(since)
            }, toCache = { items ->
                //toCache is only called if data is fetched from the backend,
                // if "since" is zero and data comes from the backend, it's a fresh start
                if (since == 0L) {
                    local.deleteAllNoteless()
                    preferences.updateLatestPageSize(items.size)
                }
                local.insertOrUpdate(items)
            }
        ).flowOn(Dispatchers.IO).map { result ->
            when (result) {
                is BackoffResult.Cached -> FindAllUsersResult.Cached(result.v, since)
                is BackoffResult.UpToDate -> FindAllUsersResult.UpToDate(result.v, since)
                BackoffResult.NoConnection -> FindAllUsersResult.NoConnection
                is BackoffResult.Error -> {
                    Timber.e(result.e)
                    FindAllUsersResult.Error(result.e)
                }
            }
        }
    }

    override fun findUser(
        user: SimpleUser,
        useCache: Boolean
    ): Flow<FindUserResult> {
        return withExponentialBackoff(
            fromCache = { enforce ->
                local.takeIf { useCache || enforce }
                    ?.findUser(user.id)
                    ?.takeIf { result ->
                        //A null v is translated into a null result
                        when (result) {
                            is LocalResult.Error -> {
                                Timber.e(result.e)
                                true
                            }
                            is LocalResult.Success -> {
                                //We use followersCount to check if the user details
                                // have been loaded before, if they haven't then it's
                                // considered to not exist in the cache
                                result.v?.followersCount != null
                            }
                        }
                    }
            }, remoteCall = { remote.findUser(user) },
            toCache = { local.insertOrUpdate(it) }
        ).flowOn(Dispatchers.IO).map { result ->
            when (result) {
                is BackoffResult.Cached -> FindUserResult.Success(result.v)
                is BackoffResult.UpToDate -> FindUserResult.Success(result.v)
                BackoffResult.NoConnection -> FindUserResult.NoConnection
                is BackoffResult.Error -> {
                    Timber.e(result.e)
                    FindUserResult.Error(result.e)
                }
            }
        }.distinctUntilChanged()
    }

    override suspend fun findUsersBy(query: String) = withContext(Dispatchers.IO) {
        when (val result = local.findUsersBy(query)) {
            is LocalResult.Success -> FindUsersByResult.Data(result.v)
            is LocalResult.Error -> {
                Timber.e(result.e)
                FindUsersByResult.Error(result.e)
            }
        }
    }

    override suspend fun updateNotes(user: User) = withContext(Dispatchers.IO) {
        when (val result = local.updateNotes(user)) {
            is LocalResult.Success -> UpdateNotesResult.Success(user)
            is LocalResult.Error -> {
                Timber.e(result.e)
                UpdateNotesResult.Error(result.e)
            }
        }
    }

    override suspend fun findSimpleUser(id: UserId) = withContext(Dispatchers.IO) {
        when (val result = local.findSimpleUser(id)) {
            is LocalResult.Success -> result.v
            is LocalResult.Error -> {
                Timber.e(result.e)
                null
            }
        }
    }
}
