package example.model

/**
 * Used in RxJava/RxKotlin code to wrap either a result type, or a throwable.  Inspired by this blog entry:
 * https://rongi.github.io/kotlin-blog/rxjava/rx/2017/08/01/error-handling-in-rxjava.html
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error<out T>(val throwable: Throwable) : Result<T>()
}
