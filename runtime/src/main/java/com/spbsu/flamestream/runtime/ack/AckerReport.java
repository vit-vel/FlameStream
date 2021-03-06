package com.spbsu.flamestream.runtime.ack;

import com.spbsu.flamestream.core.data.meta.GlobalTime;

public final class AckerReport {

  private final GlobalTime globalTime;

  private final long xor;

  /***
   * @param globalTime start time of the report.
   * @param xor xor of the dataItem acks, generated in the window [globalTime, globalTime + window)
   */
  public AckerReport(GlobalTime globalTime, long xor) {
    this.globalTime = globalTime;
    this.xor = xor;
  }

  public GlobalTime globalTime() {
    return globalTime;
  }

  public long xor() {
    return xor;
  }

  @Override
  public String toString() {
    return "AckerReport{" + "globalTime=" + globalTime +
            ", xor=" + xor +
            '}';
  }
}

