package example;

import io.micronaut.http.MutableHttpResponse;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class HelloResponseFilterSubscriber implements Subscriber<MutableHttpResponse<?>> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription s) {
        subscription = s;
        if (s != null) {
            subscription.request(1);
        }

        System.out.println(String.format("%s [%s] %s", Thread.currentThread().getName(), "HelloResponseFilterSubscriber",
                s==null ? "onSubscribe: subscription is null": "onSubscribe requested 1"));
    }

    @Override
    public void onNext(MutableHttpResponse<?> response) {
        System.out.println(String.format("%s [%s] onNext invoked with response: %s", Thread.currentThread().getName(),
                "HelloResponseFilterSubscriber", response));

    }

    @Override
    public void onError(Throwable t) {
        System.out.println(String.format("%s [%s] onError", Thread.currentThread().getName(), "HelloResponseFilterSubscriber"));

    }

    @Override
    public void onComplete() {
        System.out.println(String.format("%s [%s] onComplete", Thread.currentThread().getName(), "HelloResponseFilterSubscriber"));
    }
}
