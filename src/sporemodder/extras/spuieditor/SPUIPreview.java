package sporemodder.extras.spuieditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIMain;

public class SPUIPreview extends JFrame implements ComponentListener, WindowListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1887426878325464643L;
	
	private static final String[] SCREEN_RESOLUTIONS = new String[] {
			"800x600", "1024x768", "1280x720", "1280x800", "1280x1024", "1366x768", "1440x900", "1600x900", "1680x1050", "1920x1080"
	};
	
	private SPUIViewer viewer;
	
	private JPopupMenu contextMenu;
	
	private boolean isGoingFullscreen;
	
	public SPUIPreview(SPUIMain spui) throws InvalidBlockException, IOException {
		this(new SPUIViewer(spui, null));
	}
	
	public SPUIPreview(SPUIViewer viewer) throws InvalidBlockException, IOException {
		
		setSize(new Dimension(800, 600));
		setLocationRelativeTo(null);
		
		this.viewer = new SPUIViewer(viewer);
		initViewer();
		
		addComponentListener(this);
		addWindowListener(this);
		this.viewer.addMouseListener(this);
		
		pack();
		
		contextMenu = new JPopupMenu();
		for (final String s : SCREEN_RESOLUTIONS) {
			
			JMenuItem menuItem = new JMenuItem(s);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String[] splits = s.split("x");
					isGoingFullscreen = true;
					if (SPUIPreview.this.isUndecorated()) {
						SPUIPreview.this.dispose();
						SPUIPreview.this.setUndecorated(false);
						SPUIPreview.this.setExtendedState(JFrame.NORMAL); 
						SPUIPreview.this.setSize(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]));
						SPUIPreview.this.setVisible(true);
					}
					else {
						Dimension dim = new Dimension(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]));
						//SPUIPreview.this.viewer.setSize(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]));
						SPUIPreview.this.viewer.setPreferredSize(dim);
						SPUIPreview.this.viewer.setWindowsSize(dim);
						SPUIPreview.this.viewer.revalidateWindows();
						SPUIPreview.this.pack();
						//SPUIPreview.this.setSize(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]));
					}
				}
			});
			contextMenu.add(menuItem);
		}
		
		JMenuItem menuItem = new JMenuItem("Fullscreen");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				isGoingFullscreen = true;
				SPUIPreview.this.dispose();
				SPUIPreview.this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
				SPUIPreview.this.setUndecorated(true);
				SPUIPreview.this.setVisible(true);
			}
			
		});
		contextMenu.add(menuItem);
	}
	
	private void initViewer() {
		viewer.setIsPreview(true);
		viewer.reset();
		setContentPane(viewer);
	}
	
	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		viewer.setWindowsSize(viewer.getSize());
		viewer.revalidateWindows();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		if (viewer != null && !isGoingFullscreen) {
			viewer.dispose();
			isGoingFullscreen = false;
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (SwingUtilities.isRightMouseButton(arg0)) {

			contextMenu.show(this, arg0.getX(), arg0.getY());
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
