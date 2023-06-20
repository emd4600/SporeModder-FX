package sporemodder.view.editors;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import sporemodder.file.rw4.MaterialStateCompiler;
import sporemodder.file.rw4.RWBBox;
import sporemodder.file.rw4.RWBaseResource;
import sporemodder.file.rw4.RWCompiledState;
import sporemodder.file.rw4.RWIndexBuffer;
import sporemodder.file.rw4.RWMesh;
import sporemodder.file.rw4.RWMeshCompiledStateLink;
import sporemodder.file.rw4.RWMorphHandle;
import sporemodder.file.rw4.RWObject;
import sporemodder.file.rw4.RWRaster;
import sporemodder.file.rw4.RWTextureOverride;
import sporemodder.file.rw4.RWVertexBuffer;
import sporemodder.file.rw4.RWVertexElement;
import sporemodder.file.rw4.RenderWare;
import sporemodder.file.rw4.Direct3DEnums.RWDECLUSAGE;
import sporemodder.FileManager;
import sporemodder.HashManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.dds.DDSTexture;
import sporemodder.file.effects.ResourceID;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.gmdl.GameModelResource;
import sporemodder.file.raster.RasterTexture;
import sporemodder.file.rw4.RWHeader.RenderWareType;
import sporemodder.file.shaders.MaterialStateLink;
import sporemodder.file.shaders.ShaderData;
import sporemodder.util.ProjectItem;
import sporemodder.view.UserInterface;

public class GMDLModelViewer extends AbstractEditableEditor implements ItemEditor, EditHistoryEditor {
	
	private static abstract class UndoableAction implements EditHistoryAction {
		private String selectedObject;
		private String text;
		private UndoableAction(String text) {
			this.text = text;
		}
		@Override public String getText() {
			return text;
		}
	}
	
	/** The maximum amount of remembered edit history actions. */
	private static final int MAX_EDIT_HISTORY = 25;
	
	private static final UndoableAction ORIGINAL_ACTION = new UndoableAction(null) {

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
			return new GMDLModelViewer();
		}

		@Override
		public boolean isSupportedFile(ProjectItem item) {
			if (!item.isFolder() && "gmdl".equals(item.getSpecificExtension())) {
				return true;
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
	
	private double mousePosX, mousePosY, mouseOldX, mouseOldY;
	
	private Pane mainNode;
	private SubScene subScene;
	private Group group;
	
	private AmbientLight ambientLight;
	
	private Group axesGroup;
	
	private GameModelResource gmdl;
	private final List<MeshView> meshes = new ArrayList<>();
	private final Map<GameModelResource.TextureEntry, Image> textureToImage = new HashMap<>();  // with alpha removed
	private final Map<GameModelResource.TextureEntry, DDSTexture> textureToDDS = new HashMap<>();
	private final Map<GameModelResource.MaterialInfo, Integer> materialIndices = new HashMap<>();
	
	// -- Inspector -- //
	
	private final Pane inspectorPane = new VBox(5);
	private final ScrollPane propertiesContainer = new ScrollPane();
	private final TreeView<String> treeView = new TreeView<>();
	private final TreeItem<String> tiGMDL = new TreeItem<String>("Game Model Resource (GMDL)");
	private final Map<String, Object> nameMap = new HashMap<>();
	private final Map<String, TreeItem<String>> itemsMap = new HashMap<>();
	
	// For undo redo:
	private final Stack<UndoableAction> editHistory = new Stack<>();
	private int undoRedoIndex = -1;
	private boolean isUndoingAction;
	
	private GMDLModelViewer() {
		super();
		
		mainNode = new Pane();
		
		propertiesContainer.setFitToWidth(true);
    	
    	inspectorPane.getChildren().addAll(treeView, propertiesContainer);
    	VBox.setVgrow(propertiesContainer, Priority.ALWAYS);
    	
    	nameMap.put(tiGMDL.getValue(), tiGMDL);
    	treeView.setRoot(tiGMDL);
    	treeView.setShowRoot(true);
    	treeView.setMaxHeight(TREE_VIEW_HEIGHT);
    	
    	treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue != null) {
    			fillPropertiesPane(newValue, newValue.getValue());
    		} else {
    			propertiesContainer.setContent(null);
    		}
    	});
    	
    	tiGMDL.setExpanded(true);
    	treeView.getSelectionModel().select(tiGMDL);
    	
    	// Add an original action that does nothing:
    	addEditAction(ORIGINAL_ACTION);
	}
	
	private TriangleMesh processVertexBuffer(GameModelResource.Mesh mesh) throws IOException {
		GameModelResource.VertexBuffer buffer = mesh.vertexBuffer;
		
		try (MemoryStream vertexStream = new MemoryStream(buffer.data)) {
		
			// Find all the vertex components we are going to use
			RWVertexElement positionElement = null;
			RWVertexElement texcoordElement = null;
			RWVertexElement normalElement = null;
			
			for (RWVertexElement element : buffer.descriptor.elements) {
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
			
			int vertexCount = buffer.vertexCount;
			int vertexStart = 0;
			int vertexSize = buffer.data.length / vertexCount;
			
			float[] positions = new float[3 * vertexCount];
			float[] texCoords = new float[2 * vertexCount];
			float[] normals = normalElement == null ? null : new float[3 * vertexCount];
			
			for (int i = 0; i < vertexCount; i++) {
				vertexStream.seek((vertexStart+i) * vertexSize + positionElement.offset);
				positions[i * 3 + 0] = vertexStream.readLEFloat();
				positions[i * 3 + 1] = vertexStream.readLEFloat();
				positions[i * 3 + 2] = vertexStream.readLEFloat();
			}
			
			if (texcoordElement != null) {
				for (int i = 0; i < vertexCount; i++) {
					vertexStream.seek((vertexStart+i) * vertexSize + texcoordElement.offset);
					texCoords[i * 2 + 0] = vertexStream.readLEFloat();
					texCoords[i * 2 + 1] = vertexStream.readLEFloat();
				}
			}
			else {
				for (int i = 0; i < texCoords.length; i++) texCoords[i] = 0.5f;
			}
			
			if (normals != null) {
				for (int i = 0; i < vertexCount; i++) {
					vertexStream.seek((vertexStart+i) * vertexSize + normalElement.offset);
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
	
	private TriangleMesh readMesh(GameModelResource.Mesh mesh) throws IOException {
		
		TriangleMesh triangleMesh = processVertexBuffer(mesh);
		
		boolean hasNormals = triangleMesh.getNormals().size() != 0;
		
		int triangleCount = mesh.indexBuffer.indices.length / 3;
		
		int indexComponents = hasNormals ? 3 : 2;
		int[] javaIndices = new int[triangleCount * indexComponents * 3];
		for (int i = 0; i < triangleCount; i++) {
			for (int j = 0; j < 3; j++) {
				int index = mesh.indexBuffer.indices[i * 3 + j];
				javaIndices[i*indexComponents*3 + indexComponents*j] = index;
				javaIndices[i*indexComponents*3 + indexComponents*j + 1] = index;
				if (hasNormals) javaIndices[i*indexComponents*3 + indexComponents*j + 2] = index;
			}
		}
		
//			if (mesh.indexBuffer.startIndex != 0) {
//				for (int i = 0; i < indices.length; i++) {
//					indices[i] += mesh.indexBuffer.startIndex;
//				}
//			}
		
//		if (mesh.firstVertex != 0) {
//			for (int i = 0; i < indices.length; i++) {
//				indices[i] -= mesh.firstVertex;
//			}
//		}
		
		triangleMesh.getFaces().addAll(javaIndices);
		
		return triangleMesh;
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
	
	private void loadImages() throws IOException {
		for (GameModelResource.MaterialInfo materialInfo : gmdl.materialInfos) {
			for (GameModelResource.TextureEntry texture : materialInfo.textures) {
				ResourceKey key = new ResourceKey();
				key.setInstanceID(texture.instanceID);
				key.setGroupID(texture.groupID);
				key.setTypeID(0x2F4E681C);  // .raster
				String relativePath = ProjectManager.get().keyToRelativePath(key); 
				File file = ProjectManager.get().getFile(relativePath);
				if (file != null && file.exists()) {
					BufferedImage buffered = RasterTexture.textureFromFile(file).toBufferedImage();
					textureToImage.put(texture, removeAlphaChannel(buffered));
				}
				else {
					key.setTypeID(0x2F4E681B);  // .rw4
					relativePath = ProjectManager.get().keyToRelativePath(key); 
					file = ProjectManager.get().getFile(relativePath);
					if (file != null && file.exists()) {
						RenderWare rw4 = RenderWare.fromFile(file);
						if (rw4.isTexture()) {
							BufferedImage buffered = rw4.toTexture().toBufferedImage();
							textureToImage.put(texture, removeAlphaChannel(buffered));
						}
					}
				}
			}
		}
	}
	
	private void loadMaterial(GameModelResource.MaterialInfo materialInfo, MeshView meshView) throws IOException {
		PhongMaterial material = new PhongMaterial();
		meshView.setMaterial(material);
		
		if (!materialInfo.textures.isEmpty()) {
			Image diffuseImage = textureToImage.get(materialInfo.textures.get(0));
			material.setDiffuseMap(diffuseImage);
			
			if (materialInfo.textures.size() > 1) {
				Image normalImage = textureToImage.get(materialInfo.textures.get(1));
				material.setBumpMap(normalImage);
			}
		}
	}
	
	private void applyMaterials() throws IOException {
		for (int i = 0; i < meshes.size(); i++) {
			loadMaterial(gmdl.materialInfos.get(i), meshes.get(i));
		}
	}
	
	private void loadModel(ProjectItem item) throws IOException {
		this.file = item.getFile();
		this.item = item;
		try (StreamReader stream = new FileStream(item.getFile(), "r")) {
			gmdl = new GameModelResource();
			gmdl.read(stream);
			
			loadImages();
		}
		
		// We want some distance from the object
		initialCameraDistance = gmdl.boundingBox.getBiggest() * 2.0;
		cameraDistance = initialCameraDistance;
		// Update distance
		zoom(0);
		
		group = new Group();
		
		ambientLight = new AmbientLight();
		
		group.getChildren().add(ambientLight);
		
		int meshIndex = 0;
		for (GameModelResource.Mesh mesh : gmdl.meshes) {
			final int meshIndex2 = meshIndex;
			materialIndices.put(gmdl.materialInfos.get(meshIndex), meshIndex);
			UIManager.get().tryAction(() -> {
				MeshView meshView = new MeshView(readMesh(mesh));
				loadMaterial(gmdl.materialInfos.get(meshIndex2), meshView);
				
				meshView.setCullFace(CullFace.BACK);
				
				meshView.getTransforms().addAll(
						new Rotate(-90, Rotate.X_AXIS),
						new Rotate(180, Rotate.Y_AXIS));
				
				group.getChildren().add(meshView);
				meshes.add(meshView);
			}, "Cannot load mesh for this RW4 file");
			meshIndex++;
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
	
	private void fillTreeView() {
		if (gmdl != null) {
			int i = 0;
			for (GameModelResource.MaterialInfo materialInfo : gmdl.materialInfos) {
				String name = "Material Info " + i;
				
				TreeItem<String> materialInfoItem = new TreeItem<String>(name);
				materialInfoItem.setExpanded(true);
				itemsMap.put(name, materialInfoItem);
				nameMap.put(name, materialInfo);
				tiGMDL.getChildren().add(materialInfoItem);
				
				for (int j = 0; j < materialInfo.textures.size(); j++) {
					GameModelResource.TextureEntry textureEntry = materialInfo.textures.get(j);
					name = "Texture " + j;
					
					TreeItem<String> item = new TreeItem<String>(name);
					item.setExpanded(true);
					itemsMap.put(name, item);
					nameMap.put(name, textureEntry);
					materialInfoItem.getChildren().add(item);
				}
				
				i++;
			}
			
			i = 0;
			for (GameModelResource.BoneRange boneRange : gmdl.boneRanges) {
				String name = "Bone Range " + i;
				
				TreeItem<String> boneRangeItem = new TreeItem<String>(name);
				boneRangeItem.setExpanded(true);
				itemsMap.put(name, boneRangeItem);
				nameMap.put(name, boneRange);
				tiGMDL.getChildren().add(boneRangeItem);
				
				i++;
			}
			
			i = 0;
			for (GameModelResource.GameModelAnimData animData : gmdl.animDatas) {
				String name = "Animation Data " + i;
				
				TreeItem<String> animDataItem = new TreeItem<String>(name);
				animDataItem.setExpanded(true);
				itemsMap.put(name, animDataItem);
				nameMap.put(name, animData);
				tiGMDL.getChildren().add(animDataItem);
				
				for (int j = 0; j < animData.bakedDeforms.size(); j++) {
					GameModelResource.BakedDeforms deform = animData.bakedDeforms.get(j);
					name = "Baked Deform " + j;
					
					TreeItem<String> deformItem = new TreeItem<String>(name);
					deformItem.setExpanded(true);
					itemsMap.put(name, deformItem);
					nameMap.put(name, deform);
					animDataItem.getChildren().add(deformItem);
					
					for (int k = 0; k < deform.boneInfos.size(); k++) {
						GameModelResource.BakedDeformBoneInfo boneInfo = deform.boneInfos.get(j);
						name = "Bone Info " + k;
						
						TreeItem<String> boneItem = new TreeItem<String>(name);
						boneItem.setExpanded(true);
						itemsMap.put(name, boneItem);
						nameMap.put(name, boneInfo);
						deformItem.getChildren().add(boneItem);
					}
				}
				
				i++;
			}
		}
	}
	
	private void fillPropertiesPane(TreeItem<String> item, String selectedName) {
		Object object = nameMap.get(selectedName);
		
		if (object instanceof GameModelResource.TextureEntry) {
			fillTexturePane(selectedName, (GameModelResource.TextureEntry)object);
			return;
		}
		else if (object instanceof GameModelResource.MaterialInfo) {
			fillMaterialPane(selectedName, (GameModelResource.MaterialInfo)object);
			return;
		}
		else if (object instanceof GameModelResource.GameModelAnimData) {
			fillAnimDataPane(selectedName, (GameModelResource.GameModelAnimData)object);
			return;
		}
		else if (object instanceof GameModelResource.BoneRange) {
			fillBoneRangePane(selectedName, (GameModelResource.BoneRange)object);
			return;
		}
		else if (object instanceof GameModelResource.BakedDeformBoneInfo) {
			fillBoneInfoPane(selectedName, (GameModelResource.BakedDeformBoneInfo)object);
			return;
		}
		
		propertiesContainer.setContent(null);
	}
	
	private void fillTexturePane(String name, GameModelResource.TextureEntry texture) {
		Button exportButton = new Button("Export");
		Button modifyButton = new Button("Modify");
		
		HBox hbox = new HBox(5);
		hbox.getChildren().addAll(exportButton, modifyButton);
		
		ImageView viewer = new ImageView(textureToImage.get(texture));
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
				UIManager.get().tryAction(() -> textureToDDS.get(texture).write(file), "Texture could not be exported.");
			}
		});
		
		modifyButton.setOnAction(event -> {
			UIManager.get().tryAction(() -> {
				showTextureEntryEditor(name);
			}, "Error with compiled state.");
		});
	}
	
	private void fillMaterialPane(String name, GameModelResource.MaterialInfo materialInfo) {
		Button modifyButton = new Button("Modify");
		
		HBox hbox = new HBox(5);
		hbox.getChildren().addAll(modifyButton);
		
		VBox vbox = new VBox(5);
		vbox.getChildren().addAll(hbox);
		vbox.setFillWidth(true);
		
		propertiesContainer.setContent(vbox);
		
		modifyButton.setOnAction(event -> {
			UIManager.get().tryAction(() -> {
				showMaterialEditor(name);
			}, "Error with compiled state.");
		});
	}
	
	private void fillBoneRangePane(String name, GameModelResource.BoneRange boneRange) {
		ArgScriptWriter writer = new ArgScriptWriter();
		
		writer.command("boneRange").ints(boneRange.field_0, boneRange.field_4);
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setText(writer.toString());
		
		ScrollPane scrollPane = new ScrollPane(textArea);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		
		propertiesContainer.setContent(scrollPane);
	}
	
	private void fillAnimDataPane(String name, GameModelResource.GameModelAnimData animData) {
		ArgScriptWriter writer = new ArgScriptWriter();
		
		writer.command("transform");
		animData.transform.toArgScript(writer, false);
		
		writer.command("field_38");
		animData.field_38.toArgScript(writer, false);
		
		writer.command("field_70").ints(animData.field_70);
		writer.command("key").arguments(animData.key);
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setText(writer.toString());
		
		ScrollPane scrollPane = new ScrollPane(textArea);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		
		propertiesContainer.setContent(scrollPane);
	}
	
	private void fillBoneInfoPane(String name, GameModelResource.BakedDeformBoneInfo boneInfo) {
		StringBuilder sb = new StringBuilder();
		int bytesPerLine = 8;
		for (int i = 0; i < boneInfo.data.length; i++) {
			String text = Integer.toHexString(((int)boneInfo.data[i]) & 0xFF);
			if (text.length() == 1) {
				sb.append('0');
			}
			sb.append(text);
			
			if ((i + 1) % bytesPerLine == 0) sb.append('\n');
			else sb.append(' ');
		}
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setText(sb.toString());
		
		ScrollPane scrollPane = new ScrollPane(textArea);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		
		propertiesContainer.setContent(scrollPane);
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
			UserInterface.get().getInspectorPane().configureDefault("Model Viewer", "gmdl", inspectorPane);
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

	@Override
	protected void saveData() throws Exception {
		try (FileStream stream = new FileStream(getFile(), "rw")) {
			gmdl.write(stream);
			
			setIsSaved(true);
		}
	}

	@Override
	protected void restoreContents() throws Exception {
		loadModel(getItem());
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
		
		UndoableAction action = editHistory.get(undoRedoIndex);
		
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
		
		UndoableAction action = editHistory.get(undoRedoIndex);
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
	
	public void addEditAction(UndoableAction action) {
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
	
	private void showTextureEntryEditor(String name) throws IOException {
		HashManager.get().setUpdateProjectRegistry(true);
		
		GameModelResource.TextureEntry texture = (GameModelResource.TextureEntry) nameMap.get(name);
		
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		TextureEntryEditor editor = new TextureEntryEditor(dialog);
		ArgScriptWriter writer = new ArgScriptWriter();
		writer.command("textureID").arguments(HashManager.get().getFileName(texture.groupID) + "!" + HashManager.get().getFileName(texture.instanceID));
		writer.command("samplerIndex").ints(texture.samplerIndex);
		writer.command("extraData");
		for (int i = 0; i < texture.extraData.length; i++) {
			writer.arguments("0x" + Integer.toHexString(texture.extraData[i] & 0xff));
		}
		editor.loadContents(writer.toString());
		
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.APPLY);
		dialog.getDialogPane().setContent(editor);
		dialog.setTitle(name);
		
		editor.setPrefWidth(800);
		editor.setPrefHeight(600);
		
		dialog.setResizable(true);
		
		if (UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) != ButtonType.CANCEL) {
			HashManager.get().setUpdateProjectRegistry(false);
			ProjectManager.get().saveNamesRegistry();
			
			GameModelResource.TextureEntry oldData = new GameModelResource.TextureEntry();
			oldData.copy(texture);
			
			editor.processStream();
			GameModelResource.TextureEntry newData = new GameModelResource.TextureEntry();
			newData.copy(editor.textureEntry);
			
			if (!oldData.equals(newData)) {
				texture.copy(newData);
				loadImages();
				applyMaterials();
				
				addEditAction(new UndoableAction(name) {

					@Override public void undo() {
						texture.copy(oldData);
						try {
							loadImages();
							applyMaterials();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					@Override public void redo() {
						texture.copy(newData);
						try {
							loadImages();
							applyMaterials();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}
	
	private void showMaterialEditor(String name) throws IOException {
		HashManager.get().setUpdateProjectRegistry(true);
		
		int materialIndex = materialIndices.get(nameMap.get(name));
		MaterialInfoEntry materialInfo = new MaterialInfoEntry();
		materialInfo.transferFromGMDL(materialIndex);
		
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		MaterialInfoEditor editor = new MaterialInfoEditor(dialog);
		
		ArgScriptWriter writer = new ArgScriptWriter();
		writer.command("materialID").arguments(HashManager.get().getFileName(materialInfo.materialID));
		for (Map.Entry<Integer, byte[]> entry : materialInfo.shaderData.entrySet()) {
			writer.command("shaderData");
			String dataName = ShaderData.getName(entry.getKey());
			if (dataName != null) writer.arguments(dataName);
			else writer.arguments("0x" + Integer.toHexString(entry.getKey()));
			
			for (int i = 0; i < entry.getValue().length; i++) {
				writer.arguments("0x" + Integer.toHexString(entry.getValue()[i] & 0xff));
			}
		}
		editor.loadContents(writer.toString());
		
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.APPLY);
		dialog.getDialogPane().setContent(editor);
		dialog.setTitle(name);
		
		editor.setPrefWidth(800);
		editor.setPrefHeight(600);
		
		dialog.setResizable(true);
		
		if (UIManager.get().showDialog(dialog).orElse(ButtonType.CANCEL) != ButtonType.CANCEL) {
			HashManager.get().setUpdateProjectRegistry(false);
			ProjectManager.get().saveNamesRegistry();
			
			MaterialInfoEntry oldData = new MaterialInfoEntry();
			oldData.copy(materialInfo);
			
			editor.processStream();
			MaterialInfoEntry newData = new MaterialInfoEntry();
			newData.copy(editor.materialInfo);
			
			if (!oldData.equals(newData)) {
				materialInfo.copy(newData);
				materialInfo.transferToGMDL(materialIndex);
				
				addEditAction(new UndoableAction(name) {

					@Override public void undo() {
						materialInfo.copy(oldData);
						materialInfo.transferToGMDL(materialIndex);
					}

					@Override public void redo() {
						materialInfo.copy(newData);
						materialInfo.transferToGMDL(materialIndex);
					}
				});
			}
		}
	}
	
	private class TextureEntryEditor extends ArgScriptEditor<GameModelResource.TextureEntry> {
		private final GameModelResource.TextureEntry textureEntry = new GameModelResource.TextureEntry();
		private final Dialog<ButtonType> dialog;
		private boolean parsedTextureID = false;
		private boolean parsedSamplerIndex = false;
		private boolean parsedExtraData = false;
		
		public TextureEntryEditor(Dialog<ButtonType> dialog) {
			super();
			this.dialog = dialog;
			
			stream = new ArgScriptStream<GameModelResource.TextureEntry>();
			stream.setData(textureEntry);
			stream.addDefaultParsers();
			
			final ArgScriptArguments args = new ArgScriptArguments();
			stream.addParser("textureID", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					ResourceID id = new ResourceID();
					if (id.parse(args, 0)) {
						textureEntry.instanceID = id.getInstanceID();
						textureEntry.groupID = id.getGroupID();
						parsedTextureID = true;
					}
				}
			}));
			
			stream.addParser("samplerIndex", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					Number number = stream.parseInt(args, 0);
					if (number != null) {
						textureEntry.samplerIndex = number.intValue();
						parsedSamplerIndex = true;
					}
				}
			}));
			
			stream.addParser("extraData", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, textureEntry.extraData.length)) {
					for (int i = 0; i < textureEntry.extraData.length; i++) {
						textureEntry.extraData[i] = (byte)Optional.ofNullable(stream.parseInt(args, i, 0, 255)).orElse(0).intValue();
					}
					parsedExtraData = true;
				}
			}));
		}
		
		@Override protected void afterStreamParse() {
			super.afterStreamParse();
			
			Node button = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
			if (button != null && stream != null) {
				boolean disable = !stream.getErrors().isEmpty() || !parsedTextureID || !parsedSamplerIndex || !parsedExtraData;
				button.setDisable(disable);
			}
		}
		
		@Override protected void onStreamParse() {
			super.onStreamParse();
			
			parsedTextureID = false;
			parsedSamplerIndex = false;
			parsedExtraData = false;
		}
		
		public void processStream() {
			parsedTextureID = false;
			parsedSamplerIndex = false;
			parsedExtraData = false;
			stream.process(getText());
		}
	}
	
	private class MaterialInfoEntry {
		Map<Integer, byte[]> shaderData = new HashMap<>();
		int materialID;
		
		void transferToGMDL(int index) {
			gmdl.materialInfos.get(index).shaderData.clear();
			gmdl.materialInfos.get(index).shaderData.putAll(shaderData);
			gmdl.meshes.get(index).materialID = materialID;
		}
		
		void transferFromGMDL(int index) {
			shaderData.clear();
			shaderData.putAll(gmdl.materialInfos.get(index).shaderData);
			materialID = gmdl.meshes.get(index).materialID;
		}
		
		void copy(MaterialInfoEntry other) {
			shaderData.clear();
			shaderData.putAll(other.shaderData);
			materialID = other.materialID;
		}
	}
	
	private class MaterialInfoEditor extends ArgScriptEditor<MaterialInfoEntry> {
		private final MaterialInfoEntry materialInfo = new MaterialInfoEntry();
		private final Dialog<ButtonType> dialog;
		private boolean parsedMaterialID = false;
		
		public MaterialInfoEditor(Dialog<ButtonType> dialog) {
			super();
			this.dialog = dialog;
			
			stream = new ArgScriptStream<MaterialInfoEntry>();
			stream.setData(materialInfo);
			stream.addDefaultParsers();
			
			final ArgScriptArguments args = new ArgScriptArguments();
			stream.addParser("materialID", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					Number value = stream.parseFileID(args, 0);
					if (value != null) {
						materialInfo.materialID = value.intValue();
						parsedMaterialID = true;
					}
				}
			}));
			
			stream.addParser("shaderData", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1, Integer.MAX_VALUE)) {
					Integer value = ShaderData.getIndex(args.get(0), false);
					if (value == null) {
						value = stream.parseInt(args, 0);
					}
					int shaderDataIndex = Optional.ofNullable(value).orElse(0);
					byte[] data = new byte[args.size() - 1];
					for (int i = 0; i < data.length; i++) {
						data[i] = (byte)Optional.ofNullable(stream.parseInt(args, i+1, 0, 255)).orElse(0).intValue();
					}
					materialInfo.shaderData.put(shaderDataIndex, data);
				}
			}));
		}
		
		@Override protected void afterStreamParse() {
			super.afterStreamParse();
			
			Node button = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
			if (button != null && stream != null) {
				boolean disable = !stream.getErrors().isEmpty() || !parsedMaterialID;
				button.setDisable(disable);
			}
		}
		
		@Override protected void onStreamParse() {
			super.onStreamParse();
			
			parsedMaterialID = false;
			materialInfo.shaderData.clear();
		}
		
		public void processStream() {
			parsedMaterialID = false;
			materialInfo.shaderData.clear();
			stream.process(getText());
		}
	}
}
