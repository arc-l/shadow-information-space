package geometry.cut;


import geometry.Algorithm;

import java.awt.geom.Line2D;



/**
 * 
 * 
 * @author Jingjin Yu
 *
 */
public class Cut {
	
	// Type of cuts
	private Line2D cut;
	
	private Algorithm.CUT_TYPE cutType;

	public Cut(Line2D cut, Algorithm.CUT_TYPE cutType) {
		super();
		this.cut = cut;
		this.cutType = cutType;
	}

	public Line2D getCut() {
		return cut;
	}

	public Algorithm.CUT_TYPE getCutType() {
		return cutType;
	}
	
	
}
