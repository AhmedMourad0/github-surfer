package dev.ahmedmourad.githubsurfer.core.users.model

//Would've used an inline (or value) class, but it's still experimental
data class UserId(val value: Long)

data class User(
    val id: UserId,
    val name: String?,
    val login: String,
    val avatarUrl: String,
    val followersCount: Int?,
    val followingCount: Int?,
    val reposCount: Int?,
    val gistsCount: Int?,
    val bio: String?,
    val location: String?,
    val email: String?,
    val company: String?,
    val blog: String?,
    val notes: String?
)

fun User.simplify() = SimpleUser(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    hasNotes = notes != null
)
