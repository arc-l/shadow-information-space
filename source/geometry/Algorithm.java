package geometry;

import geometry.cut.Bitangent;
import geometry.cut.Cut;
import geometry.cut.GeneralInflection;
import geometry.cut.PathCutIntersectPoint;
import geometry.drawable.DrawingContext;
import geometry.drawable.Path;
import geometry.drawable.Polygon;
import geometry.gap.Gap;
import geometry.gap.PhysicalGap;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Caculates a variety of inflections
 * 
 * @author Jingjin Yu
 * 
 */
public class Algorithm {

	// Type of inflection
	public static enum INFLECTION_TYPE {
		GENERAL, NONGENERAL, ALL
	}

	// Type of cuts
	public static enum CUT_TYPE {
		GENERAL_INFLECTION, NONGENERAL_INFLECTION, SINGLETANGENT, BITANGENT, NONE
	}

	// Type of cuts
	// The radius for testing whether two numbers are equal
	public static double epsilon = 0.00005;

	// Purturbing amount
	public static double purturb = 0.005;

	public static void setEpsilon(double epsilon) {
		Algorithm.epsilon = epsilon;
	}

	public static void setPurturb(double purturb) {
		Algorithm.purturb = purturb;
	}

	/**
	 * Check whether two numbers are epsilon equal.
	 * 
	 * @param n1
	 * @param n2
	 * @return
	 */
	public static boolean epsilonEqual(double n1, double n2) {
		if (Math.abs(n1 - n2) <= epsilon) {
			return true;
		}
		return false;
	}

	public static Gap[][] getGaps(Polygon polygon, Path path) {

		Line2D[] lines = polygon.getLineSegmentArray();
		IDGenerator idGen = new IDGenerator();
		double pathLength = path.getLength();
		double pathRelativeTime = 0;

		// Grab all inflections and bitangents for polygon
		Cut[] cuts = getCuts(polygon);

		// Get all intersection points, ordered by distance, including start and
		// end point
		PathCutIntersectPoint[] pcips = path.getAllCriticalPoints(cuts);

		Vector<Gap[]> gapsVec = new Vector<Gap[]>();
		Vector<PhysicalGap[]> pgVec = new Vector<PhysicalGap[]>();
		PhysicalGap[] previousGaps = null;
		// Go through those intersect points
		for (int i = 0; i < pcips.length; i++) {
//			System.out.println("" + i + " " +  pcips[i].getPoint().getX() + " " + pcips[i].getPoint().getY());
			PathCutIntersectPoint pcip = pcips[i];

			// Get a point on the path that's a little further to avoid
			// numerical issues
			Point2D pp = purturbPointAlongSeg(pcip.getPoint(), pcip.getSeg(),
					purturb, true);
			PhysicalGap[] pg = getPhysicalGaps(polygon, pp);
			pgVec.add(pg);

			// Calculate how gaps will evolve. Only need to work on general
			// inflection and bitangents

			// For general inflection, either a new gap will appear or a old
			// gap will disappear
			if (pcip.getCutType() == CUT_TYPE.GENERAL_INFLECTION) {
				// Get the cut
				GeneralInflection cut = (GeneralInflection) pcip.getCut();

				// Get previous point on path
				Point2D lastPoint = pcips[i - 1].getPoint();

				// If the edge the inflection coming out is visible from
				// line then a gap will disappear. Figure out the point
				// that's not on the inflection first
				Line2D fromLine = lines[cut.getFromLine()];
				// Point2D p = fromLine.getP1();
				// if (p.distance(cut.getCut().getP1()) < epsilon
				// || p.distance(cut.getCut().getP2()) < epsilon) {
				// p = fromLine.getP2();
				// }
				// Line2D temp = new Line2D.Double(p, lastPoint);

				// A gap will appear?
				if (fromLine.relativeCCW(lastPoint) == -1) {
					Vector<PhysicalGap> gapVec = new Vector<PhysicalGap>();
					for (int j = 0; j < pg.length; j++) {
						// This is the gap that appears
						if (lineInGap(cut.getFromLine(), pg[j], lines.length)) {
							// Assign new id
							pg[j].setId(idGen.getNextId());
						}
						// Add other gaps for collapsing
						else {
							gapVec.add(pg[j]);
						}
					}
					// Collapse previous gap with this one to propagate ID
					PhysicalGap[][] pgss = new PhysicalGap[2][];
					pgss[0] = previousGaps;
					pgss[1] = gapVec.toArray(new PhysicalGap[0]);
					collapsePhysicalGaps(pgss, null, lines.length);

					// Done for this case
				}
				// Or old gap will disappear?
				else {
					// Need to find the gap that will disappear and remove
					// it from previousGaps before collapsing
					Vector<PhysicalGap> gapVec = new Vector<PhysicalGap>();
					for (int j = 0; j < previousGaps.length; j++) {
						// This is the gap that appears
						if (lineInGap(cut.getFromLine(), previousGaps[j],
								lines.length)) {
							// We dont need to do extra now
						}
						// Add other gaps for collapsing
						else {
							gapVec.add(previousGaps[j]);
						}
					}
					// Collapse previous gap with this one to propagate ID
					PhysicalGap[][] pgss = new PhysicalGap[2][];
					pgss[0] = gapVec.toArray(new PhysicalGap[0]);
					pgss[1] = pg;
					collapsePhysicalGaps(pgss, null, lines.length);

					// Done for this case
				}
			}
			// For bitangent line, a gap will split or gaps will merge
			else if (pcip.getCutType() == CUT_TYPE.BITANGENT) {
				// Get bitangent cut
				Bitangent cut = (Bitangent) pcip.getCut();

				// Get previous point on path
				Point2D lastPoint = // pcips[i - 1].getPoint();
				purturbPointAlongSeg(pcips[i - 1].getPoint(), pcips[i - 1]
						.getSeg(), purturb, true);

				// If lastPoint is on different side of curveToPoint w.r.p.t.
				// cutLine, then gaps will merge; otherwise gap will split
				Line2D cutLine = cut.getCut();
				Point2D curveToPoint = cut.getCurveToPoint();
				int curvingProduct = cutLine.relativeCCW(curveToPoint)
						* cutLine.relativeCCW(lastPoint);

				// On same side, then gap will split
				if (curvingProduct == 1) {

					// Opposite emitting point of bitagent
					int itp = cut.getThisPoint();
					int iop = cut.getOppositePoint();

					Vector<PhysicalGap> oldGapVec = new Vector<PhysicalGap>();
					Vector<PhysicalGap> newGapVec = new Vector<PhysicalGap>();

					PhysicalGap tempGap = null;
					// Find out the gap that will split. the gap that will split
					// contains the opposite point, so the point index should
					// fall between start and end edge of the cut
					for (int j = 0; j < previousGaps.length; j++) {
						if (vertexInGap(iop, previousGaps[j], lines.length)) {
							tempGap = previousGaps[j];
						} else {
							oldGapVec.add(previousGaps[j]);
						}
					}

					// Find out the gaps that will come out of the split. Each
					// of the new gaps should contain a full line on that
					// contains this point or opposite point
					int i12 = itp, i22 = iop;
					int i11 = (itp == 0 ? lines.length - 1 : itp - 1);
					int i21 = (iop == 0 ? lines.length - 1 : iop - 1);
					for (int j = 0; j < pg.length; j++) {
						// This is one of the gap that the old gap splits into
						if (lineInGap(i11, pg[j], lines.length)
								|| lineInGap(i12, pg[j], lines.length)
								|| lineInGap(i21, pg[j], lines.length)
								|| lineInGap(i22, pg[j], lines.length)) {
							// Assign new id
							pg[j].setId(idGen.getNextId());
							tempGap.addGap(pg[j].getId());
						}
						// Add other gaps for collapsing
						else {
							newGapVec.add(pg[j]);
						}
					}
					// Collapse previous gap with this one to propagate ID
					PhysicalGap[][] pgss = new PhysicalGap[2][];
					pgss[0] = oldGapVec.toArray(new PhysicalGap[0]);
					pgss[1] = newGapVec.toArray(new PhysicalGap[0]);
					collapsePhysicalGaps(pgss, null, lines.length);

					// Done for this case
				}
				// On different side, gaps will merge
				else {

					// Opposite emitting point of bitagent
					int itp = cut.getThisPoint();
					int iop = cut.getOppositePoint();

					Vector<PhysicalGap> oldGapVec = new Vector<PhysicalGap>();
					Vector<PhysicalGap> newGapVec = new Vector<PhysicalGap>();
					Vector<PhysicalGap> tempGapVec = new Vector<PhysicalGap>();

					// Find out the gaps that will merge. the gaps that will
					// merge contains a full edge from itp or iop
					int i12 = itp, i22 = iop;
					int i11 = (itp == 0 ? lines.length - 1 : itp - 1);
					int i21 = (iop == 0 ? lines.length - 1 : iop - 1);
					for (int j = 0; j < previousGaps.length; j++) {
						// This is one of the gap that the old gap splits into
						if (lineInGap(i11, previousGaps[j], lines.length)
								|| lineInGap(i12, previousGaps[j], lines.length)
								|| lineInGap(i21, previousGaps[j], lines.length)
								|| lineInGap(i22, previousGaps[j], lines.length)) {
							// Assign new id
							tempGapVec.add(previousGaps[j]);
						}
						// Add other gaps for collapsing
						else {
							oldGapVec.add(previousGaps[j]);
						}
					}

					// Find out the gaps that will come out of the merge. The
					// merged gap contains the opposite point, so the point
					// index should fall between start and end edge of the cut
					for (int j = 0; j < pg.length; j++) {
						if (vertexInGap(iop, pg[j], lines.length)) {
							pg[j].setId(idGen.getNextId());
							for (Iterator iter = tempGapVec.iterator(); iter
									.hasNext();) {
								PhysicalGap gap = (PhysicalGap) iter.next();
								gap.addGap(pg[j].getId());
							}
						} else {
							newGapVec.add(pg[j]);
						}
					}

					// Collapse previous gap with this one to propagate ID
					PhysicalGap[][] pgss = new PhysicalGap[2][];
					pgss[0] = oldGapVec.toArray(new PhysicalGap[0]);
					pgss[1] = newGapVec.toArray(new PhysicalGap[0]);
					collapsePhysicalGaps(pgss, null, lines.length);

					// Done
				}
			}

			// Get the time where a gap event first happens 
			if (i == 0 || pcips[i].getCutType() == CUT_TYPE.GENERAL_INFLECTION
					|| pcips[i].getCutType() == CUT_TYPE.BITANGENT) {
				pathRelativeTime = path.ptDistFromStart(pcips[i].getPoint())
						/ pathLength;
			}

			// If next point is the end point or general
			// inflection/bitangent crossing, then collapse all gaps
			// collected so far
			if (i == pcips.length - 1
					|| pcips[i + 1].getCutType() == CUT_TYPE.GENERAL_INFLECTION
					|| pcips[i + 1].getCutType() == CUT_TYPE.BITANGENT) {
				previousGaps = collapsePhysicalGaps(pgVec
						.toArray(new PhysicalGap[0][]), idGen, lines.length);
				previousGaps[0].setRelativeTime(pathRelativeTime);
				gapsVec.add(previousGaps);
				pgVec.clear();
			}

		}

		return gapsVec.toArray(new Gap[0][]);
	}

	/**
	 * Check whether a vertex is in the inside of a given gap
	 * 
	 * @param vertex
	 * @param gap
	 * @param numEdges
	 * @return
	 */
	private static boolean vertexInGap(int iv, PhysicalGap gap, int numEdges) {
		int s = gap.getStartEdge();
		int e = gap.getEndEdge();
		if ((iv > s && iv <= e)
				|| ((e < s) && ((iv > s && iv < numEdges) || (iv <= e && iv >= 0)))) {
			return true;
		}
		return false;
	}

	/**
	 * Check whether a line is in a given gap
	 * 
	 * @param fromLine
	 * @param gap
	 * @param numEdges
	 * @return
	 */
	private static boolean lineInGap(int fromLine, PhysicalGap gap, int numEdges) {
		int fs = gap.getFullStartEdge(numEdges);
		int fe = gap.getFullEndEdge(numEdges);
		if ((fromLine >= fs && fromLine <= fe)
				|| ((fe < fs) && ((fromLine >= fs && fromLine < numEdges) || (fromLine <= fe && fromLine >= 0)))) {
			return true;
		}
		return false;
	}

	/**
	 * Collapse the gaps into a single gap set while preserving the numbering
	 * 
	 * @param gaps
	 * @param idGen
	 * @param numEdges
	 * @return
	 */
	private static PhysicalGap[] collapsePhysicalGaps(PhysicalGap[][] pgs,
			IDGenerator idGen, int numEdges) {

		int numGaps = pgs[0].length;
		if (numGaps == 0)
			return new PhysicalGap[0];

		// Assign id for first gap if none have been assigned
		if (pgs[0][0].getId() == -1) {
			for (int i = 0; i < numGaps; i++) {
				pgs[0][i].setId(idGen.getNextId());
			}
		}

		// From the first set of gaps, pair wise link gap ids
		for (int i = 0; i < pgs.length - 1; i++) {
			// Try to match i and i + 1
			PhysicalGap pg0 = pgs[i][0];
			for (int j = 0; j < numGaps; j++) {
				PhysicalGap pg1 = pgs[i + 1][j];
				// If we get a match
				if (samePhysicalGap(pg0, pg1, numEdges)) {
					// Collapse the common edge set
					for (int k = 0; k < numGaps; k++) {
						int nextGap = k + j;
						if (nextGap >= numGaps)
							nextGap -= numGaps;

						// Bring along the ids
						pgs[i + 1][nextGap].setId(pgs[i][k].getId());
					}
				}
			}
		}

		// Collapse the common edges by picking one between
		Map<Integer, Integer> idCseMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> idCeeMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> idCeMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < numGaps; i++) {
			idCseMap.put(pgs[0][i].getId(), pgs[0][i]
					.getFullStartEdge(numEdges));
			idCeeMap.put(pgs[0][i].getId(), pgs[0][i].getFullEndEdge(numEdges));
		}

		for (int i = 1; i < pgs.length; i++) {
			for (int j = 0; j < numGaps; j++) {

				// Work on start edge. Start edge should not go smaller
				// unless it's a jump from the last edge
				int cse = idCseMap.get(pgs[i][j].getId()).intValue();
				int se = pgs[i][j].getFullStartEdge(numEdges);
				if (se != cse) {
					if (se > cse) {
						if (se == cse + 1)
							cse = se;
					} else if (se < cse) {
						if (se + cse == numEdges)
							cse = se;
					}
				}
				idCseMap.put(pgs[i][j].getId(), cse);

				// Work on end edge. End edge should not go bigger
				// unless it's a jump from the first edge to last
				int cee = idCeeMap.get(pgs[i][j].getId()).intValue();
				int ee = pgs[i][j].getFullEndEdge(numEdges);

				if (ee != cee) {
					if (ee > cee) {
						if (ee + cee == numEdges)
							cee = ee;
					} else if (ee < cee) {
						if (ee + 1 == cee)
							cee = ee;
					}
				}
				idCeeMap.put(pgs[i][j].getId(), cee);
			}
		}

		// Collapse startedge and endedge into a single commom edge
		Integer[] ids = idCseMap.keySet().toArray(new Integer[0]);
		for (int i = 0; i < ids.length; i++) {
			int ce = 0;
			int cse = idCseMap.get(ids[i]).intValue();
			int cee = idCeeMap.get(ids[i]).intValue();
			if (cee >= cse) {
				ce = (cse + cee) / 2;
			} else {
				ce = 0;
			}
			idCeMap.put(ids[i], ce);
		}

		// Assign to last physical gap set and return that
		PhysicalGap[] pg = pgs[pgs.length - 1];
		for (int i = 0; i < pg.length; i++) {
			int id = pg[i].getId();
			pg[i].setIEdge(idCeMap.get(id).intValue());
		}

		return pg;
	}

	/**
	 * Get whether two gaps are enssentially the same based on the common edges
	 * they are sharing; thre should be only one edge difference
	 * 
	 * @param pg0
	 * @param pg1
	 * @param numEdges
	 * @return
	 */
	private static boolean samePhysicalGap(PhysicalGap pg0, PhysicalGap pg1,
			int numEdges) {
		int se0 = pg0.getStartEdge();
		int se1 = pg1.getStartEdge();
		int ee0 = pg0.getEndEdge();
		int ee1 = pg1.getEndEdge();
		if ((Math.abs(se0 - se1) <= 1 || se0 + se1 == numEdges - 1)
				&& (Math.abs(ee0 - ee1) <= 1 || ee0 + ee1 == numEdges - 1)) {
			return true;
		}

		return false;
	}

	/**
	 * Purturbing the given point p on the seg so that it's either a little far
	 * or a little close to start point
	 * 
	 * @param p
	 * @param seg
	 * @return
	 */
	public static Point2D purturbPointAlongSeg(Point2D p, Line2D seg,
			double ptAmount, boolean far) {
		// Get two points on seg around p
		Point2D p1 = null, p2 = null;
		double s = getSlope(seg);
		if (Double.isInfinite(s)) {
			p1 = new Point2D.Double(p.getX(), p.getY() + ptAmount);
			p2 = new Point2D.Double(p.getX(), p.getY() - ptAmount);
		} else if (s == 0) {
			p1 = new Point2D.Double(p.getX() + ptAmount, p.getY());
			p2 = new Point2D.Double(p.getX() - ptAmount, p.getY());
		} else if (Math.abs(s) > 1) {
			double y = p.getY();
			p1 = getPointOnLineFromY(seg, y + ptAmount);
			p2 = getPointOnLineFromY(seg, y - ptAmount);
		} else if (Math.abs(s) < 1) {
			double x = p.getX();
			p1 = getPointOnLineFromX(seg, x + ptAmount);
			p2 = getPointOnLineFromX(seg, x - ptAmount);
		}

		double dist1 = p1.distance(seg.getP1());
		double dist2 = p2.distance(seg.getP1());
		if ((dist1 > dist2 && far) || (dist1 < dist2 && !far)) {
			return p1;
		} else
			return p2;
	}

	/**
	 * Get gaps for a point in a polygon. This is essentially same as getting
	 * the visibility polygon except that we want to have more information
	 * recorded to represent gaps
	 * 
	 * @param lines
	 * @param point
	 * @return
	 */
	public static PhysicalGap[] getPhysicalGaps(Polygon polygon, Point2D point) {
		Line2D lines[] = polygon.getLineSegmentArray();

		Vector<PhysicalGap> pgVec = new Vector<PhysicalGap>();

		// After lines[i] is checked and part of it is identified to be visible,
		// ip1 of next visible line cannot come from an endpoint before
		// lines[i]. This suggests that during the calculation of next ip1, we
		// don't need to check any lines after this line until after new
		// lines[i]
		int nj = 0;

		// For each line segment, check whether vertices from other segments
		// blocks view of point to this segment
		for (int i = 0; i < lines.length; i++) {

			Line2D seg = lines[i];
			Point2D p1 = seg.getP1();
			Point2D p2 = seg.getP2();
			Line2D pp1 = new Line2D.Double(point, p1);
			Line2D pp2 = new Line2D.Double(point, p2);

			boolean pp1InPolygon = segInPolygon(polygon, pp1);
			boolean pp2InPolygon = segInPolygon(polygon, pp2);

			// If the entire seg is not visible, gather all points on the
			// segment that are intersections from the extension of the segment
			// connecting point and another vertex
			if (!pp1InPolygon || !pp2InPolygon) {

				Point2D ip1 = null, ip2 = null;
				int ip1n = -1, ip2n = -1;
				double dist1 = Double.MAX_VALUE, dist2 = Double.MAX_VALUE;

				// If lines[i] is blocked by some lines[j] to produce a valid
				// ip2, then all segments between lines[i] and lines[j]
				// are not visible. ni is used to store this valid j
				int ni = i;

				// Update nj to be current i
				nj = i;

				// Snapshot of i for storing
				int lastI = i;

				for (int j = 0; j < lines.length; j++) {
					// No need to work on point of current segment and segments
					// between nj and i, exclusive
					if (j == i || (i < lines.length - 1 && j == i + 1)
							|| (i == lines.length - 1 && j == 0)
							|| (j < i && j > nj))
						continue;

					Point2D p = lines[j].getP1();
					Line2D pp = new Line2D.Double(point, p);
					Point2D ip = getIntersect(pp, seg);

					// Intersection point must be on extension of pp
					if (ip != null && onExtension(pp, ip)) {
						// If the intersection point is on the extension of seg,
						// then the whole segment is covered and we can stop
						// here
						if (onExtension(seg, ip) || onReverseExtension(seg, ip)) {
							continue;
						}

						Line2D ppExt = new Line2D.Double(p, ip);
						// Otherwise we need to make sure that pp and ppExt are
						// both inside polygon
						if (segInPolygon(polygon, pp)
								&& segInPolygon(polygon, ppExt)) {
							// We have a point that we need to keep, sort it
							// according to the distance towards p1
							double dist = ip.distance(p1);
							if (dist < dist1 && !pp1InPolygon) {
								dist1 = dist;
								ip1 = ip;
								ip1n = j;
							}

							dist = ip.distance(p2);
							if (dist < dist2 && !pp2InPolygon) {
								dist2 = dist;
								ip2 = ip;
								ip2n = j;
								ni = j;
							}
						}

						// We can break whenever we find what we need
						if ((pp1InPolygon && ip2 != null)
								|| (pp2InPolygon && ip1 != null)
								|| (ip1 != null && ip2 != null && ip1 != ip2)) {

							// Also update i to avoid unnecessary calculations
							if (ni > i) {
								i = ni - 1;
							} else if (ip2 != null) {
								// This cases covers when ni is a smaller number
								// than i. When this happens, it suggests that
								// we are done
								i = lines.length - 1;
							}
							break;
						}
					}
				}

				// Add ip1 and ip2 if any
				if (ip1 != null) {
					pgVec.add(new PhysicalGap(ip1n, lastI, null, ip1));
				}
				if (ip2 != null) {
					pgVec.add(new PhysicalGap(lastI, ip2n != 0 ? ip2n - 1
							: lines.length - 1, ip2, null));
				}
			}
		}

		return pgVec.toArray(new PhysicalGap[0]);
	}

	/**
	 * Returns the visibility polygon of a point inside polygon. The algorithm
	 * operates line by line, for each line it checks whether part of the line
	 * is visible and if so, find the critical points on visibility boundary
	 * 
	 * @param lines
	 * @param point
	 * @return
	 */
	public static Polygon getVisibilityPolygon(Polygon polygon, Point2D point, DrawingContext dc) {
		Line2D lines[] = polygon.getLineSegmentArray();

		Polygon poly = new Polygon(dc);

		// After lines[i] is checked and part of it is identified to be visible,
		// ip1 of next visible line cannot come from an endpoint before
		// lines[i]. This suggests that during the calculation of next ip1, we
		// don't need to check any lines after this line until after new
		// lines[i]
		int nj = 0;

		// For each line segment, check whether vertices from other segments
		// blocks view of point to this segment
		for (int i = 0; i < lines.length; i++) {

			Line2D seg = lines[i];
			Point2D p1 = seg.getP1();
			Point2D p2 = seg.getP2();
			Line2D pp1 = new Line2D.Double(point, p1);
			Line2D pp2 = new Line2D.Double(point, p2);

			boolean pp1InPolygon = segInPolygon(polygon, pp1);
			boolean pp2InPolygon = segInPolygon(polygon, pp2);

			// Add first point if pp1 is inside polygon
			if (pp1InPolygon)
				poly.addVertex(p1);

			// If the entire seg is not visible, gather all points on the
			// segment that are intersections from the extension of the segment
			// connecting point and another vertex
			if (!pp1InPolygon || !pp2InPolygon) {

				Point2D ip1 = null, ip2 = null;
				double dist1 = Double.MAX_VALUE, dist2 = Double.MAX_VALUE;

				// If lines[i] is blocked by some lines[j] to produce a valid
				// ip2, then all segments between lines[i] and lines[j]
				// are not visible. ni is used to store this valid j
				int ni = i;

				// Update nj to be current i
				nj = i;

				for (int j = 0; j < lines.length; j++) {
					// No need to work on point of current segment and segments
					// between nj and i, exclusive
					if (j == i || (i < lines.length - 1 && j == i + 1)
							|| (i == lines.length - 1 && j == 0)
							|| (j < i && j > nj))
						continue;

					Point2D p = lines[j].getP1();
					Line2D pp = new Line2D.Double(point, p);
					Point2D ip = getIntersect(pp, seg);

					// Intersection point must be on extension of pp
					if (ip != null && onExtension(pp, ip)) {
						// If the intersection point is on the extension of seg,
						// then the whole segment is covered and we can stop
						// here
						if (onExtension(seg, ip) || onReverseExtension(seg, ip)) {
							continue;
						}

						Line2D ppExt = new Line2D.Double(p, ip);
						// Otherwise we need to make sure that pp and ppExt are
						// both inside polygon
						if (segInPolygon(polygon, pp)
								&& segInPolygon(polygon, ppExt)) {
							// We have a point that we need to keep, sort it
							// according to the distance towards p1
							double dist = ip.distance(p1);
							if (dist < dist1 && !pp1InPolygon) {
								dist1 = dist;
								ip1 = ip;
							}

							dist = ip.distance(p2);
							if (dist < dist2 && !pp2InPolygon) {
								dist2 = dist;
								ip2 = ip;
								ni = j;
							}
						}

						// We can break whenever we find what we need
						if ((pp1InPolygon && ip2 != null)
								|| (pp2InPolygon && ip1 != null)
								|| (ip1 != null && ip2 != null && ip1 != ip2)) {

							// Also update i to avoid unnecessary calculations
							if (ni > i) {
								i = ni - 1;
							} else if (ip2 != null) {
								// This cases covers when ni is a smaller number
								// than i. When this happens, it suggests that
								// we are done
								i = lines.length - 1;
							}
							break;
						}
					}
				}

				// Add ip1 and ip2 if any
				if (ip1 != null) {
					poly.addVertex(ip1);
				}
				if (ip2 != null) {
					poly.addVertex(ip2);
				}
			}

			// Add second point if pp2 is inside polygon
			if (pp2InPolygon)
				poly.addVertex(p2);
		}

		return poly;
	}

	/**
	 * Test whether a segment is inside a polygon
	 * 
	 * @param polygon
	 * @param line
	 * @return
	 */
	public static boolean segInPolygon(Polygon polygon, Line2D seg) {

		// First test both points of segment as well as the mid point is inside
		// polygon or on polygon
		GeneralPath gp = polygon.getGeneralPath();
		Line2D[] lines = polygon.getLineSegmentArray();
		Point2D p1 = seg.getP1(), p2 = seg.getP2();
		Point2D p12 = new Point2D.Double((p1.getX() + p2.getX()) / 2, (p1
				.getY() + p2.getY()) / 2);

		if (!gp.contains(p12))
			return false;

		boolean p1OnPolygon = false, p2OnPolygon = false;
		for (int i = 0; i < lines.length; i++) {
			Line2D crossSeg = lines[i];
			if (crossSeg.ptSegDist(p1) < epsilon)
				p1OnPolygon = true;
			if (crossSeg.ptSegDist(p2) < epsilon)
				p2OnPolygon = true;
		}

		if ((p1OnPolygon || gp.contains(p1))
				&& (p2OnPolygon || gp.contains(p2))) {

			// Check whether this segment intersect other segments besides the
			// end points
			for (int i = 0; i < lines.length; i++) {
				Line2D crossSeg = lines[i];
				if (crossSeg.intersectsLine(seg)) {
					Point2D p = getIntersect(seg, crossSeg);
					if (p != null && p.distance(p1) > epsilon
							&& p.distance(p2) > epsilon) {
						return false;
					}
				}
			}
			return true;
		}

		return false;
	}

	/**
	 * Get all cuts in an array with types
	 * 
	 * @param polygon
	 * @return
	 */
	public static Cut[] getCuts(Polygon polygon) {
		Vector<Cut> cutVec = new Vector<Cut>();

		// Take care of non single tangent inflection
		Cut[] sts = Algorithm.getSingletangentCut(polygon);
		for (int i = 0; i < sts.length; i++) {
			cutVec.add(sts[i]);
		}

		// Take care of non general inflection
		Line2D[] infs = Algorithm.getInflection(polygon,
				Algorithm.INFLECTION_TYPE.NONGENERAL);
		for (int i = 0; i < infs.length; i++) {
			cutVec.add(new Cut(infs[i],
					Algorithm.CUT_TYPE.NONGENERAL_INFLECTION));
		}

		// General inflection
		GeneralInflection[] ginfs = Algorithm.getGeneralInflectionCut(polygon);
		for (int i = 0; i < ginfs.length; i++) {
			cutVec.add(ginfs[i]);
		}

		// Bitangent
		Bitangent[] bits = Algorithm.getBitangentCut(polygon);
		for (int i = 0; i < bits.length; i++) {
			cutVec.add(bits[i]);
		}

		return cutVec.toArray(new Cut[0]);

	}

	/**
	 * Calculate all single tangent lines by looking at every pair of vertices
	 * that aren't adjecent. For each pair of vertices, do the following:
	 * 
	 * @param polygon
	 * @return
	 */
	public static Cut[] getSingletangentCut(Polygon polygon) {
		Line2D[] lines = polygon.getLineSegmentArray();

		Vector<Cut> outVec = new Vector<Cut>();

		// For each vertext i
		for (int i = 0; i < lines.length; i++) {
			Point2D p1 = lines[i].getP1();
			Line2D l11 = i == 0 ? lines[lines.length - 1] : lines[i - 1];
			Line2D l12 = lines[i];

			// For each non adjacent vertex j
			for (int j = i + 2; j < lines.length && j != i; j++) {
				// j should be different from i, i - 1, and i + 1
				if (j == lines.length && (i == 0 || i == lines.length - 1))
					continue;

				Point2D p2 = lines[j].getP1();
				Line2D l21 = j == 0 ? lines[lines.length - 1] : lines[j - 1];
				Line2D l22 = lines[j];

				// Make sure at last one of the angles curve inside the polygon
				if (l11.relativeCCW(l12.getP2()) != 1
						&& l21.relativeCCW(l22.getP2()) != 1)
					continue;

				// Test whether the line segment connecting p1 and p2
				// is on one side of l11, l12 and l21, l22
				Line2D seg12 = new Line2D.Double(p1, p2);
				int fromP1 = seg12.relativeCCW(l11.getP1())
						* seg12.relativeCCW(l12.getP2());
				int fromP2 = seg12.relativeCCW(l21.getP1())
						* seg12.relativeCCW(l22.getP2());
				if (fromP1 * fromP2 != -1) {
					continue;
				}

				// seg12 must be in polygon
				if (!Algorithm.segInPolygon(polygon, seg12))
					continue;

				// Now we do have a single tangent, calculate the line segment
				Point2D ccwPoint = null, cwPoint = null;
				double ccwDist = Double.MAX_VALUE, cwDist = Double.MAX_VALUE;
				for (int k = 0; k < lines.length; k++) {
					Line2D seg = lines[k];
					// No need to check the four lines l11, l12, l21, and
					// l22
					if (seg != l11 && seg != l12 && seg != l21 && seg != l22) {
						// See whether we have intersection that's closer
						Point2D p = getIntersect(seg, seg12);
						if (p != null && seg.ptSegDist(p) < epsilon) {
							double dist = seg12.ptSegDist(p);
							if (onExtension(seg12, p)) {
								if (dist < ccwDist) {
									ccwPoint = p;
									ccwDist = dist;
								}
							} else {
								if (dist < cwDist) {
									cwPoint = p;
									cwDist = dist;
								}
							}
						}
					}
				}

				// Connect to the point that's closer
				if (fromP1 == 1) {
					if (ccwPoint != null
							&& p1.distanceSq(ccwPoint) < p2
									.distanceSq(ccwPoint)) {
						outVec.add(new Cut(new Line2D.Double(p1, ccwPoint),
								CUT_TYPE.SINGLETANGENT));
					} else {
						outVec.add(new Cut(new Line2D.Double(p1, cwPoint),
								CUT_TYPE.SINGLETANGENT));
					}

				} else {
					if (ccwPoint != null
							&& p2.distanceSq(ccwPoint) < p1
									.distanceSq(ccwPoint)) {
						outVec.add(new Cut(new Line2D.Double(p2, ccwPoint),
								CUT_TYPE.SINGLETANGENT));
					} else {
						outVec.add(new Cut(new Line2D.Double(p2, cwPoint),
								CUT_TYPE.SINGLETANGENT));
					}

				}

			}
		}

		return outVec.toArray(new Cut[0]);
	}

	/**
	 * Calculate all bitangent lines by looking at every pair of vertices that
	 * aren't adjecent. For each pair of vertices, do the following:
	 * 
	 * 1. Ensure that the line segment connecting the vertices are initially
	 * inside the polygon by looking at the curving angle. 2. Ensure the segment
	 * doesn't intersect any other line segments. 3. Calculate other two points
	 * of intersection from the extension of this line segment
	 * 
	 * @param lines
	 * @return
	 */
	public static Bitangent[] getBitangentCut(Polygon polygon) {
		Line2D[] lines = polygon.getLineSegmentArray();

		Vector<Bitangent> outVec = new Vector<Bitangent>();

		// For each vertext i
		for (int i = 0; i < lines.length; i++) {
			Point2D p1 = lines[i].getP1();
			Line2D l11 = i == 0 ? lines[lines.length - 1] : lines[i - 1];
			Line2D l12 = lines[i];

			// Make sure the angle curves inside the polygon
			if (l11.relativeCCW(l12.getP2()) != 1)
				continue;

			// For each non adjacent vertex j
			for (int j = i + 2; j < lines.length && j != i; j++) {
				// j should be different from i, i - 1, and i + 1
				if (j == lines.length && (i == 0 || i == lines.length - 1))
					continue;

				Point2D p2 = lines[j].getP1();
				Line2D l21 = j == 0 ? lines[lines.length - 1] : lines[j - 1];
				Line2D l22 = lines[j];

				// Make sure the angle curves inside the polygon
				if (l21.relativeCCW(l22.getP2()) != 1)
					continue;

				// Test whether the line segment connecting p1 and p2 could
				// is on one side of l11, l12 and l21, l22
				Line2D seg12 = new Line2D.Double(p1, p2);
				if (seg12.relativeCCW(l11.getP1())
						* seg12.relativeCCW(l12.getP2()) != 1
						|| seg12.relativeCCW(l21.getP1())
								* seg12.relativeCCW(l22.getP2()) != 1) {
					continue;
				}

				// Test whether the segment hits other segments
				boolean bitangent = true;
				for (int k = 0; k < lines.length; k++) {
					Line2D seg = lines[k];
					// No need to check the four lines l11, l12, l21, and l22
					if (seg != l11 && seg != l12 && seg != l21 && seg != l22) {
						if (seg.intersectsLine(seg12)) {
							bitangent = false;
							break;
						}
					}
				}

				// Now we do have a bitangent, calculate the line segments
				if (bitangent) {
					Point2D ccwPoint = null, cwPoint = null;
					double ccwDist = Double.MAX_VALUE, cwDist = Double.MAX_VALUE;
					for (int k = 0; k < lines.length; k++) {
						Line2D seg = lines[k];
						// No need to check the four lines l11, l12, l21, and
						// l22
						if (seg != l11 && seg != l12 && seg != l21
								&& seg != l22) {
							// See whether we have intersection that's closer
							Point2D p = getIntersect(seg, seg12);
							if (p != null && seg.ptSegDist(p) < epsilon) {
								double dist = seg12.ptSegDist(p);
								if (onExtension(seg12, p)) {
									if (dist < ccwDist) {
										ccwPoint = p;
										ccwDist = dist;
									}
								} else {
									if (dist < cwDist) {
										cwPoint = p;
										cwDist = dist;
									}
								}
							}
						}
					}

					// Connect to the point that's closer
					if (p1.distanceSq(ccwPoint) < p2.distanceSq(ccwPoint)) {
						outVec.add(new Bitangent(
								new Line2D.Double(p1, ccwPoint), i, j,
								new Line2D.Double(p2, cwPoint), l12.getP2()));
						outVec.add(new Bitangent(
								new Line2D.Double(p2, cwPoint), j, i,
								new Line2D.Double(p1, ccwPoint), l22.getP2()));
					} else {
						outVec.add(new Bitangent(
								new Line2D.Double(p1, cwPoint), i, j,
								new Line2D.Double(p2, ccwPoint), l12.getP2()));
						outVec.add(new Bitangent(
								new Line2D.Double(p2, ccwPoint), j, i,
								new Line2D.Double(p1, cwPoint), l22.getP2()));
					}
					// outVec.add(seg12);
				}
			}
		}

		return outVec.toArray(new Bitangent[0]);
	}

	/**
	 * Calculate all bitangent lines by looking at every pair of vertices that
	 * aren't adjecent. For each pair of vertices, do the following:
	 * 
	 * 1. Ensure that the line segment connecting the vertices are initially
	 * inside the polygon by looking at the curving angle. 2. Ensure the segment
	 * doesn't intersect any other line segments. 3. Calculate other two points
	 * of intersection from the extension of this line segment
	 * 
	 * @param lines
	 * @return
	 */
	public static Line2D[] getBitangent(Polygon polygon) {
		Line2D[] lines = polygon.getLineSegmentArray();

		Vector<Line2D> outVec = new Vector<Line2D>();

		// For each vertext i
		for (int i = 0; i < lines.length; i++) {
			Point2D p1 = lines[i].getP1();
			Line2D l11 = i == 0 ? lines[lines.length - 1] : lines[i - 1];
			Line2D l12 = lines[i];

			// Make sure the angle curves inside the polygon
			if (l11.relativeCCW(l12.getP2()) != 1)
				continue;

			// For each non adjacent vertex j
			for (int j = i + 2; j < lines.length && j != i; j++) {
				// j should be different from i, i - 1, and i + 1
				if (j == lines.length && (i == 0 || i == lines.length - 1))
					continue;

				Point2D p2 = lines[j].getP1();
				Line2D l21 = j == 0 ? lines[lines.length - 1] : lines[j - 1];
				Line2D l22 = lines[j];

				// Make sure the angle curves inside the polygon
				if (l21.relativeCCW(l22.getP2()) != 1)
					continue;

				// Test whether the line segment connecting p1 and p2 could
				// is on one side of l11, l12 and l21, l22
				Line2D seg12 = new Line2D.Double(p1, p2);
				if (seg12.relativeCCW(l11.getP1())
						* seg12.relativeCCW(l12.getP2()) != 1
						|| seg12.relativeCCW(l21.getP1())
								* seg12.relativeCCW(l22.getP2()) != 1) {
					continue;
				}

				// Test whether the segment hits other segments
				boolean bitangent = true;
				for (int k = 0; k < lines.length; k++) {
					Line2D seg = lines[k];
					// No need to check the four lines l11, l12, l21, and l22
					if (seg != l11 && seg != l12 && seg != l21 && seg != l22) {
						if (seg.intersectsLine(seg12)) {
							bitangent = false;
							break;
						}
					}
				}

				// Now we do have a bitangent, calculate the line segments
				if (bitangent) {
					Point2D ccwPoint = null, cwPoint = null;
					double ccwDist = Double.MAX_VALUE, cwDist = Double.MAX_VALUE;
					for (int k = 0; k < lines.length; k++) {
						Line2D seg = lines[k];
						// No need to check the four lines l11, l12, l21, and
						// l22
						if (seg != l11 && seg != l12 && seg != l21
								&& seg != l22) {
							// See whether we have intersection that's closer
							Point2D p = getIntersect(seg, seg12);
							if (p != null && seg.ptSegDist(p) < epsilon) {
								double dist = seg12.ptSegDist(p);
								if (onExtension(seg12, p)) {
									if (dist < ccwDist) {
										ccwPoint = p;
										ccwDist = dist;
									}
								} else {
									if (dist < cwDist) {
										cwPoint = p;
										cwDist = dist;
									}
								}
							}
						}
					}

					// Connect to the point that's closer
					if (p1.distanceSq(ccwPoint) < p2.distanceSq(ccwPoint)) {
						outVec.add(new Line2D.Double(p1, ccwPoint));
						outVec.add(new Line2D.Double(p2, cwPoint));
					} else {
						outVec.add(new Line2D.Double(p1, cwPoint));
						outVec.add(new Line2D.Double(p2, ccwPoint));
					}
					// outVec.add(seg12);
				}
			}
		}

		return outVec.toArray(new Line2D[0]);
	}

	/**
	 * Calculate type of inflection and return as GeneralInflection
	 * 
	 * @param lines
	 * @param type
	 * @return
	 */
	public static GeneralInflection[] getGeneralInflectionCut(Polygon polygon) {
		Line2D[] lines = polygon.getLineSegmentArray();
		Vector<GeneralInflection> outVec = new Vector<GeneralInflection>();

		// For each point of interest
		for (int i = 0; i < lines.length; i++) {
			Point2D poi = lines[i].getP1();

			// Grab previous two lines and next two lines in counterclockwise
			// order
			Line2D nextLine = lines[i];
			Line2D nnLine = (lines.length - 1) == i ? lines[0] : lines[i + 1];
			Line2D prevLine = i == 0 ? lines[lines.length - 1] : lines[i - 1];
			Line2D ppLine = i == 1 ? lines[lines.length - 1]
					: (i == 0 ? lines[lines.length - 2] : lines[i - 2]);

			// Check to make sure the angle curves inside polygon
			if (prevLine.relativeCCW(nextLine.getP2()) == 1) {
				// Work on the possible inflection coming out from prevLine if
				// there is one
				if (ppLine.relativeCCW(poi) == -1) {
					// We have a good curve
					double dist = Double.MAX_VALUE;
					Point2D np = null;
					for (int j = 0; j < lines.length; j++) {
						// Get intersection of this line with other segments
						if (lines[j] != ppLine && lines[j] != prevLine
								&& lines[j] != nextLine) {
							Point2D p = getIntersect(prevLine, lines[j]);
							// Check we have a good intersection
							if (p != null && lines[j].ptSegDist(p) < epsilon
									&& onExtension(prevLine, p)) {
								double curDist = prevLine.ptSegDist(p);
								if (curDist < dist) {
									dist = curDist;
									np = p;
								}
							}
						}
					}
					outVec.add(new GeneralInflection(
							new Line2D.Double(poi, np),
							i == 0 ? lines.length - 1 : i - 1, true));
				}

				// Work on the possible inflection coming out from nextLine if
				// there is one
				if (nnLine.relativeCCW(poi) == -1) {
					// We have a good curve
					double dist = Double.MAX_VALUE;
					Point2D np = null;
					for (int j = 0; j < lines.length; j++) {
						// Get intersection of this line with other segments
						if (lines[j] != nnLine && lines[j] != prevLine
								&& lines[j] != nextLine) {
							Point2D p = getIntersect(nextLine, lines[j]);
							// Check we have a good intersection
							if (p != null && lines[j].ptSegDist(p) < epsilon
									&& onReverseExtension(nextLine, p)) {
								double curDist = nextLine.ptSegDist(p);
								if (curDist < dist) {
									dist = curDist;
									np = p;
								}
							}
						}
					}
					outVec.add(new GeneralInflection(
							new Line2D.Double(poi, np), i, false));
				}
			}

		}

		return outVec.toArray(new GeneralInflection[0]);
	}

	/**
	 * Calculate type of inflection lines. type can be GENERAL, NON_GENERAL, or
	 * ALL.
	 * 
	 * @param lines
	 * @param type
	 * @return
	 */
	public static Line2D[] getInflection(Polygon polygon, INFLECTION_TYPE type) {
		Line2D[] lines = polygon.getLineSegmentArray();
		Vector<Line2D> outVec = new Vector<Line2D>();

		// For each point of interest
		for (int i = 0; i < lines.length; i++) {
			Point2D poi = lines[i].getP1();

			// Grab previous two lines and next two lines in counterclockwise
			// order
			Line2D nextLine = lines[i];
			Line2D nnLine = (lines.length - 1) == i ? lines[0] : lines[i + 1];
			Line2D prevLine = i == 0 ? lines[lines.length - 1] : lines[i - 1];
			Line2D ppLine = i == 1 ? lines[lines.length - 1]
					: (i == 0 ? lines[lines.length - 2] : lines[i - 2]);

			// Check to make sure the angle curves inside polygon
			if (prevLine.relativeCCW(nextLine.getP2()) == 1) {
				// Work on the possible inflection coming out from prevLine if
				// there is one
				if (type == INFLECTION_TYPE.ALL
						|| (type == INFLECTION_TYPE.NONGENERAL && ppLine
								.relativeCCW(poi) == 1)
						|| (type == INFLECTION_TYPE.GENERAL && ppLine
								.relativeCCW(poi) == -1)) {
					// We have a good curve
					double dist = Double.MAX_VALUE;
					Point2D np = null;
					for (int j = 0; j < lines.length; j++) {
						// Get intersection of this line with other segments
						if (lines[j] != ppLine && lines[j] != prevLine
								&& lines[j] != nextLine) {
							Point2D p = getIntersect(prevLine, lines[j]);
							// Check we have a good intersection
							if (p != null && lines[j].ptSegDist(p) < epsilon
									&& onExtension(prevLine, p)) {
								double curDist = prevLine.ptSegDist(p);
								if (curDist < dist) {
									dist = curDist;
									np = p;
								}
							}
						}
					}
					outVec.add(new Line2D.Double(poi, np));
				}

				// Work on the possible inflection coming out from nextLine if
				// there is one
				if (type == INFLECTION_TYPE.ALL
						|| (type == INFLECTION_TYPE.NONGENERAL && nnLine
								.relativeCCW(poi) == 1)
						|| (type == INFLECTION_TYPE.GENERAL && nnLine
								.relativeCCW(poi) == -1)) {
					// We have a good curve
					double dist = Double.MAX_VALUE;
					Point2D np = null;
					for (int j = 0; j < lines.length; j++) {
						// Get intersection of this line with other segments
						if (lines[j] != nnLine && lines[j] != prevLine
								&& lines[j] != nextLine) {
							Point2D p = getIntersect(nextLine, lines[j]);
							// Check we have a good intersection
							if (p != null && lines[j].ptSegDist(p) < epsilon
									&& onReverseExtension(nextLine, p)) {
								double curDist = nextLine.ptSegDist(p);
								if (curDist < dist) {
									dist = curDist;
									np = p;
								}
							}
						}
					}
					outVec.add(new Line2D.Double(poi, np));
				}
			}

		}

		return outVec.toArray(new Line2D[0]);
	}

	/**
	 * Check whether a colinear point is on the direction of extension from
	 * given line, pointing from P1 to P2
	 * 
	 * @param line
	 * @param point
	 * @return
	 */
	public static boolean onExtension(Line2D line, Point2D point) {
		if ((point.getX() - line.getX2()) * (line.getX2() - line.getX1()) > 0
				|| (point.getY() - line.getY2())
						* (line.getY2() - line.getY1()) > 0)
			return true;
		return false;
	}

	/**
	 * Check whether a colinear point is on the renverse direction of extension
	 * from given line, pointing from P1 to P2
	 * 
	 * @param line
	 * @param point
	 * @return
	 */
	public static boolean onReverseExtension(Line2D line, Point2D point) {
		line = new Line2D.Double(line.getP2(), line.getP1());
		if ((point.getX() - line.getX2()) * (line.getX2() - line.getX1()) > 0
				|| (point.getY() - line.getY2())
						* (line.getY2() - line.getY1()) > 0)
			return true;
		return false;
	}

	/**
	 * Tell whether a point is within epsilon distance from a given line segment
	 * 
	 * @param p
	 * @param l
	 * @return
	 */
	public static boolean pointOnSegment(Point2D p, Line2D l) {
		if (l.ptSegDist(p) < epsilon) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the intersection point of to line segments if any.
	 * 
	 * @param line1
	 * @param line2
	 * @return
	 */
	public static Point2D getSegmentIntersect(Line2D line1, Line2D line2) {
		Point2D p = getIntersect(line1, line2);
		if (p != null && line1.ptSegDist(p) < epsilon
				&& line2.ptSegDist(p) < epsilon) {
			return p;
		}
		return null;
	}

	/**
	 * Calculate the point of intersection between to lines (not segments) if
	 * any. If the lines are parallel to each other then return null. If the
	 * lines are identical null is also returned.
	 * 
	 * @param line1
	 * @param line2
	 * @return
	 */
	public static Point2D getIntersect(Line2D line1, Line2D line2) {

		Point2D p = null;

		// Take care of the case that one line has infinite slope first. In this
		// case, a line has fixed x at each point; we can use this to get the
		// point of intersection
		if (epsilonEqual(line1.getX2(), line1.getX1())
				|| epsilonEqual(line2.getX2(), line2.getX1())) {
			// line1 not vertical
			if (!epsilonEqual(line1.getX2(), line1.getX1())) {
				// Calculate the point of intersection
				p = getPointOnLineFromX(line1, line2.getX1());
			}
			// line2 not vertical
			else if (!epsilonEqual(line2.getX2(), line2.getX1())) {
				// Calculate the point of intersection
				p = getPointOnLineFromX(line2, line1.getX1());
			}
		}
		// General conditions, line can be represented as y = ax + b
		else {
			// Calculate slope and intercept
			double k1 = getSlope(line1);
			double k2 = getSlope(line2);
			double i1 = getYIntercept(k1, line1.getP1());
			double i2 = getYIntercept(k2, line2.getP2());

			// Ignore the case that two lines are parallel
			if (!epsilonEqual(k1, k2)) {
				// Calculate intersect point, solve for x first
				double x = (i2 - i1) / (k1 - k2);
				double y = x * k1 + i1;
				p = new Point2D.Double(x, y);
			}
		}

		return p;
	}

	/**
	 * Check whether two line segments (or lines) are actually the same line.
	 * 
	 * @param line1
	 * @param line2
	 * @return
	 */
	public static boolean segmentsOnSameLine(Line2D line1, Line2D line2) {
		// Test vertical lines first
		if (epsilonEqual(line1.getX2(), line1.getX1())
				&& epsilonEqual(line2.getX2(), line2.getX1())
				&& epsilonEqual(line1.getX2(), line2.getX1())) {
			return true;
		}
		// General conditions, line can be represented as y = ax + b
		else {
			// Calculate slope and intercept
			double k1 = getSlope(line1);
			double k2 = getSlope(line2);
			double i1 = getYIntercept(k1, line1.getP1());
			double i2 = getYIntercept(k2, line1.getP2());

			// If slope and intercept are equal then they are the same line
			if (epsilonEqual(k1, k2) && epsilonEqual(i1, i2)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get slope of line.
	 * 
	 * @param line
	 * @return
	 */
	public static double getSlope(Line2D line) {
		if (epsilonEqual(line.getX1(), line.getX2())) {
			return Double.POSITIVE_INFINITY;
		}
		return (line.getY2() - line.getY1()) / (line.getX2() - line.getX1());
	}

	/**
	 * Calculate intercept of a line with the Y axis
	 * 
	 * @param line
	 * @return
	 */
	public static double getYIntercept(Line2D line) {
		double k = getSlope(line);
		if (Double.isInfinite(k)) {
			return k;
		} else {
			return line.getY1() - line.getX1() * k;
		}
	}

	/**
	 * Calculate intercept of a line with the X axis
	 * 
	 * @param line
	 * @return
	 */
	public static double getXIntercept(Line2D line) {
		double k = getSlope(line);
		if (Double.isInfinite(k)) {
			return k;
		} else {
			return line.getY1() - line.getX1() * k;
		}
	}

	/**
	 * Calculate intercept of a line with the Y axis, using a point on the line
	 * and the slope
	 * 
	 * @param line
	 * @return
	 */
	public static double getYIntercept(double k, Point2D p) {
		if (Double.isInfinite(k)) {
			return k;
		} else {
			return p.getY() - p.getX() * k;
		}
	}

	/**
	 * Get Y coordinate from X on line. If the line is vertical and x is not on
	 * it, then return null; otherwise return 0
	 * 
	 * @param line
	 * @param x
	 * @return
	 */
	public static Point2D getPointOnLineFromX(Line2D line, double x) {
		Point2D p = null;
		// The line is a vertical one
		if (epsilonEqual(line.getX1(), line.getX2())) {
			// If x is on the line then return
			if (x != line.getX1()) {
				p = new Point2D.Double(x, 0);
			}
		}
		// Not vertical line
		else {
			double k = getSlope(line);
			double intercept = getYIntercept(line);
			double y = k * x + intercept;
			p = new Point2D.Double(x, y);
		}
		return p;
	}

	/**
	 * Get X coordinate from Y on line. If the line is vertical and x is not on
	 * it, then return null; otherwise return 0
	 * 
	 * @param line
	 * @param x
	 * @return
	 */
	public static Point2D getPointOnLineFromY(Line2D line, double y) {
		Point2D p = null;
		// The line is a horizontal one
		if (epsilonEqual(line.getY1(), line.getY2())) {
			// If x is on the line then return
			if (y != line.getY1()) {
				p = new Point2D.Double(0, y);
			}
		}
		// Not horizontal line
		else {
			double k = getSlope(line);
			double intercept = getYIntercept(line);
			double x = (y - intercept) / k;
			p = new Point2D.Double(x, y);
		}
		return p;
	}
}
