package com.spbsu.flamestream.runtime.front;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.spbsu.flamestream.core.data.DataItem;
import com.spbsu.flamestream.core.data.PayloadDataItem;
import com.spbsu.flamestream.core.data.meta.GlobalTime;
import com.spbsu.flamestream.core.data.meta.Meta;
import com.spbsu.flamestream.core.graph.InPort;
import com.spbsu.flamestream.runtime.actor.LoggingActor;
import com.spbsu.flamestream.runtime.actor.PingActor;
import com.spbsu.flamestream.runtime.raw.RawData;
import com.spbsu.flamestream.runtime.tick.TickInfo;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class FrontActor extends LoggingActor {
  private final Map<Integer, ActorPath> cluster;
  private final int id;
  private final NavigableMap<Long, ActorRef> tickFronts = new TreeMap<>();
  private final Map<Long, TickInfo> tickInfos = new HashMap<>();
  private final ActorRef pingActor;

  private long prevGlobalTs = -1;
  private long minWindow = Long.MAX_VALUE;

  private FrontActor(Map<Integer, ActorPath> cluster, int id) {
    this.cluster = new HashMap<>(cluster);
    this.id = id;
    this.pingActor = context().actorOf(PingActor.props(self(), FrontPing.PING).withDispatcher("front-ping-dispatcher"));
  }

  public static Props props(Map<Integer, ActorPath> cluster, int id) {
    return Props.create(FrontActor.class, cluster, id);
  }

  @Override
  public void postStop() {
    pingActor.tell(new PingActor.Stop(), self());
    super.postStop();
  }

  @Override
  public Receive createReceive() {
    //noinspection unchecked
    return ReceiveBuilder.create()
            // TODO: We will use this information to clear mini-kafka buffers
            //.match(TickCommitDone.class, committed -> )
            .match(RawData.class, rawData -> rawData.forEach(this::redirectItem))
            .match(TickInfo.class, this::createTick)
            .match(FrontPing.class, frontPing -> onReportPing())
            .match(TickFrontStopped.class, tickFrontStopped -> tickFronts.remove(tickFrontStopped.startTickTs()))
            .matchAny(this::unhandled)
            .build();
  }

  private void onReportPing() {
    tickFronts.values().forEach(actorRef -> actorRef.tell(new TickFrontPing(System.nanoTime()), self()));
  }

  private void createTick(TickInfo tickInfo) {
    LOG().info("Creating tickFront for startTs: {}", tickInfo);

    final InPort target = tickInfo.graph().frontBindings().get(id);

    final ActorRef tickFront = context().actorOf(
            TickFrontActor.props(
                    cluster,
                    target,
                    id,
                    tickInfo
            ),
            Long.toString(tickInfo.id()));

    tickFronts.put(tickInfo.startTs(), tickFront);
    tickInfos.put(tickInfo.startTs(), tickInfo);
    unstashAll();

    // TODO: 07.10.2017 handle case when min window gets bigger after removing tick front
    { //set up ping actor
      if (tickInfo.window() < minWindow) {
        minWindow = tickInfo.window();
        pingActor.tell(new PingActor.Stop(), self());
        pingActor.tell(new PingActor.Start(minWindow), self());
      }
    }
  }

  private void redirectItem(Object payload) {
    long globalTs = System.nanoTime();
    if (globalTs <= prevGlobalTs) {
      globalTs = prevGlobalTs + 1;
    }
    prevGlobalTs = globalTs;

    final GlobalTime globalTime = new GlobalTime(globalTs, id);
    final Meta now = Meta.meta(globalTime);
    final DataItem<?> dataItem = new PayloadDataItem<>(now, payload);

    final ActorRef tickFront = tickFrontFor(globalTs);
    if (tickFront != null) {
      tickFront.tell(dataItem, self());
    } else {
      stash();
      LOG().warning("There is no tick fronts for {}, stashing", globalTs);
    }
  }

  @Nullable
  private ActorRef tickFrontFor(long ts) {
    final Long tick = tickFronts.floorKey(ts);
    final TickInfo info = tickInfos.get(tick);

    //noinspection OverlyComplexBooleanExpression
    if (tick != null
            && info != null
            && ts < info.stopTs()
            && ts >= info.startTs()) {
      return tickFronts.get(tick);
    } else {
      return null;
    }
  }

  private enum FrontPing {
    PING
  }
}
