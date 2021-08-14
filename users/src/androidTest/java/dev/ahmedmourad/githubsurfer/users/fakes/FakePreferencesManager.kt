package dev.ahmedmourad.githubsurfer.users.fakes

import dev.ahmedmourad.githubsurfer.users.repo.PreferencesManager

class FakePreferencesManager : PreferencesManager {

    var latestPageSize: Int? = null

    override suspend fun latestPageSize(): Int? {
        return latestPageSize
    }

    override suspend fun updateLatestPageSize(size: Int) {
        latestPageSize = size
    }
}
