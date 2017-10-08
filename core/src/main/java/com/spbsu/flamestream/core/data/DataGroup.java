package com.spbsu.flamestream.core.data;

import com.spbsu.flamestream.core.data.meta.Meta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class DataGroup<T> {
  private List<DataItem<T>> items = new ArrayList<>();

  public DataGroup() {
  }

  private DataGroup(List<DataItem<T>> items) {
    this.items = items;
  }

  public Boolean isEmpty() {
    return items.isEmpty();
  }

  public int size() {
    return items.size();
  }

  public DataItem<T> getItem(int position) throws IndexOutOfBoundsException {
    return items.get(position);
  }

  public void clear() {
    items.clear();
  }

  public DataGroup<T> subGroup(int fromPosition, int toPosition) {
    return new DataGroup<>(items.subList(fromPosition, toPosition));
  }

  public void forEach(Consumer<? super DataItem<T>> consumer) {
    items.forEach(consumer);
  }

  public <R, A> DataItem<R> collectToDataItem(int localTime, Collector<? super T, A, R> collector) {
    final Meta meta = items.get(items.size() - 1).meta().advanced(localTime);
    final R collectedItems = items.stream().map(DataItem::payload).collect(collector);
    return new PayloadDataItem<>(meta, collectedItems);
  }

  public int insert(DataItem<T> newItem) {
    int position = items.size() - 1;
    int endPosition = -1;
    { //find position
      while (position >= 0) {
        final DataItem<T> currentItem = items.get(position);
        final int compareTo = currentItem.meta().compareTo(newItem.meta());

        if (compareTo > 0) {
          if (newItem.isInvalidatedBy(currentItem)) {
            return -1;
          }
          position--;
        } else {
          if (currentItem.isInvalidatedBy(newItem)) {
            endPosition = endPosition == -1 ? position : endPosition;
            position--;
          } else {
            break;
          }
        }
      }
    }
    { //invalidation/adding
      if (position == (items.size() - 1)) {
        items.add(newItem);
      } else {
        if (endPosition != -1) {
          items.set(position + 1, newItem);
          final int itemsForRemove = endPosition - position - 1;
          //subList.clear is faster if the number of items for removing >= 2
          if (itemsForRemove >= 2)
            items.subList(position + 2, endPosition + 1).clear();
          else if (itemsForRemove > 0)
            items.remove(endPosition);
        } else {
          items.add(position + 1, newItem);
        }
      }
    }
    return position + 1;
  }
}