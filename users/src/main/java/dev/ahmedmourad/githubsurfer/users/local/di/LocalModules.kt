package dev.ahmedmourad.githubsurfer.users.local.di

import android.content.Context
import dev.ahmedmourad.githubsurfer.users.local.LocalDataSourceImpl
import dev.ahmedmourad.githubsurfer.users.local.UsersDatabase
import dev.ahmedmourad.githubsurfer.users.local.daos.UsersDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.users.repo.LocalDataSource
import javax.inject.Singleton

@Module
internal interface LocalBindingsModule {
    @Binds
    @Reusable
    fun bindLocalDataSource(
        impl: LocalDataSourceImpl
    ): LocalDataSource
}

@Module
internal object LocalProvidersModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideUsersDatabase(appCtx: Context): UsersDatabase {
        return UsersDatabase.getInstance(appCtx)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideUsersDao(db: UsersDatabase): UsersDao {
        return db.usersDao()
    }
}
