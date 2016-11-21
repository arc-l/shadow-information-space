package geometry.gap;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Gap encapsulates the needed information for describing a gap derived from
 * visibility polygon.
 * 
 * @author Jingjin Yu
 * 
 */
public class Gap {

	// The id of the gap
	protected int id = -1;

	// The invariant edge
	protected int iEdge = -1;
	
	// Relative gap appearing time
	protected double relativeTime = 0;

	// State of gap; actual type depends on the what the evasion scenario is
	protected GapState state = null;

	// The next gap(s) this gap goes to. One gap may goes to multiple gap and
	// multiple gap may become one gap
	Set<Integer> toGapSet = new HashSet<Integer>();;

	public Gap() {
	}

	public Gap(int id, int ie) {
		this.id = id;
		this.iEdge = ie;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIEdge() {
		return iEdge;
	}

	public void setIEdge(int edge) {
		iEdge = edge;
	}

	public double getRelativeTime() {
		return relativeTime;
	}

	public void setRelativeTime(double relativeTime) {
		this.relativeTime = relativeTime;
	}

	public GapState getState() {
		return state;
	}

	public void setState(GapState state) {
		this.state = state;
	}

	public Set<Integer> getToGapSet() {
		return toGapSet;
	}

	public void addGap(int id) {
		toGapSet.add(id);
	}
	
	public String toString() {
		StringBuffer ret = new StringBuffer("[" + getId() + ", " + getIEdge());
		if(state != null) ret.append(state.toString());
		if (toGapSet.size() > 0) {
			ret.append(" | ");
			Integer[] ids = toGapSet.toArray(new Integer[0]);
			for (int i = 0; i < ids.length; i++) {
				ret.append(ids[i]);
				ret.append(" ");
			}
		}
		ret.append("]");
		return ret.toString();
	}
	
	public static Map<Integer, Gap> getGapMap(Gap[] gaps){
		Map<Integer, Gap> gm = new HashMap<Integer, Gap>();
		for (int i = 0; i < gaps.length; i++) {
			gm.put(gaps[i].id, gaps[i]);
		}
		return gm;
	}
}
