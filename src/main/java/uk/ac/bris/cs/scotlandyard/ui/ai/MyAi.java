package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Mr Holmes"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
				// Through testing we determined not necessary to account for time
		// Create graphs for each detective on the distance to them respectively
		List<List<Node>> detectiveGraphs = new ArrayList<>();
		for (Piece player : board.getPlayers()) {
			if (player.isDetective()) {
				List<Node> distances = Dijkstras(board.getDetectiveLocation((Piece.Detective)player).get(), board.getSetup().graph.asGraph());
				detectiveGraphs.add(Score(distances));
			}
		}
		
		// Combine graphs
		MutableValueGraph<Integer, Double> sumGraph = ValueGraphBuilder.undirected().build();
		for (List<Node> graph : detectiveGraphs) {
			for (Node n : graph) {
				if (n.from == null) {
					continue;
				}

				sumGraph.addNode(n.location);
				sumGraph.addNode(n.from);

				Double current = sumGraph.edgeValue(n.from, n.location).orElse(0.0);
				sumGraph.putEdgeValue(n.from, n.location, n.distance + current);
			}
		}

		// Visitor for accessing a move's destination
		Move.FunctionalVisitor<Integer> v = new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2);

		Double bestScore = Double.NEGATIVE_INFINITY;

		// Track the best move to perform
		Move bestMove = null;
		for (Move move : board.getAvailableMoves()) {
			if (move.commencedBy().isMrX()) {

				// Avoids losing in one move
				boolean canBeCaptured = detectiveCanReach(move.accept(v), detectiveGraphs);
				if (canBeCaptured) continue;

				// Preference on moves with more open routes
				int escapeRoutes = board.getSetup().graph.adjacentNodes(move.accept(v)).size();

				Optional<Double> optionalScore = sumGraph.edgeValue(move.accept(v), move.source());
				// Verify optionalScore exists
				if (optionalScore.isEmpty()) continue;

				double score = optionalScore.orElse(0.0) + (escapeRoutes * 0.5);

				// Update bestMove and bestScore when a better move is found
				// Exits loop with the best move
				if (score > bestScore) {
					bestMove = move;
					bestScore = score;
				}
			}
		}

		// Returns a random move if bestMove is null
		// Due to all moves resulting in a loss
		if (bestMove == null) {
			var moves = board.getAvailableMoves().asList();
			return moves.get(new Random().nextInt(moves.size()));
		}
		else {
			return bestMove;
		}
	}

	/**
	 * <p>Performs Dijkstra's shortest path algorithm to a graph</p>
	 * <p>Assume distance between nodes are never Double.MAX_VALUE</p>
	 * @return <b>Node : Distance</b>
	 */
	private List<Node> Dijkstras(Integer detectiveLocation, ImmutableGraph<Integer> graph) {
		List<Node> visited = new ArrayList<>();
		Set<Integer> visitedLocation = new HashSet<>(); // for performance

		visited.add(new Node(null, detectiveLocation, 0.0));
		visitedLocation.add(detectiveLocation); 

		Integer minimumFrom = null;
		Integer minimumNode = null;
		Double minimumDistance = 0.0;

		while (minimumDistance != Double.MAX_VALUE) {
			// assume next node is infinitely far away
			minimumDistance = Double.MAX_VALUE;

			for (Node visitedNode : visited) {
				for (Integer connection : graph.adjacentNodes(visitedNode.location)) {
					// skip if node has already been found/ considered
					if (visitedLocation.contains(connection)) {
						continue;
					}

					// track the node which has the lowest distance to next be "visited"
					if (1 + visitedNode.distance < minimumDistance) {
						// cost of edge is 1
						minimumDistance = 1 + visitedNode.distance;
						minimumFrom = visitedNode.location;
						minimumNode = connection;
					}
				}
			}

			visited.add(new Node(minimumFrom, minimumNode, minimumDistance));
			visitedLocation.add(minimumNode);
		}

		// since in the loop the last node will be null
		visited.remove(visited.size() - 1);
		
		return visited;
	}

	/**
	 * <p>Applies weighted distance to a graph</p>
	 * @return <b>Weighted graph</b>
	 */
	private List<Node> Score(List<Node> graph) {
		for (Node n : graph) {
			n.distance = ScoreDistance(n.distance);
		}
		return graph;
	}

	/**
	 * <p>Applies weighting to a given distance</p>
	 * @return <b>Distance</b>
	 */
	private double ScoreDistance(Double distance) {
		// inverse square
		return 1.0 / ((distance + 1) * (distance + 1));
	}

	//

	/**
	 * <p>Checks if a detective can reach a given position in one move</p>
	 * @return <b>Boolean : Detective wins</b>
	 */
	private boolean detectiveCanReach(int position, List<List<Node>> detectiveGraphs) {
		for (List<Node> graph : detectiveGraphs) {
			for (Node node : graph) {
				if (node.location == position && node.distance <= 1.0) {
					return true;
				}
			}
		}
		return false;
	}
}

/**
 * Class that provides connections between nodes and their distances
 */
class Node {
	Integer from;
	Integer location;
	Double distance;

	Node(Integer from, Integer location, Double distance) {
		this.from = from;
		this.location = location;
		this.distance = distance;
	}
}

// Initial thoughts:
// Greedy algorithm

// assume detectives will play a move that approaches mrx (hold all positions that put det equally close to mrx)
// assume detectives && mrx have unlimited tickets
// find new locaiton of all dets
// for each detective:
// do dijkstra's on all nodes on graph.
// second pass: go over each node in new graph (node: distance)
// multiply distance with detective weight for new "score"
// pass the distance itself into fn to weight nearer dets greater