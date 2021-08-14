package dev.ahmedmourad.githubsurfer.users.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ahmedmourad.githubsurfer.users.local.LocalContract.User

internal data class SimpleUserEntity(

    @ColumnInfo(name = User.COL_ID)
    val id: Long = 0,

    @ColumnInfo(name = User.COL_LOGIN)
    val login: String,

    @ColumnInfo(name = User.COL_AVATAR_URL)
    val avatarUrl: String,

    @ColumnInfo(name = User.COL_NOTES)
    val notes: String?
)
