package com.spbsu.benchmark.flink;

import com.spbsu.flamestream.example.inverted_index.model.WikipediaPage;
import com.spbsu.flamestream.example.inverted_index.model.WordIndexAdd;
import com.spbsu.flamestream.example.inverted_index.model.WordIndexRemove;
import com.spbsu.flamestream.example.inverted_index.model.WordPagePositions;
import com.spbsu.flamestream.example.inverted_index.ops.InvertedIndexState;
import com.spbsu.flamestream.example.inverted_index.utils.IndexItemInLong;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.Collector;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * User: Artem
 * Date: 05.10.2017
 */
public class InvertedIndexStream implements FlinkStream<WikipediaPage, InvertedIndexStream.Output> {

  @Override
  public DataStream<Output> stream(DataStream<WikipediaPage> source) {
    //noinspection deprecation
    return source
            .flatMap(new WikipediaPageToWordPositions())
            .keyBy(0)
            .map(new RichIndexFunction());
  }

  private static class WikipediaPageToWordPositions implements FlatMapFunction<WikipediaPage, Tuple2<String, long[]>> {
    @Override
    public void flatMap(WikipediaPage value, Collector<Tuple2<String, long[]>> out) throws Exception {
      final com.spbsu.flamestream.example.inverted_index.ops.WikipediaPageToWordPositions filter = new com.spbsu.flamestream.example.inverted_index.ops.WikipediaPageToWordPositions();
      final Stream<WordPagePositions> result = filter.apply(value);
      result.forEach(new Consumer<WordPagePositions>() {
        @Override
        public void accept(WordPagePositions v) {out.collect(new Tuple2<>(v.word(), v.positions()));}
      });
    }
  }

  private static class RichIndexFunction extends RichMapFunction<Tuple2<String, long[]>, Output> {

    private transient ValueState<InvertedIndexState> state;

    @Override
    public void open(Configuration parameters) throws Exception {
      final ValueStateDescriptor<InvertedIndexState> descriptor = new ValueStateDescriptor<>(
              "index",
              InvertedIndexState.class
      );

      state = getRuntimeContext().getState(descriptor);
    }

    @Override
    public Output map(Tuple2<String, long[]> value) throws Exception {
      final InvertedIndexState currentStat;
      if (state.value() == null) {
        currentStat = new InvertedIndexState();
      } else {
        currentStat = state.value();
      }

      final long prevValue = currentStat.updateOrInsert(value.f1);
      final WordIndexAdd wordIndexAdd = new WordIndexAdd(value.f0, value.f1);
      WordIndexRemove wordIndexRemove = null;
      if (prevValue != InvertedIndexState.PREV_VALUE_NOT_FOUND) {
        wordIndexRemove = new WordIndexRemove(
                value.f0,
                IndexItemInLong.setRange(prevValue, 0),
                IndexItemInLong.range(prevValue)
        );
      }
      state.update(currentStat);
      return new Output(wordIndexAdd, wordIndexRemove);
    }
  }

  public static class Output {
    private final WordIndexAdd wordIndexAdd;
    private final WordIndexRemove wordIndexRemove;

    Output(WordIndexAdd wordIndexAdd, WordIndexRemove wordIndexRemove) {
      this.wordIndexAdd = wordIndexAdd;
      this.wordIndexRemove = wordIndexRemove;
    }

    WordIndexAdd wordIndexAdd() {
      return wordIndexAdd;
    }

    WordIndexRemove wordIndexRemove() {
      return wordIndexRemove;
    }
  }
}
