package dev.ahmedmourad.githubsurfer.users.repo

interface PreferencesManager {
    suspend fun latestPageSize(): Int?
    suspend fun updateLatestPageSize(size: Int)
}
