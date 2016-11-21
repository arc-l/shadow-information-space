package geometry.graph.algorithm.maxflow;

import geometry.graph.Edge;
import geometry.graph.Vertex;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import geometry.Triple;
import geometry.IDGenerator;

/**
 * Ford-Fulkerson / Edmonds-Karp Algorithm for max flow in a network.
 * 
 * Input to the algorithm: A directed graph with capactities on each edge
 * 
 * Output: max flow with positive # on edges that are smaller than given
 * capacity on each edge.
 * 
 * Note: two way flow is not implemented 
 * 
 * @author Jingjin Yu
 * 
 */
public class MaxFlow {

	public static void getMaxFlow(Vertex source, Vertex sink) {
		// First construct residue graph with capacity
		buildResidueGraph(source);
		
		// Search for all augment path
		searchAllAugmentPaths(source, sink);
		
		// Build result, store as edge weight
		constructFlowGraph(source);
	}

	public static void getIncrementalMaxFlow(Vertex source, Vertex sink) {
		// Search for all augment path
		searchAllAugmentPaths(source, sink);
		
		// Build result, store as edge weight
		constructFlowGraph(source);
	}

	/**
	 * Store flow as edge weight
	 * 
	 * @param source
	 */
	private static void constructFlowGraph(Vertex source) {
		Queue<Vertex> vq = new LinkedList<Vertex>();
		source.visited = true;
		vq.add(source);
		while (vq.size() != 0) {
			Vertex fv = vq.poll();
			Edge[] edges = fv.veMap.values().toArray(new Edge[0]);
			for (int i = 0; i < edges.length; i++) {
				Edge e = edges[i];
				Vertex toVertex = e.toVertex;
				if (toVertex.visited == false) {
					toVertex.visited = true;
					vq.offer(toVertex);
				}

				Edge re = toVertex.vreMap.get(fv);
				if(re != null)e.weight = re.weight;
				else e.weight = 0;
//				System.out.println(fv.id + " " + toVertex.id + " " + e.capacity + " " + e.weight);
			}
		}

		// Reset visit state of vertices
		resetGraphVisitedState(source);
	}

	/**
	 * Search for all augment path. Nothing needs to be returned since we
	 * can retrieve the result from comparing original graph with residue 
	 * graph
	 * 
	 * @param source
	 * @param sink
	 */
	private static void searchAllAugmentPaths(Vertex source, Vertex sink) {
		while (true) {
			// BFS search residue graph from source to sink
			List<Edge> augmentPath = searchForPath(source, sink);

			// Check the path and update residue graph
			if (augmentPath != null) {
				// First get min flow
				int minFlow = Integer.MAX_VALUE;
				for (int i = 0; i < augmentPath.size(); i++) {
					Edge e = augmentPath.get(i);
					if (minFlow > e.weight)
						minFlow = e.weight;
//					System.out.println(e.toVertex.id + " " + e.weight);
				}
//				System.out.println(minFlow);

				// Update residue graph
				for (int i = 0; i < augmentPath.size(); i++) {
					Edge e = augmentPath.get(i);

					// Delete current path if it has same weight as minFlow
					if (minFlow == e.weight) {
						e.fromVertex.vreMap.remove(e.toVertex);
					}
					else {
						e.weight -= minFlow;
					}

					// Check whether the graph already has reverse edge
					Edge re = e.toVertex.vreMap.get(e.fromVertex);
					if (re == null) {
						re = new Edge(e.toVertex, e.fromVertex, 0, 0);
						e.toVertex.vreMap.put(e.fromVertex, re);
					}
					re.weight += minFlow;
				}
			} 
			else {
				break;
			}
		}
	}

	/**
	 * Search for a directed path in the residue graph using BFS and copy the
	 * residue edges into list for finding the largest flow along the path
	 * 
	 * @param source
	 * @param sink
	 * @return
	 */
	private static List<Edge> searchForPath(Vertex source, Vertex sink) {
		Queue<Triple<Vertex, Edge, Triple>> veQueue = new LinkedList<Triple<Vertex, Edge, Triple>>();
		veQueue.add(new Triple<Vertex, Edge, Triple>(source, null, null));
		source.visited = true;

		Triple<Vertex, Edge, Triple> path = null;
		while (veQueue.size() != 0) {
			Triple<Vertex, Edge, Triple> veTriple = veQueue.poll();
			Vertex fv = veTriple.first;
			Edge[] redges = fv.vreMap.values().toArray(new Edge[0]);
			for (int i = 0; i < redges.length; i++) {
				Edge e = redges[i];
				Vertex toVertex = e.toVertex;
				if (toVertex.visited == false) {
					toVertex.visited = true;
					Triple temp = new Triple<Vertex, Edge, Triple>(toVertex, e,
							veTriple);
					if (toVertex == sink) {
						path = temp;
						break;
					} else {
						veQueue.offer(new Triple<Vertex, Edge, Triple>(
								toVertex, e, veTriple));
					}
				}
			}
			if (path != null)
				break;
		}

		// Reset visit state of vertices
		resetGraphVisitedState(source);

		if (path != null) {
			// Recover the path
			List<Edge> edgeList = new LinkedList<Edge>();
			while (path != null && path.third != null) {
				edgeList.add(0, path.second);
				path = path.third;
			}
			return edgeList;
		} else {
			return null;
		}
	}

	/**
	 * Using BFS to build residue graph from source. If a vertex is not
	 * reachable from source then it's not useful anyway.
	 * 
	 * @param source
	 */
	private static void buildResidueGraph(Vertex source) {
		Queue<Vertex> vq = new LinkedList<Vertex>();
		source.visited = true;
		source.vreMap.clear();
		vq.add(source);
		while (vq.size() != 0) {
			Vertex fv = vq.poll();
			fv.vreMap.clear();
			Edge[] edges = fv.veMap.values().toArray(new Edge[0]);
			for (int i = 0; i < edges.length; i++) {
				Edge e = edges[i];
				Vertex toVertex = e.toVertex;
				if (toVertex.visited == false) {
					toVertex.visited = true;
					vq.offer(toVertex);
				}
				fv.vreMap.put(toVertex, e.createResidueEdge());
			}
		}

		// Reset visit state of vertices
		resetGraphVisitedState(source);
	}

	/**
	 * Set all vertices reacheable from source to not visited
	 * 
	 * @param source
	 */
	private static void resetGraphVisitedState(Vertex source) {
		Queue<Vertex> vq = new LinkedList<Vertex>();
		source.visited = false;
		vq.add(source);
		while (vq.size() != 0) {
			Vertex fv = vq.poll();
			Edge[] edges = fv.veMap.values().toArray(new Edge[0]);
			for (int i = 0; i < edges.length; i++) {
				Edge e = edges[i];
				Vertex toVertex = e.toVertex;
				if (toVertex.visited == true) {
					toVertex.visited = false;
					vq.offer(toVertex);
				}
			}
		}
	}

	/**
	 * Test main
	 * 
	 * @param argv
	 */
	public static void main(String argv[]) {
		// Build simple graph
		IDGenerator IDGen = new IDGenerator();
		Vertex v1 = new Vertex(IDGen.getNextId());
		Vertex v2 = new Vertex(IDGen.getNextId());
		Vertex v3 = new Vertex(IDGen.getNextId());
		Vertex v4 = new Vertex(IDGen.getNextId());
		Vertex v5 = new Vertex(IDGen.getNextId());
		Vertex v6 = new Vertex(IDGen.getNextId());
		Edge e12 = new Edge(v1, v2, 0, 7);
		Edge e13 = new Edge(v1, v3, 0, 6);
		// Edge e23 = new Edge(v2, v3, 0, 6);
		Edge e24 = new Edge(v2, v4, 0, 7);
		Edge e35 = new Edge(v3, v5, 0, 6);
		Edge e25 = new Edge(v2, v5, 0, 7);
		Edge e46 = new Edge(v4, v6, 0, 7);
		Edge e56 = new Edge(v5, v6, 0, 7);
		v1.veMap.put(v2, e12);
		v1.veMap.put(v3, e13);
		// v2.veMap.put(v3, e23);
		v2.veMap.put(v4, e24);
		v2.veMap.put(v5, e25);
		v3.veMap.put(v5, e35);
		v4.veMap.put(v6, e46);
		v5.veMap.put(v6, e56);

		getMaxFlow(v1, v6);
	}

}
