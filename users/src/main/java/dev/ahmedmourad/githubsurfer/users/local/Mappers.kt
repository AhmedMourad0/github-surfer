package dev.ahmedmourad.githubsurfer.users.local

import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import dev.ahmedmourad.githubsurfer.users.local.entities.SimpleUserEntity
import dev.ahmedmourad.githubsurfer.users.local.entities.UserEntity

internal fun User.toUserEntity(): UserEntity = UserEntity(
    id = id.value,
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
    notes = notes
)

internal fun UserEntity.toUser(): User = User(
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
    notes = notes
)

internal fun SimpleUserEntity.toSimpleUser(): SimpleUser = SimpleUser(
    id = UserId(id),
    login = login,
    avatarUrl = avatarUrl,
    hasNotes = notes != null
)

internal fun SimpleUser.toUserEntity(): UserEntity = UserEntity(
    id = id.value,
    name = null,
    login = login,
    avatarUrl = avatarUrl,
    followersCount = null,
    followingCount = null,
    reposCount = null,
    gistsCount = null,
    email = null,
    location = null,
    bio = null,
    company = null,
    blog = null,
    notes = null
)
