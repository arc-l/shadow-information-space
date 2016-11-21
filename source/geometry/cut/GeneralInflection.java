package geometry.cut;


import geometry.Algorithm;

import java.awt.geom.Line2D;



public class GeneralInflection extends Cut {

	private int fromLine;
	
	private boolean counterClockWise;
	
	public GeneralInflection(Line2D cut, int fromLine, boolean counterClockWise) {
		super(cut, Algorithm.CUT_TYPE.GENERAL_INFLECTION);
		this.fromLine = fromLine;
		this.counterClockWise = counterClockWise;
	}

	public int getFromLine() {
		return fromLine;
	}

	public boolean isCounterClockWise() {
		return counterClockWise;
	}

}
