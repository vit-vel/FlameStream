package com.spbsu.datastream.core.meta;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.spbsu.datastream.core.meta.LocalEvent.childIdOf;
import static com.spbsu.datastream.core.meta.LocalEvent.localEvent;
import static com.spbsu.datastream.core.meta.LocalEvent.localTimeOf;

@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
final class TraceImpl implements Trace {

  @SuppressWarnings("PackageVisibleField")
  final long[] trace;

  TraceImpl(long[] trace) {
    this.trace = trace;
  }

  @Override
  public TraceImpl advanced(int localTime, int childId) {
    final long[] newTrace = Arrays.copyOf(this.trace, this.trace.length + 1);
    newTrace[newTrace.length - 1] = localEvent(localTime, childId);
    return new TraceImpl(newTrace);
  }

  @Override
  public int compareTo(Trace trace) {
    final TraceImpl that = (TraceImpl) trace;
    for (int i = 0; i < Math.min(this.trace.length, that.trace.length); ++i) {
      if (this.trace[i] != this.trace[i]) {
        return Long.compare(this.trace[i], that.trace[i]);
      }
    }
    return Integer.compare(this.trace.length, that.trace.length);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final TraceImpl trace1 = (TraceImpl) o;
    return Arrays.equals(this.trace, trace1.trace);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.trace);
  }

  @Override
  public String toString() {
    return Arrays.stream(this.trace).mapToObj(event -> localTimeOf(event) + ":" + childIdOf(event))
            .collect(Collectors.joining(", ", "[", "]"));
  }
}