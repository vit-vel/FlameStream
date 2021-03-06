package com.spbsu.flamestream.example.inverted_index.ops;

import com.spbsu.flamestream.example.inverted_index.utils.IndexItemInLong;
import com.spbsu.flamestream.example.inverted_index.model.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * User: Artem
 * Date: 10.07.2017
 */
public class WordIndexToDiffOutput implements Function<List<WordBase>, Stream<WordBase>> {
  @Override
  public Stream<WordBase> apply(List<WordBase> wordBases) {
    final WordBase first = wordBases.get(0);
    if (wordBases.size() < 2) {
      final WordPagePositions wordPagePosition = (WordPagePositions) first;
      return createOutputStream(new WordIndex(wordPagePosition.word(), new InvertedIndexState()), wordPagePosition);
    } else {
      final WordBase second = wordBases.get(1);
      if (first instanceof WordIndex && second instanceof WordPagePositions) {
        return createOutputStream((WordIndex) first, (WordPagePositions) second);
      } else {
        throw new IllegalStateException("Wrong order of objects");
      }
    }
  }

  private Stream<WordBase> createOutputStream(WordIndex wordIndex, WordPagePositions wordPagePosition) {
    WordIndexRemove wordRemoveOutput = null;
    final long prevValue = wordIndex.state().updateOrInsert(wordPagePosition.positions());
    if (prevValue != InvertedIndexState.PREV_VALUE_NOT_FOUND) {
      wordRemoveOutput = new WordIndexRemove(wordIndex.word(), IndexItemInLong.setRange(prevValue, 0), IndexItemInLong.range(prevValue));
    }

    final WordIndex newWordIndex = new WordIndex(wordIndex.word(), wordIndex.state());
    final WordIndexAdd wordAddOutput = new WordIndexAdd(wordIndex.word(), wordPagePosition.positions());
    return Stream.of(newWordIndex, wordRemoveOutput, wordAddOutput).filter(Objects::nonNull);
  }
}
