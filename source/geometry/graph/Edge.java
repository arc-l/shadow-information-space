package geometry.graph;

public class Edge {

	// Vertices of the edge
	public Vertex fromVertex = null, toVertex = null;
	
	// Weight and capacity (capacity not used for residue edges)
	public int weight, capacity;

	public Edge(Vertex fromVertex, Vertex toVertex, int weight, int capacity) {
		super();
		this.fromVertex = fromVertex;
		this.toVertex = toVertex;
		this.weight = weight;
		this.capacity = capacity;
	}

	public Edge(Vertex fromVertex, Vertex toVertex) {
		super();
		this.fromVertex = fromVertex;
		this.toVertex = toVertex;
	}

	/**
	 * Create a residue edge with weight same as the capacity of this 
	 * edge
	 * 
	 * @return
	 */
	public Edge createResidueEdge(){
		Edge e = new Edge(fromVertex, toVertex, capacity, 0);
		return e;
	}
		
}
