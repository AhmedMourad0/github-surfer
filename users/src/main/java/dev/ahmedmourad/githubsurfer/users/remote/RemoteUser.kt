package dev.ahmedmourad.githubsurfer.users.remote

import com.google.gson.annotations.SerializedName
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.UserId

internal data class RemoteUser(
    @SerializedName(RemoteContract.User.KEY_ID) val id: Long,
    @SerializedName(RemoteContract.User.KEY_NAME) val name: String,
    @SerializedName(RemoteContract.User.KEY_LOGIN) val login: String,
    @SerializedName(RemoteContract.User.KEY_AVATAR_URL) val avatarUrl: String,
    @SerializedName(RemoteContract.User.KEY_FOLLOWERS_COUNT) val followersCount: Int,
    @SerializedName(RemoteContract.User.KEY_FOLLOWING_COUNT) val followingCount: Int,
    @SerializedName(RemoteContract.User.KEY_PUBLIC_REPOS) val reposCount: Int,
    @SerializedName(RemoteContract.User.KEY_PUBLIC_GISTS) val gistsCount: Int,
    @SerializedName(RemoteContract.User.KEY_EMAIL) val email: String?,
    @SerializedName(RemoteContract.User.KEY_LOCATION) val location: String?,
    @SerializedName(RemoteContract.User.KEY_BIO) val bio: String?,
    @SerializedName(RemoteContract.User.KEY_COMPANY) val company: String?,
    @SerializedName(RemoteContract.User.KEY_BLOG) val blog: String?
)

internal fun RemoteUser.toUser() = User(
    id = UserId(id),
    name = name,
    login = login,
    avatarUrl = avatarUrl,
    followersCount = followersCount,
    followingCount = followingCount,
    reposCount = reposCount,
    gistsCount = gistsCount,
    email = email,
    location = location,
    bio = bio,
    company = company,
    blog = blog,
    notes = null
)

internal fun RemoteUser.simplify() = RemoteSimpleUser(
    id = id,
    login = login,
    avatarUrl = avatarUrl
)
