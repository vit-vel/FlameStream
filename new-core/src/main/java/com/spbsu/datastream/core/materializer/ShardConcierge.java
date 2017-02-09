package com.spbsu.datastream.core.materializer;

import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * Created by marnikitta on 2/8/17.
 */
public interface ShardConcierge {
  ActorRef portLocator();

  ActorRef actorFor(Props props);
}
