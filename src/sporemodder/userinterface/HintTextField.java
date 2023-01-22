package sporemodder.userinterface;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class HintTextField extends JTextField implements FocusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1089678163089003292L;
	
	private final String hint;
	private boolean showingHint;

	public HintTextField(final String hint) {
		super(hint);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if(this.getText().isEmpty() || showingHint) {
			super.setText("");
			showingHint = false;
		}
	}
	@Override
	public void focusLost(FocusEvent e) {
		if(this.getText().isEmpty()) {
			super.setText(hint);
			showingHint = true;
		}
	}

	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}
	
	@Override
	public void setText(String text) {
		if (text == null) {
			showingHint = true;
			super.setText(hint);
		}
		else {
			showingHint = false;
			super.setText(text);
		}
	}
}
