package geometry.pe.ui;

import geometry.Geometry;
import geometry.drawable.DrawingContext;
import geometry.drawable.Polygon;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import geometry.FileHelper;

public class BasePanel extends Panel implements ComponentListener,
		MouseMotionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	// protected float SCALING_FACTOR;
	//
	// protected int CANVAS_WIDTH;
	//
	// protected int CANVAS_HEIGHT;
	//
	// protected int CANVAS_MARGIN;
	//
	// protected boolean AUTO_RESIZE;
	//
	// protected int MIN_WIDTH;
	//
	// protected int MIN_HEIGHT;
	//
	// static{
	// AbstractDrawable.setY_MAX(SCALING_FACTOR);
	// }

	protected float scalingFactor = Geometry.SCALING_FACTOR;

	protected int minWidth = Geometry.MIN_WIDTH;

	protected int minHeight = Geometry.MIN_HEIGHT;

	protected int canvasWidth = Geometry.CANVAS_WIDTH;

	protected int canvasHeight = Geometry.CANVAS_HEIGHT;

	protected int canvasMargin = Geometry.CANVAS_MARGIN;

	protected boolean autoResize = Geometry.AUTO_RESIZE;

	protected boolean doPaint = true;

	protected DrawingContext dc = new DrawingContext(Geometry.DEFAULT_DC);

	// Focus
	protected boolean windowActive = true;

	public BasePanel() {
		super();
		// Initially set preferred size
		setPreferredSize(new Dimension(canvasWidth + canvasMargin * 8,
				canvasHeight + canvasMargin * 8));

		// Set up background
		this.setBackground(new java.awt.Color(0xE0E0FF));
		this.setScalingFactor(this.getScalingFactor());

	}

	public void listeningEvents() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		// Add mouse listener
		this.addMouseMotionListener(this);
		this.addMouseListener(this);

		// If auto resizable, then set the listener to do so
		if (autoResize) {
			this.addComponentListener(this);
		}
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		// Only draws if doPaint is true
		if (doPaint) {
			Graphics2D g2d = (Graphics2D) g;
			predraw(g2d);
			draw(g2d);
			postdraw(g2d);
		}
	}

	public void setOrigion(int x, int y) {
		dc.X_ORIGION = x;
		dc.Y_ORIGION = y;
	}

	public float getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(float scalingFactor) {
		this.scalingFactor = scalingFactor;
		dc.X_MAX = (int) this.scalingFactor;
		dc.Y_MAX = (int) this.scalingFactor;
	}

	public DrawingContext getDc() {
		return dc;
	}

	public void setDc(DrawingContext dc) {
		this.dc = dc;
	}

	public String updateView(Rectangle rect) {
		// convert rect to drawing context
		int max = Math.max(Math.max(dc.X_MAX, dc.Y_MAX), Math.max(rect.width, rect.height));

		// Test whether the rect is fully in view
		if (rect.x < dc.X_ORIGION
				|| rect.x + rect.width > dc.X_ORIGION + dc.X_MAX
				|| rect.y < dc.Y_ORIGION
				|| rect.y + rect.height > dc.Y_ORIGION + dc.Y_MAX) {
			// No. Need to adjust scaling factor and origin
			setScalingFactor(max * 3 / 2);
			dc.X_ORIGION = rect.x - dc.X_MAX / 4;
			dc.Y_ORIGION = rect.y - dc.X_MAX / 4;
			return ("Adjusted view size, origin: " + (rect.x - max / 4)
					+ ", " + +(rect.y - max / 4) + ", size: " + max * 3 / 2 + "\n");
		}
		// Test whether the rect is fully in view
//		else if (4 * Math.max(rect.width, rect.height) < Math.max(dc.X_MAX, dc.Y_MAX)) {
//			// No. Need to adjust scaling factor and origin
//			setScalingFactor(Math.max(rect.width, rect.height) * 3 / 2);
//			dc.X_ORIGION = rect.x - dc.X_MAX / 2;
//			dc.Y_ORIGION = rect.y - dc.X_MAX / 2;
//			System.out.println("Adjusting view, origin: " + (rect.x - max / 2)
//					+ ", " + +(rect.x - max / 2) + ", size: " + max * 2);
//		}

		// Test whether items become too small (not occupying half of the view
		// point)
		return null;
	}

	/**
	 * Setup the drawing area for drawing
	 * 
	 * @param g2d
	 */
	public void predraw(Graphics2D g2d) {
		// Set up canvas for drawing. First tranlate a little
		g2d.translate(canvasMargin + 1, canvasMargin + 1);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// paints the background with a color gradient
		// g2d.setPaint(new GradientPaint(0.0f, 0.0f,
		// new java.awt.Color(0xD0FFFF), (float) canvasWidth + canvasMargin,
		// (float) canvasHeight + canvasMargin, new java.awt.Color(0xD0FFFF)));

		// Draw and fill the background rectangle
		g2d.setClip(null);
		g2d.setPaint(new java.awt.Color(0x7F7FD0));
		g2d.drawRect(-2, -2, canvasWidth + canvasMargin * 6 + 2, canvasHeight
				+ canvasMargin * 6 + 2);
		g2d.setPaint(new java.awt.Color(0xFFFFD0));
		g2d.fillRect(0, 0, canvasWidth + canvasMargin * 6, canvasHeight
				+ canvasMargin * 6);
		g2d.setClip(0, 0, canvasWidth + canvasMargin * 6, canvasHeight
				+ canvasMargin * 6);

		// Setup scaling and basic stroke width for scale independent drawing
		g2d.translate(canvasMargin * 3, canvasMargin * 3);
		g2d.scale(canvasWidth / scalingFactor, canvasHeight / scalingFactor);
		g2d.setStroke(new BasicStroke(scalingFactor / canvasWidth));
	}

	/**
	 * Takes care of things happen after drawing. Should call base class methods
	 * if overwritten
	 * 
	 * @param g2d
	 */
	public void postdraw(Graphics2D g2d) {

		// Reset scale for other types of rendering
		g2d.scale(scalingFactor / canvasWidth, scalingFactor / canvasHeight);

		// draws rotated text
		// g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD,
		// 16));
		// g2d.setColor(java.awt.Color.blue);
		//
		// g2d.translate(controlSize.width / 2, controlSize.height / 2);
		// int nbOfSlices = 18;
		// for (int i = 0; i < nbOfSlices; i++) {
		// g2d.drawString(" Angle = " + (i * 360 / nbOfSlices)
		// + "\u00B0", 30, 0);
		// g2d.rotate(-2 * Math.PI / nbOfSlices);
		// }
		// g2d.translate(-controlSize.width / 2, -controlSize.height / 2);
		// g2d.drawOval(0, 0, controlSize.width, controlSize.height);
	}

	/**
	 * Derived class should overwrite thie method to perform actual drawing of
	 * objects.
	 * 
	 * @param g2d
	 */
	public void draw(Graphics2D g2d) {
	}

	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	Dimension lastDim = null;

	public synchronized void componentResized(ComponentEvent arg0) {
		Component c = arg0.getComponent();
		if (c == this && (lastDim == null || !lastDim.equals(c.getSize()))) {
			lastDim = new Dimension(c.getSize());
			// Don't do any drawing if the component is smaller than 40 x 40
			canvasWidth = c.getSize().width - canvasMargin * 8;
			canvasHeight = c.getSize().height - canvasMargin * 8;
			int min = Math.min(canvasWidth, canvasHeight) + canvasMargin * 8;
			c.setSize(min, min);
			canvasWidth = c.getSize().width - canvasMargin * 8;
			canvasHeight = c.getSize().height - canvasMargin * 8;
			if (canvasWidth < minWidth || canvasHeight < minHeight) {
				doPaint = false;
			} else {
				doPaint = true;
			}
		}
	}

	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseDragged(MouseEvent arg0) {
		mouseMoved(arg0);
	}

	public synchronized void mouseMoved(MouseEvent e) {
		int x = e.getX() - canvasMargin * 4;
		int y = e.getY() - canvasMargin * 4;
		final Point2D mp = new Point2D.Double(x * scalingFactor / canvasWidth,
				scalingFactor - y * scalingFactor / canvasHeight);
		final BasePanel panel = this;
		if (pointInDrawingArea(x, y)) {
			if (!windowActive)
				return;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					panel.mouseMoved(mp);
				}
			});
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					panel.mouseExited();
				}
			});
		}
	}

	/**
	 * Take action on mouse movements
	 * 
	 */
	public void mouseMoved(Point2D mousePoint) {
	}

	/**
	 * Test whether a point is inside active drawing area
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean pointInDrawingArea(int x, int y) {
		return x >= 0 && x <= canvasWidth && y >= 0 && y <= canvasHeight;
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public synchronized void mouseEntered(MouseEvent e) {
	}

	/**
	 * Take action on mouse entering
	 * 
	 */
	public void mouseEntered(Point2D mousePoint) {
	}

	public void mouseExited(MouseEvent e) {
		final BasePanel panel = this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				panel.mouseExited();
			}
		});
	}

	/**
	 * Take action on mouse exiting
	 * 
	 */
	public void mouseExited() {
	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowActivated() {
		windowActive = true;
		windowActive();
	}

	public void windowActive() {
		// mouseExited();
	}

	public void windowDeactivated() {
		windowActive = false;
		windowInactive();
	}

	public void windowInactive() {
	}

	public static void main(String argv[]) {
		FileHelper.regularInit();
		Frame mf = new Frame();
		mf.add(new BasePanel() {
			private static final long serialVersionUID = 1L;

			private Polygon poly = Polygon
					.randomPolygonFromData(Geometry.DEFAULT_DC);

			public void draw(Graphics2D g2d) {

				g2d.setColor(new java.awt.Color(0x10CF10));
				poly.draw(g2d);

			}
		});

		mf.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		mf.pack();
		mf.setVisible(true);
	}

}
