package com.spbsu.flamestream.core.data;

import com.spbsu.flamestream.core.data.meta.Meta;

public interface DataItem<T> {
  Meta meta();

  T payload();

  long ack();

  default Boolean isInvalidatedBy(DataItem<T> that) {
    return this.meta().isInvalidatedBy(that.meta());
  }
}
