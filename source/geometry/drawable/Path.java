package geometry.drawable;

import geometry.Algorithm;
import geometry.cut.Cut;
import geometry.cut.PathCutIntersectPoint;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A 2D path fromed by a bunch of line segments, one after another, connected
 * 
 * 
 * @author Jingjin Yu
 * 
 */
public class Path extends AbstractDrawable {

	// List of point in the path, in given order
	List<Point2D> pointList = new ArrayList<Point2D>();

	List<Line2D> lineList = new ArrayList<Line2D>();

	public Path(DrawingContext dc) {
		super(dc);
	}
	
	public Path(List<Point2D> pointList, DrawingContext dc) {
		super(dc);
		this.pointList = pointList;
	}

	/**
	 * Add addtional segment at the end of the path
	 * 
	 * @param p
	 */
	public void addPoint(Point2D p) {
		pointList.add(p);
		if (pointList.size() > 1) {
			lineList.add(new Line2D.Double(pointList.get(pointList.size() - 2),
					p));
		}
	}

	/**
	 * Return points as Point2D array
	 * 
	 * @return
	 */
	public Point2D[] getPointArray() {
		return pointList.toArray(new Point2D[0]);
	}

	/**
	 * Return points as Line2D array
	 * 
	 * @return
	 */
	public Line2D[] getLineArray() {
		return lineList.toArray(new Line2D[0]);
	}

	/**
	 * Test whether a given point is on this path
	 * 
	 * @param p
	 * @return
	 */
	public boolean pointOnPath(Point2D p) {
		Line2D[] lines = getLineArray();
		for (int i = 0; i < lines.length; i++) {
			if (Algorithm.pointOnSegment(p, lines[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the length of the path
	 * 
	 * @return
	 */
	public double getLength() {
		double distance = 0;
		// Fist figure out which line this point is on
		Line2D[] lines = getLineArray();
		for (int i = 0; i < lines.length; i++) {
			distance += lines[i].getP1().distance(lines[i].getP2());
		}
		return distance;
	}

	/**
	 * Return the distance of a point (on path) from the start point of the
	 * path. If the point is not on path, return NaN
	 * 
	 * @param p
	 * @return
	 */
	public double ptDistFromStart(Point2D p) {
		double distance = Double.NaN;
		// Fist figure out which line this point is on
		Line2D[] lines = getLineArray();
		for (int i = 0; i < lines.length; i++) {
			if (Algorithm.pointOnSegment(p, lines[i])) {
				// Now we got the line. Calcualte the distances and add up
				distance = ptDistFromStart(p, lines, i);
			}
		}
		return distance;
	}

	/**
	 * Calcualtes the distance of point p from the start point following the
	 * path p is assumed to be on the segment
	 * 
	 * @param p
	 * @param lines
	 * @param endLine
	 * @return
	 */
	private double ptDistFromStart(Point2D p, Line2D[] lines, int endLine) {
		double d = 0;
		for (int i = 0; i < endLine; i++) {
			d += lines[i].getP1().distance(lines[i].getP2());
		}
		d += lines[endLine].getP1().distance(p);
		return d;
	}

	/**
	 * Get intersection point of the path with a segment
	 * 
	 * @param l
	 * @return
	 */
	public Point2D getIntersectPoint(Line2D l) {
		Line2D[] lines = getLineArray();
		for (int i = 0; i < lines.length; i++) {
			Point2D p = Algorithm.getSegmentIntersect(lines[i], l);
			if (p != null)
				return p;
		}
		return null;
	}

	/**
	 * Get intersection point of the path with bunch of segments. The returned
	 * points are ordered by distance from start point.
	 * 
	 * @param l
	 * @return
	 */
	public Point2D[] getIntersectPoints(Line2D[] segs) {

		SortedMap<Double, Point2D> pointMap = new TreeMap<Double, Point2D>();

		Line2D[] lines = getLineArray();
		for (int i = 0; i < lines.length; i++) {
			for (int j = 0; j < segs.length; j++) {
				Point2D p = Algorithm.getSegmentIntersect(lines[i], segs[j]);
				if (p != null) {
					double dist = ptDistFromStart(p, lines, i);
					pointMap.put(new Double(dist), p);
				}
			}
		}
		return pointMap.values().toArray(new Point2D[0]);
	}

	/**
	 * Get intersection point of the path with bunch of segments. The returned
	 * points are ordered by distance from start point.
	 * 
	 * @param l
	 * @return
	 */
	public PathCutIntersectPoint[] getIntersectPoints(Cut[] cuts) {

		SortedMap<Double, PathCutIntersectPoint> pointMap = new TreeMap<Double, PathCutIntersectPoint>();

		Line2D[] lines = getLineArray();
		for (int i = 0; i < lines.length; i++) {
			for (int j = 0; j < cuts.length; j++) {
				Point2D p = Algorithm.getSegmentIntersect(lines[i], cuts[j]
						.getCut());
				if (p != null) {
					double dist = ptDistFromStart(p, lines, i);
					pointMap.put(new Double(dist), new PathCutIntersectPoint(p,
							dist, lines[i], cuts[j]));
				}
			}
		}
		return pointMap.values().toArray(new PathCutIntersectPoint[0]);
	}

	/**
	 * Get intersection point plus start/end point
	 * 
	 * @param l
	 * @return
	 */
	public PathCutIntersectPoint[] getAllCriticalPoints(Cut[] cuts) {

		SortedMap<Double, PathCutIntersectPoint> pointMap = new TreeMap<Double, PathCutIntersectPoint>();

		Line2D[] lines = getLineArray();
		for (int i = 0; i < lines.length; i++) {
			for (int j = 0; j < cuts.length; j++) {
				Point2D p = Algorithm.getSegmentIntersect(lines[i], cuts[j]
						.getCut());
				if (p != null) {
					double dist = ptDistFromStart(p, lines, i);
					pointMap.put(new Double(dist), new PathCutIntersectPoint(p,
							dist, lines[i], cuts[j]));
				}
			}
		}

		// Add start and end point
		pointMap.put(new Double(0), new PathCutIntersectPoint(pointList.get(0),
				0, lines[0], Algorithm.CUT_TYPE.NONE));

		Point2D endPoint = pointList.get(pointList.size() - 1);
		double dist = ptDistFromStart(endPoint);
		pointMap.put(new Double(dist), new PathCutIntersectPoint(endPoint,
				dist, lines[lines.length - 1], Algorithm.CUT_TYPE.NONE));

		return pointMap.values().toArray(new PathCutIntersectPoint[0]);
	}

	/**
	 * Paint the polygon
	 * 
	 * @param g
	 */
	public void draw(Graphics2D g) {
		Line2D[] lines = getLineArray();

		// Draw all lines
		g.setColor(getLineColor());
		for (int i = 0; i < lines.length; i++) {
			g.drawLine((int) lines[i].getX1() + dc.X_ORIGION,
					(int) (dc.Y_MAX - lines[i].getY1() - dc.Y_ORIGION), (int) lines[i].getX2() + dc.X_ORIGION,
					(int) (dc.Y_MAX - lines[i].getY2() - dc.Y_ORIGION));
		}
	}

	/**
	 * Paint the polygon
	 * 
	 * @param g
	 */
	public void drawText(Graphics2D g) {
		Point2D[] points = getPointArray();

		// Setup
		g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 16 * dc.X_MAX / 1000));
		g.setColor(getTextColor());

		// Draw all points
		for (int i = 0; i < points.length; i++) {
			g.drawString("" + (i + 1), ((int) points[i].getX()
					+ (points[i].getX() > dc.X_MAX - 20 ? -20 : 16)) + dc.X_ORIGION,
					(int) (dc.Y_MAX - points[i].getY() - dc.Y_ORIGION) + 16);
		}
	}

	/**
	 * Paint the polygon
	 * 
	 * @param g
	 */
	public void drawDebugText(Graphics2D g) {
		Point2D[] points = getPointArray();

		// Setup
		g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 20));
		g.setColor(getTextColor());

		// Draw all points
		for (int i = 0; i < points.length; i++) {
			g.drawString("" + (i + 1) + " [" + (int) points[i].getX() + ", "
					+ (int) points[i].getY() + "]", (int) points[i].getX()
					+ (points[i].getX() > dc.X_MAX - 80 ? -80 : 6),
					(int) (dc.Y_MAX - points[i].getY())
							+ (points[i].getX() > dc.X_MAX - 80 ? -4 : 16));
		}
	}

}
