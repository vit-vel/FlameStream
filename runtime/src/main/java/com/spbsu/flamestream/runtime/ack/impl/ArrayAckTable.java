package com.spbsu.flamestream.runtime.ack.impl;

import com.spbsu.flamestream.runtime.ack.AckTable;

public final class ArrayAckTable implements AckTable {
  private final long startTs;
  private final long window;
  private final long[] xorStorage;

  private int minPosition;

  public ArrayAckTable(long startTs, long stopTs, long window) {
    this.startTs = startTs;
    this.window = window;

    final int xorStorageSize = Math.toIntExact((stopTs - startTs) / window);
    this.xorStorage = new long[xorStorageSize];
    this.minPosition = 0;
  }

  @Override
  public boolean ack(long ts, long xor) {
    final int position = position(ts);
    final long updatedXor = xor ^ xorStorage[position];
    xorStorage[position] = updatedXor;
    return updatedXor == 0 && xor != 0 && position == minPosition;
  }

  private int position(long ts) {
    return Math.toIntExact(((ts - startTs) / window));
  }

  @Override
  public long min(long ts) {
    final int position = position(ts);
    while (minPosition <= position && xorStorage[minPosition] == 0)
      this.minPosition++;
    return startTs + window * minPosition;
  }

  @Override
  public String toString() {
    return "ArrayAckTable{" +
            ", startTs=" + startTs +
            ", window=" + window +
            '}';
  }
}
