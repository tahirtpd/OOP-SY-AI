package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Mr Holmes"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation





		ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> new_graph = board.getSetup().graph;

		/*for node in new_graph {
			let mut total_score = 0;
			for graph in graphs {
				total_score += graph.get(node);
			}
		}

		let mut best = null;
		for move in mrx moves {
			if new_graph.get(move).score > best.score {
				best = move
			}
		}

		return best*/

		var moves = board.getAvailableMoves().asList();
		return moves.get(new Random().nextInt(moves.size()));
	}
}
