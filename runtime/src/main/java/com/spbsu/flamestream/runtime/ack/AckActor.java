package com.spbsu.flamestream.runtime.ack;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.spbsu.flamestream.core.data.meta.GlobalTime;
import com.spbsu.flamestream.runtime.ack.impl.ArrayAckTable;
import com.spbsu.flamestream.runtime.actor.LoggingActor;
import com.spbsu.flamestream.runtime.range.HashRange;
import com.spbsu.flamestream.runtime.tick.StartTick;
import com.spbsu.flamestream.runtime.tick.TickInfo;
import com.spbsu.flamestream.runtime.tick.TickRoutes;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public final class AckActor extends LoggingActor {
  private final TickInfo tickInfo;
  private final AckTable ackTable;
  private final ActorRef tickWatcher;
  private final AckerStatistics stat = new AckerStatistics();
  private final Collection<HashRange> committers = new HashSet<>();

  private long currentMin = Long.MIN_VALUE;
  @Nullable
  private TickRoutes tickRoutes;

  private AckActor(TickInfo tickInfo, ActorRef tickWatcher) {
    this.tickInfo = tickInfo;
    this.tickWatcher = tickWatcher;
    this.ackTable = new ArrayAckTable(tickInfo.startTs(), tickInfo.stopTs(), tickInfo.window());
  }

  public static Props props(TickInfo tickInfo, ActorRef tickWatcher) {
    return Props.create(AckActor.class, tickInfo, tickWatcher);
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create()
            .match(StartTick.class, start -> {
              LOG().info("Received start tick");
              tickRoutes = start.tickRoutingInfo();
              unstashAll();
              getContext().become(acking());
            })
            .matchAny(m -> stash())
            .build();
  }

  private Receive acking() {
    return ReceiveBuilder.create()
            .match(RequestGlobalTime.class, this::handleRequestGT)
            .match(Ack.class, this::handleAck)
            .build();
  }

  @Override
  public void postStop() {
    super.postStop();
    LOG().info("Acker statistics: {}", stat);
    LOG().debug("Acker table: {}", ackTable);
  }

  private void handleRequestGT(RequestGlobalTime request) {
    final long ts = System.nanoTime();
    ackTable.ack(ts, request.xor());
    sender().tell(new GlobalTime(ts, tickInfo.ackerLocation()), self());
  }

  private void handleAck(Ack ack) {
    final long start = System.nanoTime();
    //assertMonotonicAck(ack.time());

    if (ackTable.ack(ack.time().time(), ack.xor())) {
      checkMinTime();
      stat.recordReleasingAck(System.nanoTime() - start);
    } else {
      stat.recordNormalAck(System.nanoTime() - start);
    }
  }

  private void checkMinTime() {
    final long min = ackTable.min(System.nanoTime());
    if (min > currentMin) {
      currentMin = min;
      sendMinUpdates(new GlobalTime(currentMin, tickInfo.ackerLocation()));
    }

    if (min == tickInfo.stopTs()) {
      sendCommit();
      getContext().become(
              ReceiveBuilder.create()
                      .match(RangeCommitDone.class, this::handleDone)
                      .build()
      );
    } else if (min > tickInfo.stopTs()) {
      throw new IllegalStateException("Min must be less or equal to tick stop ts");
    }
  }

  @SuppressWarnings("unused")
  private void assertMonotonicAck(long newTime) {
    if (newTime < currentMin) {
      throw new IllegalStateException("Not monotonic acks. Fixme");
    }
  }

  private void handleDone(RangeCommitDone rangeCommitDone) {
    LOG().debug("Received: {}", rangeCommitDone);

    final HashRange committer = rangeCommitDone.committer();
    committers.add(committer);
    if (committers.equals(tickInfo.hashMapping().keySet())) {
      LOG().info("Tick commit done");
      tickWatcher.tell(new CommitTick(tickInfo.id()), self());
      context().stop(self());
    }
  }

  private void sendCommit() {
    LOG().info("Committing");
    //noinspection ConstantConditions
    tickRoutes.rangeConcierges().values()
            .forEach(r -> r.tell(new Commit(), self()));
  }

  private void sendMinUpdates(GlobalTime min) {
    LOG().debug("New min time: {}", min);
    //noinspection ConstantConditions
    tickRoutes.rangeConcierges().values()
            .forEach(r -> r.tell(new MinTimeUpdate(min), self()));
  }
}
