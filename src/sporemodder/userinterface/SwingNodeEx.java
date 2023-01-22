package sporemodder.userinterface;

import java.awt.Dimension;

import javax.swing.JComponent;

import javafx.embed.swing.SwingNode;

public class SwingNodeEx extends SwingNode {
	public SwingNodeEx() {
		super();
	}
	public SwingNodeEx(JComponent content) {
		this();
		this.setContent(content);
	}
	
	@Override
	public void setContent(JComponent content) {
		content.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		super.setContent(content);
	}
	
	@Override
	public boolean isResizable() {
		return true;
	}
	
	@Override
	public double maxWidth(double height) {
		return Double.MAX_VALUE;
	}
	
	@Override
	public double maxHeight(double width) {
		return Double.MAX_VALUE;
	}
}
