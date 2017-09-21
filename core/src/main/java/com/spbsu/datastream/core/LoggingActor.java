package com.spbsu.datastream.core;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.Optional;

public abstract class LoggingActor extends AbstractActorWithStash {
  private final LoggingAdapter LOG = Logging.getLogger(context().system(), self());

  @Override
  public void aroundReceive(PartialFunction<Object, BoxedUnit> receive, Object msg) {
    receive.apply(msg);
    //final Message message;
    //
    //if (msg instanceof UnresolvedMessage) {
    //  message = ((UnresolvedMessage) msg).payload();
    //} else if (msg instanceof Message) {
    //  message = ((Message) msg);
    //} else {
    //  receive.apply(msg);
    //  return;
    //}
    //
    //final AtomicMessage atomicMessage;
    //
    //if (message instanceof AtomicMessage) {
    //  atomicMessage = ((AtomicMessage) message);
    //} else {
    //  receive.apply(msg);
    //  return;
    //}
    //
    //final Object data = atomicMessage.payload().payload();
    //
    //if (data.toString().contains("фигей")) {
    //  LOG.info("Before: {}", System.nanoTime());
    //  receive.apply(msg);
    //  LOG.info("After: {}", System.nanoTime());
    //} else {
    //  receive.apply(msg);
    //}
  }

  @Override
  public void preStart() throws Exception {
    LOG().info("Starting...");
    super.preStart();
  }

  @Override
  public void postStop() {
    LOG().info("Stopped");
    super.postStop();
  }

  @Override
  public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
    LOG().error("Restarting, reason: {}, payload: {}", reason, message);
    super.preRestart(reason, message);
  }

  @Override
  public void unhandled(Object message) {
    LOG().error("Can't handle payload: {}", message);
    super.unhandled(message);
  }

  @Override
  public SupervisorStrategy supervisorStrategy() {
    final SupervisorStrategy supervisorStrategy = super.supervisorStrategy();
    return SupervisorStrategy.stoppingStrategy();
  }

  protected final LoggingAdapter LOG() {
    return LOG;
  }
}
