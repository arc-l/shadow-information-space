package geometry.drawable;


import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import geometry.FileHelper;


/**
 * Polygon class that contains all the points of a singly connected polygon. The
 * polygon is not checked for validity.
 * 
 * @author Jingjin Yu
 * 
 */

public class Polygon extends AbstractDrawable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Polygon.class);

	// Load the number of polygon files available
	private static final int numberOfPolygonDataFiles;

	private static final String dataFilePostfix;
	static {
		Properties prop = FileHelper
				.loadUrlProfile("polygons/polygons.properties");
		numberOfPolygonDataFiles = Integer.parseInt(prop.getProperty("number"));
		dataFilePostfix = prop.getProperty("postfix");
	}
	
	private GeneralPath generalPath = new GeneralPath(); 

	private List<Point2D> pointList = new ArrayList<Point2D>();

	private List<Line2D> lineList = null;

	public Polygon(DrawingContext dc) {
		super(dc);
	}

	/**
	 * Number of vertices/edges
	 * 
	 * @return
	 */
	public int getNumberOfVertices(){
		return pointList.size();
	}
	
	/**
	 * Add a single vertex at the end of polygon list
	 * 
	 * @param p
	 */
	public void addVertex(Point2D p) {
		pointList.add(p);
	}

	/**
	 * Add a single vertex at the end of polygon list
	 * 
	 * @param x
	 * @param y
	 */
	public void addVertex(double x, double y) {
		pointList.add(new Point2D.Double(x, y));
	}

	/**
	 * Finished creating polygon, create the general path
	 * 
	 */
	public void done() {
		Point2D[] points = getPointArray();

		generalPath.moveTo((float)points[0].getX(), (float)points[0].getY());
		for (int i = 0; i < points.length; i++) {
			generalPath.lineTo((float)points[i].getX(), (float)points[i].getY());
		}
	}

	/**
	 * Retrieves points as an array
	 * 
	 * @return
	 */
	public Point2D[] getPointArray() {
		return pointList.toArray(new Point2D[0]);
	}

	/**
	 * Get line segments as array
	 * 
	 * @return
	 */
	public Line2D[] getLineSegmentArray() {
		if (lineList == null) {
			lineList = new ArrayList<Line2D>();
			Point2D[] points = getPointArray();

			// All but last line
			for (int i = 0; i < points.length - 1; i++) {
				lineList.add(new Line2D.Double(points[i], points[i + 1]));
			}

			// Last line
			lineList
					.add(new Line2D.Double(points[points.length - 1], points[0]));
		}

		return lineList.toArray(new Line2D[0]);
	}

	/**
	 * Check whether a point is in a polygon
	 * 
	 * @param p
	 * @return
	 */
	public boolean pointInPolygon(Point2D p){
		return generalPath.contains(p);
	}
	
	/**
	 * Paint the polygon
	 * 
	 * @param g
	 */
	public void draw(Graphics2D g) {
		Line2D[] lines = getLineSegmentArray();

		// Thicker frame
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(((BasicStroke) g.getStroke())
				.getLineWidth() * 2));

		// Fill the polygon first
		int[] xPoints = new int[lines.length];
		int[] yPoints = new int[lines.length];
		for (int i = 0; i < lines.length; i++) {
			xPoints[i] = (int) lines[i].getX1() - dc.X_ORIGION;
			yPoints[i] = (int) (dc.Y_MAX - lines[i].getY1() + dc.Y_ORIGION);
		}
		g.setColor(getFillColor());
		g.fillPolygon(xPoints, yPoints, xPoints.length);

		// Draw all lines
		g.setColor(getLineColor());
		for (int i = 0; i < lines.length; i++) {
			g.drawLine((int) lines[i].getX1() - dc.X_ORIGION,
					(int) (dc.Y_MAX - lines[i].getY1() + dc.Y_ORIGION), (int) lines[i].getX2() - dc.X_ORIGION,
					(int) (dc.Y_MAX - lines[i].getY2() + dc.Y_ORIGION));
		}

		// Reset stroke
		g.setStroke(oldStroke);
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
			g.drawString("" + (i), ((int) points[i].getX()
					+ (points[i].getX() > dc.X_MAX - 20 ? -20 : 16) - dc.X_ORIGION),
					(int) (dc.Y_MAX - points[i].getY() + dc.Y_ORIGION) + 16);
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
			g.drawString("" + (i) + " [" + (int) points[i].getX() + ", "
					+ (int) points[i].getY() + "]",
					(int) points[i].getX() + (points[i].getX() > dc.X_MAX - 80 ? -80 : 6), (int) (dc.Y_MAX - points[i]
							.getY()) + (points[i].getX() > dc.X_MAX - 80 ? -4 : 16));
		}
	}

	/**
	 * Write out the object as point lists in a readable form
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void write(BufferedWriter out) throws IOException {
		for (Iterator iter = pointList.iterator(); iter.hasNext();) {
			Point2D element = (Point2D) iter.next();
			out.write("" + element.getX() + " " + element.getY() + "\n");
		}
		out.write("\n");
	}

	/**
	 * Read in objects as point lists
	 * 
	 * @param in
	 * @throws IOException
	 */
	public void read(BufferedReader in) throws IOException {
		String line = null;
		while ((line = in.readLine()) != null && !line.equals("\n")) {
			String[] xy = line.trim().split(" ");
			addVertex(new Point2D.Double(Double.parseDouble(xy[0]), Double
					.parseDouble(xy[1])));
		}
		done();
	}

	/**
	 * Generates random polygon, bounded withing given dimesion
	 * 
	 * @param numVertices
	 * @param dimension
	 * @return
	 */
	public static Polygon randomPolygon(int numVertices, Dimension dimension, DrawingContext dc) {
		Polygon p = new Polygon(dc);
		for (int i = 0; i < numVertices; i++) {
			p.addVertex(new Point2D.Double(
					dimension.getWidth() * Math.random(), dimension.getHeight()
							* Math.random()));
		}
		p.done();
		return p;
	}

	public static String getDataFilePostfix() {
		return dataFilePostfix;
	}

	public static int getNumberOfPolygonDataFiles() {
		return numberOfPolygonDataFiles;
	}

	/**
	 * Get a random polygon from existing data files
	 * 
	 * @return
	 */
	public static Polygon randomPolygonFromData(DrawingContext dc) {
		Polygon p = new Polygon(dc);
		String file = "polygons/"
				+ (int) (Math.random() * numberOfPolygonDataFiles + 1)
				+ dataFilePostfix;
		InputStream in = FileHelper.getStreamFromUrl(file);
		try {
			p.read(FileHelper.getBufferedReaderFromStream(in));
		} catch (IOException e) {
			logger.error("Error reading polygon data file into object, file: "
					+ file);
		}
		return p;
	}

	/**
	 * Get a polygon from existing data files
	 * 
	 * @return
	 */
	public static Polygon getPolygon(int number, DrawingContext dc) {
		Polygon p = new Polygon(dc);
		String file = "polygons/" + number + dataFilePostfix;
		InputStream in = FileHelper.getStreamFromUrl(file);
		try {
			p.read(FileHelper.getBufferedReaderFromStream(in));
		} catch (IOException e) {
			logger.error("Error reading polygon data file into object, file: "
					+ file);
		}
		return p;
	}

	public GeneralPath getGeneralPath() {
		return generalPath;
	}

}
