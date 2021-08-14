package dev.ahmedmourad.githubsurfer.users.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.pow

private class NoConnectionException : Exception("No internet connection!")
private class CachingException : Exception("Unable to use cache!")

sealed class BackoffResult<out T : Any> {
    data class UpToDate<out T : Any>(val v: T) : BackoffResult<T>()
    data class Cached<out T : Any>(val v: T) : BackoffResult<T>()
    object NoConnection : BackoffResult<Nothing>()
    data class Error(val e: Throwable) : BackoffResult<Nothing>()
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : Any> withExponentialBackoff(
    initialDelay: Long = 1000,
    factor: Float = 2f,
    maxDelay: Long = 60_000,
    maxAttempts: Int = 40,
    fromCache: suspend (enforce: Boolean) -> LocalResult<T?>?,
    remoteCall: suspend () -> RemoteResult<T>,
    toCache: suspend (T) -> Unit
): Flow<BackoffResult<T>> {
    //Whenever the internet state changes, we retry
    return observeInternetState().flatMapLatest {
        withExponentialBackoffImpl(
            initialDelay,
            factor,
            maxDelay,
            maxAttempts,
            fromCache,
            remoteCall,
            toCache
        )
    }.flowOn(Dispatchers.IO).distinctUntilChanged().catch { cause ->
        emit(BackoffResult.Error(cause))
    }
}

fun <T : Any> withExponentialBackoffImpl(
    initialDelay: Long,
    factor: Float,
    maxDelay: Long,
    maxAttempts: Int,
    fromCache: suspend (enforce: Boolean) -> LocalResult<T?>?,
    remoteCall: suspend () -> RemoteResult<T>,
    toCache: suspend (T) -> Unit
): Flow<BackoffResult<T>> {
    return flow {
        //We first check the cache, which only happens if both useCache and enforce are true
        val cached = when (val c = fromCache(false)) {
            is LocalResult.Success -> {
                c.v
            }
            // in case of errors we halt and deliver the error
            is LocalResult.Error -> {
                emit(BackoffResult.Error(c.e))
                return@flow
            }
            null -> null
        }
        //If data is found in cache we return it and complete
        if (cached != null) {
            emit(BackoffResult.Cached(cached))
            return@flow
        }
        //Otherwise we fetch data remotely
        when (val result = remoteCall()) {
            //If data is found
            is RemoteResult.Success -> {
                //We store it in cache
                toCache(result.v)
                //Which is our source of truth
                val newlyCached = when (val c = fromCache(true)) {
                    is LocalResult.Success -> {
                        c.v
                    }
                    // in case of errors we halt and deliver the error
                    is LocalResult.Error -> {
                        emit(BackoffResult.Error(c.e))
                        return@flow
                    }
                    null -> null
                }
                //This's just for good measure, newlyCached should never be null
                if (newlyCached != null) {
                    emit(BackoffResult.UpToDate(newlyCached))
                } else {
                    emit(BackoffResult.Error(CachingException()))
                }
            }
            //If we encounter a connection problem
            RemoteResult.NoConnection -> {
                emit(BackoffResult.NoConnection)
                //We throw an exception to trigger retryWhen
                throw NoConnectionException()
            }
        }
    }.flowOn(Dispatchers.IO).retryWhen { cause, attempts ->
        //Exponential Backoff
        if (attempts < maxAttempts && cause is NoConnectionException) {
            delay(calculateDelay(initialDelay, factor, maxDelay, attempts))
            true
        } else {
            false
        }
    }.distinctUntilChanged().catch { cause ->
        if (cause is NoConnectionException) {
            emit(BackoffResult.NoConnection)
        } else {
            emit(BackoffResult.Error(cause))
        }
    }
}

private fun calculateDelay(
    initialDelay: Long,
    factor: Float,
    maxDelay: Long,
    attempts: Long
): Long {
    return (initialDelay * factor.pow(attempts.toFloat()).toLong()).coerceAtMost(maxDelay)
}

//I'm not comfortable doing this, But It's the most reliable way to do it
private fun observeInternetState(): Flow<Boolean> {
    return flow {
        while (true) {
            emit(isInternetAvailable())
            delay(10000)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)
}

private fun isInternetAvailable(): Boolean {
    return try {
        val url = URL("https://google.com")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.connect()
        connection.responseCode == 200
    } catch (e: IOException) {
        false
    }
}
