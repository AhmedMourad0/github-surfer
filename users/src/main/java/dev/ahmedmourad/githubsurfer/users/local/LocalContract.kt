package dev.ahmedmourad.githubsurfer.users.local

internal object LocalContract {

    const val DATABASE_NAME = "users"

    object User {
        const val TABLE_NAME = "user"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_LOGIN = "login"
        const val COL_AVATAR_URL = "avatar_url"
        const val COL_FOLLOWERS_COUNT = "followers_count"
        const val COL_FOLLOWING_COUNT = "following_count"
        const val COL_PUBLIC_REPOS = "public_repos"
        const val COL_PUBLIC_GISTS = "public_gists"
        const val COL_BIO = "bio"
        const val COL_LOCATION = "location"
        const val COL_EMAIL = "email"
        const val COL_COMPANY = "company"
        const val COL_BLOG = "blog"
        const val COL_NOTES = "notes"
    }
}
