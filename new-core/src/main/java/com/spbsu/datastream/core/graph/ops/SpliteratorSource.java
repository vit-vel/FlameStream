package com.spbsu.datastream.core.graph.ops;

import com.spbsu.datastream.core.graph.Graph;
import com.spbsu.datastream.core.graph.Source;
import com.spbsu.datastream.core.materializer.GraphStageLogic;

import java.util.Objects;
import java.util.Spliterator;

/**
 * Created by marnikitta on 2/7/17.
 */
public final class SpliteratorSource<T> extends Source<T> {
  private final Spliterator<T> spliterator;

  public SpliteratorSource(final Spliterator<T> spliterator) {
    this.spliterator = spliterator;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final SpliteratorSource<?> that = (SpliteratorSource<?>) o;
    return Objects.equals(spliterator, that.spliterator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spliterator);
  }

  @Override
  public String toString() {
    return "SpliteratorSource{" + "spliterator=" + spliterator +
            '}';
  }

  @Override
  public GraphStageLogic logic() {
    return null;
  }

  @Override
  public Graph deepCopy() {
    return new SpliteratorSource<>(spliterator);
  }
}
