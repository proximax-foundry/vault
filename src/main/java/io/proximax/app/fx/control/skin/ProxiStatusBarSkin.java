package io.proximax.app.fx.control.skin;

/**
 *
 * @author thcao
 */
import com.jfoenix.controls.JFXComboBox;
import javafx.beans.Observable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import io.proximax.app.fx.control.ProxiStatusBar;
import javafx.beans.binding.When;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ProxiStatusBarSkin extends SkinBase<ProxiStatusBar> {

    private HBox leftBox;
    private HBox rightBox;
    private Label statusLbl;
    private JFXComboBox<String> nodeCbx;
    private ProgressBar progressBar;
    private Label nodeLbl;
    private ImageView imageStatus;

    public ProxiStatusBarSkin(ProxiStatusBar statusBar) {
        super(statusBar);
        leftBox = new HBox();
        leftBox.getStyleClass().add("left-items");

        rightBox = new HBox();
        rightBox.getStyleClass().add("right-items");

        progressBar = new ProgressBar();
        progressBar.setPrefHeight(20.0);
        progressBar.progressProperty().bind(statusBar.progressProperty());

        statusLbl = new Label();
        statusLbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        statusLbl.textProperty().bind(statusBar.textProperty());
        statusLbl.graphicProperty().bind(statusBar.graphicProperty());
        statusLbl.getStyleClass().add("status-label");

        imageStatus = new ImageView();
        imageStatus.setId("status-image");
        imageStatus.imageProperty().bind(
                new When(statusBar.imageStatusBooleanProperty()).
                        then(statusBar.getImageSuccess()).
                        otherwise(statusBar.getImageFailed()));
        imageStatus.addEventHandler(MouseEvent.MOUSE_CLICKED, statusBar.getEventHandler());

        nodeLbl = new Label(" Node: ");
        nodeLbl.getStyleClass().add("status-label");

        nodeCbx = new JFXComboBox<String>();
        nodeCbx.setId("status-nodes");
        nodeCbx.getStyleClass().add("status-combox");
        nodeCbx.itemsProperty().bind(statusBar.nodeItemsProperty());
        nodeCbx.valueProperty().bind(statusBar.nodeSelectedProperty());
        nodeCbx.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            statusBar.setNodeSelected((String) newValue);
            statusBar.getEventHandler().handle(new Event(nodeCbx, nodeCbx, ActionEvent.ACTION));
        });
        leftBox.setAlignment(Pos.CENTER);
        leftBox.getChildren().setAll(getSkinnable().getLeftItems());
        rightBox.getChildren().setAll(getSkinnable().getRightItems());
        statusBar.getLeftItems().addListener(
                (Observable evt) -> leftBox.getChildren().setAll(
                        getSkinnable().getLeftItems()));
        statusBar.getRightItems().addListener(
                (Observable evt) -> rightBox.getChildren().setAll(
                        getSkinnable().getRightItems()));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(4);
        GridPane.setFillHeight(leftBox, true);
        GridPane.setFillHeight(rightBox, true);
        GridPane.setFillHeight(nodeLbl, true);
        GridPane.setFillHeight(nodeCbx, true);
        GridPane.setFillHeight(statusLbl, true);
        GridPane.setFillHeight(progressBar, true);

        GridPane.setVgrow(leftBox, Priority.ALWAYS);
        GridPane.setVgrow(rightBox, Priority.ALWAYS);
        GridPane.setVgrow(nodeLbl, Priority.ALWAYS);
        GridPane.setVgrow(nodeCbx, Priority.ALWAYS);
        GridPane.setVgrow(statusLbl, Priority.ALWAYS);
        GridPane.setVgrow(progressBar, Priority.ALWAYS);

        GridPane.setHgrow(statusLbl, Priority.ALWAYS);

        gridPane.add(leftBox, 0, 0);
        gridPane.add(statusLbl, 1, 0);
        gridPane.add(imageStatus, 2, 0);
        gridPane.add(nodeLbl, 3, 0);
        gridPane.add(nodeCbx, 4, 0);
        gridPane.add(progressBar, 5, 0);
        gridPane.add(rightBox, 7, 0);
        getChildren().add(gridPane);
    }
}
