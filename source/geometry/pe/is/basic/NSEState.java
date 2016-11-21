package geometry.pe.is.basic;

import geometry.gap.GapState;

/**
 * The gap states that captures only the fact that a gap is either clear or
 * contaminated, which is the case for the basic PE scenario where pursuer
 * never really sees an evader. 
 * 
 * @author Jingjin Yu
 * 
 */
public class NSEState implements GapState {
	private final boolean clear;

	public static final NSEState CLEAR = new NSEState(true); 
	public static final NSEState CONTAMINATED = new NSEState(false); 
	
	private NSEState(boolean clear) {
		super();
		this.clear = clear;
	}

	public boolean isClear() {
		return clear;
	}
	
	public String toString(){
		return clear ? "| 0" : "| 1";
	}

}
