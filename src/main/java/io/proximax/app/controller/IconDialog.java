package io.proximax.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import de.slackspace.openkeepass.domain.CustomIcon;
import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.CONST;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

/**
 *
 * @author thcao
 */
public class IconDialog extends AbstractController {

    @FXML
    private GridView<IconData> standardLv;

    @FXML
    private GridView<IconData> customLv;

    @FXML
    private JFXRadioButton standardChk;

    @FXML
    private JFXRadioButton customChk;

    @FXML
    private ToggleGroup toggleCheck;

    private ProxiKeePassImpl handle;

    final private int MIN_ICONID = 0;
    final private int MAX_ICONID = 68;
    ObservableList<IconData> standardImgs = FXCollections.<IconData>observableArrayList();
    ObservableList<IconData> customImgs = FXCollections.<IconData>observableArrayList();
    private IconData iconCell;

    @FXML
    private JFXButton btnAdd;
    @FXML
    private JFXButton btnRemove;

    public IconDialog(IconData iconCell) {
        super(true);
        this.iconCell = iconCell;
    }

    public void setHandle(ProxiKeePassImpl handle) {
        this.handle = handle;
    }

    @Override
    protected void initialize() {
        standardLv.disableProperty().bind(standardChk.selectedProperty().not());
        customLv.disableProperty().bind(standardChk.selectedProperty());
        if (iconCell.iconUUID == null) {
            standardChk.setSelected(true);
            btnAdd.setDisable(true);
            btnRemove.setDisable(true);
        } else {
            customChk.setSelected(true);
            btnAdd.setDisable(false);
            btnRemove.setDisable(false);
        }
        standardLv.setItems(standardImgs);
        standardLv.setCellWidth(20.0);
        standardLv.setCellHeight(20.0);
        ToggleGroup toggle1Group = new ToggleGroup();
        standardLv.setCellFactory(new Callback<GridView<IconData>, GridCell<IconData>>() {
            @Override
            public GridCell<IconData> call(GridView<IconData> arg0) {
                return new IconGridCell(toggle1Group);
            }
        });
        for (int i = MIN_ICONID; i <= MAX_ICONID; i++) {
            standardImgs.add(new IconData(i));
        }
        customLv.setItems(customImgs);
        customLv.setCellWidth(19.0);
        customLv.setCellHeight(19.0);

        //customLv.setCellWidth(20.0);
        //customLv.setCellHeight(20.0);
        ToggleGroup toggle2Group = new ToggleGroup();
        customLv.setCellFactory(new Callback<GridView<IconData>, GridCell<IconData>>() {
            @Override
            public GridCell<IconData> call(GridView<IconData> arg0) {
                return new IconGridCell(toggle2Group);
            }
        });
        if (handle.getMeta() != null && handle.getCustomIcons() != null) {
            for (CustomIcon icon : handle.getCustomIcons().getIcons()) {
                if (icon.getData() != null) {
                    customImgs.add(new IconData(icon.getUuid(), icon.getData()));
                }
            }
        }
    }

    @FXML
    protected void addBtn(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(mainApp.getCurrentDir()));
            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg");
            fileChooser.getExtensionFilters().add(extFilter);
            // Show save file dialog
            List<File> list = fileChooser.showOpenMultipleDialog(primaryStage);
            List<IconData> newIcons = handle.addIcons(list);
            if (newIcons != null && newIcons.size() > 0) {
                mainApp.saveCurrentDir(list.get(0).getAbsoluteFile().getParent());
                customImgs.addAll(newIcons);
            }
            if (!customImgs.isEmpty()) {
                btnRemove.setDisable(false);
            } else {
                btnRemove.setDisable(true);
            }
        } catch (Exception ex) {

        }
    }

    @FXML
    protected void removeBtn(ActionEvent event) {
        if (customChk.isSelected()) {
            IconData selectedIcon = null;
            for (IconData icon : customImgs) {
                if (icon.isSelected.getValue()) {
                    selectedIcon = icon;
                    break;
                }
            }
            if (selectedIcon != null) {
                handle.removeIconByUuid(selectedIcon.iconUUID);
                customImgs.remove(selectedIcon);
            }
        }
        if (!customImgs.isEmpty()) {
            btnRemove.setDisable(false);
        } else {
            btnRemove.setDisable(true);
        }
    }

    @FXML
    protected void checkStandardIcon(ActionEvent event) {
        btnAdd.setDisable(true);
        btnRemove.setDisable(true);
    }

    @FXML
    protected void checkCustomIcon(ActionEvent event) {
        btnAdd.setDisable(false);
        if (!customImgs.isEmpty()) {
            btnRemove.setDisable(false);
        } else {
            btnRemove.setDisable(true);
        }
    }

    @FXML
    protected void saveBtn(ActionEvent event) {
        if (standardChk.isSelected()) {
            for (IconData icon : standardImgs) {
                if (icon.isSelected.getValue()) {
                    iconCell = icon;
                    break;
                }
            }
        } else {
            for (IconData icon : customImgs) {
                if (icon.isSelected.getValue()) {
                    iconCell = icon;
                    break;
                }
            }
        }
        setButtonType(ButtonType.OK);
        close();
    }

    public IconData getReturnIcon() {
        return iconCell;
    }

    @Override
    protected void dispose() {
    }

    @Override
    public String getTitle() {
        return CONST.ICONDLG_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.ICONDLG_FXML;
    }

}

class IconGridCell extends GridCell<IconData> {

    private final ToggleButton button;

    public IconGridCell(ToggleGroup toggleGroup) {
        button = new ToggleButton();
        button.setToggleGroup(toggleGroup);
        button.setId("gridview-btn");
    }

    @Override
    protected void updateItem(IconData item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            item.isSelected.bind(button.selectedProperty());
            ImageView imv = IApp.createImageViewFromIconData(item.data, 20.0, 20.0);
            button.setGraphic(imv);
            setGraphic(button);
        }
    }

}
