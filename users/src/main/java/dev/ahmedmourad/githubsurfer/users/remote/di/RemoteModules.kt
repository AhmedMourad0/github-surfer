package dev.ahmedmourad.githubsurfer.users.remote.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.users.remote.RemoteDataSourceImpl
import dev.ahmedmourad.githubsurfer.users.remote.services.UsersService
import dev.ahmedmourad.githubsurfer.users.remote.services.createRetrofit
import dev.ahmedmourad.githubsurfer.users.repo.RemoteDataSource
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
internal interface RemoteBindingsModule {
    @Binds
    @Reusable
    fun bindRemoteDataSource(
        impl: RemoteDataSourceImpl
    ): RemoteDataSource
}

@Module
internal object RemoteProvidersModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideRetrofit(): Retrofit {
        return createRetrofit()
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideUsersService(retrofit: Retrofit): UsersService {
        return retrofit.create(UsersService::class.java)
    }
}
