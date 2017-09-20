package com.spbsu.datastream.core.ack;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Envelope;
import akka.dispatch.MailboxType;
import akka.dispatch.MessageQueue;
import akka.dispatch.ProducesMessageQueue;
import com.spbsu.datastream.core.LoggingActor;
import com.spbsu.datastream.core.ack.impl.AckLedgerImpl;
import com.spbsu.datastream.core.configuration.HashRange;
import com.spbsu.datastream.core.message.BroadcastMessage;
import com.spbsu.datastream.core.meta.GlobalTime;
import com.spbsu.datastream.core.node.UnresolvedMessage;
import com.spbsu.datastream.core.range.atomic.AtomicActor;
import com.spbsu.datastream.core.stat.AckerStatistics;
import com.spbsu.datastream.core.tick.TickInfo;
import com.typesafe.config.Config;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import scala.Option;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class AckActor extends LoggingActor {
  private final AckLedger ledger;
  private final TickInfo tickInfo;
  private final ActorRef dns;
  private GlobalTime currentMin = GlobalTime.MIN;

  private final AckerStatistics stat = new AckerStatistics();

  private final Collection<HashRange> committers = new HashSet<>();

  private AckActor(TickInfo tickInfo, ActorRef dns) {
    this.ledger = new AckLedgerImpl(tickInfo);
    this.tickInfo = tickInfo;
    this.dns = dns;
  }

  public static Props props(TickInfo tickInfo, ActorRef dns) {
    return Props.create(AckActor.class, tickInfo, dns);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(AckerReport.class, this::handleReport)
            .match(Ack.class, this::handleAck)
            .build();
  }

  @Override
  public void postStop() throws Exception {
    super.postStop();
    LOG().info("Acker statistics: {}", stat);

    LOG().debug("Acker ledger: {}", ledger);
    LOG().info("Tss: {}", LoggingQueue.ts);
    LOG().info("Sizee: {}", LoggingQueue.size);
    LOG().info("Ack TTT: {}" + AtomicActor.acks.values());
  }

  private void handleReport(AckerReport report) {
    LOG().debug("Front report received: {}", report);
    ledger.report(report.globalTime(), report.xor());
    checkLedgerTime();
  }

  private void handleAck(Ack ack) {
    AtomicActor.acks.computeIfPresent(ack.xor(), (k, v) -> System.nanoTime() - v);
    final long start = System.nanoTime();
    //assertMonotonicAck(ack.time());

    if (ledger.ack(ack.time(), ack.xor())) {
      checkLedgerTime();
      stat.recordReleasingAck(System.nanoTime() - start);
    } else {
      stat.recordNormalAck(System.nanoTime() - start);
    }
  }

  private void checkLedgerTime() {
    final GlobalTime ledgerMin = ledger.min();
    if (ledgerMin.compareTo(currentMin) > 0) {
      this.currentMin = ledgerMin;
      sendMinUpdates(currentMin);
    }

    if (ledgerMin.time() >= tickInfo.stopTs()) {
      sendCommit();
      getContext().become(receiveBuilder().match(CommitDone.class, this::handleDone).build());
    }
  }

  private void assertMonotonicAck(GlobalTime newTime) {
    if (newTime.compareTo(currentMin) < 0) {
      throw new IllegalStateException("Not monotonic acks. Fixme");
    }
  }

  private void handleDone(CommitDone commitDone) {
    LOG().debug("Received: {}", commitDone);
    final HashRange committer = commitDone.committer();
    committers.add(committer);
    if (committers.equals(tickInfo.hashMapping().asMap().keySet())) {
      LOG().info("COOOOMMMMITTTITITITITITI");
    }
  }

  private void sendCommit() {
    LOG().info("Committing");
    dns.tell(new UnresolvedMessage<>(new BroadcastMessage<>(new Commit(), tickInfo.startTs())), self());
  }

  private void sendMinUpdates(GlobalTime min) {
    LOG().debug("New min time: {}", min);
    dns.tell(new UnresolvedMessage<>(new BroadcastMessage<>(new MinTimeUpdate(min), tickInfo.startTs())), self());
  }

  public static class LoggingQueue implements MailboxType, ProducesMessageQueue<LoggingQueue.MyMessageQueue> {
    public static final TLongList ts = new TLongArrayList(10000);
    public static final TLongList size = new TLongArrayList(10000);

    @Override
    public MessageQueue create(Option<ActorRef> owner, Option<ActorSystem> system) {
      return new MyMessageQueue();
    }

    // This is the MessageQueue implementation
    public static class MyMessageQueue implements MessageQueue {
      private final Queue<Envelope> queue = new ConcurrentLinkedQueue<>();

      @Override
      public void enqueue(ActorRef receiver, Envelope handle) {
        queue.offer(handle);
        //ts.add(System.nanoTime());
        //size.add(queue.size());
      }

      @Override
      public Envelope dequeue() { return queue.poll(); }

      @Override
      public int numberOfMessages() { return queue.size(); }

      @Override
      public boolean hasMessages() { return !queue.isEmpty(); }

      @Override
      public void cleanUp(ActorRef owner, MessageQueue deadLetters) {
        for (Envelope handle : queue) {
          deadLetters.enqueue(owner, handle);
        }
      }
    }

    public LoggingQueue(ActorSystem.Settings settings, Config config) {
    }
  }
}
