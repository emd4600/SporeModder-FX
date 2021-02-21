/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.view.editors;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.TreeSet;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import sporemodder.file.filestructures.FileStream;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sporemodder.UIManager;
import sporemodder.file.shaders.FXCompiler;
import sporemodder.file.shaders.StandardShader;
import sporemodder.util.ProjectItem;
import sporemodder.view.UserInterface;

public class CompiledShaderViewer implements ItemEditor {
	
	private static final String VertexShaderText = "Vertex Shader";
	private static final String PixelShaderText = "Pixel Shader";
	
	private final CodeArea codeArea = new CodeArea();
	private final VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
	
	private final TreeView<String> treeView = new TreeView<>();
	private final Pane inspectorPane = new VBox(5);
	
	private StandardShader shader;
	private String shaderText;
	
	public CompiledShaderViewer() {
		scrollPane.setPrefWidth(Double.MAX_VALUE);
		scrollPane.setPrefHeight(Double.MAX_VALUE);
		
		treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				UIManager.get().tryAction(() -> {
					if (newValue.isLeaf()) {
						loadShader(Integer.parseInt(newValue.getParent().getValue()), newValue.getValue().equals(VertexShaderText));
					} else {
						loadShader(Integer.parseInt(newValue.getValue()), true);
					}
				}, "Cannot load shader.");
			}
		});
		
		inspectorPane.getChildren().add(treeView);
	}

	@Override public boolean isEditable() {
		return false;
	}
	
	@Override public void setActive(boolean isActive) {
		if (isActive) {
			if (!FXCompiler.get().isAvailable()) {
				Label label = new Label("This format is not supported by SporeModder and it cannot be read as text, therefore it cannot be edited.");
				label.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
				
				UIManager.get().getUserInterface().setStatusInfo(label);
			}
		} else {
			UIManager.get().getUserInterface().setStatusInfo(null);
		}
		
		showInspector(isActive);
	}
	
	private void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("Compiled Shader", "smt", inspectorPane);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	private void loadShader(int index, boolean isVertexShader) throws IOException {
		if (FXCompiler.get().isAvailable()) {
			try {
				shaderText = decompile(isVertexShader ? shader.entries.get(index).vertexShader : shader.entries.get(index).pixelShader);
			} 
			catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
		
		codeArea.replaceText(shaderText);
		codeArea.moveTo(0);
		codeArea.scrollToPixel(0, 0);
	}
	
	private static String decompile(byte[] data) throws IOException, InterruptedException {
		File tempInput = File.createTempFile("SporeModderFX-decompiled-shader", ".obj.tmp");
		Files.write(tempInput.toPath(), data, StandardOpenOption.WRITE);
		
		File decompiledFile = FXCompiler.get().decompile(FXCompiler.VS_PROFILE, tempInput);
		String result =  new String(Files.readAllBytes(decompiledFile.toPath()), Charset.defaultCharset());
		
		tempInput.delete();
		decompiledFile.delete();
		return result;
	}

	@Override public void loadFile(ProjectItem item) throws IOException {
		if (item != null) {
			shader = new StandardShader();
			try (FileStream stream = new FileStream(item.getFile(), "r")) {
				shader.read(stream, 1);
			}
			
			TreeItem<String> root = new TreeItem<>();
			treeView.setRoot(root);
			treeView.setShowRoot(false);
			
			Set<Integer> entries = new TreeSet<>(shader.entries.keySet());
			for (int i : entries) {
				TreeItem<String> treeItem = new TreeItem<>(Integer.toString(i));
				root.getChildren().add(treeItem);
				treeItem.setExpanded(true);
				
				treeItem.getChildren().add(new TreeItem<>(VertexShaderText));
				treeItem.getChildren().add(new TreeItem<>(PixelShaderText));
			}
			
			treeView.getSelectionModel().select(root.getChildren().get(0).getChildren().get(0));
		}
	}

	@Override public void setDestinationFile(File file) {
	}

	@Override public Node getUI() {
		return scrollPane;
	}

	@Override public void save() {
	}

	@Override public boolean supportsSearching() {
		return false;
	}

	@Override public boolean supportsEditHistory() {
		return false;
	}
}
