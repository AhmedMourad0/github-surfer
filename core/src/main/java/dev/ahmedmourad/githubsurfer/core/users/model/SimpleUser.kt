package dev.ahmedmourad.githubsurfer.core.users.model

data class SimpleUser(
    val id: UserId,
    val login: String,
    val avatarUrl: String,
    val hasNotes: Boolean
)
