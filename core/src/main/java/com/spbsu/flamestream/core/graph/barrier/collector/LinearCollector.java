package com.spbsu.flamestream.core.graph.barrier.collector;

import com.spbsu.flamestream.core.data.DataGroup;
import com.spbsu.flamestream.core.data.DataItem;
import com.spbsu.flamestream.core.data.meta.GlobalTime;
import com.spbsu.flamestream.core.graph.ops.Grouping;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

public final class LinearCollector implements BarrierCollector {
  private final SortedMap<GlobalTime, DataGroup<Object>> invalidationPool = new TreeMap<>();

  @Override
  public void releaseFrom(GlobalTime minTime, Consumer<DataItem<?>> consumer) {
    final SortedMap<GlobalTime, DataGroup<Object>> headMap = invalidationPool.headMap(minTime);
    headMap.values().forEach(group -> group.forEach(consumer));
    headMap.clear();
  }

  @Override
  public void enqueue(DataItem<?> item) {
    //noinspection unchecked
    final DataItem<Object> dataItem = (DataItem<Object>) item;
    GlobalTime key = item.meta().globalTime();
    DataGroup<Object> value = invalidationPool.computeIfAbsent(key, k -> new DataGroup<>());
    value.insert(dataItem);
  }

  @Override
  public boolean isEmpty() {
    return invalidationPool.isEmpty();
  }
}
