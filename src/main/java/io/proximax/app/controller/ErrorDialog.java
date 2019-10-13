package io.proximax.app.controller;

import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.CONST;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 *
 * @author thcao
 */
public class ErrorDialog extends AbstractController {

    @FXML
    private Label titleLbl;
    @FXML
    private Label msgLbl;

    private String title;
    private StringProperty msgProperty = new SimpleStringProperty();

    public ErrorDialog() {
        super(true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        msgLbl.textProperty().bind(msgProperty);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContentText(String msg) {
        msgProperty.set(msg);
    }

    @Override
    protected void dispose() {
    }

    @Override
    public String getTitle() {
        return CONST.ERRORDLG_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.ERRORDLG_FXML;
    }

    public static void showError(AbstractController parent, String msg) {
        try {
            ErrorDialog dlg = new ErrorDialog();
            dlg.setContentText(msg);
            dlg.setParent(parent);
            dlg.openWindow();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void showErrorFX(AbstractController parent, String msg) {
        IApp.runSafe(() -> {
            showError(parent, msg);
        });
    }

}
