package dev.ahmedmourad.githubsurfer.users.preferences

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.users.repo.PreferencesManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal val KEY_LATEST_PAGE_SIZE = intPreferencesKey("latest_page_size")

@Reusable
internal class PreferencesManagerImpl @Inject constructor(
    private val context: Context
) : PreferencesManager {

    override suspend fun latestPageSize(): Int? {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_LATEST_PAGE_SIZE]
        }.firstOrNull()
    }

    override suspend fun updateLatestPageSize(size: Int) {
        context.dataStore.edit { settings ->
            settings[KEY_LATEST_PAGE_SIZE] = size
        }
    }
}
