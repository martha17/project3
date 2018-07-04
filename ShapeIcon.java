import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.lang.*;

enum ShapeType {
	CIRCLE, 
	SQUARE, 
	TRIANGLE, 
	STAR;
}

public class ShapeIcon extends Observable implements Serializable {
	private String name;
	private boolean selected = false;
	private ShapeType shapeType;
	private String path;
	private Point dropPoint;
	private File file;
	private Color fillColor;
	private Color lineColor;
	private transient BufferedImage image;
	private ImageIcon icon;
	private ArrayList<Point> anchors = new ArrayList<Point>();
	private static Map<ShapeIcon, String> iconList = new HashMap<ShapeIcon, String>();
	private static Map<JLabel, ShapeIcon> shapeList = new HashMap<JLabel, ShapeIcon>();
		private JLabel shape = new JLabel() {
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2d = (Graphics2D) g.create();
			setIcon(new ImageIcon(image));
			
			if (selected) {
				Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8}, 0);
				g2d.setStroke(dashed);
				g2d.setColor(new Color(0, 0, 255, 100));
				g2d.drawRect(0, 0, (int) getPreferredSize().getWidth() - 1, (int) getPreferredSize().getHeight() - 1);
			}
			
			g2d.dispose();
		}
	};
	
	// Used by DrawPanelOne
	public ShapeIcon(String name) {
		this.name = name;
		fillColor = Color.WHITE;
		lineColor = Color.BLACK;
		setIconPath(name);
		this.icon = new ImageIcon(path + name);
		shape.setIcon(icon);
		iconToImage();
	}
	
	// Used by DrawPanelTwo
	public ShapeIcon(Icon icon, Point location, String type) {
		this.icon = (ImageIcon) icon;
		fillColor = Color.WHITE;
		lineColor = Color.BLACK;
		
		switch (type) {
			case "CIRCLE":
				this.shapeType = ShapeType.CIRCLE; break;
			case "SQUARE":
				this.shapeType = ShapeType.SQUARE; break;
			case "TRIANGLE":
				this.shapeType = ShapeType.TRIANGLE; break;
			case "STAR":
				this.shapeType = ShapeType.STAR; break;
		}
		
		shape.setLocation((int) location.getX(), (int) location.getY());
		shape.setIcon(icon);
		iconToImage();
	}
	
	public void draw(JPanel p) {
		if (SwingUtilities.getAncestorOfClass(DrawPanelTwo.class, p) != null) {
			shapeList.put(shape, this);
			if ((DrawPanelTwo.getShapes().indexOf(this)) == -1) {
				DrawPanelTwo.addShape(this);
			}
			setChanged();
		}
		calculateAnchors();
		p.add(shape);
		p.revalidate();
		p.repaint();
		notifyObservers("add");
	}
	
	public void remove(JPanel p) {
		Component[] components = p.getComponents();
		for (Component c : components) {
			if (SwingUtilities.getAncestorOfClass(DrawPanelTwo.class, c) != null) {
				shapeList.remove(shape);
				/*if ((DrawPanelTwo.getShapes().indexOf(this)) != -1) {
					DrawPanelTwo.removeShape(this);
				}*/
				setChanged();
			}
		}
		p.remove(shape);
		p.revalidate();
		p.repaint();
		notifyObservers("remove");
	}
	
	public static ShapeIcon findShape(JLabel label) {
		for (JLabel shape : shapeList.keySet()) {
			if (label == shape) {
				return shapeList.get(shape);
			}
		}
		return null;
	}
	
	// Deep copy for BufferedImage
	private BufferedImage copyImage(BufferedImage copy) {
		ColorModel cm = copy.getColorModel();
		boolean alphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster r = copy.copyData(null);
		return new BufferedImage(cm, r, alphaPremultiplied, null);
	}
	
	// Do not place in paintComponent or you will get a spinner
	public void rotate(int degrees) {
		//Create copy of image
		BufferedImage temp = copyImage(image);
		Graphics2D g2d = (Graphics2D) temp.createGraphics();
		// Perform rotation on copy of image
		AffineTransform transform = AffineTransform.getRotateInstance(Math.toRadians(degrees), image.getWidth() / 2, image.getHeight() / 2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		// Draw the transformation and store it in the original image
		g2d.drawImage(op.filter(temp, image), 0, 0, null);
		g2d.dispose();
	}
	
	public void flip(String orientation) {
		//Create copy of image
		BufferedImage temp = copyImage(image);
		Graphics2D g2d = (Graphics2D) temp.createGraphics();
		
		// Perform flip on copy of image
		AffineTransform transform = new AffineTransform();
		if (orientation.equals("horizontal")) {
			transform = AffineTransform.getScaleInstance(1, -1);
			transform.translate(0, -image.getHeight());
		} else if (orientation.equals("vertical")) {
			transform = AffineTransform.getScaleInstance(-1, 1);
			transform.translate(-image.getWidth(), 0);
		}
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		
		// Draw the transformation and store it in the original image
		g2d.drawImage(op.filter(temp, image), 0, 0, null);
		g2d.dispose();
	}
	
	public void color(Color fillColor, Color lineColor) {
		int color;
		int fillColorNoAlpha = this.fillColor.getRGB() & 0xffffff;
		int lineColorNoAlpha = this.lineColor.getRGB() & 0xffffff;
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				color = image.getRGB(i, j);
				if (((color & 0xffffff) == fillColorNoAlpha) && ((color & 0xff000000) != 0x0)) {
					image.setRGB(i, j, fillColor.getRGB());
				}
				if (((color & 0xffffff) == lineColorNoAlpha) && ((color & 0xff000000) != 0x0)) {
					image.setRGB(i, j, lineColor.getRGB());
				}
			}
		}
		
		this.fillColor = fillColor;
		this.lineColor = lineColor;
	}
	
	private void iconToImage() {
		image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.createGraphics();
		icon.paintIcon(shape, g, 0, 0);
		g.dispose();
	}
	
	private void setIconPath(String name) {
		path = "src/";
		file = new File(path + name);
		
		if (!(file.exists()))
			path = "";
	}
	
        public static Map generateIcons(String name) {
            
		/*final File folder = new File(name);
		
		for (final File file : folder.listFiles()) {
			String fileName = file.getName();
			String shapeName = fileName.substring(0, fileName.lastIndexOf('.'));
			String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
			if (file.isFile() && fileType.equals("png")) {
				ShapeIcon icon = new ShapeIcon(folder.getName() + "/" + fileName);
				iconList.put(icon, shapeName);
			}
		}
		
		return iconList;*/
                
                String fileName = "star.png";
                String shapeName = "STAR";
		String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
		if (fileType.equals("png")) {
			ShapeIcon icon = new ShapeIcon("src/" + fileName);
			iconList.put(icon, shapeName);
		}
                fileName = "circle.png";
                shapeName = "CIRCLE";
		fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
		if (fileType.equals("png")) {
			ShapeIcon icon = new ShapeIcon("src/" + fileName);
			iconList.put(icon,shapeName);
		}
                fileName = "triangle.png";
                shapeName = "TRIANGLE";
		fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
		if (fileType.equals("png")) {
			ShapeIcon icon = new ShapeIcon("src/" + fileName);
			iconList.put(icon,shapeName);
		}
                fileName = "square.png";
                shapeName = "SQUARE";
		fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
		if (fileType.equals("png")) {
			ShapeIcon icon = new ShapeIcon("src/" + fileName);
			iconList.put(icon,shapeName);
		}
		return iconList;
	}
	
	private void calculateAnchors() {
		if (shapeType == ShapeType.CIRCLE) {
			anchors.add(new Point(shape.getWidth() / 2, shape.getHeight()));
			anchors.add(new Point(shape.getWidth() / 2, 0));
			anchors.add(new Point(shape.getWidth(), shape.getHeight() / 2));
			anchors.add(new Point(0, shape.getHeight() / 2));
		} else if (shapeType == ShapeType.SQUARE){
			anchors.add(new Point(shape.getWidth() / 2, shape.getHeight()));
			anchors.add(new Point(shape.getWidth() / 2, 0));
			anchors.add(new Point(shape.getWidth(), shape.getHeight() / 2));
			anchors.add(new Point(0, shape.getHeight() / 2));
		}
	}
	
	public Map<ShapeIcon, Point> findBestAnchor(ShapeIcon otherShape, JPanel source) {
		ArrayList<Point> cAnchors = new ArrayList<Point>();
		ArrayList<Point> cOtherAnchors = new ArrayList<Point>();
		Map<ShapeIcon, Point> points = new HashMap<ShapeIcon, Point>();
		for (Point point : anchors) {
			cAnchors.add(SwingUtilities.convertPoint(shape, point, source));
		}
		for (Point point : otherShape.getAnchors()) {
			cOtherAnchors.add(SwingUtilities.convertPoint(otherShape.getShape(), point, source));
		}
		if (cAnchors.size() == 0 || cOtherAnchors.size() == 0) {
			return null;
		}
		Point bestParentPoint = cAnchors.get(0);
		Point bestChildPoint = cOtherAnchors.get(0);
		double distance = Math.hypot(bestChildPoint.getX() - bestParentPoint.getX(), bestChildPoint.getY() - bestParentPoint.getY());
		for (Point anchor : cAnchors) {
			for (Point otherAnchor : cOtherAnchors) {
				double newDistance = Math.hypot(otherAnchor.getX() - anchor.getX(), otherAnchor.getY() - anchor.getY());
				if (newDistance < distance) {
					distance = newDistance;
					bestParentPoint = anchor;
					bestChildPoint = otherAnchor;
				}
			}
		}
		
		points.put(this, bestParentPoint);
		points.put(otherShape, bestChildPoint);
		return points;
	}
	
	public ArrayList<Point> getAnchors() {
		return anchors;
	}
	
	public static Map<JLabel, ShapeIcon> getShapeList() {
		return shapeList;
	}
	
	public static Map<ShapeIcon, String> getIconList() {
		return iconList;
	}
	
	public String getName() {
		return this.name;
	}
	
	public JLabel getShape() {
		return shape;
	}
	
	public ShapeType getShapeType() {
		return shapeType;
	}
	
	public void setShapeType(ShapeType s) {
		shapeType = s;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
		shape.revalidate();
		shape.repaint();
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public Color getFillColor() {
		return fillColor;
	}
	
	public Color getLineColor() {
		return lineColor;
	}
}
