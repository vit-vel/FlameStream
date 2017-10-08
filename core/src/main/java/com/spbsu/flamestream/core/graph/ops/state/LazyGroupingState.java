package com.spbsu.flamestream.core.graph.ops.state;

import com.spbsu.flamestream.core.data.DataGroup;
import com.spbsu.flamestream.core.data.DataItem;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * User: Artem
 * Date: 22.02.2017
 * Time: 22:29
 */
@SuppressWarnings({"TypeMayBeWeakened"})
public final class LazyGroupingState<T> implements GroupingState<T> {
  private final ToIntFunction<? super T> hash;
  private final BiPredicate<? super T, ? super T> equalz;
  private final TLongObjectMap<Object> buffers = new TLongObjectHashMap<>();

  public LazyGroupingState(ToIntFunction<? super T> hash, BiPredicate<? super T, ? super T> equalz) {
    this.hash = hash;
    this.equalz = equalz;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DataGroup<T> getGroupFor(DataItem<T> item) {
    final long hashValue = hash.applyAsInt(item.payload());
    final Object obj = buffers.get(hashValue);
    if (obj == null) {
      final DataGroup<T> newGroup = new DataGroup<>();
      buffers.put(hashValue, newGroup);
      return newGroup;
    } else if (obj instanceof DataGroup<?>) {
      final DataGroup<T> group = (DataGroup<T>) obj;
      return getFromBucket(item, group);
    } else {
        final List<DataGroup<T>> container = (List<DataGroup<T>>) obj;
        return getFromContainer(item, container);
    }
  }

  private DataGroup<T> getFromContainer(DataItem<T> item, List<DataGroup<T>> container) {
    final DataGroup<T> result = searchBucket(item, container);
    if (result.isEmpty()) {
      container.add(result);
    }
    return result;
  }

  private DataGroup<T> getFromBucket(DataItem<T> item, DataGroup<T> bucket) {
    if (equalz.test(bucket.getItem(0).payload(), item.payload())) {
      return bucket;
    } else {
      final List<DataGroup<T>> container = new ArrayList<>();
      container.add(bucket);
      final DataGroup<T> newList = new DataGroup<>();
      container.add(newList);
      buffers.put(hash.applyAsInt(item.payload()), container);
      return newList;
    }
  }


  @Override
  public void forEach(Consumer<DataGroup<T>> consumer) {
    buffers.forEachValue(obj -> {
      final List<?> list = (List<?>) obj;
      if (list.get(0) instanceof List) {
        //noinspection unchecked
        final List<DataGroup<T>> buckets = (List<DataGroup<T>>) list;
        buckets.forEach(consumer);
      } else {
        //noinspection unchecked
        consumer.accept((DataGroup<T>) list);
      }
      return true;
    });
  }

  private DataGroup<T> searchBucket(DataItem<T> item, List<DataGroup<T>> container) {
    return container.stream()
        .filter(bucket -> equalz.test(bucket.getItem(0).payload(), item.payload()))
        .findFirst()
        .orElse(new DataGroup<>());
  }
}
