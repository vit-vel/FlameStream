package com.spbsu.datastream.core;

import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.spbsu.datastream.core.raw.RawData;

import java.util.function.Consumer;

final class CollectingActor<T> extends LoggingActor {
  private final LoggingAdapter LOG = Logging.getLogger(this);

  private final Consumer<T> queue;

  static <T> Props props(Consumer<T> queue) {
    return Props.create(CollectingActor.class, queue);
  }

  private CollectingActor(Consumer<T> queue) {
    this.queue = queue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(RawData.class, m -> m.forEach(o -> {
              if (o.toString().contains("askdfwladsjflak")) {
                LOG.info("Received in collector {}", System.nanoTime());
              }
              queue.accept((T) o);
            }))
            .build();
  }
}
