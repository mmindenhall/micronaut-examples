package example;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

@Filter("/**")
public class HelloFilter implements HttpServerFilter {

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {

        System.out.println(String.format("%s [%s] doFilter(1) before proceed", Thread.currentThread().getName(), "HelloFilter"));

        Publisher<MutableHttpResponse<?>> responsePublisher = chain.proceed(request);

        System.out.println(String.format("%s [%s] doFilter(2) after proceed", Thread.currentThread().getName(), "HelloFilter"));

        responsePublisher.subscribe(new HelloResponseFilterSubscriber());

        System.out.println(String.format("%s [%s] doFilter(3) after subscribe", Thread.currentThread().getName(), "HelloFilter"));

        return responsePublisher;
    }
}
