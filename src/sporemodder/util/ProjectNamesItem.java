package sporemodder.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import sporemodder.HashManager;
import sporemodder.UIManager;
import sporemodder.file.DocumentError;
import sporemodder.view.editors.ItemEditor;
import sporemodder.view.editors.TextEditor;
import sporemodder.view.syntax.SyntaxHighlighter;

public class ProjectNamesItem extends ProjectItem {

	public ProjectNamesItem(File file, Project project) {
		super(file, project);

		this.name = "Project Names";
		this.file = new File(file, "names.txt");
	}

	@Override public boolean isFolder() {
		return false;
	}
	
	@Override public void setName(String s) {
	}
	@Override public void setFile(File f) {
	}
	
	@Override public boolean canRemoveItem() {
		return false;
	}
	@Override public boolean canModifyItem() {
		return false;
	}
	@Override public boolean canDuplicateItem() {
		return false;
	}
	@Override public boolean canCreateNewFile() {
		return false;
	}
	@Override public boolean canCreateNewFolder() {
		return false;
	}
	@Override public boolean canRenameItem() {
		return false;
	}
	@Override public boolean canImportFiles() {
		return false;
	}
	@Override public boolean canRefreshItem() {
		return false;
	}
	
	@Override public ItemEditor createEditor() { 
		TextEditor editor = new TextEditor() {
			@Override protected void saveData() throws Exception {
				// Some errors shown to the user are actually ignored by NameRegistry.read(), 
				// but it wouldn't be okay to accept them
				// We must discard them before actually saving
				
				SyntaxHighlighter syntax = new SyntaxHighlighter();
				syntax.setText(getCodeArea().getText(), null);
				getSyntaxHighlighting().generateStyle(getCodeArea().getText(), syntax);
				
				if (syntax.getEntryCount() != 0) {
					throw new IOException("There are errors in the file");
				}
				
				super.saveData();
				
				if (isSaved()) {
					
					NameRegistry reg = HashManager.get().getProjectRegistry();
					NameRegistry copy = new NameRegistry(HashManager.get(), "", "");
					copy.names.putAll(reg.names);
					copy.hashes.putAll(reg.hashes);
					
					try {
						reg.clear();
						reg.read(file);
					}
					catch (Exception e) {
						
						reg.clear();
						reg.names.putAll(copy.names);
						reg.hashes.putAll(copy.hashes);
						
						throw e;
					}
				}
			}
		};
		
		editor.setSyntaxHighlighting((text, syntax) -> {
			int lineIndex = 0;
			for (String line : text.split("\n")) {
				int indexOf = line.indexOf('~');
				if (indexOf != -1 && !(indexOf + 1 < line.length() && !Character.isWhitespace(indexOf + 1))) {
					if (indexOf == 0 || !(
							Character.isAlphabetic(line.charAt(indexOf - 1)) || Character.isDigit(line.charAt(indexOf - 1)) 
							|| line.charAt(indexOf - 1) == '_' 
							)) {
						syntax.addExtra(lineIndex + indexOf, 1, DocumentError.STYLE_ERROR, false);
					}
					
					if (indexOf == line.length() - 1 || !line.substring(indexOf + 1).startsWith("\t0x")) {
						syntax.addExtra(lineIndex + indexOf+1, line.length() - indexOf - 1, DocumentError.STYLE_ERROR, false);
					}
				}
				
				lineIndex += line.length() + 1;
			}
		});
		
		return editor;
	}
}
