package geometry.drawable;


import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Basic implementation of drawable
 * 
 * @author Jingjin Yu
 *
 */
public abstract class AbstractDrawable implements Drawable {

	private Color lineColor = Color.black;
	private Color textColor = Color.black;
	private Color fillColor = Color.lightGray;
	
	private boolean drawText = true;
	private boolean drawDebugText = false;
	
	protected DrawingContext dc = null;
	
	public AbstractDrawable(DrawingContext dc){
		this.dc = dc;
	}
	
	public AbstractDrawable(Color lineColor, Color textColor, Color fillColor, DrawingContext dc){
		this(dc);
		this.lineColor = lineColor;
		this.textColor = textColor;
		this.fillColor = fillColor;
	}
	
	public DrawingContext getDc() {
		return dc;
	}

	public void setDc(DrawingContext dc) {
		this.dc = dc;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Color getTextColor() {
		return textColor;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public boolean isDrawText() {
		return drawText;
	}

	public void setDrawText(boolean drawText) {
		this.drawText = drawText;
	}

	public boolean isDrawDebugText() {
		return drawDebugText;
	}

	public void setDrawDebugText(boolean drawDebugText) {
		this.drawDebugText = drawDebugText;
	}

	public void preDraw(Graphics2D g){
		g.setColor(lineColor);
		g.setBackground(fillColor);
	}
	
	public void draw(Graphics2D g){
		
	}
	
	public void drawText(Graphics2D g){
		
	}

	public void drawDebugText(Graphics2D g){
		
	}

	public void postDraw(Graphics2D g){
		
	}
	
	public void paint(Graphics2D g){
		preDraw(g);
		draw(g);
		if(isDrawText()) {
			drawText(g);
		}
		if(isDrawDebugText()){
			drawDebugText(g);
		}
		postDraw(g);
	}
}
