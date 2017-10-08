package com.spbsu.flamestream.core.graph.ops;

import com.spbsu.flamestream.core.data.DataGroup;
import com.spbsu.flamestream.core.data.DataItem;
import com.spbsu.flamestream.core.data.meta.GlobalTime;
import com.spbsu.flamestream.core.graph.AbstractAtomicGraph;
import com.spbsu.flamestream.core.graph.AtomicHandle;
import com.spbsu.flamestream.core.graph.InPort;
import com.spbsu.flamestream.core.graph.OutPort;
import com.spbsu.flamestream.core.graph.ops.stat.GroupingStatistics;
import com.spbsu.flamestream.core.graph.ops.state.GroupingState;
import com.spbsu.flamestream.core.graph.ops.state.LazyGroupingState;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@SuppressWarnings({"ConditionalExpression"})
public final class Grouping<T> extends AbstractAtomicGraph {
  //private static final int MIN_BUFFER_SIZE_FOR_MIN_TIME_UPDATE = 200; //magic number, tuning is welcome
  private final GroupingStatistics stat = new GroupingStatistics();

  private final InPort inPort;
  private final OutPort outPort = new OutPort();

  private final ToIntFunction<? super T> hash;
  private final BiPredicate<? super T, ? super T> equalz;
  private final int window;
  private GroupingState<T> state;

  public Grouping(ToIntFunction<? super T> hash, BiPredicate<? super T, ? super T> equalz, int window) {
    this.inPort = new InPort(hash);
    this.window = window;
    this.hash = hash;
    this.equalz = equalz;
  }

  @Override
  public void onStart(AtomicHandle handle) {
    // TODO: 5/18/17 Load state
    this.state = new LazyGroupingState<>(hash, equalz);
  }

  @Override
  public void onPush(InPort inPort, DataItem<?> item, AtomicHandle handle) {
    //noinspection unchecked
    final DataItem<T> dataItem = (DataItem<T>) item;

    final DataGroup<T> group = state.getGroupFor(dataItem);
    final int position = group.insert(dataItem);
    stat.recordBucketSize(group.size());
    replayAround(position, group, handle);
  }

  private void replayAround(int position, DataGroup<T> group, AtomicHandle handle) {
    int replayCount = 0;

    for (int right = position + 1; right <= Math.min(position + window, group.size()); ++right) {
      replayCount++;
      final int left = Math.max(right - window, 0);
      pushSubGroup(group, left, right, handle);
    }

    stat.recordReplaySize(replayCount);
  }

  private void pushSubGroup(DataGroup<T> group, int left, int right, AtomicHandle handle) {
    final DataItem<List<T>> subGroupItem = group.subGroup(left, right)
        .collectToDataItem(incrementLocalTimeAndGet(), Collectors.toList());
    handle.push(outPort(), subGroupItem);
    handle.ack(subGroupItem.ack(), subGroupItem.meta().globalTime());
  }

  @Override
  public void onCommit(AtomicHandle handle) {
    //handle.saveState(this.inPort, this.state);
    handle.submitStatistics(stat);
  }

  @Override
  public void onMinGTimeUpdate(GlobalTime globalTime, AtomicHandle handle) {
    // TODO: 29.09.2017 optimize or remove
    /*final Consumer<DataGroup<T>> removeOldConsumer = group -> {
      if (group.size() < MIN_BUFFER_SIZE_FOR_MIN_TIME_UPDATE)
        return;

      int left = 0;
      int right = group.size();
      { //upper-bound binary search
        while (right - left > 1) {
          final int middle = left + (right - left) / 2;
          if (group.getItem(middle).meta().globalTime().compareTo(globalTime) <= 0) {
            left = middle;
          } else {
            right = middle;
          }
        }
      }

      final int position = Math.max(left - window, 0);
      if (position > 0) {
        group.subGroup(0, position).clear();
      }
    };
    state.forEach(removeOldConsumer);*/
  }

  public InPort inPort() {
    return inPort;
  }

  @Override
  public List<InPort> inPorts() {
    return Collections.singletonList(inPort);
  }

  public OutPort outPort() {
    return outPort;
  }

  @Override
  public List<OutPort> outPorts() {
    return Collections.singletonList(outPort);
  }
}
