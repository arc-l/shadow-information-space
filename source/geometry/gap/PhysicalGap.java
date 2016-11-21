package geometry.gap;


import java.awt.geom.Point2D;

public class PhysicalGap extends Gap{

	// Start edge, end edge (counterclockwise)
	protected int startEdge, endEdge;

	// Start point on start edge, end point on end edge
	Point2D startPoint = null, endPoint = null;

	public PhysicalGap(int se, int ee, Point2D sp, Point2D ep){
		super(-1, -1);
		startEdge = se;
		endEdge = ee;
		startPoint = sp;
		endPoint = ep;
	}

	public int getEndEdge() {
		return endEdge;
	}

	public Point2D getEndPoint() {
		return endPoint;
	}

	public int getStartEdge() {
		return startEdge;
	}

	public Point2D getStartPoint() {
		return startPoint;
	}

	public int getFullStartEdge(int numEdges) {
		if(startPoint == null) return startEdge;
		else return startEdge + 1 == numEdges ? 0 : startEdge + 1;
	}

	public int getFullEndEdge(int numEdges) {
		if(endPoint == null) return endEdge;
		else return endEdge == 0 ? numEdges - 1 : endEdge - 1; 
	}

	
}
