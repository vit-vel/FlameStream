package com.spbsu.flamestream.example.inverted_index.ranking;

import java.util.stream.Stream;

/**
 * User: Artem
 * Date: 30.07.2017
 */
public interface RankingFunction {
  Stream<Rank> rank(CharSequence query);
}
