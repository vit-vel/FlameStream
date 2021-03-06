package com.spbsu.flamestream.runtime.ack;

import com.spbsu.flamestream.core.data.meta.GlobalTime;

public interface AckLedger {
  void report(GlobalTime windowHead, long xor);

  GlobalTime min();

  boolean ack(GlobalTime windowHead, long xor);
}
