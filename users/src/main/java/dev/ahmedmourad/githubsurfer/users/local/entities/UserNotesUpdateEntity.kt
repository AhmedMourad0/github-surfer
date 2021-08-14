package dev.ahmedmourad.githubsurfer.users.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import dev.ahmedmourad.githubsurfer.users.local.LocalContract.User

@Entity
internal data class UserNotesUpdateEntity(
    @ColumnInfo(name = User.COL_ID)
    val id: Long,
    @ColumnInfo(name = User.COL_NOTES)
    val notes: String?,
)

internal fun UserEntity.toUserNotesUpdateEntity() = UserNotesUpdateEntity(
    id = id,
    notes = notes
)
