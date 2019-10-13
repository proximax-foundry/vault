package io.proximax.app.controller;

import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.StringUtils;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public abstract class AbstractController implements Initializable, EventHandler {

    @FXML
    protected Pane shadowPane; //shadow dialog
    @FXML
    protected Pane mainPane; //main pane    
    protected Stage primaryStage = null;
    protected Scene scene = null;
    protected AbstractController parentWindow = null;
    protected boolean resizable = true;
    protected boolean modal = false;
    protected IApp mainApp = CONST.IAPP;

    private double xOffset = 0;
    private double yOffset = 0;

    private ButtonType buttonType;

    protected abstract void dispose();

    protected void onClosing(Event event) {
        dispose();
    }

    public IApp getMainApp() {
        return mainApp;
    }

    public AbstractController(boolean modal) {
        this.modal = modal;

    }

    public void setParent(AbstractController parentWindow) {
        this.parentWindow = parentWindow;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public AbstractController getParent() {
        return parentWindow;
    }

    public void setModal(boolean modal) {
        this.modal = modal;
    }

    public void openWindow(AbstractController parent) throws IOException {
        setParent(parent);
        openWindow();
    }

    public void openWindow() throws IOException {
        if (getParent() != null && modal) {
            openWindow(getParent().getStage(), getTitle(), getFXML());
        } else {
            openWindow(null, getTitle(), getFXML());
        }
    }

    public Stage getStage() {
        return primaryStage;
    }

    private void openWindow(Stage stage, String title, String fxml) throws IOException {
        if (stage == null) {
            stage = mainApp.getPrimaryStage();
        }
        if (modal) {
            primaryStage = new Stage();
            primaryStage.initModality(Modality.WINDOW_MODAL);
            primaryStage.initOwner(stage);
        } else {
            primaryStage = stage ;//new Stage();
        }
        scene = createScene(fxml);
        String themeUrl = getStylesheet();
        if (!scene.getStylesheets().contains(themeUrl)) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(themeUrl);
        }
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setResizable(resizable);
        primaryStage.setOnCloseRequest(this);
        setIcon();

        getMainPane().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        getMainPane().setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });
        if (shadowPane != null) {
            scene.setFill(Color.TRANSPARENT);
            primaryStage.initStyle(StageStyle.TRANSPARENT);
        }
        initialize();
        //keyboard
        scene.getAccelerators().put(new KeyCodeCombination(
                KeyCode.F1), (Runnable) () -> {
            aboutUs(null);
        });
        if (modal) {
            primaryStage.showAndWait();
        } else {
            primaryStage.show();
        }

    }

    public void reloadTheme() {
        String themeUrl = getStylesheet();
        if (!scene.getStylesheets().contains(themeUrl)) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(themeUrl);
        }
    }

    protected String getStylesheet() {
        return mainApp.getCurrentThemeUrl();
    }

    private void setIcon() {
        if (getMainApp() != null && getStage() != null) {
            getStage().getIcons().add(getMainApp().getIcon());
        }
    }

    public abstract String getTitle();

    public abstract String getFXML();

    protected void initialize() {
        //init private data
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    protected Scene createScene(String fxml) throws IOException {
        FXMLLoader loader = loadXML(fxml);
        loader.setController(this);
        Parent root = loader.load();
        return new Scene(root);
    }

    protected FXMLLoader loadXML(String fxml) throws IOException {
        URL url = null;
        if (fxml.startsWith(CONST.FXML_PATH)) {
            url = getClass().getResource(fxml);
        } else {
            String os = IApp.getOS();
            if (!StringUtils.isEmpty(os)) {
                url = getClass().getResource(CONST.FXML_PATH + os + "/" + fxml);
            }
        }
        if (url == null) { //by default
            url = getClass().getResource(CONST.FXML_PATH + fxml);
        }
        return new FXMLLoader(url);
    }

    protected void hide() {
        primaryStage.hide();
    }

    protected void show() {
        primaryStage.show();
    }

    protected void close() {
        if (canExit()) {
            dispose();
            primaryStage.close();
        }
    }

    @Override
    public void handle(Event event) {
        if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
            if (canExit()) {
                buttonType = ButtonType.CLOSE;
                onClosing(event);
            } else {
                event.consume();
            }
        }
    }

    protected boolean canExit() {
        return true;
    }

    public void showParent() {
        if (parentWindow != null) {
            parentWindow.show();
        }
    }

    public void setMinimize() {
        primaryStage.setIconified(true);
    }

    public void setMaximize() {
        if (primaryStage.isMaximized()) {
            primaryStage.setMaximized(false);
        } else {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(primaryScreenBounds.getMinX());
            primaryStage.setY(primaryScreenBounds.getMinY());
            primaryStage.setWidth(primaryScreenBounds.getWidth());
            primaryStage.setHeight(primaryScreenBounds.getHeight());
            primaryStage.setMaximized(true);
        }
    }

    private Pane getMainPane() {
        if (shadowPane != null) {
            return shadowPane;
        }
        return mainPane;
    }

    @FXML
    protected void closeBtn(ActionEvent event) {
        if (canExit()) {
            buttonType = ButtonType.CLOSE;
            onClosing(event);
            close();
        }
    }

    @FXML
    protected void maximizeBtn(ActionEvent event) {
        setMaximize();
    }

    @FXML
    protected void minimizeBtn(ActionEvent event) {
        //setMinimize();
        addAppToTray();
        primaryStage.hide();
    }

    private TrayIcon trayIcon = null;

    protected void addAppToTray() {
        try {
            if (trayIcon == null) {
                PopupMenu menu = new PopupMenu();
                MenuItem restoreMenu = new MenuItem("Open Window");
                BufferedImage img = SwingFXUtils.fromFXImage(getMainApp().getIcon16(), null);
                trayIcon = new TrayIcon(img, CONST.APP_NAME, menu);                
                SystemTray.getSystemTray().add(trayIcon);
                menu.add(restoreMenu);
                restoreMenu.addActionListener(e -> {
                    Platform.runLater(() -> {
                        try {
                            primaryStage.show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                });                
            }
//            restoreMenu.addActionListener(e -> {
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        //primaryStage.setMaximized(false);
//                        getMainApp().getPrimaryStage().setMaximized(false);
//                        getMainApp().getPrimaryStage().toFront();
//                        SystemTray.getSystemTray().remove(trayIcon);
//                    }
//                });
//            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void aboutUs(ActionEvent event) {
        AboutUsDialog.showAboutSafe(this);
    }

    public ButtonType getResultType() {
        return buttonType;
    }

    public void setButtonType(ButtonType buttonType) {
        this.buttonType = buttonType;
    }

}
