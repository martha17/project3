import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;
import java.lang.*;

enum LineType {
	AGGREGATION,
	INHERITANCE,
	ASSOCIATION;
}

public class Line extends Observable {
	private int x1, x2, y1, y2;
	private final double PHI = Math.toRadians(40);
	private final int BARB = 20;
	private double slope;
	private boolean selected;
	private ShapeIcon head, tail;
	private LineType lineType;
	private static JLabel lineLabel;
	private static Map<JLabel, Line> lineList;
	private Shape lineGraphic;
	private static BufferedImage img;
	private boolean removed = false;
	
	public Line() {
		lineLabel = DrawPanelTwo.getLineLabel();
	}
	
	public Line(ShapeIcon head, ShapeIcon tail, String type) {
		this();
		this.head = head;
		this.tail = tail;
		setLineType(type);
	}
	
	public Line(int x1, int y1, int x2, int y2, ShapeIcon head, ShapeIcon tail, String type) {
		this(head, tail, type);
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		calculateSlope();
	}
	
	public Line(Point beginning, Point end, ShapeIcon head, ShapeIcon tail, String type) {
		this(head, tail, type);
		this.x1 = (int) beginning.getX();
		this.x2 = (int) end.getX();
		this.y1 = (int) beginning.getY();
		this.y2 = (int) end.getY();
		calculateSlope();
	}
	
	public Line(ShapeIcon head, ShapeIcon tail, JPanel source, String type) {
		this(head, tail, type);
		Map<ShapeIcon, Point> points = head.findBestAnchor(tail, source);
		Point headPoint = SwingUtilities.convertPoint(head.getShape(), head.getShape().getWidth() / 2, head.getShape().getHeight() / 2, source);
		Point tailPoint = SwingUtilities.convertPoint(tail.getShape(), tail.getShape().getWidth() / 2, tail.getShape().getHeight() / 2, source);
		if (points != null) {
			headPoint = points.get(head);
			tailPoint = points.get(tail);
		}
		this.x1 = (int) headPoint.getX();
		this.x2 = (int) tailPoint.getX();
		this.y1 = (int) headPoint.getY();
		this.y2 = (int) tailPoint.getY();
		calculateSlope();
	}
	
	public static void updateLines() {		
		ArrayList<Line> lines = DrawPanelTwo.getLines();
		ListIterator<Line> i = lines.listIterator();
		boolean recentlyRemoved = false;
		while (i.hasNext()) {
			recentlyRemoved = false;
			Line line = i.next();
			//System.out.println(DrawPanelTwo.getLines().contains(line));
			if (line.toBeRemoved()) {
				i.remove();
				recentlyRemoved = true;
			}
			//System.out.println(DrawPanelTwo.getLines().contains(line));
			if (line.isSelected()) {
				line.draw(Color.BLUE);
			} else {
				line.draw(Color.BLACK);
			}
		}
		
		if (lines.size() != 0 && lineLabel != null) {
			// Attach image to label
			lineLabel.setIcon(new ImageIcon(img));
			
			lineLabel.revalidate();
			lineLabel.repaint();	
		}
		
		if (recentlyRemoved) {
			img = null;
			lineLabel.setIcon(null);
			
			lineLabel.revalidate();
			lineLabel.repaint();
		}
	}
	
	public void draw(Color color) {
		// Get Graphics object and set its settings
		if (img == null) {
			img = new BufferedImage(850, 375, BufferedImage.TYPE_INT_ARGB);
		}
        Graphics2D g2 = (Graphics2D) img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Get Points to draw line and set lines
        Point sw = new Point(x1, y1);
        Point ne = new Point(x2, y2);
        g2.setColor(color);
		
		// Check if line needs to be dashed
		Stroke defaultStroke = g2.getStroke();
		
		if (lineType == LineType.INHERITANCE && head.getShapeType() == ShapeType.SQUARE && tail.getShapeType() == ShapeType.CIRCLE) {
			Stroke dashedStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8}, 0);
			g2.setStroke(dashedStroke);
		}
		
		// Draw line
		lineGraphic = new Line2D.Double(sw, ne);
		g2.draw(lineGraphic);
		g2.setStroke(defaultStroke);
        
        switch (lineType) {
			case ASSOCIATION:
				drawArrow(g2, ne, sw, color); break;
			case INHERITANCE:
				drawArrowHead(g2, ne, sw, color); break;
			case AGGREGATION:
				drawDiamond(g2, ne, sw, color); break;
		}
		
		g2.drawImage(img, 0, 0, null);
		
		// Notify Observers
		setChanged();
		notifyObservers("add");
    }
	
	public void remove() {
		removed = true;
		
		// Notify Observers
		setChanged();
		notifyObservers("remove");
	}
	
	//Association
    private void drawArrow(Graphics2D g2, Point tip, Point tail, Color color){
		g2.setPaint(Color.BLACK);
        double dy = tip.y - tail.y;
        double dx = tip.x - tail.x;
        double theta = Math.atan2(dy, dx);
        double x, y, rho = theta + PHI;
        for(int j = 0; j < 2; j++){
            x = tip.x - BARB * Math.cos(rho);
            y = tip.y - BARB * Math.sin(rho);
            g2.draw(new Line2D.Double(tip.x, tip.y, x, y));
            rho = theta - PHI;
        }
    }
    //Inheritance
    private void drawArrowHead(Graphics2D g2, Point tip, Point tail, Color color){
        g2.setPaint(color);
        double dy = tip.y - tail.y;
        double dx = tip.x - tail.x;
        double theta = Math.atan2(dy, dx);
        double x, y, rho = theta + PHI;
        double p1x = 0, p1y = 0, p2x = 0, p2y = 0;
        for(int j = 0; j < 2; j++){  
            if(j==0){
                x = tip.x - BARB * Math.cos(rho);
                y = tip.y - BARB * Math.sin(rho);
                p1x=x;
                p1y=y;
                rho = theta - PHI;
            }
            
            if(j==1){
                x = tip.x - BARB * Math.cos(rho);
                y = tip.y - BARB * Math.sin(rho);
                p2x=x;
                p2y=y;
                rho = theta - PHI;
            }
        }
        g2.setColor(Color.WHITE);
        g2.fillPolygon(new int[] {(int)p1x, (int)p2x, (int)tip.x}, new int[] {(int)p1y, (int)p2y, (int)tip.y},3);
        g2.setColor(color);
        g2.drawPolygon(new int[] {(int)p1x, (int)p2x, (int)tip.x}, new int[] {(int)p1y, (int)p2y, (int)tip.y},3);
    }
    // Aggregation
    private void drawDiamond(Graphics2D g2, Point tip, Point tail, Color color){
        g2.setPaint(color);
        double dy = tip.y - tail.y;
        double dx = tip.x - tail.x;
        double theta = Math.atan2(dy, dx);
        double x, y, rho = theta + PHI;
        double p1x = 0, p1y = 0, p2x = 0, p2y = 0;
        double p3x =0, p3y = 0;
        for(int j = 0; j < 2; j++){  
            if(j==0){
                x = tip.x - BARB * Math.cos(rho);
                y = tip.y - BARB * Math.sin(rho);
                p1x=x;
                p1y=y;
                rho = theta - PHI;
            }
            
            if(j==1){
                x = tip.x - BARB * Math.cos(rho);
                y = tip.y - BARB * Math.sin(rho);
                p2x=x;
                p2y=y;
                rho = theta - PHI;
            }
        }

        double d=Math.sqrt(Math.pow(((double) tail.x-tip.x),2.0)+Math.pow((double) tail.y-tip.y,2.0));
        double t=30.2/d;
        p3x=((1-t)*tip.x+t*tail.x);
        p3y=((1-t)*tip.y+t*tail.y);
        
        g2.setColor(Color.WHITE);
        g2.fillPolygon(new int[] {(int)p1x, (int)tip.x, (int)p2x, (int)p3x}, new int[] {(int)p1y, (int)tip.y, (int)p2y, (int)p3y},4);
        g2.setColor(color);
        g2.drawPolygon(new int[] {(int)p1x, (int)tip.x, (int)p2x, (int)p3x}, new int[] {(int)p1y, (int)tip.y, (int)p2y, (int)p3y},4);
    }
	
	public static boolean containsPoint(Line l, Point p){
        double d=Math.hypot(l.getX2()-l.getX1(), l.getY2()-l.getY1());
        double dt=.000000001;
        while(dt<=d){
            double t=dt/d;
            double x=((1-t)*l.getX1()+t*l.getX2());
            double y=((1-t)*l.getY1()+t*l.getY2());
            if(p.getX()<x+1&&p.getX()>x-1){
                if(p.getY()<y+1&&p.getY()>y-1){
                    return true;
                }
            }
            dt=dt+.1;
        }
        return false;
    }
	
	public static boolean validConnection(Line line) {
		LineType type = line.getLineType();
		ShapeType[] shapeTypes = {line.getHead().getShapeType(), line.getTail().getShapeType()};
		for (ShapeType shapeType : shapeTypes) {
			if (shapeType == ShapeType.STAR || shapeType == ShapeType.TRIANGLE) {
				return false;
			}
		}
		switch (type) {
			case AGGREGATION:
				if (shapeTypes[1] != ShapeType.CIRCLE && shapeTypes[0] != ShapeType.CIRCLE) {
					return true;
				}
				break;
			case ASSOCIATION:
				if (shapeTypes[1] != ShapeType.CIRCLE && shapeTypes[0] != ShapeType.CIRCLE) {
					return true;
				}
				break;
			case INHERITANCE:
				if (shapeTypes[1] == ShapeType.SQUARE && shapeTypes[0] == ShapeType.SQUARE) {
					return true;
				} else if (shapeTypes[1] == ShapeType.CIRCLE && shapeTypes[0] == ShapeType.SQUARE) {
					return true;
				}
				break;
		}
		return false;
	}
	
	public void calculateSlope() {
		if ((x2 - x1) == 0) {
			slope = 0;
		} else {
			slope = (y2 - y1) / (x2 - x1);
		}
	}
	
	public int getX1() {
		return x1;
	}
	
	public int getX2() {
		return x2;
	}
	
	public int getY1() {
		return y1;
	}
	
	public int getY2() {
		return y2;
	}
	
	public ShapeIcon getHead() {
		return head;
	}
	
	public ShapeIcon getTail() {
		return tail;
	}
	
	public void setBeginning(Point point) {
		x1 = (int) point.getX();
		y1 = (int) point.getY();
	}
	
	public void setEnd(Point point) {
		x2 = (int) point.getX();
		y2 = (int) point.getY();
	}
	
	public double getSlope() {
		return slope;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public LineType getLineType() {
		return lineType;
	}
	
	public void setLineType(String type) {
		switch (type) {
			case "association":
				lineType = LineType.ASSOCIATION; break;
			case "aggregation":
				lineType = LineType.AGGREGATION; break;
			case "inheritance":
				lineType = LineType.INHERITANCE; break;
		}	
	}
	
	public boolean toBeRemoved() {
		return removed;
	}
	
}