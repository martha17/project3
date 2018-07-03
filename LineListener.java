import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class LineListener extends MouseAdapter {
	private static JPanel p;
	private static ArrayList<Line> selectedLines;
	
	public LineListener(JPanel panel) {
		p = panel;
		selectedLines = new ArrayList<Line>();
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		if (event.getSource() instanceof JLabel) {
			for (Line line : DrawPanelTwo.getLines()) {
				if (Line.containsPoint(line, event.getPoint())) {
					line.setSelected(true);
				}
			}
		}
		
		if (event.getSource() instanceof JPanel) {
			for (Line line : DrawPanelTwo.getLines()) {
				line.setSelected(false);
			}
			selectedLines.clear();
		}
	}
}