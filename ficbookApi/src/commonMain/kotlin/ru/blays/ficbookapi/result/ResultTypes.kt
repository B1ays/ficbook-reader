package ru.blays.ficbookapi.result

@JvmInline
value class StringBody(val value: String?) {
    companion object {
        val empty: StringBody get() = StringBody(null)
    }
}

sealed class ApiResult<T> {
    data class Success<T: Any>(val value: T) : ApiResult<T>()
    data class Error<T: Any>(val message: String) : ApiResult<T>() {
        constructor(throwable: Throwable): this(throwable.message ?: "")
    }

}