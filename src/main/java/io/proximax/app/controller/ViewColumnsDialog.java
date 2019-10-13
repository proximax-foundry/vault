package io.proximax.app.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.proximax.app.utils.CONST;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.CheckBox;

public class ViewColumnsDialog extends AbstractController {

    @FXML
    private TableView<ColumnProperty> tableEntry;

    @FXML
    private TableColumn column;

    @FXML
    private TableColumn checkBox;

    private static final List<String> columnList = new ArrayList<>(
            Arrays.asList("TITLE", "USERNAME", "PASSWORD", "EXPIRY TIME", "URL", "NOTES"));
    public List<ColumnProperty> list = new ArrayList<>();
    public static Map<String, Boolean> colMap = new HashMap<>();
    private static int index = 1;

    @Override
    protected void initialize() {

        columnList.forEach(col -> {
            ColumnProperty columnProperty = new ColumnProperty();
            columnProperty.setColumn(col);
            boolean visible = true;
            CheckBox cb = new CheckBox();
            cb.setSelected(colMap.getOrDefault(col, true));
            cb.setId("checkbox_" + index);
            index++;
            columnProperty.setCheckBox(cb);
            list.add(columnProperty);
        });

        ObservableList<ColumnProperty> observableList = FXCollections.observableArrayList(list);
        tableEntry.setItems(observableList);
        column.setCellValueFactory(new PropertyValueFactory<ColumnProperty, String>("column"));
        checkBox.setCellValueFactory(new PropertyValueFactory<ColumnProperty, Object>("checkBox"));

    }

    public ViewColumnsDialog(boolean modal) {
        super(modal);
    }

    @Override
    protected void dispose() {
        list.forEach(col -> {
            colMap.put(col.getColumn(), col.getCheckBox().isSelected());
        });
    }

    @Override
    public String getTitle() {
        return CONST.VIEW_COLUMNS_DIALOG_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.VIEW_COLUMNS_DIALOG;
    }

    @FXML
    protected void closeBtn(ActionEvent event) {
        close();
    }

}
