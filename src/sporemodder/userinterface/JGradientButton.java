package sporemodder.userinterface;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JButton;

public class JGradientButton extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5987321611378710248L;
	
	private Color color;
	
    public JGradientButton() {
        this("...", null);
    }
    
    public JGradientButton(String text) {
        this(text, null);
    }
    
    public JGradientButton(String text, Color color) {
        super(text);
        this.color = color;
        setContentAreaFilled(false);
        // setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        Color finalColor = Color.GRAY;
        if (color != null) {
        	finalColor = color;
        }
        
        g2.setPaint(new GradientPaint(
                new Point(0, 0), 
                finalColor, /* Color.WHITE, */ 
                new Point(0, getHeight()), 
                finalColor.darker()));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.black);
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        g2.dispose();

        super.paintComponent(g);
    }

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
