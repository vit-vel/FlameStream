package com.spbsu.datastream.core.dataitem;

import com.spbsu.datastream.core.DataItem;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Experts League
 * Created by solar on 05.11.16.
 */
public class SystemTypeMeta implements DataItem.Meta<SystemTypeMeta>, Serializable {
  public static final SystemTypeMeta ZERO = new SystemTypeMeta() {
    @Override
    public int compareTo(@NotNull final SystemTypeMeta o) {
      return -1;
    }
  };

  public static final SystemTypeMeta INFINITY = new SystemTypeMeta() {
    @Override
    public int compareTo(@NotNull final SystemTypeMeta o) {
      return 1;
    }
  };

  private final long globalTime;
  private final int localTime;

  private static volatile int currentLocalTime = 0;

  private SystemTypeMeta() {
    globalTime = -1;
    localTime = currentLocalTime++;
  }

  public SystemTypeMeta(long globalTime) {
    this.globalTime = globalTime;
    localTime = currentLocalTime++;
  }

  public int tick() {
    return (int) (globalTime / 1000);
  }

  @Override
  public SystemTypeMeta advanced() {
    return new SystemTypeMeta(globalTime);
  }

  @Override
  public int compareTo(@NotNull final SystemTypeMeta o) {
    return globalTime == o.globalTime ? Integer.compare(localTime, o.localTime) : Long.compare(globalTime, o.globalTime);
  }
}
