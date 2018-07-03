import javax.swing.*;
import java.util.*;

public class CodeObserver implements Observer {
	private String code;
	private ShapeIcon shape;
	private Line line;
	private Map<ShapeIcon, String[]> classes;
	
	public CodeObserver() {
		classes = new HashMap<ShapeIcon, String[]>();
	}
	
	public void update(Observable o, Object arg) {
		generateCode();
		CodeGeneratorPanel.updateCode(code);
	}
	
	private void generateCode() {
		code = "";
		int num = 0;
		ArrayList<ShapeIcon> shapes = DrawPanelTwo.getShapes();
		
		ShapeIcon[] shapeArr = new ShapeIcon[shapes.size()];
		String[] classTypes = calculateClassTypes(shapes.toArray(shapeArr));
		
		for (int i = 0; i < shapeArr.length; i++) {
			String[] relationships = calculateRelationship(shapeArr[i]);
			String relationship = relationships[0];
			String content = relationships[1];
			
			num = DrawPanelTwo.getShapes().indexOf(shapeArr[i]) + 1;
				
			code += String.format("public %s Icon%d %s{\n%s\n}\n", classTypes[i], num, relationship, content);
		}
	}
	
	private String[] calculateClassTypes(ShapeIcon[] shapes) {
		String[] types = new String[shapes.length];
		for (int i = 0; i < shapes.length; i++) {
			switch(shapes[i].getShapeType()) {
				case CIRCLE:
					types[i] = "interface"; break;
				case SQUARE:
					types[i] = "class"; break;
				case TRIANGLE:
				case STAR:
					break;
			}
		}
		
		return types;
	}
	
	private String[] calculateRelationship(ShapeIcon shape) {
		int shapeNum = DrawPanelTwo.getShapes().indexOf(shape) + 1;
		String relationship = "";
		String content = "";
		String aggregationContent = "";
		String associationContent = "";
		String inheritRelationship = "";
		String realizeRelationship = "";
		String associationSubcontent = "";
		ArrayList<String> realizationIcons = new ArrayList<String>();
		ArrayList<Line> lines = DrawPanelTwo.getLines();

		// Gather all relationships on a shape
		for (Line line : lines) {
			LineType type = line.getLineType();
			ShapeIcon[] lineShapes = {line.getHead(), line.getTail()};
			String[] shapeTypes = calculateClassTypes(lineShapes);
			int[] shapeNums = {DrawPanelTwo.getShapes().indexOf(lineShapes[0]) + 1, DrawPanelTwo.getShapes().indexOf(lineShapes[1]) + 1};
			
			if (!RelationshipUtilities.validConnection(line) || lineShapes[0] != shape) {
				continue;
			}
			
			if (type == LineType.AGGREGATION) {
				aggregationContent += String.format("\tprivate Icon%d icon%d;\n", shapeNums[1], shapeNums[1]);
			} else if (type == LineType.ASSOCIATION) {
				associationSubcontent += String.format("\t\tIcon%d icon%d;\n", shapeNums[1], shapeNums[1]);
			} else if (type == LineType.INHERITANCE) {
				if (shapeTypes[1].equals("class") && shapeTypes[0].equals("class")) {
					inheritRelationship = String.format("extends Icon%d", shapeNums[1]);
				} else if (shapeTypes[1].equals("interface") && shapeTypes[0].equals("class")) {
					realizationIcons.add(String.format("Icon%d", shapeNums[1]));
				}
			}
		}

		// Generate correct realization code
		for (String icon : realizationIcons) {
			if (icon.equals(realizationIcons.get(0)) && realizationIcons.size() == 1) {
				realizeRelationship += String.format("implements %s", icon);
			} else if (icon.equals(realizationIcons.get(0))) {
				realizeRelationship += String.format("implements %s, ", icon);
			} else if (icon.equals(realizationIcons.get(realizationIcons.size() - 1))) {
				realizeRelationship += icon;
			} else {
				realizeRelationship += String.format("%s, ", icon);
			}
		}
		
		// Set association content if there is an association relationship
		if (!associationSubcontent.equals("")) {
			associationContent = String.format("\t public Icon%d() {\n %s\n\t }", shapeNum, associationSubcontent);
		}
		
		// Format relationships for the class
		content = String.format("%s %s", aggregationContent, associationContent);
		relationship = String.format("%s %s", inheritRelationship, realizeRelationship); 
		
		return new String[] {relationship, content};
	}
	
}
