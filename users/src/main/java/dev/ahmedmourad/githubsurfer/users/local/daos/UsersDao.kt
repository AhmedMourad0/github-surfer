package dev.ahmedmourad.githubsurfer.users.local.daos

import androidx.annotation.VisibleForTesting
import androidx.room.*
import dev.ahmedmourad.githubsurfer.users.local.LocalContract.User
import dev.ahmedmourad.githubsurfer.users.local.entities.*
import dev.ahmedmourad.githubsurfer.users.local.entities.SimpleUserEntity
import dev.ahmedmourad.githubsurfer.users.local.entities.UserEntity
import dev.ahmedmourad.githubsurfer.users.local.entities.UserNotesUpdateEntity
import dev.ahmedmourad.githubsurfer.users.local.entities.UserUpdateEntity
import dev.ahmedmourad.githubsurfer.users.local.entities.toUserUpdateEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class UsersDao {

    @Query(
        """
        SELECT
            ${User.COL_ID},
            ${User.COL_LOGIN},
            ${User.COL_AVATAR_URL},
            ${User.COL_NOTES}
        FROM
            ${User.TABLE_NAME}
        WHERE
            ${User.COL_ID} > :since
        ORDER BY ${User.COL_ID} ASC
        LIMIT :limit;
    """
    )
    abstract suspend fun findUsers(since: Long, limit: Int): List<SimpleUserEntity>

    @Query(
        """
        SELECT
            ${User.COL_ID},
            ${User.COL_LOGIN},
            ${User.COL_AVATAR_URL},
            ${User.COL_NOTES}
        FROM
            ${User.TABLE_NAME}
        WHERE
            ${User.COL_LOGIN} LIKE :query OR ${User.COL_NOTES} LIKE :query
        ORDER BY ${User.COL_ID} ASC;
    """
    )
    abstract suspend fun findUsersBy(query: String): List<SimpleUserEntity>

    @Query("""
        SELECT
         ${User.COL_ID},
            ${User.COL_LOGIN},
            ${User.COL_AVATAR_URL},
            ${User.COL_NOTES}
        FROM
            ${User.TABLE_NAME}
        WHERE
            ${User.COL_ID} = :id
        ORDER BY ${User.COL_ID} ASC
    """)
    abstract suspend fun findSimpleUser(id: Long): SimpleUserEntity?

    @Query("""
        SELECT
            ${User.COL_ID},
            ${User.COL_NAME},
            ${User.COL_LOGIN},
            ${User.COL_AVATAR_URL},
            ${User.COL_FOLLOWERS_COUNT},
            ${User.COL_FOLLOWING_COUNT},
            ${User.COL_PUBLIC_REPOS},
            ${User.COL_PUBLIC_GISTS},
            ${User.COL_BIO},
            ${User.COL_LOCATION},
            ${User.COL_EMAIL},
            ${User.COL_COMPANY},
            ${User.COL_BLOG},
            ${User.COL_NOTES}
        FROM
            ${User.TABLE_NAME}
        WHERE
            ${User.COL_ID} = :id
        ORDER BY ${User.COL_ID} ASC
    """)
    abstract suspend fun findUser(id: Long): UserEntity?

    @Query("DELETE FROM ${User.TABLE_NAME} WHERE ${User.COL_NOTES} IS NULL OR ${User.COL_NOTES} = '';")
    abstract suspend fun deleteAllNoteless(): Int

    @Update(entity = UserEntity::class)
    abstract suspend fun updateNotes(user: UserNotesUpdateEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insert(user: List<UserEntity>)

    @Update(entity = UserEntity::class)
    protected abstract suspend fun update(user: List<UserUpdateEntity>)

    @Transaction
    open suspend fun upsert(user: UserEntity) {
        insert(listOf(user))
        update(listOf(user.toUserUpdateEntity()))
    }

    @Transaction
    open suspend fun bulkUpsert(users: List<UserEntity>) {
        insert(users)
        update(users.map(UserEntity::toUserUpdateEntity))
    }

    @Query("""
        SELECT
            ${User.COL_ID},
            ${User.COL_NAME},
            ${User.COL_LOGIN},
            ${User.COL_AVATAR_URL},
            ${User.COL_FOLLOWERS_COUNT},
            ${User.COL_FOLLOWING_COUNT},
            ${User.COL_PUBLIC_REPOS},
            ${User.COL_PUBLIC_GISTS},
            ${User.COL_BIO},
            ${User.COL_LOCATION},
            ${User.COL_EMAIL},
            ${User.COL_COMPANY},
            ${User.COL_BLOG},
            ${User.COL_NOTES}
        FROM
            ${User.TABLE_NAME}
        ORDER BY ${User.COL_ID} ASC
    """)
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal abstract fun findAllUsersForTesting(): Flow<List<UserEntity>>
}
