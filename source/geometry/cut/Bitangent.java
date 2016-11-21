package geometry.cut;


import geometry.Algorithm;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;



public class Bitangent extends Cut {

	private int thisPoint, oppositePoint;

	private Line2D  oppositeSegment;
	
	private Point2D curveToPoint;

	public Bitangent(Line2D cut, int thisPoint, int oppositePoint, Line2D oppositeSegment, Point2D curveToPoint) {
		super(cut, Algorithm.CUT_TYPE.BITANGENT);
		this.thisPoint = thisPoint;
		this.oppositePoint = oppositePoint;
		this.oppositeSegment = oppositeSegment;
		this.curveToPoint = curveToPoint;
	}

	public int getThisPoint() {
		return thisPoint;
	}

	public int getOppositePoint() {
		return oppositePoint;
	}

	public Line2D getOppositeSegment() {
		return oppositeSegment;
	}

	public Point2D getCurveToPoint() {
		return curveToPoint;
	}
	
}
