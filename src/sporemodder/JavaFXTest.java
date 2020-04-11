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
package sporemodder;

import java.io.File;
import java.net.MalformedURLException;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import sporemodder.file.dbpf.DBPFUnpackingTask;

public class JavaFXTest extends Application {
	
	final Group root = new Group();
    final XformWorld world = new XformWorld();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final XformCamera cameraXform = new XformCamera();
    private static final double CAMERA_INITIAL_DISTANCE = -1000;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    double mousePosX, mousePosY, mouseOldX, mouseOldY, mouseDeltaX, mouseDeltaY;
    double mouseFactorX, mouseFactorY;
    
    // DOn't forget to remove this file, like wtf is this doing here? xD
    private int remove;

//    @Override
//    public void start(Stage primaryStage) {
//        root.getChildren().add(world);
//        root.setDepthTest(DepthTest.ENABLE);
//        buildCamera();
//        buildBodySystem();
//        Scene scene = new Scene(root, 800, 600, true);
//        scene.setFill(Color.GREY);
//        handleMouse(scene);
//        primaryStage.setTitle("TrafoTest");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//        scene.setCamera(camera);
//        mouseFactorX = 180.0 / scene.getWidth();
//        mouseFactorY = 180.0 / scene.getHeight();
//    }
    
    private final ObjectProperty<Color> sceneColorProperty = new SimpleObjectProperty<>(Color.RED);

//    @Override
//    public void start(Stage primaryStage) throws MalformedURLException {
//
////        Rectangle rect = new Rectangle(400,400);
////        rect.fillProperty().bind(sceneColorProperty);
////
////        Scene scene = new Scene(new StackPane(rect), 400, 400);
////        scene.getStylesheets().add(new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Styles\\Default\\color-swatch.css").toURI().toURL().toExternalForm());
////        scene.setOnMouseClicked(e->{
////            if(e.getButton().equals(MouseButton.SECONDARY)){
////                ColorSwatchUI myCustomColorPicker = new ColorSwatchUI();
////                myCustomColorPicker.setCurrentColor(sceneColorProperty.get());
////
////                CustomMenuItem itemColor = new CustomMenuItem(myCustomColorPicker);
////                itemColor.setHideOnClick(false);
////                sceneColorProperty.bind(myCustomColorPicker.customColorProperty());
////                ContextMenu contextMenu = new ContextMenu(itemColor);
////                contextMenu.setOnHiding(t->sceneColorProperty.unbind());
////                contextMenu.show(scene.getWindow(),e.getScreenX(),e.getScreenY());
////            }
////        });
//
//        primaryStage.setTitle("Custom Color Selector");
//        //primaryStage.setScene(scene);
//        primaryStage.show();
//        
//        MainApp.testInit();
//		
//		String path = "C:\\Users\\Eric\\Downloads\\!!!!!!!!!!!!!!!!_DroneParts_2017_PublicRelease_1_2.package";
//		String projectPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\!!!!!!!!!!!!!!!!_DroneParts_2017_PublicRelease_1_2";
//		
//		File projectFolder = new File(projectPath);
//		
//		FileManager.get().deleteDirectory(projectFolder);
//		projectFolder.mkdir();
//		DBPFUnpackingTask oldTask = new DBPFUnpackingTask(new File(path), projectFolder, null, FormatManager.get().getConverters());
//		
//		long time = System.currentTimeMillis();
//		oldTask.run();
//		while (oldTask.isRunning());
//		time = System.currentTimeMillis() - time;
//		
//		System.out.println("old time: " + time);
//		
//		FileManager.get().deleteDirectory(projectFolder);
//		projectFolder.mkdir();
//		DBPFUnpackingTask newTask = new DBPFUnpackingTask(new File(path), projectFolder, null, FormatManager.get().getConverters());
//		
//		time = System.currentTimeMillis();
//		newTask.run();
//		time = System.currentTimeMillis() - time;
//		
//		System.out.println("new time: " + time);
//    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(camera);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
    }

    private void buildBodySystem() {
        PhongMaterial whiteMaterial = new PhongMaterial();
        whiteMaterial.setDiffuseColor(Color.WHITE);
        whiteMaterial.setSpecularColor(Color.LIGHTBLUE);
        Box box = new Box(400, 200, 100);
        box.setMaterial(whiteMaterial);
        PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        Sphere sphere = new Sphere(5);
        sphere.setMaterial(redMaterial);
        sphere.setTranslateX(200.0);
        sphere.setTranslateY(-100.0);
        sphere.setTranslateZ(-50.0);
        world.getChildren().addAll(box);
        world.getChildren().addAll(sphere);
    }

    private void handleMouse(Scene scene) {
        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);
            if (me.isPrimaryButtonDown()) {
                cameraXform.ry(mouseDeltaX * 180.0 / scene.getWidth());
                cameraXform.rx(-mouseDeltaY * 180.0 / scene.getHeight());
            } else if (me.isSecondaryButtonDown()) {
                camera.setTranslateZ(camera.getTranslateZ() + mouseDeltaY);
            }
        });
    }

//    public static void main(String[] args) {
//        launch(args);
//    }
    
    private Parent createContent() throws Exception {
        Sphere sphere = new Sphere(2.5);
        sphere.setMaterial(new PhongMaterial(Color.FORESTGREEN));

        sphere.setTranslateZ(7);
        sphere.setTranslateX(2);

        Box box = new Box(5, 5, 5);
        box.setMaterial(new PhongMaterial(Color.RED));

        Translate pivot = new Translate();
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);

        // Create and position camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll (
                pivot,
                yRotate,
                new Rotate(-20, Rotate.X_AXIS),
                new Translate(0, 0, -50)
        );

        // animate the camera position.
        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(0), 
                        new KeyValue(yRotate.angleProperty(), 0)
                ),
                new KeyFrame(
                        Duration.seconds(15), 
                        new KeyValue(yRotate.angleProperty(), 360)
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Build the Scene Graph
        Group root = new Group();       
        root.getChildren().add(camera);
        root.getChildren().add(box);
        root.getChildren().add(sphere);

        // set the pivot for the camera position animation base upon mouse clicks on objects
        root.getChildren().stream()
                .filter(node -> !(node instanceof Camera))
                .forEach(node ->
                        node.setOnMouseClicked(event -> {
                            pivot.setX(node.getTranslateX());
                            pivot.setY(node.getTranslateY());
                            pivot.setZ(node.getTranslateZ());
                        })
                );

        // Use a SubScene
        SubScene subScene = new SubScene(
                root,
                300,300,
                true,
                SceneAntialiasing.BALANCED
        );
        subScene.setFill(Color.ALICEBLUE);
        subScene.setCamera(camera);
        Group group = new Group();
        group.getChildren().add(subScene);

        return group;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setResizable(false);
        Scene scene = new Scene(createContent());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    class XformWorld extends Group {
        final Translate t = new Translate(0.0, 0.0, 0.0);
        final Rotate rx = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
        final Rotate ry = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
        final Rotate rz = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);

        public XformWorld() {
            super();
            this.getTransforms().addAll(t, rx, ry, rz);
        }
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
}
