package com.iptvplayer.core

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val exception: Throwable) : AppResult<Nothing>()
    object Loading : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        else -> null
    }

    companion object {
        fun <T> success(data: T): AppResult<T> = Success(data)
        fun error(exception: Throwable): AppResult<Nothing> = Error(exception)
        fun loading(): AppResult<Nothing> = Loading
    }
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onError(action: (Throwable) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(exception)
    return this
}

inline fun <T> AppResult<T>.onLoading(action: () -> Unit): AppResult<T> {
    if (this is AppResult.Loading) action()
    return this
}

suspend fun <T> runCatchingSuspend(block: suspend () -> T): AppResult<T> = try {
    AppResult.success(block())
} catch (e: Exception) {
    AppResult.error(e)
}
