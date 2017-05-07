package com.spbsu.datastream.core.tick;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.spbsu.datastream.core.LoggingActor;
import com.spbsu.datastream.core.ack.AckActor;
import com.spbsu.datastream.core.graph.AtomicGraph;
import com.spbsu.datastream.core.graph.InPort;
import com.spbsu.datastream.core.range.RangeRouterApi;
import com.spbsu.datastream.core.tick.atomic.AtomicActor;
import com.spbsu.datastream.core.tick.atomic.AtomicHandleImpl;
import com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.DbImpl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TickConcierge extends LoggingActor {
  private final DB db;

  private TickConcierge(final TickContext context) {
    final Map<AtomicGraph, ActorRef> inMapping = this.initializedAtomics(context.tickInfo().graph().graph().subGraphs(), context);

    final ActorRef localRouter;
    if (context.localRange().equals(context.tickInfo().ackerRange())) {
      final ActorRef acker = this.context().actorOf(AckActor.props(context), "acker");
      localRouter = this.localRouter(TickConcierge.withFlattenedKey(inMapping), acker);
    } else {
      localRouter = this.localRouter(TickConcierge.withFlattenedKey(inMapping));
    }

    context.rangeRouter().tell(new RangeRouterApi.RegisterMe(context.tickInfo().startTs(), localRouter), this.self());

    try {
      this.db = new DbImpl(new Options().createIfMissing(true), new File("./leveldb" + context.localRange() + '#' + context.tickInfo().startTs()));
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<AtomicGraph, ActorRef> initializedAtomics(final Collection<? extends AtomicGraph> atomicGraphs,
                                                        final TickContext context) {
    return atomicGraphs.stream().collect(Collectors.toMap(Function.identity(), a -> this.actorForAtomic(a, context)));
  }

  private ActorRef localRouter(final Map<InPort, ActorRef> portMappings, final ActorRef acker) {
    return this.context().actorOf(TickLocalRouter.props(portMappings, acker), "localRouter");
  }

  private static Map<InPort, ActorRef> withFlattenedKey(final Map<AtomicGraph, ActorRef> map) {
    final Map<InPort, ActorRef> result = new HashMap<>();
    for (final Map.Entry<AtomicGraph, ActorRef> e : map.entrySet()) {
      for (final InPort port : e.getKey().inPorts()) {
        result.put(port, e.getValue());
      }
    }
    return result;
  }

  private ActorRef localRouter(final Map<InPort, ActorRef> portMappings) {
    return this.context().actorOf(TickLocalRouter.props(portMappings), "localRouter");
  }

  private ActorRef actorForAtomic(final AtomicGraph atomic, final TickContext context) {
    final String id = UUID.randomUUID().toString();
    return this.context().actorOf(AtomicActor.props(atomic, new AtomicHandleImpl(this.db, context, this.context())), id);
  }

  public static Props props(final TickContext context) {
    return Props.create(TickConcierge.class, context);
  }

  @Override
  public void onReceive(final Object message) throws Throwable {
    this.LOG().debug("Received: {}", message);
  }
}
