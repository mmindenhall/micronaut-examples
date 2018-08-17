package example

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import org.reactivestreams.Publisher

@Filter("/**")
class HelloFilter : HttpServerFilter {

    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {

        println("${Thread.currentThread().name} [HelloFilter] doFilter (1) before proceed")

        val responsePublisher = chain.proceed(request)

        println("${Thread.currentThread().name} [HelloFilter] doFilter (2) after proceed")

        responsePublisher.subscribe(HelloResponseFilterSubscriber())

        println("${Thread.currentThread().name} [HelloFilter] doFilter (3) after subscribe")

        return responsePublisher
    }
}