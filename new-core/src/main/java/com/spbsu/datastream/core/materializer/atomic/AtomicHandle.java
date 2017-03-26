package com.spbsu.datastream.core.materializer.atomic;

import com.spbsu.datastream.core.DataItem;
import com.spbsu.datastream.core.graph.OutPort;
import com.spbsu.datastream.core.graph.TheGraph;

public interface AtomicHandle {
  void deploy(TheGraph graph);

  void push(OutPort out, DataItem<?> result);

  void panic(Exception e);

  /**
   * Inspired by Apache Storm
   */
  void ack(DataItem<?> dataItem);

  void fail(DataItem<?> dataItem, Exception reason);
}
