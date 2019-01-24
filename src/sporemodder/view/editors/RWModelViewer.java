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
import java.util.List;

import javax.imageio.ImageIO;

import emord.filestructures.FileStream;
import emord.filestructures.MemoryStream;
import emord.filestructures.StreamReader;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.Shadow;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
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
import sporemodder.file.BoundingBox;
import sporemodder.file.rw4.Direct3DEnums.RWDECLUSAGE;
import sporemodder.file.rw4.MaterialStateCompiler;
import sporemodder.file.rw4.RWBBox;
import sporemodder.file.rw4.RWBaseResource;
import sporemodder.file.rw4.RWCompiledState;
import sporemodder.file.rw4.RWHeader.RenderWareType;
import sporemodder.file.rw4.RWIndexBuffer;
import sporemodder.file.rw4.RWMesh;
import sporemodder.file.rw4.RWMeshCompiledStateLink;
import sporemodder.file.rw4.RWObject;
import sporemodder.file.rw4.RWRaster;
import sporemodder.file.rw4.RWVertexBuffer;
import sporemodder.file.rw4.RWVertexElement;
import sporemodder.file.rw4.RenderWare;
import sporemodder.util.ProjectItem;

/**
 * An editor used for visualizing RenderWare models. This has a built-in texture patcher. It is called 'viewer' instead of 'editor' because
 * although some things can be modified, it's main goal is to visualize it.
 */
public class RWModelViewer implements ItemEditor {
	
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
	private final List<MeshView> meshes = new ArrayList<MeshView>();
	private final List<RWMeshCompiledStateLink> rwMeshes = new ArrayList<RWMeshCompiledStateLink>();
	
	private double mouseX;
	private double mouseY;
	private double cameraAngleX;
	private double cameraAngleY;
	
	double mousePosX, mousePosY, mouseOldX, mouseOldY, mouseDeltaX, mouseDeltaY;
	
	private RWModelViewer() {
		super();
		
		mainNode = new Pane();
	}
	
	private TriangleMesh readMesh(RWMesh mesh) throws IOException {
		
		//TODO this will not work for shape key meshes
		RWVertexBuffer buffer = mesh.vertexBuffers.get(0);
		
		try (MemoryStream vertexStream = new MemoryStream(buffer.vertexData.data);
				MemoryStream indexStream = new MemoryStream(mesh.indexBuffer.indexData.data)) {
		
			// Find all the vertex components we are going to use
			RWVertexElement positionElement = null;
			RWVertexElement texcoordElement = null;
			RWVertexElement normalElement = null;
			
			for (RWVertexElement element : buffer.vertexDescription.elements) {
				if (element.typeCode == RWDECLUSAGE.POSITION) {
					positionElement = element;
				}
				else if (element.typeCode == RWDECLUSAGE.TEXCOORD) {
					texcoordElement = element;
				}
				else if (element.typeCode == RWDECLUSAGE.NORMAL) {
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
			
			// Now do the indices
			int indexComponents = normals == null ? 2 : 3;
			int[] indices = new int[mesh.triangleCount * indexComponents * 3];
			indexStream.seek(mesh.firstIndex * 2);
			for (int i = 0; i < mesh.triangleCount; i++) {
				for (int j = 0; j < 3; j++) {
					int index = indexStream.readLEUShort();
					indices[i*indexComponents*3 + indexComponents*j] = index;
					indices[i*indexComponents*3 + indexComponents*j + 1] = index;
					if (normals != null) indices[i*indexComponents*3 + indexComponents*j + 2] = index;
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
			
			// Finally, create the JavaFX mesh
			
			TriangleMesh triangleMesh = new TriangleMesh();
			triangleMesh.setVertexFormat(normals == null ? VertexFormat.POINT_TEXCOORD : VertexFormat.POINT_NORMAL_TEXCOORD);
			triangleMesh.getPoints().addAll(positions);
			triangleMesh.getTexCoords().addAll(texCoords);
			
			if (normals != null) triangleMesh.getNormals().addAll(normals);
			
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
	
	private void loadMaterial(RWCompiledState compiledState, MeshView meshView) throws IOException {
		MaterialStateCompiler state = compiledState.data;
		state.decompile();
		
		PhongMaterial material = new PhongMaterial();
		meshView.setMaterial(material);
		
//		material.setDiffuseColor(new Color(state.materialColor.getR(), state.materialColor.getG(), state.materialColor.getB(), state.materialColor.getA()));
		
		if (!state.textureSlots.isEmpty()) {
			RWObject diffuseRaster = state.textureSlots.get(0).raster;
			if (diffuseRaster != null) {
				Image image = ((RWRaster) diffuseRaster).toJavaFX();
				
				// Special case: SkinPaint parts use the alpha channel not for transparency, but for tinting,
				// so we want to remove the transparency:
//				if (state.rendererID == 0x80000004) {
//					image = removeAlphaChannel(image);
//				}
				
				// Note: apparently not only SkinPaints. Most cases will use this.
				image = removeAlphaChannel(image);
				
				material.setDiffuseMap(image);
			}
			
			if (state.textureSlots.size() > 1) {
				RWObject normalRaster = state.textureSlots.get(1).raster;
				if (normalRaster != null) {
					material.setBumpMap(((RWRaster) normalRaster).toJavaFX());
				}
			}
		}
	}
	
	private void loadModel(ProjectItem item) throws IOException {
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
		}
		
		rwMeshes.addAll(renderWare.getObjects(RWMeshCompiledStateLink.class));
		
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
			MeshView meshView = new MeshView(readMesh(rwLink.mesh));
			loadMaterial(rwLink.compiledStates.get(0), meshView);
			
			meshView.setCullFace(CullFace.BACK);
//			meshView.setDrawMode(DrawMode.LINE);
			
			meshView.getTransforms().addAll(
					new Rotate(-90, Rotate.X_AXIS),
					new Rotate(180, Rotate.Y_AXIS));
			
			group.getChildren().add(meshView);
			meshes.add(meshView);
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
			loadModel(item);
		}
	}

	@Override
	public void setActive(boolean isActive) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Node getUI() {
		return mainNode;
	}

	@Override
	public void save() {
	}

	@Override
	public boolean isEditable() {
		// RWs are not editable.... yet
		return false;
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
	public void setDestinationFile(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supportsSearching() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsEditHistory() {
		// TODO Auto-generated method stub
		return false;
	}
}
