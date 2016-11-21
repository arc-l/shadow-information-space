package geometry.pe.ui;

import geometry.Algorithm;
import geometry.Geometry;
import geometry.cut.Cut;
import geometry.drawable.LineSegment;
import geometry.drawable.Path;
import geometry.drawable.Point;
import geometry.drawable.Polygon;
import geometry.gap.Gap;
import geometry.gap.PhysicalGap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Vector;

public class ProjectPanel3 extends BasePanel {

	private static final long serialVersionUID = 5048182358859494135L;

	private Polygon p = Polygon.getPolygon(5, Geometry.DEFAULT_DC);

	private Line2D[] lines = null;

	private Line2D[] lines2 = new Line2D[0];

	private Line2D[] lines3 = new Line2D[0];

	private Line2D[] oldPoly = new Line2D[0];

	private Line2D[] newPoly = new Line2D[0];

	private Polygon np = new Polygon(Geometry.DEFAULT_DC);

	private Polygon op = new Polygon(Geometry.DEFAULT_DC);

	private Point2D[] points = new Point2D[0];

	private Point2D[] points2 = new Point2D[0];

	private Cut[] singleTangents = new Cut[0];

	private Path path = new Path(Geometry.DEFAULT_DC);

	private Point2D oldMousePoint = null;

	public ProjectPanel3() {
		super();

		p.setFillColor(Color.white);
		p.setLineColor(Color.gray);
		p.setTextColor(Color.black);
		p.setDrawDebugText(false);
		p.setDrawText(true);
		lines = p.getLineSegmentArray();
		lines2 = Algorithm.getInflection(p,
				Algorithm.INFLECTION_TYPE.NONGENERAL);
		lines3 = Algorithm.getInflection(p, Algorithm.INFLECTION_TYPE.GENERAL);
		singleTangents = Algorithm.getSingletangentCut(p);
		lines = Algorithm.getBitangent(p);

		// polygon 1
		// path.addPoint(new Point2D.Double(886, 281));
		// path.addPoint(new Point2D.Double(886, 48));
		// path.addPoint(new Point2D.Double(556, 36));

//		path.addPoint(new Point2D.Double(930, 886));
//		path.addPoint(new Point2D.Double(913, 700));
//		path.addPoint(new Point2D.Double(746, 583));
//		path.addPoint(new Point2D.Double(75, 565));
//		path.addPoint(new Point2D.Double(136, 215));
//		path.addPoint(new Point2D.Double(375, 69));
//		path.addPoint(new Point2D.Double(788, 27));
//		path.addPoint(new Point2D.Double(905, 95));

//		path.addPoint(new Point2D.Double(930, 886));
//		path.addPoint(new Point2D.Double(913, 700));
//		path.addPoint(new Point2D.Double(746, 583));
//		path.addPoint(new Point2D.Double(471, 580));
//		path.addPoint(new Point2D.Double(469, 780));
//		path.addPoint(new Point2D.Double(665, 860));
//		path.addPoint(new Point2D.Double(650, 965));
//		path.addPoint(new Point2D.Double(415, 786));
//		path.addPoint(new Point2D.Double(295, 568));
//		path.addPoint(new Point2D.Double(75, 565));
//		path.addPoint(new Point2D.Double(136, 215));
//		path.addPoint(new Point2D.Double(375, 69));
//		path.addPoint(new Point2D.Double(788, 27));
//		path.addPoint(new Point2D.Double(905, 95));

		// polygon 5
		path.addPoint(new Point2D.Double(946, 501));
		path.addPoint(new Point2D.Double(723, 591));
		path.addPoint(new Point2D.Double(670, 511));
		path.addPoint(new Point2D.Double(596, 643));
		path.addPoint(new Point2D.Double(606, 930));
		path.addPoint(new Point2D.Double(580, 919));
		path.addPoint(new Point2D.Double(588, 672));
		path.addPoint(new Point2D.Double(250, 611));
		path.addPoint(new Point2D.Double(266, 535));
		path.addPoint(new Point2D.Double(115, 433));
		path.addPoint(new Point2D.Double(93, 358));
		path.addPoint(new Point2D.Double(245, 286));	// The one
		path.addPoint(new Point2D.Double(98, 288));
		path.addPoint(new Point2D.Double(93, 123));

		path.setLineColor(Color.black);
		path.setTextColor(Color.magenta);
		path.setDrawDebugText(false);
		path.setDrawText(true);

		points = path.getIntersectPoints(lines);
		points2 = path.getIntersectPoints(lines3);

		Gap[][] gapss = Algorithm.getGaps(p, path);

		geometry.pe.Algorithm.processGapHistoryInformation(gapss,
				geometry.pe.Algorithm.SCENARIO.NEVER_SEE_EVADER, null);

		for (int i = 0; i < gapss.length; i++) {
			for (int j = 0; j < gapss[i].length; j++) {
				System.out.print(gapss[i][j].toString());
				System.out.print(" ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
	}

	public void draw(Graphics2D g) {
		p.paint(g);

		for (int i = 0; i < lines2.length; i++) {
			LineSegment seg = new LineSegment(lines2[i], Geometry.DEFAULT_DC);
			seg.setLineColor(Color.yellow);
			seg.paint(g);
		}

		for (int i = 0; i < lines.length; i++) {
			LineSegment seg = new LineSegment(lines[i], Geometry.DEFAULT_DC);
			seg.setLineColor(Color.green);
			seg.paint(g);
		}

		for (int i = 0; i < lines3.length; i++) {
			LineSegment seg = new LineSegment(lines3[i], Geometry.DEFAULT_DC);
			seg.setLineColor(Color.blue);
			seg.paint(g);
		}

		for (int i = 0; i < singleTangents.length; i++) {
			LineSegment seg = new LineSegment(singleTangents[i].getCut(), Geometry.DEFAULT_DC);
			seg.setLineColor(Color.yellow);
			seg.paint(g);
		}

		path.paint(g);

		for (int i = 0; i < points.length; i++) {
			Point p = new Point(points[i], Geometry.DEFAULT_DC);
			p.setFillColor(Color.magenta);
			p.paint(g);
		}

		for (int i = 0; i < points2.length; i++) {
			Point p = new Point(points2[i], Geometry.DEFAULT_DC);
			p.setFillColor(Color.magenta);
			p.paint(g);
		}

	}

	public void mouseMoved(Point2D mousePoint) {

		Graphics2D g = setupGraphics();
		if (oldMousePoint != null) {
			drawText(g, oldMousePoint);
		}
		drawText(g, mousePoint);
		oldMousePoint = mousePoint;

		// pp = new Point2D.Double(873, 535);
		if (p.pointInPolygon(mousePoint)) {
			newPoly = getPolyFromPhysicalGap(p.getLineSegmentArray(), Algorithm
					.getPhysicalGaps(p, mousePoint));
			// newPoly = Algorithm.getVisibilityPolygon(p, mousePoint);
			np = Algorithm.getVisibilityPolygon(p, mousePoint, Geometry.DEFAULT_DC);
			np.setFillColor(Color.black);
			np.setDrawDebugText(false);
			np.setLineColor(Color.blue);
			np.setDrawText(false);
		} else {
			newPoly = new Line2D[0];
			np = new Polygon(Geometry.DEFAULT_DC);
		}

		g.setXORMode(Color.blue);
		if (oldPoly.length > 0) {
			drawPoly(g, oldPoly, false);
			op.paint(g);
			oldPoly = new Line2D[0];
			op = new Polygon(Geometry.DEFAULT_DC);
		}
		if (newPoly.length > 0) {
			drawPoly(g, newPoly, false);
			np.paint(g);
			oldPoly = newPoly;
			op = np;
			newPoly = new Line2D[0];
			np = new Polygon(Geometry.DEFAULT_DC);
		}

		g.setPaintMode();
	}

	public void mouseExited() {

		Graphics2D g = setupGraphics();
		if (oldMousePoint != null) {
			drawText(g, oldMousePoint);
			oldMousePoint = null;
		}

		newPoly = new Line2D[0];

		g.setXORMode(Color.blue);
		if (oldPoly.length > 0) {
			drawPoly(g, oldPoly, false);
			op.paint(g);
			oldPoly = new Line2D[0];
			op = new Polygon(Geometry.DEFAULT_DC);
		}

		g.setPaintMode();
	}

	private Line2D[] getPolyFromPhysicalGap(Line2D[] lines, PhysicalGap[] gaps) {
		Vector<Line2D> lineVec = new Vector<Line2D>();
		for (int i = 0; i < gaps.length; i++) {
			Point2D p1 = null, p2 = null;
			if (gaps[i].getStartPoint() == null) {
				p1 = lines[gaps[i].getStartEdge()].getP1();
				p2 = gaps[i].getEndPoint();
			} else {
				p2 = lines[gaps[i].getEndEdge()].getP2();
				p1 = gaps[i].getStartPoint();
			}
			lineVec.add(new Line2D.Double(p1, p2));
		}
		return lineVec.toArray(new Line2D[0]);
	}

	public Graphics2D setupGraphics() {
		Graphics2D g = (Graphics2D) getGraphics();
		g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 20));
		g.translate(canvasMargin * 4 + 1, canvasMargin * 4 + 1);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.scale(canvasWidth / scalingFactor, canvasHeight / scalingFactor);
		g.setStroke(new BasicStroke(scalingFactor / canvasWidth));
		return g;
	}

	public void drawText(Graphics2D g, Point2D point) {
		g.setXORMode(Color.gray);
		if (point != null) {
			g.drawString("" + (int) point.getX() + "," + (int) point.getY(),
					(int) point.getX() + 5, (int) (scalingFactor - point
							.getY()) - 5);
		}
	}

	public void drawPoly(Graphics2D g, Line2D[] poly, boolean printPoints) {
		if (poly.length <= 0)
			return;

		// for (int i = 0; i < poly.length; i++) {
		// g.drawLine((int)poly[i].getX1(), (int)(SCALING_FACTOR -
		// poly[i].getY1()), (int)poly[i].getX2(), (int)(SCALING_FACTOR -
		// poly[i].getY2()));
		// }

		if (printPoints) {
			int[] xPoints = new int[poly.length];
			int[] yPoints = new int[poly.length];
			for (int i = 0; i < poly.length; i++) {
				xPoints[i] = (int) poly[i].getX1();
				yPoints[i] = (int) (scalingFactor - poly[i].getY1());
			}

			for (int i = 0; i < poly.length; i++) {
				System.out.print("" + xPoints[i] + ",");
			}
			System.out.println();
			for (int i = 0; i < poly.length; i++) {
				System.out.print("" + yPoints[i] + ",");
			}
			System.out.println();
		}

	}

	public static void main(String argv[]) {
		Frame mf = new Frame();
		mf.setTitle("Base Application");
		final ProjectPanel3 p = new ProjectPanel3();
		mf.add(p);
		
		mf.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			public void windowActivated(WindowEvent e) {
				p.windowActivated();
			}

			public void windowDeactivated(WindowEvent e) {
				p.windowDeactivated();
			}
		});
		
		mf.pack();
		mf.setVisible(true);
		p.listeningEvents();
	}
	

}
