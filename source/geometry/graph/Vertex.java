package geometry.graph;

import java.util.HashMap;
import java.util.Map;

public class Vertex {

	// Id
	public final int id;

	// Visited?
	public boolean visited;
	
	// Weight, if any
	public int minWeight = -1, maxWeight = -1;

	// Edge list
	public Map<Vertex, Edge> veMap = new HashMap<Vertex, Edge>();

	// Residue graph edge list
	public Map<Vertex, Edge> vreMap = new HashMap<Vertex, Edge>();

	public Vertex(int id) {
		super();
		this.id = id;
	}
}
