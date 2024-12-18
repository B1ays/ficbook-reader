package ru.blays.ficbook.api.result

sealed class ApiResult<T: Any> {
    data class Success<T: Any>(val value: T) : ApiResult<T>()
    data class Error<T: Any>(val exception: Throwable) : ApiResult<T>()

    fun getOrNull(): T? = if(this is Success) value else null
    fun getOrElse(defaultValue: T): T = if(this is Success) value else defaultValue

    fun getOrThrow(exception: Throwable = IllegalStateException("Result is Error")): T =
        if(this is Success) value else throw exception

    inline fun <reified R: Any> map(transform: (T) -> R): ApiResult<R> {
        return when(this) {
            is Success -> success(transform(value))
            is Error -> failure(exception)
        }
    }

    companion object {
        fun <T: Any> success(value: T): ApiResult<T> = Success(value)
        fun <T: Any> failure(exception: Throwable): ApiResult<T> = Error(exception)
    }
}

sealed class ResponseResult<T: Any> {
    data class Success<T: Any>(val value: T): ResponseResult<T>()
    data class Error<T: Any>(val code: Int): ResponseResult<T>()

    fun getOrNull(): T? = if(this is Success) value else null
    fun getOrElse(defaultValue: T): T = if(this is Success) value else defaultValue
}