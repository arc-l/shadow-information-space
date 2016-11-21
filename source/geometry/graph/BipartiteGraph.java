package geometry.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BipartiteGraph {

	public Map<Integer, Vertex> leftVertexMap = new TreeMap<Integer,Vertex>();
	public Map<Integer, Vertex> rightVertexMap = new TreeMap<Integer,Vertex>();
	
	public Map<Vertex, Set<Edge>> rightVertexEdgeMap = new HashMap<Vertex, Set<Edge>>();
	
	public String toString(){
		StringBuffer buf = new StringBuffer();
		Vertex lvs[] = leftVertexMap.values().toArray(new Vertex[0]);
		Vertex rvs[] = rightVertexMap.values().toArray(new Vertex[0]);
		buf.append("Left vertices\n");
		for (int i = 0; i < lvs.length; i++) {
			buf.append("Vertex: ").append(lvs[i].id).append(" W: ").append(lvs[i].maxWeight).append(", goes to: ");
			Edge[] es = lvs[i].veMap.values().toArray(new Edge[0]);
			for(int j = 0; j < es.length; j++){
				buf.append(es[j].toVertex.id).append(" ");
			}
			buf.append("\n");
		}

		buf.append("Right vertices\n");
		for (int i = 0; i < rvs.length; i++) {
			buf.append("Vertex: ").append(rvs[i].id).append(" W: ").append(rvs[i].maxWeight).append("\n");
		}
		return buf.toString();
	}
	
}
