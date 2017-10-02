package com.spbsu.flamestream.runtime.ack;

/**
 * User: Artem
 * Date: 02.10.2017
 */
public class RequestGlobalTime {
  private final long xor;

  public RequestGlobalTime(long xor) {
    this.xor = xor;
  }

  public long xor() {
    return xor;
  }
}
