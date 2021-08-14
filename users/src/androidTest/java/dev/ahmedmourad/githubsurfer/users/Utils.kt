package dev.ahmedmourad.githubsurfer.users

import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.core.users.model.UserId
import dev.ahmedmourad.githubsurfer.core.users.model.simplify
import java.util.*
import kotlin.random.Random

fun randomUsers(min: Int = 5, max: Int = 25, allowNulls: Boolean = true): List<User> {
    val list = mutableListOf<User>()
    repeat(max - min) { index ->
        if (allowNulls) {
            list.add(randomNullsUser(min + index.toLong()))
        } else {
            list.add(randomUser(min + index.toLong()))
        }
    }
    return list.distinct()
}

fun randomNullsUser(id: Long) = User(
    id = UserId(id),
    login = UUID.randomUUID().toString(),
    followersCount = nullable { (0..Int.MAX_VALUE).random() },
    followingCount = nullable { (0..Int.MAX_VALUE).random() },
    gistsCount = nullable { (0..Int.MAX_VALUE).random() },
    reposCount = nullable { (0..Int.MAX_VALUE).random() },
    avatarUrl = UUID.randomUUID().toString(),
    name = nullable { UUID.randomUUID().toString() },
    location = nullable { UUID.randomUUID().toString() },
    company = nullable { UUID.randomUUID().toString() },
    blog = nullable { UUID.randomUUID().toString() },
    email = nullable { UUID.randomUUID().toString() },
    notes = nullable { UUID.randomUUID().toString() },
    bio = nullable { UUID.randomUUID().toString() },
)

fun randomUser(id: Long) = User(
    id = UserId(id),
    login = UUID.randomUUID().toString(),
    followersCount = (0..Int.MAX_VALUE).random(),
    followingCount = (0..Int.MAX_VALUE).random(),
    gistsCount = (0..Int.MAX_VALUE).random(),
    reposCount = (0..Int.MAX_VALUE).random(),
    avatarUrl = UUID.randomUUID().toString(),
    name = UUID.randomUUID().toString(),
    location = UUID.randomUUID().toString(),
    company = UUID.randomUUID().toString(),
    blog = UUID.randomUUID().toString(),
    email = UUID.randomUUID().toString(),
    notes = UUID.randomUUID().toString(),
    bio = UUID.randomUUID().toString(),
)

private fun <T> nullable(generate: () -> T): T? {
    return if (Random.nextBoolean()) null else generate()
}

fun randomSimpleUsers(min: Int = 5, max: Int = 25): List<SimpleUser> {
    return randomUsers(min, max).map(User::simplify)
}
