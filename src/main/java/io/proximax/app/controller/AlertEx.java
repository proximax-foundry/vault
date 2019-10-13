package io.proximax.app.controller;

import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.CONST;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 *
 * @author thcao
 */
public class AlertEx extends Alert {

    protected IApp mainApp = CONST.IAPP;

    public AlertEx(AlertType alertType, String contentText, ButtonType... buttons) {
        super(alertType, contentText, buttons);
    }

    public AlertEx(AlertType alertType) {
        super(alertType);
    }

    public void setTitleEx(String title) {
        setTitle(title);
        ((Stage) getDialogPane().getScene().getWindow()).getIcons().add(mainApp.getIcon());
    }
}
