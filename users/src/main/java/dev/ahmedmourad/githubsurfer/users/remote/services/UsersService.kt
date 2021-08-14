package dev.ahmedmourad.githubsurfer.users.remote.services

import dev.ahmedmourad.githubsurfer.users.remote.RemoteSimpleUser
import dev.ahmedmourad.githubsurfer.users.remote.RemoteUser
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://api.github.com/"

internal interface UsersService {

    @GET("users")
    suspend fun getUsers(@Query("since") since: Long): List<RemoteSimpleUser>

    @GET("users/{login}")
    suspend fun getUser(@Path("login") login: String): RemoteUser
}

internal fun createRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
