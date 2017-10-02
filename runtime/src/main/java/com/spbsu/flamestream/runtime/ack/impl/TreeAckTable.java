package com.spbsu.flamestream.runtime.ack.impl;

import com.spbsu.flamestream.runtime.ack.AckTable;

import java.util.SortedMap;
import java.util.TreeMap;

public final class TreeAckTable implements AckTable {
  // FIXME: 7/6/17 DO NOT BOX
  private final SortedMap<Long, Long> table;
  private final long startTs;
  private final long window;

  public TreeAckTable(long startTs, long window) {
    this.startTs = startTs;
    this.window = window;
    this.table = new TreeMap<>();
  }

  @Override
  public boolean ack(long ts, long xor) {
    final long lowerBound = startTs + window * ((ts - startTs) / window);
    final long updatedXor = xor ^ table.getOrDefault(lowerBound, 0L);
    if (updatedXor == 0) {
      table.remove(lowerBound);
      return true;
    } else {
      table.put(lowerBound, updatedXor);
      return false;
    }
  }

  @Override
  public long min(long ts) {
    return table.isEmpty() ? ts : Math.min(ts, table.firstKey());
  }

  @Override
  public String toString() {
    return "TreeAckTable{" + "table=" + table +
            ", startTs=" + startTs +
            ", window=" + window +
            '}';
  }
}
