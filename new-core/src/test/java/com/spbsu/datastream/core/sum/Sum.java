package com.spbsu.datastream.core.sum;

import java.util.Objects;

public final class Sum implements Numb {
  private final long value;

  public Sum(long value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return Long.toString(this.value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    final Sum sum = (Sum) o;
    return this.value == sum.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.value);
  }

  @Override
  public long value() {
    return this.value;
  }
}
