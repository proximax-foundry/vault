package io.proximax.app.controller;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTimePicker;
import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.DateTimeUtils;
import io.proximax.app.utils.StringUtils;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;

/**
 *
 * @author thcao
 */
public class EntryDialog extends AbstractController {

    @FXML
    private Label titleLbl;
    @FXML
    private JFXTextField titleField;
    @FXML
    private JFXTextField usernameField;
    @FXML
    private JFXPasswordField passwordField;
    @FXML
    private JFXTextField passField;
    @FXML
    private ToggleButton viewBtn;
    @FXML
    private JFXPasswordField repeatField;
    @FXML
    private ProgressBar qualityProgress;
    @FXML
    private JFXTextField urlField;
    @FXML
    private JFXTextArea notesArea;
    @FXML
    private CheckBox expiryCheck;
    @FXML
    private JFXDatePicker expiryDate;
    @FXML
    private JFXTimePicker expiryTime;
    @FXML
    private Button iconBtn;
    @FXML
    private ImageView iconImv;

    private EntryProperty entry = null;
    private GroupProperty parent = null;

    public EntryDialog(EntryProperty entry) {
        super(true);
        this.entry = entry;
        this.parent = entry.getParent();
    }

    public EntryDialog(GroupProperty parent) {
        super(true);
        this.parent = parent;
    }

    @Override
    protected void initialize() {
        passField.setManaged(false);
        passField.setVisible(false);
        passField.managedProperty().bind(viewBtn.selectedProperty());
        passField.visibleProperty().bind(viewBtn.selectedProperty());
        passwordField.managedProperty().bind(viewBtn.selectedProperty().not());
        passwordField.visibleProperty().bind(viewBtn.selectedProperty().not());
        passField.textProperty().bindBidirectional(passwordField.textProperty());
        repeatField.disableProperty().bind(viewBtn.selectedProperty());
        if (entry == null) {
            iconCell = new IconData(CONST.ENTRY_ICON);
            expiryDate.setValue(LocalDate.now());
            expiryTime.setValue(LocalTime.now());
        } else {
            titleLbl.setText("EDIT ENTRY");
            iconImv.setId("entryedit-img");
            titleField.setText(entry.getTitle());
            if (entry.getCustomIconUUID() == null) {
                iconCell = new IconData(entry.getIconId());
            } else {
                iconCell = new IconData(entry.getCustomIconUUID(), entry.getIconData());
            }
            usernameField.setText(entry.getUsername());
            passwordField.setText(entry.getPassword());
            repeatField.setText(entry.getPassword());
            urlField.setText(entry.getUrl());
            notesArea.setText(entry.getNotes());
            if (!entry.getTimes().expires()) {
                expiryCheck.setSelected(false);
                expiryDate.setValue(LocalDate.now());
                expiryTime.setValue(LocalTime.now());
            } else {
                expiryDate.setValue(DateTimeUtils.calendar2LocalDate(entry.getTimes().getExpiryTime()));
                expiryTime.setValue(DateTimeUtils.calendar2LocalTime(entry.getTimes().getExpiryTime()));
                expiryCheck.setSelected(true);
            }
        }
        updateIcon();
        expiryDate.disableProperty().bind(expiryCheck.selectedProperty().not());
        expiryTime.disableProperty().bind(expiryCheck.selectedProperty().not());
        qualityProgress.setProgress(0.0);
    }

    private void updateIcon() {
        ImageView ivDf = IApp.createImageViewFromIconData(getClass().getResourceAsStream(String.format(CONST.ICON_PATH, CONST.ENTRY_ICON)), 20, 20);
        iconBtn.setGraphic(IApp.createImageViewFromIconData(iconCell.data, ivDf));
    }

    IconData iconCell;

    @FXML
    protected void iconPicker(ActionEvent event) {
        try {
            IconDialog dlg = new IconDialog(iconCell);
            dlg.setHandle(parent.getHandle());
            dlg.openWindow(this);
            if (dlg.getResultType() == ButtonType.OK) {
                iconCell = dlg.getReturnIcon();
                updateIcon();
            }
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

    @FXML
    protected void generateBtn(ActionEvent event) {
        try {
            PasswordDialog dlg = new PasswordDialog();
            dlg.openWindow(this);
            if (dlg.getResultType()==ButtonType.OK) {
                passwordField.setText(dlg.passwords);
                repeatField.setText(dlg.passwords);
            }
        } catch (Exception ex) {
        }
    }
    
    @FXML
    protected void filterBtn(ActionEvent event) {
    	
    }
    
    
    @FXML
    protected void saveBtn(ActionEvent event) {
        String title = titleField.getText();
        if (StringUtils.isEmpty(title)) {
            ErrorDialog.showError(this, "Title cannot be empty");
            titleField.requestFocus();
            return;
        }
        String username = usernameField.getText();
        if (StringUtils.isEmpty(username)) {
            ErrorDialog.showError(this, "Username cannot be empty");
            usernameField.requestFocus();
            return;
        }
        String password = passwordField.getText();
        if (StringUtils.isEmpty(password)) {
            ErrorDialog.showError(this, "Password cannot be empty");
            passwordField.requestFocus();
            return;
        }
        if (!viewBtn.isSelected()) {
            String confirm = repeatField.getText();
            if (password == null ? confirm != null : !password.equals(confirm)) {
                ErrorDialog.showError(this, "Password and repeated password aren't identical");
                passwordField.requestFocus();
                return;
            }
        }
        if (entry == null) {
            entry = new EntryProperty(parent);
            if (expiryCheck.isSelected()) {
                entry.setExpiryDate(expiryCheck.isSelected(), DateTimeUtils.localDateTime2Calendar(expiryDate.getValue(), expiryTime.getValue()));
            }
            parent.addEntry(entry);
        } else {
            entry.setExpiryDate(expiryCheck.isSelected(), DateTimeUtils.localDateTime2Calendar(expiryDate.getValue(), expiryTime.getValue()));
        }
        entry.setTitle(title);
        entry.setUsername(username);
        entry.setPassword(password);
        entry.setUrl(urlField.getText());
        entry.setNotes(notesArea.getText());
        if (iconCell.iconUUID != null) {
            entry.setCustomIconUUID(iconCell.iconUUID);
            entry.setIconData(iconCell.data);
        } else {
            entry.setIconId(iconCell.iconId);
        }
        setButtonType(ButtonType.OK);
        close();
    }

    @Override
    protected void dispose() {
    }

    @Override
    public String getTitle() {
        return CONST.ENTRY_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.ENTRY_FXML;
    }

}
