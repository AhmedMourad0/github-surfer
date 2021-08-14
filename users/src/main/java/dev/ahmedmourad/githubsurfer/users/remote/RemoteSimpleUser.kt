package dev.ahmedmourad.githubsurfer.users.remote

import com.google.gson.annotations.SerializedName
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import dev.ahmedmourad.githubsurfer.users.remote.RemoteContract.User

internal data class RemoteSimpleUser(
    @SerializedName(User.KEY_ID) val id: Long,
    @SerializedName(User.KEY_LOGIN) val login: String,
    @SerializedName(User.KEY_AVATAR_URL) val avatarUrl: String
)

internal fun RemoteSimpleUser.toSimpleUser() = SimpleUser(
    id = UserId(id),
    login = login,
    avatarUrl = avatarUrl,
    hasNotes = false
)
