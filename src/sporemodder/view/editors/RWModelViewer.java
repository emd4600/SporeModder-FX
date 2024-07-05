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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javafx.scene.control.*;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import sporemodder.FileManager;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.BoundingBox;
import sporemodder.file.dds.DDSTexture;
import sporemodder.file.rw4.*;
import sporemodder.file.rw4.Direct3DEnums.RWDECLUSAGE;
import sporemodder.file.rw4.RWHeader.RenderWareType;
import sporemodder.file.shaders.MaterialStateLink;
import sporemodder.util.ProjectItem;
import sporemodder.util.Vector3;
import sporemodder.view.UserInterface;
import sporemodder.view.inspector.InspectorFloatSpinner;
import sporemodder.view.inspector.InspectorString;
import sporemodder.view.inspector.InspectorVector3;
import sporemodder.view.inspector.PropertyPane;

/**
 * An editor used for visualizing RenderWare models. This has a built-in texture patcher. It is called 'viewer' instead of 'editor' because
 * although some things can be modified, it's main goal is to visualize it.
 */
public class RWModelViewer extends AbstractEditableEditor implements ItemEditor, EditHistoryEditor {
	
	private static abstract class RWUndoableAction implements EditHistoryAction {
		private String selectedObject;
		private String text;
		private RWUndoableAction(String text) {
			this.text = text;
		}
		@Override public String getText() {
			return text;
		}
	}
	
	/** The maximum amount of remembered edit history actions. */
	private static final int MAX_EDIT_HISTORY = 25;
	
	private static final RWUndoableAction ORIGINAL_ACTION = new RWUndoableAction(null) {

		@Override public void undo() {}

		@Override public void redo() {}

		@Override public String getText() {
			return "Original";
		}
	};
	
	private static final double AXIS_LENGTH = 250.0;
	private static final double AXIS_WIDTH = 0.005;
	
	private static final double FOV = 45;
	
	/** The minimum camera distance allowed. */
	private static final double MIN_DISTANCE = 0.1;
	/** The maximum camera distance allowed. */
	private static final double MAX_DISTANCE = 200;
	/** How much distance is advanced per mouse wheel scroll unit. */
	private static final double ZOOM_FACTOR = 1 / 80.0;

	public static class Factory implements EditorFactory {
		
		@Override
		public ItemEditor createInstance() {
			return new RWModelViewer();
		}

		@Override
		public boolean isSupportedFile(ProjectItem item) {
			if (!item.isFolder() && "rw4".equals(item.getSpecificExtension())) {
				try {
					return RenderWare.peekType(item.getFile()) == RenderWareType.MODEL;
				}
				catch (IOException e) {
					return false;
				}
			}
			
			return false;
		}
		
		@Override
		public Node getIcon(ProjectItem item) {
			return null;
		}
	}
	
	private static final double TREE_VIEW_HEIGHT = 300;
	
	// WARNING: JavaFX uses Y axis for Spore's Z axis (up-down) 

	// We start with a rotation so the model is viewed from an angle
	private PerspectiveCamera camera;
	/** The camera rotation around its X axis, which produces a vertical movement of the camera. */
	private final Rotate cameraRotateX = new Rotate(-20, Rotate.X_AXIS);
	/** The camera rotation around its Y axis, which produces a horizontal movement of the camera. */
	private final Rotate cameraRotateY = new Rotate(45, Rotate.Y_AXIS);
	/** The camera translation used to create a zoom effect. Only the Z coordinate is used. */
	private final Translate cameraTranslation = new Translate(0, 0, -5);
	/** The initial camera distance from the object. At this distance, there is no zoom. */
	private double initialCameraDistance;
	/** The current distance from the object. */
	private double cameraDistance;
	
	private Pane mainNode;
	private SubScene subScene;
	private Group group;
	
	private AmbientLight ambientLight;
	
	private Group axesGroup;
	
	private RenderWare renderWare;
	private BoundingBox bbox;
	private final List<MeshView> meshes = new ArrayList<>();
	private final List<RWMeshCompiledStateLink> rwMeshes = new ArrayList<>();
	private final List<RWTextureOverride> externalTextures = new ArrayList<>();
	private final Map<RWRaster, Image> rasterImages = new HashMap<>();  // with alpha removed
	private final Map<RWRaster, Image> rasterOriginalImages = new HashMap<>();
	// DDS are read as BufferedImages, then converted to JavaFX images
	// During that process, they lose the color information on transparent pixels, so we keep this here
	private final Map<RWRaster, BufferedImage> bufferedImages = new HashMap<>();
	private final Map<RWRaster, ObjectProperty<Image>> rasterImageProperties = new HashMap<>();
	
	private double mousePosX, mousePosY, mouseOldX, mouseOldY;
	
	// -- Inspector -- //
	
	private final Pane inspectorPane = new VBox(5);
	private final ScrollPane propertiesContainer = new ScrollPane();
	private final TreeView<String> treeView = new TreeView<>();
	private final CheckBox cbIgnoreAlpha = new CheckBox("Ignore alpha");

	private final TreeItem<String> tiAnimations = new TreeItem<String>("Animations");
	private final TreeItem<String> tiMorphs = new TreeItem<String>("Morph Handles");
	private final TreeItem<String> tiTextures = new TreeItem<String>("Textures");
	private final TreeItem<String> tiExternalTextures = new TreeItem<String>("External Textures");
	private final TreeItem<String> tiCompiledStates = new TreeItem<String>("Compiled States");
	
	private final Map<String, RWObject> nameMap = new HashMap<>();
	private final Map<String, TreeItem<String>> itemsMap = new HashMap<>();
	
	// For undo redo:
	private final Stack<RWUndoableAction> editHistory = new Stack<>();
	private int undoRedoIndex = -1;
	private boolean isUndoingAction;
	
	
	private RWModelViewer() {
		super();
		
		mainNode = new Pane();
		
		propertiesContainer.setFitToWidth(true);
    	
    	inspectorPane.getChildren().addAll(cbIgnoreAlpha, treeView, propertiesContainer);
    	VBox.setVgrow(propertiesContainer, Priority.ALWAYS);
    	
    	TreeItem<String> rootItem = new TreeItem<>();
    	treeView.setRoot(rootItem);
    	treeView.setShowRoot(false);
    	treeView.setMaxHeight(TREE_VIEW_HEIGHT);

    	rootItem.getChildren().add(tiAnimations);
		rootItem.getChildren().add(tiMorphs);
    	rootItem.getChildren().add(tiTextures);
    	rootItem.getChildren().add(tiExternalTextures);
    	rootItem.getChildren().add(tiCompiledStates);
    	tiMorphs.setExpanded(true);
    	tiTextures.setExpanded(true);
    	tiExternalTextures.setExpanded(true);
    	tiCompiledStates.setExpanded(true);
    	
    	treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue != null) {
    			fillPropertiesPane(newValue, newValue.getValue());
    		} else {
    			propertiesContainer.setContent(null);
    		}
    	});
    	
    	cbIgnoreAlpha.selectedProperty().addListener((obs, oldValue, newValue) -> {
    		for (Map.Entry<RWRaster, ObjectProperty<Image>> entry : rasterImageProperties.entrySet()) {
    			if (newValue) {
    				entry.getValue().set(rasterOriginalImages.get(entry.getKey()));
    			} else {
    				entry.getValue().set(rasterImages.get(entry.getKey()));
    			}
    		}
    	});
    	
    	// Add an original action that does nothing:
    	addEditAction(ORIGINAL_ACTION);
    	
	}
	
	private TriangleMesh processBlendShape(RWMesh mesh) throws IOException {
		
		List<RWBlendShapeBuffer> buffers = renderWare.getObjects(RWBlendShapeBuffer.class);
		if (buffers.size() != 1) {
			throw new IOException("Unsupported type of blend shape mesh");
		}
		
		RWBlendShapeBuffer buffer = buffers.get(0);
		
		if (buffer.data[RWBlendShapeBuffer.INDEX_POSITION] == null) {
			throw new IOException("Cannot process BlendShape without POSITION.");
		}
		
		int vertexCount = mesh.vertexCount;
		int vertexStart = mesh.firstVertex;
		float[] positions = new float[3 * vertexCount];
		float[] texCoords = buffer.data[RWBlendShapeBuffer.INDEX_TEXCOORD] == null ? null : new float[2 * vertexCount];
		float[] normals = buffer.data[RWBlendShapeBuffer.INDEX_POSITION] == null ? null : new float[3 * vertexCount];
		
		try (MemoryStream stream = new MemoryStream(buffer.data[RWBlendShapeBuffer.INDEX_POSITION])) {
			stream.seek(vertexStart * 16);
			for (int i = 0; i < vertexCount; ++i) {
				positions[i * 3 + 0] = stream.readLEFloat();
				positions[i * 3 + 1] = stream.readLEFloat();
				positions[i * 3 + 2] = stream.readLEFloat();
				stream.skip(4);
			}
		}
		
		if (texCoords != null) {
			try (MemoryStream stream = new MemoryStream(buffer.data[RWBlendShapeBuffer.INDEX_TEXCOORD])) {
				for (int i = 0; i < vertexCount; ++i) {
					stream.seek((vertexStart+i) * 16);
					texCoords[i * 2 + 0] = stream.readLEFloat();
					texCoords[i * 2 + 1] = stream.readLEFloat();
					stream.skip(8);
				}
			}
		}
		
		if (normals != null) {
			try (MemoryStream stream = new MemoryStream(buffer.data[RWBlendShapeBuffer.INDEX_NORMAL])) {
				for (int i = 0; i < vertexCount; ++i) {
					stream.seek((vertexStart+i) * 16);
					normals[i * 3 + 0] = stream.readLEFloat();
					normals[i * 3 + 1] = stream.readLEFloat();
					normals[i * 3 + 2] = stream.readLEFloat();
					stream.skip(4);
				}
			}
		}
		
		TriangleMesh triangleMesh = new TriangleMesh();
		triangleMesh.setVertexFormat(normals == null ? VertexFormat.POINT_TEXCOORD : VertexFormat.POINT_NORMAL_TEXCOORD);
		triangleMesh.getPoints().addAll(positions);
		if (texCoords != null) triangleMesh.getTexCoords().addAll(texCoords);
		if (normals != null) triangleMesh.getNormals().addAll(normals);
		
		return triangleMesh;
	}
	
	private TriangleMesh processVertexBuffer(RWMesh mesh) throws IOException {
		RWVertexBuffer buffer = mesh.vertexBuffers.get(0);
		
		try (MemoryStream vertexStream = new MemoryStream(buffer.vertexData.data);
				MemoryStream indexStream = new MemoryStream(mesh.indexBuffer.indexData.data)) {
		
			// Find all the vertex components we are going to use
			RWVertexElement positionElement = null;
			RWVertexElement texcoordElement = null;
			RWVertexElement normalElement = null;
			
			for (RWVertexElement element : buffer.vertexDescription.elements) {
				if (element.typeCode == RWDECLUSAGE.POSITION.getId()) {
					positionElement = element;
				}
				else if (element.typeCode == RWDECLUSAGE.TEXCOORD0.getId()) {
					texcoordElement = element;
				}
				else if (element.typeCode == RWDECLUSAGE.NORMAL.getId()) {
					normalElement = element;
				}
			}
			
			if (positionElement == null) {
				throw new IOException("Mesh requires at least POSITION vertex element.");
			}
			
			int vertexCount = mesh.vertexCount;
			int vertexStart = mesh.firstVertex;
			
			float[] positions = new float[3 * vertexCount];
			float[] texCoords = new float[2 * vertexCount];
			float[] normals = normalElement == null ? null : new float[3 * vertexCount];
			
			for (int i = 0; i < vertexCount; i++) {
				vertexStream.seek((vertexStart+i) * buffer.vertexSize + positionElement.offset);
				positions[i * 3 + 0] = vertexStream.readLEFloat();
				positions[i * 3 + 1] = vertexStream.readLEFloat();
				positions[i * 3 + 2] = vertexStream.readLEFloat();
			}
			
			if (texcoordElement != null) {
				for (int i = 0; i < vertexCount; i++) {
					vertexStream.seek((vertexStart+i) * buffer.vertexSize + texcoordElement.offset);
					texCoords[i * 2 + 0] = vertexStream.readLEFloat();
					texCoords[i * 2 + 1] = vertexStream.readLEFloat();
				}
			}
			else {
				for (int i = 0; i < texCoords.length; i++) texCoords[i] = 0.5f;
			}
			
			if (normals != null) {
				for (int i = 0; i < vertexCount; i++) {
					vertexStream.seek((vertexStart+i) * buffer.vertexSize + normalElement.offset);
					normals[i * 3 + 0] = (((float)vertexStream.readByte()) - 127.5f) / 127.5f;
					normals[i * 3 + 1] = (((float)vertexStream.readByte()) - 127.5f) / 127.5f;
					normals[i * 3 + 2] = (((float)vertexStream.readByte()) - 127.5f) / 127.5f;
				}
			}
			
			TriangleMesh triangleMesh = new TriangleMesh();
			triangleMesh.setVertexFormat(normals == null ? VertexFormat.POINT_TEXCOORD : VertexFormat.POINT_NORMAL_TEXCOORD);
			triangleMesh.getPoints().addAll(positions);
			triangleMesh.getTexCoords().addAll(texCoords);
			if (normals != null) triangleMesh.getNormals().addAll(normals);
			
			return triangleMesh;
		}
	}
	
	private TriangleMesh readMesh(RWMesh mesh) throws IOException {
		
		RWVertexBuffer buffer = mesh.vertexBuffers.get(0);
		TriangleMesh triangleMesh = buffer == null ? processBlendShape(mesh) : processVertexBuffer(mesh);
		
		boolean hasNormals = triangleMesh.getNormals().size() != 0;
		
		try (MemoryStream indexStream = new MemoryStream(mesh.indexBuffer.indexData.data)) 
		{
			int indexComponents = hasNormals ? 3 : 2;
			int[] indices = new int[mesh.triangleCount * indexComponents * 3];
			indexStream.seek(mesh.firstIndex * 2);
			for (int i = 0; i < mesh.triangleCount; i++) {
				for (int j = 0; j < 3; j++) {
					int index = indexStream.readLEUShort();
					indices[i*indexComponents*3 + indexComponents*j] = index;
					indices[i*indexComponents*3 + indexComponents*j + 1] = index;
					if (hasNormals) indices[i*indexComponents*3 + indexComponents*j + 2] = index;
				}
			}
			
			if (mesh.indexBuffer.startIndex != 0) {
				for (int i = 0; i < indices.length; i++) {
					indices[i] += mesh.indexBuffer.startIndex;
				}
			}
			
			if (mesh.firstVertex != 0) {
				for (int i = 0; i < indices.length; i++) {
					indices[i] -= mesh.firstVertex;
				}
			}
			
			triangleMesh.getFaces().addAll(indices);
			
			return triangleMesh;
		}
	}
	
	private void getBBox() {
		List<RWBBox> list = renderWare.getObjects(RWBBox.class);
		
		if (!list.isEmpty()) {
			bbox = list.get(0).boundingBox;
		}
	}
	
	private void zoom(double increment) {
		cameraDistance -= increment;
		
		if (cameraDistance < MIN_DISTANCE) {
			cameraDistance = MIN_DISTANCE;
		}
		else if (cameraDistance > MAX_DISTANCE) {
			cameraDistance = MAX_DISTANCE;
		}
		
		// Negative because we want to be behind the object to see it
		cameraTranslation.setZ(-cameraDistance);
	}
	
	private void buildAxes() {
		final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.RED);
 
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.GREEN);
 
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.BLUE);
        
        final Box xAxis = new Box(AXIS_LENGTH, AXIS_WIDTH, AXIS_WIDTH);
        final Box yAxis = new Box(AXIS_WIDTH, AXIS_LENGTH, AXIS_WIDTH);
        final Box zAxis = new Box(AXIS_WIDTH, AXIS_WIDTH, AXIS_LENGTH);
        
        // We only want to show one half
        xAxis.setTranslateX(- AXIS_LENGTH / 2.0);
        // Y axis in JavaFX goes the other way
        yAxis.setTranslateY(AXIS_LENGTH / 2.0);
        zAxis.setTranslateZ(- AXIS_LENGTH / 2.0);
        
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
        
        axesGroup = new Group();
 
        axesGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axesGroup.setVisible(true);
        axesGroup.getTransforms().addAll(
				new Rotate(-90, Rotate.X_AXIS));
        
        group.getChildren().add(axesGroup);
	}
	
	private Image removeAlphaChannel(BufferedImage original) {
		int width = (int) original.getWidth();
		int height = (int) original.getHeight();
		WritableImage newImage = new WritableImage(width, height);
		
		PixelWriter writer = newImage.getPixelWriter();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				writer.setArgb(x, y, original.getRGB(x, y) | 0xFF000000);
			}
		}
		
		return newImage;
	}
	
	private Image blendAlphaChannel(BufferedImage original, Color blendColor) {
		int width = (int) original.getWidth();
		int height = (int) original.getHeight();
		WritableImage newImage = new WritableImage(width, height);
		
		PixelWriter writer = newImage.getPixelWriter();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int color = original.getRGB(x, y);
				double alpha = (((long)color & 0xFF000000L) >> 24) / 255.0;
				double r = ((color & 0x00FF0000) >> 16) / 255.0;
				double g = ((color & 0x0000FF00) >> 8) / 255.0;
				double b = (color & 0x000000FF) / 255.0;
				
				writer.setColor(x, y, new Color(
						r * alpha + blendColor.getRed() * (1 - alpha),
						g * alpha + blendColor.getGreen() * (1 - alpha),
						b * alpha + blendColor.getBlue() * (1 - alpha),
						1.0));
			}
		}
		
		return newImage;
	}
	
	private Image blendAlphaChannel(BufferedImage original) {
		return blendAlphaChannel(original, Color.rgb(206, 212, 175));
	}
	
	private void loadImages() throws IOException {
		List<RWRaster> rasters = renderWare.getObjects(RWRaster.class);
		for (RWRaster raster : rasters) {
			BufferedImage buffered = raster.toDDSTexture().toBufferedImage();
			rasterOriginalImages.put(raster, removeAlphaChannel(buffered));
			rasterImages.put(raster, blendAlphaChannel(buffered));
			bufferedImages.put(raster, buffered);
		}
		
		List<RWTextureOverride> overrides = renderWare.getObjects(RWTextureOverride.class);
		for (RWTextureOverride texture : overrides) {
			if (texture.name != null) externalTextures.add(texture);
		}
	}
	
	private void loadMaterial(RWCompiledState compiledState, MeshView meshView) throws IOException {
		
		MaterialStateCompiler state = compiledState.data;
		state.decompile();
		
		PhongMaterial material = new PhongMaterial();
		meshView.setMaterial(material);
		
//		material.setDiffuseColor(new Color(state.materialColor.getR(), state.materialColor.getG(), state.materialColor.getB(), state.materialColor.getA()));
		
		if (!state.textureSlots.isEmpty()) {
			RWObject diffuseRaster = state.textureSlots.get(0).raster;
			if (diffuseRaster != null && diffuseRaster.getTypeCode() == RWRaster.TYPE_CODE) {
				rasterImageProperties.put((RWRaster)diffuseRaster, material.diffuseMapProperty());
				material.setDiffuseMap(rasterImages.get(diffuseRaster));
			}
			
			if (state.textureSlots.size() > 1) {
				RWObject normalRaster = state.textureSlots.get(1).raster;
				if (normalRaster != null && diffuseRaster.getTypeCode() == RWRaster.TYPE_CODE) {
					rasterImageProperties.put((RWRaster)normalRaster, material.bumpMapProperty());
					material.setBumpMap(rasterImages.get(normalRaster));
				}
			}
		}
	}
	
	private void loadModel(ProjectItem item) throws IOException {
		this.file = item.getFile();
		this.item = item;
		try (StreamReader stream = new FileStream(item.getFile(), "r")) {
			renderWare = new RenderWare();
			renderWare.read(stream);
			
			rwMeshes.addAll(renderWare.getObjects(RWMeshCompiledStateLink.class));
			
			// Some addon version don't set the 'size' parameters on buffers correctly,
			// so they are not getting read; fix it here
			for (RWVertexBuffer buffer : renderWare.getObjects(RWVertexBuffer.class)) {
				RWBaseResource data = buffer.vertexData;
				if (data.getSectionInfo().size == 0) {
					stream.seek(data.getSectionInfo().pData);
					data.data = new byte[buffer.vertexCount*buffer.vertexSize];
					stream.read(data.data);
				}
			}
			for (RWIndexBuffer buffer : renderWare.getObjects(RWIndexBuffer.class)) {
				RWBaseResource data = buffer.indexData;
				if (data.getSectionInfo().size == 0) {
					stream.seek(data.getSectionInfo().pData);
					data.data = new byte[buffer.primitiveCount * 2];
					stream.read(data.data);
				}
			}
			
			loadImages();
		}
		
		getBBox();
		
		// We want some distance from the object
		initialCameraDistance = bbox.getBiggest() * 2.0;
		cameraDistance = initialCameraDistance;
		// Update distance
		zoom(0);
		
		group = new Group();
		
		ambientLight = new AmbientLight();
		
		group.getChildren().add(ambientLight);
		
		for (RWMeshCompiledStateLink rwLink : rwMeshes) {
			UIManager.get().tryAction(() -> {
				MeshView meshView = new MeshView(readMesh(rwLink.mesh));
				loadMaterial(rwLink.compiledStates.get(0), meshView);
				
				meshView.setCullFace(CullFace.BACK);
				
				meshView.getTransforms().addAll(
						new Rotate(-90, Rotate.X_AXIS),
						new Rotate(180, Rotate.Y_AXIS));
				
				group.getChildren().add(meshView);
				meshes.add(meshView);
			}, "Cannot load mesh for this RW4 file");
		}

		buildCamera();
		
		buildAxes();
		
		subScene = new SubScene(group, 800, 600, true, SceneAntialiasing.BALANCED);
		subScene.setFill(Color.LIGHTBLUE);
		subScene.setCamera(camera);
		
		bindDimensions();
		
		mainNode.getChildren().clear();
		mainNode.getChildren().add(subScene);
		
		subScene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        subScene.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            double mouseDeltaX = (mousePosX - mouseOldX);
            double mouseDeltaY = (mousePosY - mouseOldY);
            
            if (me.isPrimaryButtonDown()) {
            	cameraRotateY.setAngle(cameraRotateY.getAngle() + mouseDeltaX * 180.0 / subScene.getWidth());
            	cameraRotateX.setAngle(cameraRotateX.getAngle() - mouseDeltaY * 180.0 / subScene.getHeight());
            }
        });
		
		subScene.setOnScroll((event) -> {
			zoom(event.getDeltaY() * ZOOM_FACTOR);
		});
		
		fillTreeView();
	}
	
	private void fillTreeView() {
		if (renderWare != null) {
			List<RWRaster> rasters = renderWare.getObjects(RWRaster.class);
			for (RWRaster raster : rasters) {
				String name = renderWare.getName(raster);
				nameMap.put(name, raster);
				
				TreeItem<String> item = new TreeItem<String>(name);
				itemsMap.put(name, item);
				tiTextures.getChildren().add(item);
			}
			
			List<RWTextureOverride> overrides = renderWare.getObjects(RWTextureOverride.class);
			for (RWTextureOverride texture : overrides) {
				if (texture.name != null) {
					String name = renderWare.indexOf(texture) + " \"" + texture.name + "\"";
					nameMap.put(name, texture);
					
					TreeItem<String> item = new TreeItem<String>(name);
					itemsMap.put(name, item);
					tiExternalTextures.getChildren().add(item);
				}
			}
			
			List<RWCompiledState> compiledStates = renderWare.getObjects(RWCompiledState.class);
			for (RWCompiledState compiledState : compiledStates) {
				String name = renderWare.getName(compiledState);
				nameMap.put(name, compiledState);
				
				TreeItem<String> item = new TreeItem<String>(name);
				itemsMap.put(name, item);
				tiCompiledStates.getChildren().add(item);
			}
			
			List<RWMorphHandle> morphs = renderWare.getObjects(RWMorphHandle.class);
			for (RWMorphHandle morph : morphs) {
				String name = HashManager.get().getFileName(morph.handleID);
				nameMap.put(name, morph);
				
				TreeItem<String> item = new TreeItem<String>(name);
				itemsMap.put(name, item);
				tiMorphs.getChildren().add(item);
			}

			List<RWAnimations> animations = renderWare.getObjects(RWAnimations.class);
			if (!animations.isEmpty()) {
				nameMap.put("Animations", animations.get(0));
				itemsMap.put("Animations", tiAnimations);
			}
		}
	}
	
	private void buildCamera() {
		camera = new PerspectiveCamera(true);
		camera.setFieldOfView(FOV);
		camera.setFarClip(1000.0);
		
		camera.getTransforms().clear();
		camera.getTransforms().addAll(
                cameraRotateY,
                cameraRotateX,
                cameraTranslation
        );
		
		zoom(0);
		
        group.getChildren().add(camera);
    }
	
	private void bindDimensions() {
		subScene.widthProperty().bind(mainNode.widthProperty());
		subScene.heightProperty().bind(mainNode.heightProperty());
	}
	
	@Override
	public void loadFile(ProjectItem item) throws IOException {
		if (item != null) {
			this.item = item;
			loadModel(item);
		}
	}
	
	private void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("Model Viewer", "rw4", inspectorPane);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}

	@Override public void setActive(boolean isActive) {
		super.setActive(isActive);
		showInspector(isActive);
	}

	@Override
	public Node getUI() {
		return mainNode;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean supportsSearching() {
		return false;
	}

	@Override
	public boolean supportsEditHistory() {
		return true;
	}
	
	private void fillPropertiesPane(TreeItem<String> item, String selectedName) {
		RWObject object = nameMap.get(selectedName);

		if (object instanceof RWMorphHandle) {
			fillMorphPane(item, selectedName, (RWMorphHandle) object);
			return;
		}
		else if (object instanceof RWRaster) {
			fillTexturePane(selectedName, (RWRaster) object);
			return;
		}
		else if (object instanceof RWTextureOverride) {
			fillExternalTexturePane(item, selectedName, (RWTextureOverride) object);
			return;
		}
		else if (object instanceof RWCompiledState) {
			UIManager.get().tryAction(() -> {
				showCompiledStateEditor(selectedName);
			}, "Error with compiled state.");
			// We don't return because we want to empty the properties container
		}
		else if (object instanceof RWAnimations) {
			fillAnimationsPane(item, (RWAnimations) object);
			return;
		}
		
		propertiesContainer.setContent(null);
	}

	private void fillAnimationsPane(TreeItem<String> item, RWAnimations animations) {
		PropertyPane pane = new PropertyPane();

		for (int animationID : animations.animations.keySet()) {
			Label label = new Label(HashManager.get().getFileName(animationID));
			label.getStyleClass().add("inspector-value-label");
			pane.add(label);
		}

		propertiesContainer.setContent(pane.getNode());
	}
	
	private void fillExternalTexturePane(TreeItem<String> item, String name, RWTextureOverride texture) {
		PropertyPane pane = new PropertyPane();
		
		InspectorString nameField = new InspectorString();
		nameField.setText(texture.name);
		
		pane.add("Name", "A name used to identify this texture slot.", nameField);
		
		nameField.addValueListener((obs, oldValue, newValue) -> {
			item.setValue(renderWare.indexOf(texture) + " \"" + newValue + "\"");
			
			addEditAction(new RWUndoableAction("External texture: name") {

				@Override public void undo() {
					texture.name = oldValue;
					item.setValue(renderWare.indexOf(texture) + " \"" + oldValue + "\"");
				}

				@Override public void redo() {
					texture.name = newValue;
					item.setValue(renderWare.indexOf(texture) + " \"" + newValue + "\"");
				}
			});
		});
		
		propertiesContainer.setContent(pane.getNode());
	}
	
	private void fillMorphPane(TreeItem<String> item, String name, RWMorphHandle morph) {
		PropertyPane pane = new PropertyPane();
		
		InspectorString nameField = new InspectorString();
		nameField.setText(HashManager.get().getFileName(morph.handleID));
		
		InspectorFloatSpinner initialTime = new InspectorFloatSpinner(0);
		initialTime.setValue((double)morph.defaultTime);
		
		InspectorVector3 startPos = new InspectorVector3(new Vector3(morph.startPos));
		InspectorVector3 endPos = new InspectorVector3(new Vector3(morph.endPos));
		
		pane.add("Handle ID", "The ID of the handle, it determines the type.", nameField);
		pane.add("Default Time (s)", "The time of animation, in seconds (24 frames = 1s), where the morph is by default.", initialTime);
		pane.add(PropertyPane.createTitled("Start Position", "The start position of the handle", startPos.getNode()));
		pane.add(PropertyPane.createTitled("End Position", "The start position of the handle", endPos.getNode()));
		
		propertiesContainer.setContent(pane.getNode());
		
		nameField.addValueListener((obs, oldValue, newValue) -> {
			morph.handleID = HashManager.get().getFileHash(newValue);
			item.setValue(newValue);
			
			addEditAction(new RWUndoableAction(name + ": Handle ID") {

				@Override public void undo() {
					morph.handleID = HashManager.get().getFileHash(oldValue);
					item.setValue(oldValue);
				}

				@Override public void redo() {
					morph.handleID = HashManager.get().getFileHash(newValue);
					item.setValue(newValue);
				}
			});
		});
		
		initialTime.addValueListener((obs, oldValue, newValue) -> {
			morph.defaultTime = newValue.floatValue();
			
			addEditAction(new RWUndoableAction(name + ": Default Time") {

				@Override public void undo() {
					morph.defaultTime = oldValue.floatValue();
				}

				@Override public void redo() {
					morph.defaultTime = newValue.floatValue();
				}
			});
		});
		
		startPos.addValueListener((obs, oldValue, newValue) -> {
			morph.startPos[0] = newValue.getX();
			morph.startPos[1] = newValue.getY();
			morph.startPos[2] = newValue.getZ();
			
			addEditAction(new RWUndoableAction(name + ": Start Position") {

				@Override public void undo() {
					morph.startPos[0] = oldValue.getX();
					morph.startPos[1] = oldValue.getY();
					morph.startPos[2] = oldValue.getZ();
				}

				@Override public void redo() {
					morph.startPos[0] = newValue.getX();
					morph.startPos[1] = newValue.getY();
					morph.startPos[2] = newValue.getZ();
				}
			});
		});
		endPos.addValueListener((obs, oldValue, newValue) -> {
			morph.endPos[0] = newValue.getX();
			morph.endPos[1] = newValue.getY();
			morph.endPos[2] = newValue.getZ();
			
			addEditAction(new RWUndoableAction(name + ": End Position") {

				@Override public void undo() {
					morph.endPos[0] = oldValue.getX();
					morph.endPos[1] = oldValue.getY();
					morph.endPos[2] = oldValue.getZ();
				}

				@Override public void redo() {
					morph.endPos[0] = newValue.getX();
					morph.endPos[1] = newValue.getY();
					morph.endPos[2] = newValue.getZ();
				}
			});
		});
	}
	
	private void fillTexturePane(String name, RWRaster raster) {
		Button exportButton = new Button("Export");
		Button importButton = new Button("Import");
		
		HBox hbox = new HBox(5);
		hbox.getChildren().addAll(exportButton, importButton);
		
		ImageView viewer = new ImageView(rasterOriginalImages.get(raster));
		ScrollPane scrollPane = new ScrollPane(viewer);
		scrollPane.setPrefHeight(300);
		
		VBox vbox = new VBox(5);
		vbox.getChildren().addAll(hbox, scrollPane);
		vbox.setFillWidth(true);
		
		propertiesContainer.setContent(vbox);
		
		
		exportButton.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll(FileManager.FILEFILTER_ALL, FileManager.FILEFILTER_DDS);
			chooser.setSelectedExtensionFilter(FileManager.FILEFILTER_DDS);
			File file = chooser.showSaveDialog(UIManager.get().getScene().getWindow());
			if (file != null) {
				UIManager.get().tryAction(() -> raster.toDDSTexture().write(file), "Texture could not be exported.");
			}
		});
		
		importButton.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll(FileManager.FILEFILTER_ALL, FileManager.FILEFILTER_DDS);
			chooser.setSelectedExtensionFilter(FileManager.FILEFILTER_DDS);
			File file = chooser.showOpenDialog(UIManager.get().getScene().getWindow());
			if (file != null) {
				UIManager.get().tryAction(() -> {
					DDSTexture oldTexture = raster.toDDSTexture();
					Image oldRemovedAlpha = rasterImages.get(raster);
					Image oldOriginalImage = rasterOriginalImages.get(raster);
					BufferedImage oldBufferedImage = bufferedImages.get(raster);
					byte[] oldData = raster.textureData.data;
					
					DDSTexture texture = new DDSTexture();
					texture.read(file);
					raster.fromDDSTexture(texture);
					raster.textureData.data = texture.getData();
					
					BufferedImage bufferedImage = texture.toBufferedImage();
					Image originalImage = removeAlphaChannel(bufferedImage);
					Image removedAlpha = blendAlphaChannel(bufferedImage);
					rasterImages.put(raster, removedAlpha);
					rasterOriginalImages.put(raster, originalImage);
					bufferedImages.put(raster, bufferedImage);
					
					viewer.setImage(originalImage);
					rasterImageProperties.get(raster).set(cbIgnoreAlpha.isSelected() ? originalImage : removedAlpha);
					
					addEditAction(new RWUndoableAction(name + ": Texture") {

						@Override public void undo() {
							raster.fromDDSTexture(oldTexture);
							raster.textureData.data = oldData;
							rasterImages.put(raster, oldRemovedAlpha);
							rasterOriginalImages.put(raster, oldOriginalImage);
							bufferedImages.put(raster, oldBufferedImage);
							
							rasterImageProperties.get(raster).set(cbIgnoreAlpha.isSelected() ? oldOriginalImage : oldRemovedAlpha);
						}

						@Override public void redo() {
							raster.fromDDSTexture(texture);
							raster.textureData.data = texture.getData();
							rasterImages.put(raster, removedAlpha);
							rasterOriginalImages.put(raster, originalImage);
							bufferedImages.put(raster, bufferedImage);
							
							rasterImageProperties.get(raster).set(cbIgnoreAlpha.isSelected() ? originalImage : removedAlpha);
						}
					});
				}, "Texture could not be imported. Only DDS textures are supported.");
			}
		});
	}
	
	@Override
	public boolean canUndo() {
		// We can't undo the first action cause we couldn't go to the previous action
		return editHistory.size() > 1 && undoRedoIndex > 0;
	}

	@Override
	public boolean canRedo() {
		return undoRedoIndex != editHistory.size() - 1;
	}

	private String getSelectedName() {
		if (treeView.getSelectionModel().getSelectedItem() == null) return null;
		return treeView.getSelectionModel().getSelectedItem().getValue();
	}
	
	@Override public void undo() {
		isUndoingAction = true;
		
		RWUndoableAction action = editHistory.get(undoRedoIndex);
		
		action.undo();
		--undoRedoIndex;
		
		if (action.selectedObject != null) {
			if (action.selectedObject.equals(getSelectedName())) {
				fillPropertiesPane(treeView.getSelectionModel().getSelectedItem(), action.selectedObject);
			} else {
				treeView.getSelectionModel().select(itemsMap.get(action.selectedObject));
			}
		}
		
		if (undoRedoIndex == 0 && editHistory.get(0) == ORIGINAL_ACTION) {
			setIsSaved(true);
		} else {
			setIsSaved(false);
		}
		
		isUndoingAction = false;
		
		UIManager.get().notifyUIUpdate(false);
	}

	@Override public void redo() {
		isUndoingAction = true;
		
		// We redo the last action we did
		++undoRedoIndex;
		
		RWUndoableAction action = editHistory.get(undoRedoIndex);
		action.redo();
		
		if (action.selectedObject != null) {
			if (action.selectedObject.equals(getSelectedName())) {
				fillPropertiesPane(treeView.getSelectionModel().getSelectedItem(), action.selectedObject);
			} else {
				treeView.getSelectionModel().select(itemsMap.get(action.selectedObject));
			}
		}
		
		setIsSaved(false);
		
		isUndoingAction = false;
		
		UIManager.get().notifyUIUpdate(false);
	}
	
	private void deleteActionsAfterIndex(int index) {
		for (int i = editHistory.size()-1; i > index; --i) {
			editHistory.remove(i);
		}
	}
	
	public void addEditAction(RWUndoableAction action) {
		if (!isUndoingAction) {
			// If the edit undoed certain actions we start a new edit branch now
			deleteActionsAfterIndex(undoRedoIndex);
			action.selectedObject = getSelectedName();
			editHistory.push(action);
			++undoRedoIndex;
			
			if (editHistory.size() > MAX_EDIT_HISTORY) {
				editHistory.remove(0);
				--undoRedoIndex;
			}
			
			if (action != ORIGINAL_ACTION) setIsSaved(false);
			
			UIManager.get().notifyUIUpdate(false);
		}
	}

	@Override public List<? extends EditHistoryAction> getActions() {
		return editHistory;
	}

	@Override public int getUndoRedoIndex() {
		return undoRedoIndex;
	}

	@Override
	protected void saveData() throws Exception {
		try (FileStream stream = new FileStream(getFile(), "rw")) {
			renderWare.write(stream);
			
			setIsSaved(true);
		}
	}

	@Override
	protected void restoreContents() throws Exception {
		loadModel(getItem());
	}
	
	public void showCompiledStateEditor(String name) throws IOException {
		HashManager.get().setUpdateProjectRegistry(true);
		
		RWCompiledState state = (RWCompiledState) nameMap.get(name);
		
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		CompiledStateEditor editor = new CompiledStateEditor(dialog);
		if (!state.data.isDecompiled()) {
			state.data.decompile();
		}
		editor.loadContents(state.data.toArgScript(name));
		
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.APPLY);
		dialog.getDialogPane().setContent(editor);
		dialog.setTitle(name);
		
		editor.setPrefWidth(800);
		editor.setPrefHeight(600);
		
		dialog.setResizable(true);
		
		if (UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) != ButtonType.CANCEL) {
			HashManager.get().setUpdateProjectRegistry(false);
			ProjectManager.get().saveNamesRegistry();
			
			byte[] oldData = state.data.getData();
			
			editor.processStream();
			MaterialStateCompiler compiler = editor.stream.getData().definedStates.get(0);
			compiler.vertexDescription = state.data.vertexDescription;
			compiler.compile();
			byte[] newData = compiler.getData();
			
			if (!Arrays.equals(oldData, newData)) {
				state.data = compiler;
				
				addEditAction(new RWUndoableAction(name) {

					@Override public void undo() {
						state.data.setData(oldData);
						try {
							state.data.decompile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					@Override public void redo() {
						state.data.setData(newData);
						try {
							state.data.decompile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}
	
	private class CompiledStateEditor extends ArgScriptEditor<MaterialStateLink> {
		private final MaterialStateLink stateLink = new MaterialStateLink();
		private final Dialog<ButtonType> dialog;
		
		public CompiledStateEditor(Dialog<ButtonType> dialog) {
			super();
			this.dialog = dialog;
			
			stateLink.renderWare = renderWare;
			stream = stateLink.generateStream(false);
		}
		
		@Override protected void onStreamParse() {
			super.onStreamParse();
			
			stateLink.reset();
		}
		
		@Override protected void afterStreamParse() {
			super.afterStreamParse();
			
			Node button = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
			if (button != null && stream != null) {
				boolean disable = !stream.getErrors().isEmpty() || stream.getData().definedStates.isEmpty();
				button.setDisable(disable);
			}
		}
		
		public void processStream() {
			stream.getData().reset();
			stream.process(getText());
		}
	}
}
