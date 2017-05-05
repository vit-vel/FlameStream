package com.spbsu.datastream.core.tick.atomic;

import akka.actor.ActorRef;
import com.google.common.primitives.Longs;
import com.spbsu.datastream.core.DataItem;
import com.spbsu.datastream.core.HashFunction;
import com.spbsu.datastream.core.RoutingException;
import com.spbsu.datastream.core.ack.Ack;
import com.spbsu.datastream.core.configuration.HashRange;
import com.spbsu.datastream.core.graph.InPort;
import com.spbsu.datastream.core.graph.OutPort;
import com.spbsu.datastream.core.range.AddressedMessage;
import com.spbsu.datastream.core.tick.PortBindDataItem;
import com.spbsu.datastream.core.tick.TickContext;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.DbImpl;

import java.io.*;
import java.util.Optional;

public final class AtomicHandleImpl implements AtomicHandle {
  private final TickContext tickContext;
  private final DB db;

  public AtomicHandleImpl(final TickContext tickContext) {
    this.tickContext = tickContext;
    try {
      db = new DbImpl(new Options().createIfMissing(true), new File("./leveldb"));
    } catch (IOException e) {
      throw new RuntimeException("LevelDB is not initialized: " + e);
    }
  }

  @Override
  public void push(final OutPort out, final DataItem<?> result) {
    final Optional<InPort> destination = Optional.ofNullable(this.tickContext.graph().graph().downstreams().get(out));
    final InPort address = destination.orElseThrow(() -> new RoutingException("Unable to find port for " + out));

    @SuppressWarnings("rawtypes") final HashFunction hashFunction = address.hashFunction();

    @SuppressWarnings("unchecked") final int hash = hashFunction.applyAsInt(result.payload());

    final AddressedMessage<?> addressedMessage = new AddressedMessage<>(new PortBindDataItem(result, address), hash, this.tickContext.tick());
    this.ack(result);
    this.tickContext.rootRouter().tell(addressedMessage, ActorRef.noSender());
  }

  @Override
  public void ack(final DataItem<?> item) {
    final int hash = this.tickContext.ackerRange().from();

    final AddressedMessage<?> addressedMessage = new AddressedMessage<>(new Ack(item.ack(), item.meta().globalTime()), hash, this.tickContext.tick());
    this.tickContext.rootRouter().tell(addressedMessage, ActorRef.noSender());
  }

  @Override
  public Optional<Object> loadState(final InPort inPort) {
    final byte[] key = Longs.toByteArray(inPort.id());
    final byte[] value = db.get(key);
    if (value != null) {
      final ByteArrayInputStream in = new ByteArrayInputStream(value);
      try {
        final ObjectInputStream is = new ObjectInputStream(in);
        final Object state = is.readObject();
        is.close();
        in.close();
        return Optional.of(state);
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void saveState(final InPort inPort, final Object state) {
    final byte[] key = Longs.toByteArray(inPort.id());
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      final ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(state);
      oos.close();

      final byte[] value = bos.toByteArray();
      bos.close();
      db.put(key, value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void removeState(final InPort inPort) {
    final byte[] key = Longs.toByteArray(inPort.id());
    db.delete(key);
  }

  @Override
  public HashRange localRange() {
    return this.tickContext.localRange();
  }
}
