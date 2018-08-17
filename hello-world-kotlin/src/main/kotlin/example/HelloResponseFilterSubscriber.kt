package example

import io.micronaut.http.MutableHttpResponse
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class HelloResponseFilterSubscriber: Subscriber<MutableHttpResponse<*>> {

    var subscription: Subscription? = null

    override fun onComplete() {
        println("${Thread.currentThread().name} [HelloResponseFilterSubscriber] onComplete")
    }

    override fun onSubscribe(s: Subscription?) {
        subscription = s
        s?.request(1)
        println("${Thread.currentThread().name} [HelloResponseFilterSubscriber] " + if (s==null) "onSubscribe: subscription is null" else "onSubscribe requested 1")
    }

    override fun onNext(response: MutableHttpResponse<*>?) {
        println("${Thread.currentThread().name} [HelloResponseFilterSubscriber] onNext invoked with response: $response")
    }

    override fun onError(t: Throwable?) {
        println("${Thread.currentThread().name} [HelloResponseFilterSubscriber] onError")
    }
}