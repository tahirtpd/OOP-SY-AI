package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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

		// assume detectives will play a move that approaches mrx (hold all positions that put det equally close to mrx)
		// assume detectives && mrx have unlimited tickets
		// find new locaiton of all dets
		// parallel
		// for each detective:
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
		// }
		Dijkstras(board.getDetectiveLocation(null).get(), board.getSetup().graph.asGraph());

		// let mut new_graph = board.getSetup().graph;
		// for node in new_graph {
		// 	let mut total_score = 0;
		// 	for graph in graphs {
		// 		total_score += graph.get(node);
		// 	}
		// }

		// let mut best = null;
		// for move in mrx moves {
		// 	if new_graph.get(move).score > best.score {
		// 		best = move
		// 	}
		// }

		// return best

		var moves = board.getAvailableMoves().asList();
		return moves.get(new Random().nextInt(moves.size()));
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
