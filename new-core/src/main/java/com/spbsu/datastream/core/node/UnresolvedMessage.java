package com.spbsu.datastream.core.node;

public final class UnresolvedMessage<T> {
  private final int destination;
  private final T payload;
  private final boolean broadcast;

  public UnresolvedMessage(final int destination, final T payload) {
    this.destination = destination;
    this.payload = payload;
    this.broadcast = false;
  }

  public UnresolvedMessage(final T payload) {
    this.destination = Integer.MAX_VALUE;
    this.payload = payload;
    this.broadcast = true;
  }

  public boolean isBroadcast() {
    return this.broadcast;
  }

  public int destination() {
    return this.destination;
  }

  public T payload() {
    return this.payload;
  }

  @Override
  public String toString() {
    return "UnresolvedMessage{" + "destination=" + this.destination +
            ", payload=" + this.payload +
            '}';
  }
}