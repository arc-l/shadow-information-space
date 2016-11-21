package geometry.pe.is.singleTypeAgent;

import geometry.gap.Gap;
import geometry.graph.BipartiteGraph;
import geometry.graph.Edge;
import geometry.graph.Vertex;
import geometry.graph.algorithm.maxflow.MaxFlow;
import geometry.pe.is.Equation;
import geometry.pe.is.Oracle;
import geometry.pe.is.Oracle.EVENT_TYPE;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class SingleTypeAgentAlgorithm {

	/**
	 * Derive a simple system of equations that describes how the system evolves
	 * with time.
	 * 
	 * @param gapss
	 * @param oracle
	 * @return
	 */
	public static Equation[] deriveGapEvolvingEquations(Gap[][] gapss,
			Oracle oracle) {

		Vector<Equation> eqVec = new Vector<Equation>();
		// There will be an initial equation to describe the gap structure at
		// the beginning. Then every time the gap structure changes, new
		// equations come up
		int totalAgent = oracle.getTotalAgentNumber();
		for (int i = 0; i < gapss.length; i++) {
			Gap[] gaps = gapss[i];
			Equation eq = new Equation();
			SingleTypeAgentEvent e = (SingleTypeAgentEvent) oracle
					.getEventByTime(gapss[i][0].getRelativeTime());
			if (i == 0) {
				for (int j = 0; j < gaps.length; j++) {
					eq.addTerm(gaps[j].getId(), 1);
				}
				eq.setConstant(totalAgent - e.getVisibleAgents());
			} else {
				EVENT_TYPE et = e.getEventType();
				switch (et) {
				case APPEAR:
					eq.addTerm(e.getToGap()[0], 1);
					eq.setConstant(e.getMovedAgents());
					break;
				case DISAPPEAR:
					eq.addTerm(e.getFromGap()[0], 1);
					eq.setConstant(e.getMovedAgents());
					break;
				case SPLIT:
					eq.addTerm(e.getToGap()[0], 1);
					eq.addTerm(e.getToGap()[1], 1);
					eq.addTerm(e.getFromGap()[0], -1);
					break;
				case MERGE:
					eq.addTerm(e.getToGap()[0], 1);
					eq.addTerm(e.getFromGap()[0], -1);
					eq.addTerm(e.getFromGap()[1], -1);
					break;
				}
			}
			eqVec.add(eq);
		}
		return eqVec.toArray(new Equation[0]);
	}

	/**
	 * Processes information for the transition
	 * 
	 * @param gapss
	 * @param oracle
	 */
	public static BipartiteGraph deriveShadowInfoState(Gap[][] gapss,
			Oracle oracle) {

		int centerAgents = 0;
		BipartiteGraph bg = new BipartiteGraph();

		// Process events
		int events = oracle.getNumberOfEvents();
		for (int i = 0; i < events; i++) {
			SingleTypeAgentEvent e = (SingleTypeAgentEvent) oracle.getEvent(i);
			if (i == 0 && gapss[0].length > 0) {
				// Get initial state, we know nothing about gaps
				Vertex vl = new Vertex(0);
				vl.minWeight = vl.maxWeight = oracle.getTotalAgentNumber()
						- e.getVisibleAgents();
				bg.leftVertexMap.put(vl.id, vl);
				for (int j = 0; j < gapss[0].length; j++) {
					// For each initial gap, assume they have same uncertainty
					// shared
					// so we need to create on left vertex and multiple right
					// vertex if needed, and edges between left/right
					Vertex v = new Vertex(gapss[0][j].getId());
					Edge ed = new Edge(vl, v);
					vl.veMap.put(v, ed);
					bg.rightVertexMap.put(v.id, v);

					Set<Edge> es = new HashSet<Edge>();
					es.add(ed);
					bg.rightVertexEdgeMap.put(v, es);

//					 For more general case
//					 Vertex v = new Vertex(gapss[0][j].getId());
//					 Edge ed = new Edge(v, v);
//					 v.veMap.put(v, ed);
//					 bg.leftVertexMap.put(v.id, v);
//					 bg.rightVertexMap.put(v.id, v);
//					
//					 Set<Edge> es = new HashSet<Edge>();
//					 es.add(ed);
//					 bg.rightVertexEdgeMap.put(v, es);
				}
				centerAgents = e.getVisibleAgents();
			} else {
				switch (e.getEventType()) {
				case AGENT_APPEAR: {
					// Agent appear, more agents visible. Need to look
					// up gap in right vertex map of graph and update the
					// numbers.
					// int iGap = e.getFromGap()[0];
					// int agents = e.getMovedAgents();
					// centerAgents += agents;
					// Vertex v = bg.rightVertexMap.get(iGap);
					// v.maxWeight = v.minWeight = agents;
				}
					break;
				case AGENT_DISAPPEAR: {
					// Agent disappear, less agents visible. Need to create
					// a new left gap and right gap and put in the number;
					// also create an edge in between
					// int iGap = e.getToGap()[0];
					// int agents = e.getMovedAgents();
					// centerAgents -= agents;
					// Vertex v = new Vertex(iGap);
					// v.minWeight = v.maxWeight = agents;
					// Edge ed = new Edge(v, v);
					// v.veMap.put(v, ed);
					// bg.leftVertexMap.put(v.id, v);
					// bg.rightVertexMap.put(v.id, v);
				}
					break;
				case APPEAR: {
					// Agent disappear, less agents visible. Need to create
					// a new left gap and right gap and put in the number;
					// also create an edge in between
					int iGap = e.getToGap()[0];
					int agents = e.getMovedAgents();
					centerAgents -= agents;
					Vertex v = new Vertex(iGap);
					v.minWeight = v.maxWeight = agents;
					Edge ed = new Edge(v, v);
					v.veMap.put(v, ed);
					Set<Edge> es = new HashSet<Edge>();
					es.add(ed);
					bg.rightVertexEdgeMap.put(v, es);
					bg.leftVertexMap.put(v.id, v);
					bg.rightVertexMap.put(v.id, v);
				}
					break;
				case DISAPPEAR: {
					// Agent appear, more agents visible. Need to look
					// up gap in right vertex map of graph and update the
					// numbers.
					int iGap = e.getFromGap()[0];
					int agents = e.getMovedAgents();
					centerAgents += agents;
					Vertex v = bg.rightVertexMap.get(iGap);
					v.maxWeight = v.minWeight = agents;
				}
					break;
				case SPLIT: {
					// Gap splits, need to duplicate the gap to two and
					// duplicate all edge into that gap
					int iGap = e.getFromGap()[0];
					int gaps[] = e.getToGap();

					Vertex from = bg.rightVertexMap.get(iGap);
					Set<Edge> es = bg.rightVertexEdgeMap.get(from);
					Edge edges[] = es.toArray(new Edge[0]);
					Vertex v1 = new Vertex(gaps[0]);
					Vertex v2 = new Vertex(gaps[1]);
					bg.rightVertexMap.remove(from.id);
					bg.rightVertexMap.put(v1.id, v1);
					bg.rightVertexMap.put(v2.id, v2);

					// Setup edge set
					bg.rightVertexEdgeMap.remove(from);
					Set<Edge> es1 = new HashSet<Edge>();
					bg.rightVertexEdgeMap.put(v1, es1);
					Set<Edge> es2 = new HashSet<Edge>();
					bg.rightVertexEdgeMap.put(v2, es2);

					// Duplicate all edges
					for (int ie = 0; ie < edges.length; ie++) {
						// Update current and create a new one
						Edge ce = edges[ie];
						Edge ne1 = new Edge(ce.fromVertex, v1, 0, 0);
						Edge ne2 = new Edge(ce.fromVertex, v2, 0, 0);
						ce.fromVertex.veMap.remove(from);
						ce.fromVertex.veMap.put(v1, ne1);
						ce.fromVertex.veMap.put(v2, ne2);
						es1.add(ne1);
						es2.add(ne2);
					}
				}
					break;
				case MERGE: {
					// Collapse two vertices into one
					Vertex v1 = bg.rightVertexMap.get(e.getFromGap()[0]);
					Vertex v2 = bg.rightVertexMap.get(e.getFromGap()[1]);
					bg.rightVertexMap.remove(v1.id);
					bg.rightVertexMap.remove(v2.id);
					Vertex v = new Vertex(e.getToGap()[0]);
					bg.rightVertexMap.put(v.id, v);

					// Use edge set of v1
					Set<Edge> es1 = bg.rightVertexEdgeMap.get(v1);
					Set<Edge> es2 = bg.rightVertexEdgeMap.get(v2);
					bg.rightVertexEdgeMap.remove(v1);
					bg.rightVertexEdgeMap.remove(v2);

					// Need to get rid of duplicates in es1 and es2
					Set<Edge> es = new HashSet<Edge>();
					bg.rightVertexEdgeMap.put(v, es);
					es.addAll(es1);
					es.addAll(es2);
					Set<Vertex> fromVertexSet = new HashSet<Vertex>();
					Edge edges[] = es.toArray(new Edge[0]);
					for (int j = 0; j < edges.length; j++) {
						fromVertexSet.add(edges[j].fromVertex);
						edges[j].fromVertex.veMap.remove(edges[j].toVertex);
					}

					// Create new vertices
					Vertex vs[] = fromVertexSet.toArray(new Vertex[0]);
					es.clear();
					for (int j = 0; j < vs.length; j++) {
						Edge ed = new Edge(vs[j], v);
						vs[j].veMap.put(v, ed);
						es.add(ed);
					}
				}
					break;
				}
			}
		}
		System.out.println(bg.toString());
		return bg;
	}

	/**
	 * Formulate problem and obtain upper/lower bounds on each current 
	 * shadow component
	 * 
	 * @param bg
	 * @return
	 */
	public static Vertex[] deriveShadowBounds(BipartiteGraph bg) {
		
		// Create new flow graph from the bipartite graph
		int max = 0;
		Vertex source = new Vertex(-1);
		Vertex sink1 = new Vertex(-2);
		Vertex sink2 = new Vertex(-3);
		Vertex lvs[] = bg.leftVertexMap.values().toArray(new Vertex[0]);
		for (int i = 0; i < lvs.length; i++) {
			// Delete a vertex if it goes from self to self
			if(lvs[i].veMap.get(lvs[i])!=null){
				bg.leftVertexMap.remove(lvs[i].id);
				bg.rightVertexMap.remove(lvs[i].id);
				bg.rightVertexEdgeMap.remove(lvs[i]);
				continue;
			}
			
			// Create edge from source to this vertex
			Edge e = new Edge(source, lvs[i], 0, lvs[i].maxWeight);
			source.veMap.put(lvs[i], e);
			max += e.capacity;
			
			// For each vertex, update outgoing edge weight
			Edge edges[] = lvs[i].veMap.values().toArray(new Edge[0]);
			for (int j = 0; j < edges.length; j++) {
				edges[j].capacity = e.capacity;
			}
		}

		// Create outgoing edges
		int sureFlow = 0;
		Map<Integer, Edge> currentShadowEdgeMap = new TreeMap<Integer, Edge>();
		Vertex rvs[] = bg.rightVertexMap.values().toArray(new Vertex[0]);
		for (int i = 0; i < rvs.length; i++) {
			// Create edge from source to this vertex. First set current edges
			// from current shadows to sink to be 0 to get base flow
			if(rvs[i].maxWeight == -1){
				Edge e = new Edge(rvs[i], sink1, 0, 0);
				rvs[i].veMap.put(sink1, e);
				currentShadowEdgeMap.put(e.fromVertex.id, e);
			}
			else{
				Edge e = new Edge(rvs[i], sink2, 0, 0);
				rvs[i].veMap.put(sink2, e);
				e.capacity = rvs[i].maxWeight;
				sureFlow += rvs[i].maxWeight;
			}
		}
		Edge currentShadowEdges[] = currentShadowEdgeMap.values().toArray(new Edge[0]);

		// Link sink1 to sink2
		Edge e = new Edge(sink1, sink2, 0, max - sureFlow);
		sink1.veMap.put(sink2, e);
		
		// Calculate lower/upper bound for each current shadow
		for (int j = 0; j < currentShadowEdges.length; j++) {
			// First fill the basic flow
//			for (int i = 0; i < rvs.length; i++) {
//				if(rvs[i].maxWeight != -1)
//				{
//					rvs[i].veMap.get(sink1).capacity = rvs[i].maxWeight;
//				}
//				else
//				{
//					rvs[i].veMap.get(sink1).capacity = 0;
//				}
//				System.out.println("V: " + rvs[i].id + " W: " +  rvs[i].maxWeight);
//			}
			
			// Now max
			for (int i = 0; i < currentShadowEdges.length; i++) {
				if(i == j) currentShadowEdges[i].capacity = max - sureFlow;
				else currentShadowEdges[i].capacity = 0;
			}
			MaxFlow.getMaxFlow(source, sink2);
			
			System.out.println("Max flow in shadow " + currentShadowEdges[j].fromVertex.id + 
					 " is " + currentShadowEdges[j].weight);
			
			// To get min for a shadow, set it's capacity to be 0 and others to be max possible
			for (int i = 0; i < currentShadowEdges.length; i++) {
				if(i == j) currentShadowEdges[i].capacity = 0;
				else currentShadowEdges[i].capacity = max - sureFlow;
			}
			MaxFlow.getMaxFlow(source, sink2);
			
			// Gather total outgoing flow
			int subtotal = sureFlow;
			for (int i = 0; i < currentShadowEdges.length; i++) {
				subtotal += currentShadowEdges[i].weight;
			}
			
			System.out.println("Min flow in shadow " + currentShadowEdges[j].fromVertex.id + 
					 " is " + (max - subtotal));
			
		}
		System.out.println("\n");
		
		return new Vertex[]{source, sink2};
	}
}
