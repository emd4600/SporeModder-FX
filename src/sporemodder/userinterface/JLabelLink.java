package sporemodder.userinterface;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JLabel;

public class JLabelLink extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8912901563855529060L;
	
	private static final String A_HREF = "<FONT color=\"#000099\"><U>";
	private static final String HREF_END = "</U></FONT>";
	private static final String HTML = "<html>";
	private static final String HTML_END = "</html>";
	
	private boolean isActionActive;
	private Action linkAction;
	private MouseListener mouseListener;
	private Action syncToolTipAction = null;
	
	public JLabelLink(String text, Action action, Action syncToolTipAction) {
		this(text, action);
		if (syncToolTipAction != null) {
			this.syncToolTipAction = syncToolTipAction;
			this.syncToolTipAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
		}
	}
	public JLabelLink(String text, Action action) {
	    super(text);
	    linkAction = action;
	    mouseListener = new LinkMouseListener();
	    addMouseListener(mouseListener);
	    if (action != null) {
	        makeLinkable();
	    }
	}
	
	private void makeLinkable() {
		super.setText(htmlIfy(linkIfy(getText())));
	    setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
	}
	
	private void makeLinkable(String text) {
		super.setText(htmlIfy(linkIfy(text)));
	    setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
	}
	
	private void removeLinkable(String text) {
		if (text.startsWith(HTML + A_HREF)) {
			text = text.substring((HTML + A_HREF).length(), text.lastIndexOf(HREF_END + HTML_END));
		}
	    super.setText(text);
	    setCursor(null);
	}
	
	private void removeLinkable() {
		String text = getText();
		if (text.startsWith(HTML + A_HREF)) {
			text = text.substring((HTML + A_HREF).length(), text.lastIndexOf(HREF_END + HTML_END));
		}
		
		super.setText(text);
	    setCursor(null);
	}
	
	private class LinkMouseListener extends MouseAdapter {
	
	    @Override
	    public void mouseClicked(java.awt.event.MouseEvent evt) {
	    	if (linkAction != null && isActionActive) {
	    		linkAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
	    	}
	    }
	}
	
	
	//WARNING
	//This method requires that s is a plain string that requires
	//no further escaping
	private static String linkIfy(String s) {
	    return A_HREF.concat(s).concat(HREF_END);
	}
	
	//WARNING
	//This method requires that s is a plain string that requires
	//no further escaping
	private static String htmlIfy(String s) {
	    return HTML.concat(s).concat(HTML_END);
	}
	
	@Override
	public void setText(String text) {
		if (linkAction != null && isActionActive) {
			makeLinkable(text);
		}
		else {
			removeLinkable(text);
		}
		
		if (syncToolTipAction != null) {
			syncToolTipAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
		}
	}
	
	public void setLinkAction(Action action) {
		this.linkAction = action;
		if (linkAction != null && isActionActive) {
			makeLinkable();
		} else {
			removeLinkable();
		}
	}

	public boolean isActionActive() {
		return isActionActive;
	}

	public void setActionActive(boolean isActionActive) {
		this.isActionActive = isActionActive;
		if (!isActionActive) {
			removeLinkable();
		} else {
			makeLinkable();
		}
	}
	
}