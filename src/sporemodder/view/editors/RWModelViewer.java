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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.imageio.ImageIO;

import emord.filestructures.FileStream;
import emord.filestructures.MemoryStream;
import emord.filestructures.StreamReader;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.Shadow;
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
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import sporemodder.FileManager;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.BoundingBox;
import sporemodder.file.dds.DDSTexture;
import sporemodder.file.rw4.Direct3DEnums.RWDECLUSAGE;
import sporemodder.file.rw4.MaterialStateCompiler;
import sporemodder.file.rw4.RWBBox;
import sporemodder.file.rw4.RWBaseResource;
import sporemodder.file.rw4.RWBlendShapeBuffer;
import sporemodder.file.rw4.RWCompiledState;
import sporemodder.file.rw4.RWHeader.RenderWareType;
import sporemodder.file.rw4.RWIndexBuffer;
import sporemodder.file.rw4.RWMesh;
import sporemodder.file.rw4.RWMeshCompiledStateLink;
import sporemodder.file.rw4.RWMorphHandle;
import sporemodder.file.rw4.RWObject;
import sporemodder.file.rw4.RWRaster;
import sporemodder.file.rw4.RWVertexBuffer;
import sporemodder.file.rw4.RWVertexElement;
import sporemodder.file.rw4.RenderWare;
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
	
	/** How much the camera can rotate for every pixel of mouse dragged, in degree angles. */
	private static final double CAMERA_SPEED = 0.25;
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

	private final XformCamera cameraXform = new XformCamera();
	private PerspectiveCamera camera;
	/** The camera rotation around its X axis, which produces a vertical movement of the camera. */
	// We start with a rotation so the model is viewed from an angle
	private final Rotate cameraRotateX = new Rotate(-20, Rotate.X_AXIS);
	/** The camera rotation around its Y axis, which produces a horizontal movement of the camera. */
	private final Rotate cameraRotateY = new Rotate(45, Rotate.Y_AXIS);
	/** The camera translation used to create a zoom effect. Only the Z coordinate is used. */
	private final Translate cameraZoomTranslate = new Translate(0, 0, -5);
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
	private final Map<RWRaster, Image> rasterImages = new HashMap<>();
	private final Map<RWRaster, ObjectProperty<Image>> rasterImageProperties = new HashMap<>();
	
	private double mouseX;
	private double mouseY;
	private double cameraAngleX;
	private double cameraAngleY;
	
	double mousePosX, mousePosY, mouseOldX, mouseOldY, mouseDeltaX, mouseDeltaY;
	
	// -- Inspector -- //
	
	private final Pane inspectorPane = new VBox(5);
	private final ScrollPane propertiesContainer = new ScrollPane();
	private TreeView<String> treeView = new TreeView<>();
	
	private TreeItem<String> tiMorphs = new TreeItem<String>("Morph Handles");
	private TreeItem<String> tiTextures = new TreeItem<String>("Textures");
	private TreeItem<String> tiCompiledStates = new TreeItem<String>("Compiled States");
	
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
    	
    	inspectorPane.getChildren().addAll(treeView, propertiesContainer);
    	VBox.setVgrow(propertiesContainer, Priority.ALWAYS);
    	
    	TreeItem<String> rootItem = new TreeItem<>();
    	treeView.setRoot(rootItem);
    	treeView.setShowRoot(false);
    	treeView.setMaxHeight(TREE_VIEW_HEIGHT);
    	
    	rootItem.getChildren().add(tiMorphs);
    	rootItem.getChildren().add(tiTextures);
    	rootItem.getChildren().add(tiCompiledStates);
    	tiMorphs.setExpanded(true);
    	tiTextures.setExpanded(true);
    	tiCompiledStates.setExpanded(true);
    	
    	treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue != null) {
    			fillPropertiesPane(newValue, newValue.getValue());
    		} else {
    			propertiesContainer.setContent(null);
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
			for (int i = 0; i < vertexCount; ++i) {
				stream.seek((vertexStart+i) * 16);
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
			
			if (positionElement == null || texcoordElement == null) {
				throw new IOException("Mesh requires at least POSITION and TEXCOORD vertex elements.");
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
			
			for (int i = 0; i < vertexCount; i++) {
				vertexStream.seek((vertexStart+i) * buffer.vertexSize + texcoordElement.offset);
				texCoords[i * 2 + 0] = vertexStream.readLEFloat();
				texCoords[i * 2 + 1] = vertexStream.readLEFloat();
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
		cameraZoomTranslate.setZ(- cameraDistance);
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
	
	private Image removeAlphaChannel(Image original) {
		int width = (int) original.getWidth();
		int height = (int) original.getHeight();
		WritableImage newImage = new WritableImage(width, height);
		
		PixelWriter writer = newImage.getPixelWriter();
		PixelReader reader = original.getPixelReader();
		
//		Color testColor = Color.gray(0.62);
		Color testColor = Color.rgb(206, 212, 175);
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color color = reader.getColor(x, y);
				double alpha = color.getOpacity();
				writer.setColor(x, y, new Color(
						color.getRed() * alpha + testColor.getRed() * (1 - alpha),
						color.getGreen() * alpha + testColor.getGreen() * (1 - alpha),
						color.getBlue() * alpha + testColor.getBlue() * (1 - alpha),
						1.0));
			}
		}
		
		return newImage;
	}
	
	private Image imageFromRaster(RWRaster raster) throws IOException {
		Image image = raster.toJavaFX();
		// Note: apparently not only SkinPaints. Most cases will use this.
		return removeAlphaChannel(image);
	}
	
	private void loadImages() throws IOException {
		List<RWRaster> rasters = renderWare.getObjects(RWRaster.class);
		for (RWRaster raster : rasters) {
			Image image = imageFromRaster(raster);
			rasterImages.put(raster, image);
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
			if (diffuseRaster != null) {
				rasterImageProperties.put((RWRaster)diffuseRaster, material.diffuseMapProperty());
				material.setDiffuseMap(rasterImages.get(diffuseRaster));
			}
			
			if (state.textureSlots.size() > 1) {
				RWObject normalRaster = state.textureSlots.get(1).raster;
				if (normalRaster != null) {
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
		
//		camera = new PerspectiveCamera(true);
//		camera.setFieldOfView(FOV);
//		
//		camera.getTransforms().addAll(
//				cameraRotateY,
//				cameraRotateX,
//				cameraZoomTranslate);
		
		group = new Group();
		// We have to add the camera here as well
//		group.getChildren().add(camera);
		
		ambientLight = new AmbientLight();
		
		group.getChildren().add(ambientLight);
		
		for (RWMeshCompiledStateLink rwLink : rwMeshes) {
			UIManager.get().tryAction(() -> {
				MeshView meshView = new MeshView(readMesh(rwLink.mesh));
				loadMaterial(rwLink.compiledStates.get(0), meshView);
				
				meshView.setCullFace(CullFace.BACK);
//				meshView.setDrawMode(DrawMode.LINE);
				
				meshView.getTransforms().addAll(
						new Rotate(-90, Rotate.X_AXIS),
						new Rotate(180, Rotate.Y_AXIS));
				
				group.getChildren().add(meshView);
				meshes.add(meshView);
			}, "Cannot load mesh for this RW4 file");
		}
		
//		cameraRotateY.setAngle(-57 + 90*3);
//		cameraRotateX.setAngle(-28.6 + 90*0);
//		cameraZoomTranslate.setZ(-bbox.getBiggest()*3.8);
//		camera.setRotate(0);
//		
		buildCamera();
		
		buildAxes();
		
		subScene = new SubScene(group, 800, 600, true, SceneAntialiasing.BALANCED);
		subScene.setFill(Color.LIGHTBLUE);
		subScene.setCamera(camera);
		
		bindDimensions();
		
		mainNode.getChildren().clear();
		mainNode.getChildren().add(subScene);
		
//		mainNode.setOnMousePressed((event) -> {
//			mouseX = event.getX();
//			mouseY = event.getY();
//			
//			cameraAngleX = cameraRotateX.getAngle();
//			cameraAngleY = cameraRotateY.getAngle();
//		});
//		
//		mainNode.setOnMouseDragged((event) -> {
//			
//			double deltaX = event.getX() - mouseX;
//			double deltaY = event.getY() - mouseY;
//			
//			double newX = cameraAngleX + -deltaY * CAMERA_SPEED;
//			double newY = cameraAngleY + deltaX * CAMERA_SPEED;
//			
//			if (newX < 0) newX = 360 + newX;
//			else if (newX > 360) newX = newX - 360;
//			
//			if (newY < 0) newY = 360 + newY;
//			else if (newY > 360) newY = newY - 360;
//			
//			cameraRotateX.setAngle(newX);
//			cameraRotateY.setAngle(newY);
//		});
		
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
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);
            
            if (me.isPrimaryButtonDown()) {
                cameraXform.ry(mouseDeltaX * 180.0 / subScene.getWidth());
                cameraXform.rx(-mouseDeltaY * 180.0 / subScene.getHeight());
            	
//            	cameraRotateY.setAngle(mouseDeltaX * 180.0 / subScene.getWidth());
//            	cameraRotateX.setAngle(-mouseDeltaY * 180.0 / subScene.getHeight());
            }
//            else if (me.isSecondaryButtonDown()) {
//                camera.setTranslateZ(camera.getTranslateZ() + mouseDeltaY);
//            }
        });
		
		subScene.setOnScroll((event) -> {
			zoom(event.getDeltaY() * ZOOM_FACTOR);
		}); 
		
//		subScene.setOnMouseClicked((event) -> {
//			if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
//				try {
//					takeIconSnapshot();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});
		
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
		}
	}
	
	private void buildCamera() {
		camera = new PerspectiveCamera(true);
		camera.setFieldOfView(FOV);
//		camera.setTranslateZ(-initialCameraDistance);
		
		camera.getTransforms().addAll(
//			cameraRotateY,
//			cameraRotateX,
			cameraZoomTranslate);
		
		zoom(0);
		
        group.getChildren().add(cameraXform);
        cameraXform.getChildren().add(camera);
    }
	
	private void bindDimensions() {
		subScene.widthProperty().bind(mainNode.widthProperty());
		subScene.heightProperty().bind(mainNode.heightProperty());
	}
	
	private void takeIconSnapshot() throws IOException {
		// Store previous parameters
		Paint originalFill = subScene.getFill();
		Camera originalCamera = subScene.getCamera();
		boolean originalAxes = axesGroup.isVisible();
		boolean lightOn = ambientLight.isLightOn();
		
		double halfX = bbox.getLengthX() / 2.0;
		double halfY = bbox.getLengthY() / 2.0;
		double halfZ = bbox.getLengthZ() / 2.0;
		
		double movementX = halfX - bbox.getMax().getX();
		double movementY = halfY - bbox.getMax().getY();
		double movementZ = halfZ - bbox.getMax().getZ();
		
		final List<List<Transform>> originalTransforms = new ArrayList<List<Transform>>();
		
		double extraRotationX = -5;
		double extraRotationY = -20;  // -5
		double extraRotationZ = -10;  // -10 
		
		for (MeshView meshView : meshes) {
			originalTransforms.add(new ArrayList<Transform>(meshView.getTransforms()));
			
			meshView.getTransforms().clear();
			meshView.getTransforms().add(new Rotate(extraRotationX + -90, Rotate.X_AXIS));
			meshView.getTransforms().add(new Rotate(extraRotationY + 180 + 51, Rotate.Y_AXIS));
			meshView.getTransforms().add(new Rotate(extraRotationZ, Rotate.Z_AXIS));
			meshView.getTransforms().add(new Translate(movementX, movementY, movementZ));
		}
		
		// Remove binding
		subScene.widthProperty().unbind();
		subScene.heightProperty().unbind();
		
		// Set the camera settings
		PerspectiveCamera snapshotCamera = new PerspectiveCamera(true);
		snapshotCamera.setFieldOfView(45);
		snapshotCamera.setNearClip(0.2);
		snapshotCamera.setFarClip(200);
//		snapshotCamera.getTransforms().addAll(
//				new Rotate(50 + 90*2, Rotate.Y_AXIS),
//				new Rotate(-15, Rotate.X_AXIS),
//				new Translate(0, 0, -bbox.getBiggest()*2)
//				);
		
		snapshotCamera.getTransforms().addAll(
				new Rotate(-57 + 90*3, Rotate.Y_AXIS),
				new Rotate(-28.6 + 90*0, Rotate.X_AXIS),
				new Translate(0, 0, -bbox.getBiggestLength()*3.8*0.7)
				);
		
		PointLight pointLight = new PointLight(Color.gray(0.38));
		pointLight.getTransforms().addAll(snapshotCamera.getTransforms());
		
		PointLight pointLightTop = new PointLight(Color.gray(0.2));
		pointLightTop.getTransforms().addAll(new Translate(0, bbox.getLengthZ()*2, 0));
		
		PointLight pointLightBottom = new PointLight(Color.gray(0.2));
		pointLightBottom.getTransforms().addAll(new Translate(0, -bbox.getLengthZ()*2, 0));
		
		PointLight pointLightFront = new PointLight(Color.gray(0.3));
		pointLightFront.getTransforms().addAll(new Translate(0, 0, -bbox.getLengthY()*2));
		
		PointLight pointLightBack = new PointLight(Color.gray(0.2));
		pointLightBack.getTransforms().addAll(new Translate(0, 0, bbox.getLengthY()*2));
		
//		Box top = new Box(0.05, 0.05, 0.05);
//		top.setMaterial(new PhongMaterial(Color.RED));
//		top.getTransforms().addAll(new Translate(0, bbox.getLengthZ()*2, 0));
//		
//		Box bottom = new Box(0.05, 0.05, 0.05);
//		bottom.setMaterial(new PhongMaterial(Color.DARKRED));
//		bottom.getTransforms().addAll(new Translate(0, -bbox.getLengthZ()*2, 0));
		
		Box front = new Box(0.05, 0.05, 0.05);
		front.setMaterial(new PhongMaterial(Color.PALEVIOLETRED));
		front.getTransforms().addAll(new Translate(0, 0, bbox.getLengthY()*2));
		
//		group.getChildren().addAll(front);
		
//		ambientLight.setColor(Color.gray(0.8));
		
		group.getChildren().add(pointLight);
//		group.getChildren().add(pointLightTop);
//		group.getChildren().add(pointLightBottom);
//		group.getChildren().add(pointLightFront);
//		group.getChildren().add(pointLightBack);
		
		Shadow shadowEffect = new Shadow();
		shadowEffect.setBlurType(BlurType.GAUSSIAN);
		shadowEffect.setColor(Color.BLACK);
		shadowEffect.setWidth(17);
		shadowEffect.setHeight(17);

		
		int dimensions = 128;
		
		subScene.setCamera(snapshotCamera);
		subScene.setWidth(dimensions);
		subScene.setHeight(dimensions);
		axesGroup.setVisible(false);
		
		SnapshotParameters sp = new SnapshotParameters();
	    sp.setFill(Color.TRANSPARENT);
	    subScene.setFill(Color.TRANSPARENT);
	    
	    // Take 2 snapshots: one for the shadow and the other the image itself
	    BufferedImage normalSnapshot = SwingFXUtils.fromFXImage(subScene.snapshot(sp, null), null);
	    
	    subScene.setEffect(shadowEffect);
	    BufferedImage shadowSnapshot = SwingFXUtils.fromFXImage(subScene.snapshot(sp, null), null);
	    
	    BufferedImage thumbnail = new BufferedImage(dimensions, dimensions, BufferedImage.TYPE_INT_ARGB);
	    Graphics g = thumbnail.getGraphics();
	    g.drawImage(shadowSnapshot, (int) (-dimensions*0.05), (int) (-dimensions*0.05), null);
	    g.drawImage(normalSnapshot, 0, 0, null);
		
		// Save the thumbnail
		ImageIO.write(thumbnail, "png", new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\DebuggingTest\\creatureparticons~\\snapshot.png"));
		
		// Restore previous parameters
		subScene.setFill(originalFill);
		subScene.setCamera(originalCamera);
		axesGroup.setVisible(originalAxes);
		ambientLight.setLightOn(lightOn);
		subScene.setEffect(null);
		bindDimensions();
		
//		group.getChildren().remove(pointLight);
		
		for (int i = 0; i < meshes.size(); i++) {
			meshes.get(i).getTransforms().setAll(originalTransforms.get(i));
		}
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
	
	class XformCamera extends Group {
	    Point3D px = new Point3D(1.0, 0.0, 0.0);
	    Point3D py = new Point3D(0.0, 1.0, 0.0);
	    Rotate r;
	    Transform t = new Rotate();

	    public XformCamera() {
	        super();
	    }

	    public void rx(double angle) {
	        r = new Rotate(angle, px);
	        this.t = t.createConcatenation(r);
	        this.getTransforms().clear();
	        this.getTransforms().addAll(t);
	    }

	    public void ry(double angle) {
	        r = new Rotate(angle, py);
	        this.t = t.createConcatenation(r);
	        this.getTransforms().clear();
	        this.getTransforms().addAll(t);
	    }

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
		else if (object instanceof RWCompiledState) {
			UIManager.get().tryAction(() -> {
				showCompiledStateEditor(selectedName);
			}, "Error with compiled state.");
			// We don't return because we want to empty the properties container
		}
		
		propertiesContainer.setContent(null);
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
		
		ImageView viewer = new ImageView(rasterImages.get(raster));
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
					Image oldImage = rasterImages.get(raster);
					byte[] oldData = raster.textureData.data;
					
					DDSTexture texture = new DDSTexture();
					texture.read(file);
					raster.fromDDSTexture(texture);
					raster.textureData.data = texture.getData();
					
					Image image = imageFromRaster(raster);
					viewer.setImage(image);
					rasterImages.put(raster, image);
					rasterImageProperties.get(raster).set(image);
					
					addEditAction(new RWUndoableAction(name + ": Texture") {

						@Override public void undo() {
							raster.fromDDSTexture(oldTexture);
							raster.textureData.data = oldData;
							rasterImages.put(raster, oldImage);
							rasterImageProperties.get(raster).set(oldImage);
						}

						@Override public void redo() {
							raster.fromDDSTexture(texture);
							raster.textureData.data = texture.getData();
							rasterImages.put(raster, image);
							rasterImageProperties.get(raster).set(image);
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
			if (getSelectedName().equals(action.selectedObject)) {
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
