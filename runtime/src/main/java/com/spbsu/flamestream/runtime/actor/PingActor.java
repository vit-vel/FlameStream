package com.spbsu.flamestream.runtime.actor;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * User: Artem
 * Date: 26.09.2017
 */
public class PingActor extends LoggingActor {
  private final ActorRef actorToPing;
  private final long delayInNanos;
  private final Object objectForPing;

  private boolean started = false;
  private Cancellable scheduler;

  private PingActor(ActorRef actorToPing, long delayInNanos, Object objectForPing) {
    this.actorToPing = actorToPing;
    this.delayInNanos = delayInNanos;
    this.objectForPing = objectForPing;
  }

  public static Props props(ActorRef actorToPing, long delayInNanos, Object objectForPing) {
    return Props.create(PingActor.class, actorToPing, delayInNanos, objectForPing);
  }

  @Override
  public void postStop() {
    stop();
    super.postStop();
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(API.class, api -> {
              if (api == API.START) {
                if (!started) {
                  start();
                  started = true;
                }
              } else if (api == API.STOP) {
                if (started) {
                  stop();
                  started = false;
                }
              }
            })
            .match(InnerPing.class, innerPing -> {
              if (started)
                handleInnerPing();
            })
            .build();
  }

  private void start() throws IllegalAccessException, InstantiationException {
    if (delayInNanos >= TimeUnit.MILLISECONDS.toNanos(10)) {
      scheduler = context().system().scheduler().schedule(
              Duration.create(0, NANOSECONDS),
              FiniteDuration.apply(delayInNanos, NANOSECONDS),
              actorToPing,
              objectForPing,
              context().system().dispatcher(),
              context().parent()
      );
    } else {
      self().tell(InnerPing.PING, self());
    }
  }

  private void stop() {
    if (scheduler != null)
      scheduler.cancel();
  }

  private void handleInnerPing() throws IllegalAccessException, InstantiationException {
    actorToPing.tell(objectForPing, context().parent());
    LockSupport.parkNanos(delayInNanos);
    self().tell(InnerPing.PING, self());
  }

  public enum API {
    START,
    STOP
  }

  private enum InnerPing {
    PING
  }
}

