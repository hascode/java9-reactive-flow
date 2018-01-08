package com.hascode.tutorial;

import java.time.LocalDate;
import rx.Observable;
import rx.Subscriber;

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
    Observable.just(News.create("Important news"), News.create("Some other news"),
        News.create("And news, news, news")).subscribe(new Subscriber<News>() {

      private static final int MAX_NEWS = 3;
      private int newsReceived = 0;

      @Override
      public void onStart() {
        System.out.println("new subscription");
        request(1);
      }

      @Override
      public void onCompleted() {
        System.out.println("fetching news completed");
      }

      @Override
      public void onError(Throwable throwable) {
        System.err.printf("error occurred fetching news: %s\n", throwable.getMessage());
        throwable.printStackTrace(System.err);
      }

      @Override
      public void onNext(News news) {
        System.out.printf("news received: %s (%s)\n", news.getHeadline(), news.getDate());
        newsReceived++;
        if (newsReceived >= MAX_NEWS) {
          System.out.printf("%d news received (max: %d), cancelling subscription\n", newsReceived,
              MAX_NEWS);
          unsubscribe();
          return;
        }

        request(1);
      }
    });

  }
}
