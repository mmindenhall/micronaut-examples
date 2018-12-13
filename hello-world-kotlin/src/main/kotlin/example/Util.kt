package example

const val BACKOFF_LIMIT = 10000

fun backoff(n: Int): Boolean {
    return when(n) {
        0 -> true
        in 1..512 -> n and (n - 1) == 0
        in 513..10000 -> n % 1000 == 0
        else -> n % BACKOFF_LIMIT == 0
    }
}
