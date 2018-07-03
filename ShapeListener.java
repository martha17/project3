import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class ShapeListener extends MouseAdapter {
	private static JPanel p;
	private JPanel linePanel;
	private Point initial, screen;
	private Point beginning, end;
	private static ArrayList<ShapeIcon> selectedShapes;
	private static ArrayList<Line> selectedLines;
	
	public ShapeListener(JPanel panel) {
		p = panel;
		selectedShapes = new ArrayList<ShapeIcon>();
		selectedLines = new ArrayList<Line>();
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		if (event.getSource() instanceof JLabel) {
			ShapeIcon shape = ShapeIcon.findShape((JLabel) event.getSource());
			for (Line line : DrawPanelTwo.getLines()) {
				if (Line.containsPoint(line, event.getPoint())) {
					line.setSelected(true);
					selectedLines.add(line);
					System.out.println("line selected");
				}
			}
			if (shape != null) {
				shape.setSelected(true);
				selectedShapes.add(shape);
			}
			//System.out.println(event.getSource());
			// Set up for moving shape
			screen = new Point(event.getXOnScreen(), event.getYOnScreen());
			initial = SwingUtilities.convertPoint((Component) event.getSource(), event.getX(), event.getY(), p);
		} 
		if (event.getSource() instanceof JPanel) {
			for (ShapeIcon shape : DrawPanelTwo.getShapes()) {
				shape.setSelected(false);
			}
			selectedShapes.clear();
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent event) {
		if (event.getSource() instanceof JLabel) {
			ShapeIcon shape = ShapeIcon.findShape((JLabel) event.getSource());
			if (shape == null) return;
			JLabel label = shape.getShape();
			if (shape.isSelected()) {
				// Calculate new position
				int x = (int) initial.getX() + (event.getXOnScreen() - (int) screen.getX());
				int y = (int) initial.getY() + (event.getYOnScreen() - (int) screen.getY());
				// Move shape
				label.setLocation(x, y);
				// Set corresponding lines
				/*for (Line line : DrawPanelTwo.getLines()) {
					if (shape == line.getHead()) {
						line.setBeginning(new Point(x + (label.getWidth() / 2), y + (label.getHeight() / 2)));
					}
					if (shape == line.getTail()) {
						line.setEnd(new Point(x + (label.getWidth() / 2), y + (label.getHeight() / 2)));
					}
				}*/
			}
		}
	}
	
	public static void updatePanel(JPanel panel) {
		p = panel;
	}
	
	public static ArrayList<ShapeIcon> getSelectedShapes() {
		return selectedShapes;
	}
	
	public static ArrayList<Line> getSelectedLines() {
		return selectedLines;
	}
}