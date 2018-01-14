package com.hascode.tutorial;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.time.LocalDate;
import java.util.List;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class Main {

  static class News {

    private final String headline;
    private final LocalDate date;

    private News(String headline, LocalDate date) {
      this.headline = headline;
      this.date = date;
    }

    public static News create(String headline) {
      return new News(headline, LocalDate.now());
    }

    public String getHeadline() {
      return headline;
    }

    public LocalDate getDate() {
      return date;
    }
  }

  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("sample-system");
    final Materializer materializer = ActorMaterializer.create(system);

    final Publisher<News> publisher =
        Source.from(List.of(News.create("Important news"), News.create("Some other news"),
            News.create("And news, news, news")))
            .runWith(Sink.asPublisher(AsPublisher.WITH_FANOUT), materializer);

    Subscriber<News> newsSubscriber = new Subscriber<>() {
      private org.reactivestreams.Subscription subscription;
      private static final int MAX_NEWS = 3;
      private int newsReceived = 0;

      @Override
      public void onSubscribe(org.reactivestreams.Subscription subscription) {
        System.out.printf("new subscription %s\n", subscription);
        this.subscription = subscription;
        subscription.request(1);
      }

      @Override
      public void onNext(News news) {
        System.out.printf("news received: %s (%s)\n", news.getHeadline(), news.getDate());
        newsReceived++;
        if (newsReceived >= MAX_NEWS) {
          System.out.printf("%d news received (max: %d), cancelling subscription\n", newsReceived,
              MAX_NEWS);
          subscription.cancel();
          return;
        }

        subscription.request(1);
      }

      @Override
      public void onError(Throwable throwable) {
        System.err.printf("error occurred fetching news: %s\n", throwable.getMessage());
        throwable.printStackTrace(System.err);
      }

      @Override
      public void onComplete() {
        System.out.println("fetching news completed");
      }
    };

    publisher.subscribe(newsSubscriber);
  }
}
