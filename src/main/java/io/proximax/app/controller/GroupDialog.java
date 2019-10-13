package io.proximax.app.controller;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTimePicker;
import de.slackspace.openkeepass.domain.Meta;
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
import javafx.scene.image.ImageView;

/**
 *
 * @author thcao
 */
public class GroupDialog extends AbstractController {

    @FXML
    private Label titleLbl;
    @FXML
    private Label descLbl;
    @FXML
    private JFXTextArea notesArea;
    @FXML
    private JFXTextField nameField;
    @FXML
    private Button iconBtn;
    @FXML
    private CheckBox expireCheck;
    @FXML
    private JFXDatePicker expiryDate;
    @FXML
    private JFXTimePicker expiryTime;
    @FXML
    private ImageView iconImv;

    private GroupProperty group = null;

    private GroupProperty parent = null;

    private boolean newGroup = true;

    public GroupDialog(GroupProperty group, boolean newGroup) {
        super(true);
        if (newGroup) {
            this.parent = group;
        } else {
            this.group = group;
            this.parent = group.getParent();
        }
        this.newGroup = newGroup;
    }

    public GroupDialog(GroupProperty group) {
        super(true);
        this.group = group;
        this.parent = group.getParent();
        this.newGroup = false;
    }

    @Override
    protected void initialize() {
        if (!newGroup) {
            iconImv.setId("groupedit-img");
            titleLbl.setText("EDIT GROUP");
            descLbl.setText("Edit properties of selected group");
            nameField.setText(group.getName());
            if (group.getCustomIconUuid() == null) {
                iconCell = new IconData(group.getIconId());
            } else {
                iconCell = new IconData(group.getCustomIconUuid(), group.getIconData());
            }
            if(group.getTimes()!= null){
            if (!group.getTimes().expires()) {
                expireCheck.setSelected(false);
                expiryDate.setValue(LocalDate.now());
                expiryTime.setValue(LocalTime.now());
            } else {
                expiryDate.setValue(DateTimeUtils.calendar2LocalDate(group.getTimes().getExpiryTime()));
                expiryTime.setValue(DateTimeUtils.calendar2LocalTime(group.getTimes().getExpiryTime()));
                expireCheck.setSelected(true);
            }
            }
        } else {
            iconCell = new IconData(CONST.GROUP_ICON);
            expiryDate.setValue(LocalDate.now());
            expiryTime.setValue(LocalTime.now());
        }
        updateIcon();
        expiryDate.disableProperty().bind(expireCheck.selectedProperty().not());
        expiryTime.disableProperty().bind(expireCheck.selectedProperty().not());
        nameField.requestFocus();
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
    protected void saveBtn(ActionEvent event) {
        String str = nameField.getText();
        if (StringUtils.isEmpty(str)) {
            ErrorDialog.showError(this, "Group name cannot empty");
            nameField.requestFocus();
            return;
        }
        if (newGroup) {
            group = new GroupProperty(parent);
            group.setName(str);
            if (expireCheck.isSelected()) {
                group.setExpiryDate(expireCheck.isSelected(), DateTimeUtils.localDateTime2Calendar(expiryDate.getValue(), expiryTime.getValue()));
            }
            parent.addGroup(group);
        } else {
            group.setName(str);
            group.setExpiryDate(expireCheck.isSelected(), DateTimeUtils.localDateTime2Calendar(expiryDate.getValue(), expiryTime.getValue()));
        }
        if (iconCell.iconUUID != null) {
            group.setCustomIconUuid(iconCell.iconUUID);
            group.setIconData(iconCell.data);
        } else {
            group.setIconId(iconCell.iconId);
        }
        setButtonType(ButtonType.OK);
        close();
    }

    @Override
    protected void dispose() {
    }

    @Override
    public String getTitle() {
        return CONST.GROUP_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.GROUP_FXML;
    }

}
