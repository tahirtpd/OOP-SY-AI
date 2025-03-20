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
		// Greedy algorithm

		/*
		 * Move that increases the moves required by all detectives (moves count now quantified)
		 */

		// assume detectives will play a move that approaches mrx (hold all positions that put det equally close to mrx)
		// assume detectives && mrx have unlimited tickets
		// find new locaiton of all dets
		// parallel
		// for each detective:
			// do dijkstra's on all nodes on graph.
			// second pass: go over each node in new graph (node: distance)
			// // multiply distance with detective weight for new "score"
			// pass the distance itself into fn: e^d (optional: for each node connecting to itself, sum the connections of these nodes and add to total score)
		// do dijkstra's on all nodes on graph.
		// second pass: go over each node in new graph (node: distance)
		// // multiply distance with detective weight for new "score"
		// pass the distance itself into fn: e^d (optional: for each node connecting to itself, sum the connections of these nodes and add to total score)

		// for each graph:
		// for each of identical nodes:
		// sum the "score"
		//
		// for each move of detective's
		// find the score in the new single graph
		// find move with max score
		// return move

		// let mut graphs = vec![];
		// for (Piece plr : board.getPlayers()) {
		// 	if (plr.isDetective()) {
		// 		let graph = Dijkstras(board.getDetectiveLocation((Detective) plr).get(), board.getSetup().graph);
		// 		let graph = score(graph);
		// 		graphs.push(graph);
		// 	}
		// } */


		//Dijkstras(board.getDetectiveLocation(null).get(), board.getSetup().graph.asGraph());
		
		// go to a node in the graph
		// find this node in all players graphs, where the player's graphs' nodes are scored based on distance to mrX, and get the score.
		// score graph edges:  source = location, target = score
		// sum the scores
		
		List<List<Node>> detectiveGraphs = new ArrayList<>();
		for (Piece player : board.getPlayers()) {
			if (player.isDetective()) {
				// create thread:                   ?????????????????????????????????????????????

				List<Node> distances = Dijkstras(board.getDetectiveLocation((Piece.Detective)player).get(), board.getSetup().graph.asGraph());
				detectiveGraphs.add(Score(distances));
			}
		}
		
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
		// Track the best move to perform, should not be null on return as Mr X would have lost already
		Move bestMove = null;
		// Track the least bad move in the unlikely event that no best move is found
		Move fallbackMove = null;

		for (Move move : board.getAvailableMoves()) {
			if (move.commencedBy().isMrX()) {
				// avoid losing in 1

				boolean canBeCaptured = detectiveCanReach(move.accept(v), detectiveGraphs);
				if (canBeCaptured && fallbackMove == null) {
					fallbackMove = move;
				}

				if (canBeCaptured) continue;

				// greater weighting on nodes with more open routes
				int escapeRoutes = board.getSetup().graph.adjacentNodes(move.accept(v)).size();

				Optional<Double> optionalScore = sumGraph.edgeValue(move.accept(v), move.source());

				if (optionalScore.isEmpty()) {
					continue;
				}

				double score = optionalScore.orElse(0.0) + (escapeRoutes * 0.5);

				if (score > bestScore) {
					bestMove = move;
					bestScore = score;
				}
			}
		}

		if (bestMove == null) {
            return fallbackMove;
		}
		else {
			return bestMove;
		}
	}

	/**
	 * <p>assume distance between nodes are never Integer.MAX_VALUE</p>
	 * @return <b>Node : Distance</b>
	 */
	private List<Node> Dijkstras(Integer detectiveLocation, ImmutableGraph<Integer> graph) {
		List<Node> visited = new ArrayList<>();
		Set<Integer> visitedLocation = new HashSet<>();

		visited.add(new Node(null, detectiveLocation, 0.0));
		visitedLocation.add(detectiveLocation);

		Integer minimumFrom = null;
		Integer minimumNode = null;
		Double minimumDistance = 0.0;

		while (minimumDistance != Double.MAX_VALUE) {
			minimumDistance = Double.MAX_VALUE;

			for (Node visitedNode : visited) {
				for (Integer connection : graph.adjacentNodes(visitedNode.location)) {
					if (visitedLocation.contains(connection)) {
						continue;
					}

					// cost of edge is 1
					if (1 + visitedNode.distance < minimumDistance) {
						minimumDistance = 1 + visitedNode.distance;
						minimumFrom = visitedNode.location;
						minimumNode = connection;
					}
				}
			}

			visited.add(new Node(minimumFrom, minimumNode, minimumDistance));
			visitedLocation.add(minimumNode);
		}

		visited.remove(visited.size() - 1);
		
		return visited;
	}

	private List<Node> Score(List<Node> graph) {
		for (Node n : graph) {
			System.out.println(n.distance);
			n.distance = ScoreDistance(n.distance);
		}

		return graph;
	}

	// Applies weighting to distance
	private double ScoreDistance(Double distance) {
		// inverse square
		return 1.0 / ((distance + 1) * (distance + 1));
	}

	// Check if a detective can reach a given position in one move
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