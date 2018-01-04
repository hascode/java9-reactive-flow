package com.hascode.tutorial;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;

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

  static class NewsPublisher extends SubmissionPublisher<News> {

  }

  static class NewsSubscriber implements Flow.Subscriber<News> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
      this.subscription = subscription;
      subscription.request(1);
    }

    @Override
    public void onNext(News item) {
      System.out.printf("news received: %s (%s)\n", item.getHeadline(), item.getDate());
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
  }


  public static void main(String[] args) {
    try (NewsPublisher newsPublisher = new NewsPublisher()) {

      NewsSubscriber newsSubscriber = new NewsSubscriber();
      newsPublisher.subscribe(newsSubscriber);

      List.of(News.create("Important news"), News.create("Some other news"),
          News.create("And news, news, newws")).forEach(newsPublisher::submit);
    }
  }
}
