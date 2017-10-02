package com.spbsu.flamestream.runtime.ack;

/**
 * User: Artem
 * Date: 06.09.2017
 */
public interface AckTable {
  /**
   * @return true if min time may be updated
   */
  boolean ack(long ts, long xor);

  long min(long ts);
}
