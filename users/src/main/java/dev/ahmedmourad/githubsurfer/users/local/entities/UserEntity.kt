package dev.ahmedmourad.githubsurfer.users.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ahmedmourad.githubsurfer.users.local.LocalContract.User

@Entity(tableName = User.TABLE_NAME)
internal data class UserEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = User.COL_ID)
    val id: Long,

    @ColumnInfo(name = User.COL_NAME)
    val name: String?,

    @ColumnInfo(name = User.COL_LOGIN)
    val login: String,

    @ColumnInfo(name = User.COL_AVATAR_URL)
    val avatarUrl: String,

    @ColumnInfo(name = User.COL_FOLLOWERS_COUNT)
    val followersCount: Int?,

    @ColumnInfo(name = User.COL_FOLLOWING_COUNT)
    val followingCount: Int?,

    @ColumnInfo(name = User.COL_PUBLIC_REPOS)
    val reposCount: Int?,

    @ColumnInfo(name = User.COL_PUBLIC_GISTS)
    val gistsCount: Int?,

    @ColumnInfo(name = User.COL_BIO)
    val bio: String?,

    @ColumnInfo(name = User.COL_LOCATION)
    val location: String?,

    @ColumnInfo(name = User.COL_EMAIL)
    val email: String?,

    @ColumnInfo(name = User.COL_COMPANY)
    val company: String?,

    @ColumnInfo(name = User.COL_BLOG)
    val blog: String?,

    @ColumnInfo(name = User.COL_NOTES)
    val notes: String?
)
