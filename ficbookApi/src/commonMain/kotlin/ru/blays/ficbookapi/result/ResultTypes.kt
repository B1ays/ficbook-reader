package ru.blays.ficbookapi.result

sealed class ApiResult<T: Any> {
    data class Success<T: Any>(val value: T) : ApiResult<T>()
    data class Error<T: Any>(val exception: Throwable) : ApiResult<T>()

    fun getOrNull(): T? = if(this is Success) value else null
    fun getOrElse(defaultValue: T): T = if(this is Success) value else defaultValue

    companion object {
        fun <T: Any> success(value: T): ApiResult<T> = Success(value)
        fun <T: Any> failure(exception: Throwable): ApiResult<T> = Error(exception)
    }
}