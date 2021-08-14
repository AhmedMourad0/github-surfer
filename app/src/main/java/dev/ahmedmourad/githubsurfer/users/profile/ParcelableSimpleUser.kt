package dev.ahmedmourad.githubsurfer.users.profile

import android.os.Parcelable
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParcelableSimpleUser(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val hasNotes: Boolean
) : Parcelable

fun SimpleUser.parcel() = ParcelableSimpleUser(
    id = this.id.value,
    login = this.login,
    avatarUrl = this.avatarUrl,
    hasNotes = this.hasNotes
)

fun ParcelableSimpleUser.unparcel() = SimpleUser(
    id = UserId(this.id),
    login = this.login,
    avatarUrl = this.avatarUrl,
    hasNotes = this.hasNotes
)
