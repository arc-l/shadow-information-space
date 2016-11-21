package geometry.cut;


import geometry.Algorithm;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;



/**
 * Represents the intersection point of a cut with a path
 * 
 * @author Jingjin Yu
 *
 */
public class PathCutIntersectPoint {

	// Point of intersection
	private Point2D point;
	
	// Distance from start of path
	private double distance;
	
	// The line on path that intersects cut
	private Line2D seg;
	
	// Cut type
	private Algorithm.CUT_TYPE cutType;
	
	// The Cut
	private Cut cut;
	
	public PathCutIntersectPoint(Point2D point, double distance, Line2D seg, Cut cut) {
		super();
		this.point = point;
		this.distance = distance;
		this.seg = seg;
		this.cut = cut;
		cutType = cut.getCutType();
	}

	public PathCutIntersectPoint(Point2D point, double distance, Line2D seg, Algorithm.CUT_TYPE cutType) {
		super();
		this.point = point;
		this.distance = distance;
		this.seg = seg;
		this.cutType = cutType;
	}

	public Line2D getSeg() {
		return seg;
	}

	public Cut getCut() {
		return cut;
	}

	public Algorithm.CUT_TYPE getCutType() {
		return cutType;
	}

	public double getDistance() {
		return distance;
	}

	public Point2D getPoint() {
		return point;
	}
	
	
}
