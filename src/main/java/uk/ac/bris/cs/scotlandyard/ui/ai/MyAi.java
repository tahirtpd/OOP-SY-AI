package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import java.util.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.*;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Mr Holmes"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		// Greedy algorithm

		/*
		 * Move that increases the moves required by all detectives (moves count now quantified)
		*/
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


		Dijkstras(board.getDetectiveLocation(null).get(), board.getSetup().graph.asGraph());

		List<Move> moves = board.getAvailableMoves().asList();
		ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> new_graph = board.getSetup().graph;
		ImmutableSet<Piece> players = board.getPlayers();
		ArrayList<Move> mrxMoves = new ArrayList<>();
		Move.FunctionalVisitor<Integer> v = new Move.FunctionalVisitor<>(m -> m.destination, m -> m.destination2);

		for (Move move : moves) {
			if (move.commencedBy().isMrX()) {
				mrxMoves.add(move);
			}
		}

		// go to a node in the graph
		// find this node in all players graphs, where the player's graphs' nodes are scored based on distance to mrX, and get the score.
		// score graph edges:  source = location, target = score
		// sum the scores

		double totalScore = 0;
		Set<Integer> nodes = new_graph.nodes();

		for (Integer node : nodes) {
			for (Piece player : players) {
				if (player.isDetective()) {
					// get distances between detective and mrX
					ImmutableValueGraph<Integer, Integer> distances = Dijkstras(board.getDetectiveLocation((Piece.Detective)player).get(), new_graph.asGraph());
					// add score for the matched node
					for (EndpointPair<Integer> pair : Score(distances).edges()) {
						if(pair.source().equals(node)) {
							totalScore += pair.target();
							break;
						}
					}
				}
			}
		}

		int bestScore = 0;
		Move bestMove = null;

		MutableValueGraph<Integer, Integer> b = ValueGraphBuilder.undirected().build();
		for (Integer node : new_graph.nodes()) {

			if (!new_graph.nodes().contains(node)) {
				b.addNode(node);
			}

			Set<Integer> neighbors = new_graph.adjacentNodes(node);
			for (Integer n : neighbors) {

				if (!new_graph.nodes().contains(n)) {
					b.addNode(n);
				}
				b.putEdgeValue(node, n, 0);
			}
		}

		for (Move move : mrxMoves) {
			Optional<Integer> score = b.edgeValue(move.accept(v), move.source());
			if (score.isPresent() && score.get() > bestScore) {
				bestMove = move;
				bestScore = score.get();
			}
		}
		
		return bestMove;
	}

	/**
	 * <p>assume distance between nodes are never Integer.MAX_VALUE</p>
	 * @return <b>Node : Distance</b>
	 */
	private ImmutableValueGraph<Integer, Integer> Dijkstras(Integer detectiveLocation, ImmutableGraph<Integer> graph) {
		List<Node> visited = new ArrayList<>();
		Set<Integer> visitedLocation = new HashSet<>();

		visited.add(new Node(null, detectiveLocation, 0));
		visitedLocation.add(detectiveLocation);

		Integer minimumFrom = null;
		Integer minimumNode = null;
		Integer minimumDistance = null;

		while (minimumDistance != Integer.MAX_VALUE) {
			minimumDistance = Integer.MAX_VALUE;

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

		MutableValueGraph<Integer, Integer> newGraph = ValueGraphBuilder.undirected().build();

		for (Node n : visited) {
			newGraph.addNode(n.from);
			newGraph.addNode(n.location);
			newGraph.putEdgeValue(n.from, n.location, n.distance);
		}

		return ImmutableValueGraph.copyOf(newGraph);
	}

	private ImmutableValueGraph<Integer, Integer> Score(ImmutableValueGraph<Integer, Integer> graph) {
		// go through graph
		// apply fn exagerate(distance) -> score;
		// could do page rank
		// return graph
		return null;
	}
}

class Node {
	Integer from;
	Integer location;
	Integer distance;

	Node(Integer from, Integer location, Integer distance) {
		this.from = from;
		this.location = location;
		this.distance = distance;
	}
}