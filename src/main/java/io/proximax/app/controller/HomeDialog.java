package io.proximax.app.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import io.proximax.app.core.ui.IApp;
import io.proximax.app.db.LocalAccount;
import io.proximax.app.db.LocalFile;
import io.proximax.app.fx.control.ProxiStatusBar;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.LocalFileHelpers;
import io.proximax.app.utils.NetworkUtils;
import io.proximax.download.DownloadParameter;
import io.proximax.download.DownloadResult;
import io.proximax.download.Downloader;
import io.proximax.upload.UploadParameter;
import io.proximax.upload.UploadResult;
import io.proximax.upload.Uploader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author thcao
 */
public class HomeDialog extends AbstractController {

    @FXML
    private TableView<EntryProperty> tableEntry;
    @FXML
    private TreeView<GroupProperty> groupTree;
    @FXML
    private JFXTextField searchField;
    @FXML
    private JFXTextArea statusText;
    @FXML
    private Button uploadBtn;
    @FXML
    private Button downloadBtn;
    @FXML
    private Button saveBtn;
    @FXML
    private Button filterBtn;

    private ProxiStatusBar statusBar;

    // bind status text property
    private final StringProperty statusProperty = new SimpleStringProperty();

    final ContextMenu groupMenu = new ContextMenu();
    final ContextMenu entryMenu = new ContextMenu();
    final Menu urlMenu = new Menu();
    // The table's data
    private ObservableList<EntryProperty> entryList = FXCollections.observableArrayList();
    // The table's data
    private FilteredList<EntryProperty> filteredEntries = null;

    private ProxiKeePassImpl keePassDB;

    private BooleanProperty bConnected = new SimpleBooleanProperty(false);

    public HomeDialog(LocalAccount localAccount) throws Exception {
        super(true);
        keePassDB = new ProxiKeePassImpl(localAccount);
    }

    @Override
    protected void initialize() {
        initializeStatusBar();
        MenuItem addGroup = createMenuItem("Add Group", "addgroup", this::addGroup, false);
        MenuItem editGroup = createMenuItem("Edit Group", "editgroup", this::editGroup, false);
        MenuItem delGroup = createMenuItem("Delete Group", "delgroup", this::deleteGroup, false);
        MenuItem dupGroup = createMenuItem("Duplicate Group", "dupgroup", this::duplicateGroup, false);
        SeparatorMenuItem sep = new SeparatorMenuItem();
        groupMenu.getItems().addAll(addGroup, sep, editGroup, delGroup, dupGroup);

        groupTree.setContextMenu(groupMenu);

        MenuItem cpUsernameEntry = createMenuItem("Copy Username", "cpusername", this::copyUsernameToClipboard, true);
        MenuItem cpPasswordEntry = createMenuItem("Copy Password", "cppassword", this::copyPasswordToClipboard, true);
        MenuItem cpUrlEntry = createMenuItem("Copy Url", "clipboard-img", this::copyUrlToClipboard, true);
        MenuItem addEntry = createMenuItem("Add Entry", "addentry", this::addEntry, false);
        MenuItem editEntry = createMenuItem("Edit Entry", "editentry", this::editEntry, true);
        MenuItem dupEntry = createMenuItem("Duplicate Entry", "dupentry", this::duplicateEntry, true);
        MenuItem delEntry = createMenuItem("Delete Entry", "delentry", this::deleteEntry, true);
        MenuItem chromeEntry = createMenuItem("Open With Chrome", "chromeEntry", () -> openWithChrome(false), false);
        MenuItem firefoxEntry = createMenuItem("Open With Firefox", "firefoxEntry", () -> openFirefox(false), false);
        MenuItem internetExplorerEntry = createMenuItem("Open With Internet Explorer", "internetExplorerEntry", () -> openInternetExplorer(false), false);
        MenuItem chromeEntryIncognito = createMenuItem("Open With Chrome Incognito", "chromeEntry", () -> openWithChrome(true), false);
        MenuItem firefoxEntryIncognito = createMenuItem("Open With Firefox Incognito", "firefoxEntry", () -> openFirefox(true), false);
        MenuItem internetExplorerEntryIncognito = createMenuItem("Open With Internet Explorer Incognito", "internetExplorerEntry", () -> openInternetExplorer(true), false);

        urlMenu.setText("Url Entry");
        urlMenu.setDisable(false);
        urlMenu.getItems().addAll(chromeEntry, firefoxEntry, internetExplorerEntry, chromeEntryIncognito, firefoxEntryIncognito, internetExplorerEntryIncognito);
        entryMenu.getItems().addAll(cpUsernameEntry, cpPasswordEntry, cpUrlEntry, sep, addEntry, editEntry, dupEntry,
                delEntry, urlMenu);
        tableEntry.setContextMenu(entryMenu);

        TableColumn<EntryProperty, String> col1 = createTableColumn("TITLE", "title", 140.0);
        TableColumn<EntryProperty, String> col2 = createTableColumn("USERNAME", "username", 140.0);
        TableColumn<EntryProperty, String> col3 = createTableColumn("PASSWORD", "encPassword", 116.0);
        TableColumn<EntryProperty, String> col4 = createTableColumn("URL", "url", 180.0);
        TableColumn<EntryProperty, String> col5 = createTableColumn("NOTES", "notes", 180.0);
        TableColumn<EntryProperty, String> col6 = createTableColumn("EXPIRY TIME", "expiryTime", 160.0);

        tableEntry.getColumns().add(col1);
        tableEntry.getColumns().add(col2);
        tableEntry.getColumns().add(col3);
        tableEntry.getColumns().add(col6);
        tableEntry.getColumns().add(col4);
        tableEntry.getColumns().add(col5);

        if (searchField != null) {
            filteredEntries = new FilteredList<>(entryList, p -> true);
            tableEntry.setItems(filteredEntries);
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredEntries.setPredicate(theFile -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (String.valueOf(theFile.getUsername()).toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false; // Does not match.
                });
            });
        } else {
            tableEntry.setItems(entryList);
        }
        groupTree.setCellFactory(cv -> {
            TreeCell<GroupProperty> cell = new TreeCell<GroupProperty>() {
                @Override
                protected void updateItem(GroupProperty item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty && item != null) {
                        HBox hbox = new HBox();
                        ImageView imv = null;
                        Text text = new Text(item.getName());
                        if (item.getTimes() != null && item.getTimes().expires()
                                && item.getTimes().getExpiryTime().before(Calendar.getInstance())) {
                            text.setStrikethrough(true);
                            // imv = new ImageView(mainApp.getImageFromResource("expiry.png"));
                            imv = new ImageView();
                            imv.setId("expiry-img");
                        } else {
                            imv = new ImageView(mainApp.getIcon(item.getIconId()));
                        }
                        if (imv != null) {
                            imv.setFitWidth(20.0);
                            imv.setFitHeight(20.0);
                            hbox.getChildren().add(imv);
                        }
                        hbox.getChildren().add(text);
                        hbox.setSpacing(5.0);
                        setGraphic(hbox);
                    } else {
                        setText(null);
                        setGraphic(null);
                    }
                }

            };
            cell.setOnMouseClicked(event -> {
                BooleanProperty bDisable = new SimpleBooleanProperty(
                        groupTree.getSelectionModel().getSelectedIndex() == 0);
                delGroup.disableProperty().bind(bDisable);
            });
            return cell;
        });
        initializeGroupAndEntry();

        groupTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (treeIdx != groupTree.getSelectionModel().getSelectedIndex()) {
                treeIdx = groupTree.getSelectionModel().getSelectedIndex();
                TreeItem<GroupProperty> selectedItem = (TreeItem) newValue;
                onSeletedGroup(selectedItem.getValue());

            }
        });
        tableEntry.setRowFactory(tv -> {
            TableRow<EntryProperty> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (!row.isEmpty()) {
                        EntryProperty entry = row.getItem();
                        statusText.setText(entry.toStringDebug());
                    }
                }
                BooleanProperty bDisable = new SimpleBooleanProperty(row.isEmpty() || !row.isFocused());
                for (MenuItem item : entryMenu.getItems()) {
                    if (!"Add Entry".equals(item.getText())) {
                        item.disableProperty().bind(bDisable);
                    }
                }
            });
            return row;
        });
        col1.setCellFactory(col -> new TableCell<EntryProperty, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int idx = getIndex();
                if (idx >= 0 && idx < entryList.size()) {
                    EntryProperty entry = entryList.get(idx);
                    HBox hbox = new HBox();
                    ImageView imv = null;
                    Text text = new Text(entry.getTitle());
                    if (entry.getTimes() != null && entry.getTimes().expires()
                            && entry.getTimes().getExpiryTime().before(Calendar.getInstance())) {
                        text.setStrikethrough(true);
                        // imv = new ImageView(mainApp.getImageFromResource("expiry.png"));
                        imv = new ImageView();
                        imv.setId("expiry-img");
                    } else {
                        if (entry.getIconData() != null) {
                            imv = IApp.createImageViewFromIconData(entry.getIconData(), null);
                        } else {
                            imv = new ImageView(mainApp.getIcon(entry.getIconId()));
                        }
                    }
                    if (imv != null) {
                        imv.setFitWidth(20.0);
                        imv.setFitHeight(20.0);
                        hbox.getChildren().add(imv);
                    }
                    hbox.getChildren().add(text);
                    hbox.setSpacing(5.0);
                    setGraphic(empty ? null : hbox);
                }
            }
        });

        initializeCheckingConnection();
    }

    int treeIdx = 0;

    private void addGroup() {
        try {
            TreeItem<GroupProperty> treeItem = groupTree.getSelectionModel().getSelectedItem();
            GroupProperty group = treeItem.getValue();
            int size = group.getGroupsProperty().size();
            GroupDialog dlg = new GroupDialog(group, true);
            dlg.openWindow(this);
            if (dlg.getResultType() == ButtonType.OK) {
                if (group.isModified()) {
                    treeItem.getChildren().add(new TreeItem(group.getGroupsProperty().get(size)));
                }
                groupTree.refresh();
            }
        } catch (Exception ex) {
            ErrorDialog.showError(this, ex.getMessage());
        }
    }

    private void duplicateEntry() {
        try {
            EntryProperty entry = tableEntry.getSelectionModel().getSelectedItem();
            EntryProperty newEntry = new EntryProperty(entry);
            newEntry.getParent().addEntry(newEntry);
            entryList.add(newEntry);
            tableEntry.refresh();
        } catch (Exception ex) {
            ErrorDialog.showError(this, ex.getMessage());
        }
    }

    private void duplicateGroup() {
        try {
            TreeItem<GroupProperty> treeItem = groupTree.getSelectionModel().getSelectedItem();
            GroupProperty group = treeItem.getValue();
            GroupProperty newGroup = new GroupProperty(group, group.getParent());
            newGroup.getParent().addGroup(newGroup);
            treeItem.getParent().getChildren().add(groupTree.getSelectionModel().getSelectedIndex(),
                    new TreeItem(newGroup));
            groupTree.refresh();
        } catch (Exception ex) {
            ErrorDialog.showError(this, ex.getMessage());
        }
    }

    private void addEntry() {
        try {
            TreeItem<GroupProperty> treeItem = groupTree.getSelectionModel().getSelectedItem();
            GroupProperty group = treeItem.getValue();
            int size = group.getEntriesProperty().size();
            EntryDialog dlg = new EntryDialog(group);
            dlg.openWindow(this);
            if (dlg.getResultType() == ButtonType.OK) {
                if (group.isModified()) {
                    entryList.add(group.getEntriesProperty().get(size));
                }
                tableEntry.refresh();
            }
        } catch (Exception ex) {
            ErrorDialog.showError(this, ex.getMessage());
        }
    }

    private void editEntry() {
        try {
            EntryProperty entry = tableEntry.getSelectionModel().getSelectedItem();
            EntryDialog dlg = new EntryDialog(entry);
            dlg.openWindow(this);
            if (dlg.getResultType() == ButtonType.OK) {
                tableEntry.refresh();
            }
        } catch (Exception ex) {
            ErrorDialog.showError(this, ex.getMessage());
        }
    }

    private void editGroup() {
        try {
            TreeItem<GroupProperty> treeItem = groupTree.getSelectionModel().getSelectedItem();
            GroupProperty group = treeItem.getValue();
            GroupDialog dlg = new GroupDialog(group, false);
            dlg.openWindow(this);
            if (dlg.getResultType() == ButtonType.OK) {
                groupTree.refresh();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorDialog.showError(this, ex.getMessage());
        }
    }

    private void deleteGroup() {
        AlertEx alert = new AlertEx(AlertType.CONFIRMATION,
                "Are you sure want to permanently delete the selected group ?\n\nDeleting a group will also delete all entries and subgroups in that group",
                ButtonType.YES, ButtonType.NO);
        alert.setTitleEx(CONST.APP_NAME);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.NO) {
            return;
        }
        GroupProperty group = groupTree.getSelectionModel().getSelectedItem().getValue();
        GroupProperty parent = group.getParent();
        parent.removeGroup(group);
        initializeGroupAndEntry();
        groupTree.refresh();
    }

    private void deleteEntry() {
        EntryProperty entry = tableEntry.getSelectionModel().getSelectedItem();
        AlertEx alert = new AlertEx(AlertType.CONFIRMATION,
                "Are you sure want to permanently delete the selected entry ?\n\n - " + entry.getTitle(),
                ButtonType.YES, ButtonType.NO);
        alert.setTitleEx(CONST.APP_NAME);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.NO) {
            return;
        }
        GroupProperty parent = entry.getParent();
        parent.removeEntry(entry);
        entryList.remove(entry);
        tableEntry.refresh();
    }

    private void openWithChrome(boolean incognito) {
        try {
            EntryProperty entry = tableEntry.getSelectionModel().getSelectedItem();
            String filename = entry.getUrl();
            String browser = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
            ProcessBuilder pb = null;
            if (incognito) {
                pb = new ProcessBuilder(browser, filename, "-incognito");
            } else {
                pb = new ProcessBuilder(browser, filename);
            }
            // setup other options ..
            // .. and run
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void openFirefox(boolean incognito) {
        try {
            EntryProperty entry = tableEntry.getSelectionModel().getSelectedItem();
            String filename = entry.getUrl();
            String browser = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
            ProcessBuilder pb = null;
            if (incognito) {
                pb = new ProcessBuilder(browser, filename, "-private");
            } else {
                pb = new ProcessBuilder(browser, filename);
            }
            // setup other options ..
            // .. and run
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void openInternetExplorer(boolean incognito) {
        try {
            EntryProperty entry = tableEntry.getSelectionModel().getSelectedItem();
            String filename = entry.getUrl();
            String browser = "C:\\Program Files\\Internet Explorer\\iexplore.exe";
            ProcessBuilder pb = null;
            if (incognito) {
                pb = new ProcessBuilder(browser, filename, "-private");
            } else {
                pb = new ProcessBuilder(browser, filename);
            }
            // setup other options ..
            // .. and run
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void copyUsernameToClipboard() {
        EntryProperty entry = tableEntry.getSelectionModel().getSelectedItem();
        if (entry != null) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(entry.getUsername());
            clipboard.setContent(content);
        }
    }

    private void copyPasswordToClipboard() {
        EntryProperty entry = tableEntry.getSelectionModel().getSelectedItem();
        if (entry != null) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(entry.getPassword());
            clipboard.setContent(content);
        }
    }

    private void copyUrlToClipboard() {
        EntryProperty entry = tableEntry.getSelectionModel().getSelectedItem();
        if (entry != null) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(entry.getUrl());
            clipboard.setContent(content);
        }
    }

    private void onSeletedGroup(GroupProperty group) {
        entryList.clear();
        statusText.clear();
        if (group != null) {
            entryList.addAll(group.getEntriesProperty());
            tableEntry.refresh();
        }
    }

    private TableColumn<EntryProperty, String> createTableColumn(String title, String proName, double prefWidth) {
        TableColumn<EntryProperty, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(proName));
        col.setPrefWidth(prefWidth);
        col.setStyle("-fx-alignment: CENTER;");
        return col;
    }

    private MenuItem createMenuItem(String title, String styleClass, Runnable action, boolean disable) {
        ImageView iv = new ImageView();
        iv.setId(styleClass);
        iv.setFitWidth(20.0);
        iv.setFitHeight(20.0);
        MenuItem menuItem = new MenuItem(title, iv);
        // menuItem.getStyleClass().add(styleClass);
        if (action != null) {
            menuItem.setOnAction(evt -> {
                action.run();
            });
        }
        menuItem.setDisable(disable);
        return menuItem;
    }

    private void initializeGroupAndEntry() {
        try {
            groupTree.setRoot(keePassDB.getRoot().createTreeItem());
            groupTree.getSelectionModel().selectFirst();
            entryList.clear();
            entryList.addAll(keePassDB.getRoot().getEntriesProperty());
            groupTree.refresh();
            tableEntry.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Initialize status bar
     */
    private void initializeStatusBar() {
        statusBar = new ProxiStatusBar();
        statusBar.setImageSuccess(getMainApp().getImageFromResource(CONST.IMAGE_GREEN, 14.0, 14.0));
        statusBar.setImageFailed(getMainApp().getImageFromResource(CONST.IMAGE_RED, 14.0, 14.0));
        statusBar.setEventHandler(this);
        // text in status bar
        statusBar.textProperty().bind(statusProperty);
        ((BorderPane) mainPane).setBottom(statusBar);
        BorderPane.setAlignment(statusBar, Pos.BOTTOM_CENTER);
        List<String> list = keePassDB.getLocalAccount().getNodes();
        ObservableList<String> obList = FXCollections.observableList(list);
        if (keePassDB.getLocalAccount().getCurrentNodeIndex() == -1) {
            keePassDB.getLocalAccount().setConnectionIndex(0);
        }
        statusBar.setNodeItems(obList, keePassDB.getLocalAccount().getCurrentNodeIndex());

        // connection status
        setConnection(keePassDB.isConnected());
    }

    public void setConnection(Boolean connected) {
        this.statusBar.setImageStatus(connected);
        bConnected.set(connected);
    }

    private ScheduledService<Boolean> serverCheck = null;

    /**
     * Initialize service checking connection
     */
    private void initializeCheckingConnection() {
        serverCheck = new ScheduledService<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                Task<Boolean> aliveTask = new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        if (keePassDB.getLocalAccount().testNode()) {
                            if (keePassDB.getLocalAccount().status == 0) {
                                NetworkUtils.activeAccount(keePassDB.getLocalAccount());
                            }
                            return true;
                        }
                        return false;
                    }

                    @Override
                    protected void succeeded() {
                        serverCheck.setPeriod(Duration.minutes(1));
                        if (getValue()) { // alive
                            setConnection(Boolean.TRUE);
                        } else {
                            setConnection(Boolean.FALSE);
                        }
                    }
                };
                return aliveTask;
            }
        };
        serverCheck.setPeriod(Duration.minutes(1));
        serverCheck.start();
    }

    @Override
    protected void dispose() {
        if (serverCheck != null) {
            serverCheck.cancel();
        }
        execJob.shutdownNow();
        try {
            if (!execJob.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                execJob.shutdownNow();
            }
        } catch (InterruptedException e) {
            execJob.shutdownNow();
        }
    }

    @Override
    public String getTitle() {
        return CONST.DASHBOARD_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.DASHBOARD_FXML;
    }

    private void setDisableButtons(boolean bEnable) {
        uploadBtn.setDisable(bEnable);
        downloadBtn.setDisable(bEnable);
        saveBtn.setDisable(bEnable);
    }

    @FXML
    void uploadDB(ActionEvent event) {
        if (keePassDB.isModified()) {
            AlertEx alert = new AlertEx(AlertType.CONFIRMATION, "Save database changes before sync to remote database?",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitleEx(CONST.APP_NAME);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES) {
                saveKeePassDB();
            }
        }
        LocalFile localFile = keePassDB.getLatestFile();
        if (localFile == null) {
            localFile = new LocalFile();
            localFile.fileName = keePassDB.getLocalAccount().address + ".kdbx";
            localFile.uType = CONST.UTYPE_SECURE_NEMKEYS;
            localFile.address = keePassDB.getLocalAccount().address;
            localFile.privateKey = keePassDB.getLocalAccount().privateKey;
            localFile.publicKey = keePassDB.getLocalAccount().publicKey;
        } else {
            if (keePassDB.isBefore(localFile.modified)) {
                // need popup ask yes/no
                AlertEx alert = new AlertEx(AlertType.CONFIRMATION,
                        "Local database is oldest version. Do you want to overwrite remote database ?", ButtonType.YES,
                        ButtonType.NO);
                alert.setTitleEx(CONST.APP_NAME);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.NO) {
                    return;
                }
            }
        }
        setDisableButtons(true);
        IntegerProperty pendingTasks = new SimpleIntegerProperty(0);
        Task<Void> task = createUploadTask(localFile);
        pendingTasks.set(pendingTasks.get() + 1);

        task.setOnSucceeded(taskEvent -> {
            statusBar.progressProperty().unbind();
            statusBar.textProperty().unbind();
            pendingTasks.set(pendingTasks.get() - 1);
        });
        // run task in single-thread executor (will queue if another task is running):
        execJob.submit(task);
    }

    @FXML
    void filterBtn(ActionEvent event) {

        try {
            ViewColumnsDialog dlg = new ViewColumnsDialog(true);
            dlg.openWindow(this);
            Iterator iterate = tableEntry.getColumns().iterator();
            while (iterate.hasNext()) {
                TableColumn col = (TableColumn) iterate.next();
                boolean visible = dlg.colMap.get(col.getText());
                col.setVisible(visible);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @FXML
    void downloadDB(ActionEvent event) {
        LocalFile localFile = keePassDB.getLatestFile();
        if (localFile == null) {
            setStatus("You didn't have db file from remote");
            return;
        } else {
            if (!keePassDB.isBefore(localFile.modified)) {
                String str = "Local database is newest version\n";
                if (keePassDB.isModified()) {
                    str += "You're changing database\n\nDo you want to discard all changes and sync from remote ?";
                } else {
                    str += "\nDo you want to overwrite it ?";
                }
                AlertEx alert = new AlertEx(AlertType.CONFIRMATION, str, ButtonType.YES, ButtonType.NO);
                alert.setTitleEx(CONST.APP_NAME);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.NO) {
                    return;
                }
            } else {
                if (keePassDB.isModified()) {
                    AlertEx alert = new AlertEx(AlertType.CONFIRMATION,
                            "You're changing database. Do you want to discard all changes and sync from remote ?",
                            ButtonType.YES, ButtonType.NO);
                    alert.setTitleEx(CONST.APP_NAME);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.NO) {
                        return;
                    }
                }

            }
        }
        setDisableButtons(true);
        IntegerProperty pendingTasks = new SimpleIntegerProperty(0);
        Task<Void> task = createDownloadTask(localFile);
        pendingTasks.set(pendingTasks.get() + 1);

        task.setOnSucceeded(taskEvent -> {
            statusBar.progressProperty().unbind();
            statusBar.textProperty().unbind();
            pendingTasks.set(pendingTasks.get() - 1);
        });
        // run task in single-thread executor (will queue if another task is running):
        execJob.submit(task);
    }

    private AtomicInteger taskCount = new AtomicInteger(0);
    private ExecutorService execJob = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // allows app to exit if tasks are running
        return t;
    });

    /**
     * Create download task when user click download
     *
     * @param localFile
     * @return
     */
    private Task<Void> createDownloadTask(LocalFile localFile) {
        final HomeDialog homeDlg = this;
        final int taskNumber = taskCount.incrementAndGet();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                IApp.runSafe(() -> {
                    statusBar.progressProperty().bind(progressProperty());
                    statusBar.textProperty().bind(messageProperty());
                });
                updateProgress(1, 99);
                DownloadParameter parameter = LocalFileHelpers.createDownloadParameter(localFile);
                Downloader download = new Downloader(keePassDB.getConnectionConfig());
                updateMessage("Status: downloading");
                DownloadResult downloadResult = download.download(parameter);
                updateMessage("Status: downloading...");
                updateProgress(10, 99);
                try {
                    InputStream downloadStream = downloadResult.getData().getByteStream();
                    FileOutputStream keePassFile = new FileOutputStream(keePassDB.getKeePassDB());
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    long downloadedSize = 0;
                    double percent = 0;
                    while ((read = downloadStream.read(buffer)) >= 0) {
                        downloadedSize += read;
                        keePassFile.write(buffer, 0, read);
                        percent = (downloadedSize * 90.0) / localFile.fileSize;
                        updateProgress(percent, 99);
                    }
                    updateProgress(99, 99);
                    updateMessage("Status: sync from remote to local completed.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    updateProgress(0, 99);
                    updateMessage("Status: sync failed.");
                    ErrorDialog.showErrorFX(homeDlg, ex.getMessage());
                }
                setDisableButtons(false);
                return null;
            }
        };
        return task;
    }

    /**
     * Create download task when user click download
     *
     * @param localFile
     * @return
     */
    private Task<Void> createUploadTask(LocalFile localFile) {
        final HomeDialog homeDlg = this;
        final int taskNumber = taskCount.incrementAndGet();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                IApp.runSafe(() -> {
                    statusBar.progressProperty().bind(progressProperty());
                    statusBar.textProperty().bind(messageProperty());
                });
                updateMessage("Status: uploading ...");
                updateProgress(1, 99);
                File file = new File(keePassDB.getKeePassDB());
                if (localFile.id == 0) {
                    try {
                        updateProgress(10, 99);
                        Uploader upload = new Uploader(keePassDB.getConnectionConfig());
                        UploadParameter parameter = LocalFileHelpers
                                .createUploadFileParameter(keePassDB.getLocalAccount(), localFile, file);
                        updateProgress(20, 99);
                        UploadResult uploadResult = upload.upload(parameter);
                        updateProgress(70, 99);
                        localFile.filePath = file.getAbsolutePath();
                        localFile.uploadDate = System.currentTimeMillis();
                        localFile.hash = uploadResult.getData().getDataHash();
                        localFile.nemHash = uploadResult.getTransactionHash();
                        localFile.fileSize = file.length();
                        localFile.modified = file.lastModified();
                        localFile.status = CONST.FILE_STATUS_NEW;
                        keePassDB.addLocalFile(localFile);
                        updateProgress(99, 99);
                        updateMessage("Status: sync from local to remote completed.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        updateProgress(0, 99);
                        updateMessage("Status: sync failed.");
                        ErrorDialog.showErrorFX(homeDlg, ex.getMessage());
                    }
                    setDisableButtons(false);
                } else {
                    LocalFile saveFile = new LocalFile(localFile);
                    saveFile.metadata = "";
                    try {
                        updateProgress(30, 99);
                        Uploader upload = new Uploader(keePassDB.getConnectionConfig());
                        UploadParameter parameter = LocalFileHelpers
                                .createUploadFileParameter(keePassDB.getLocalAccount(), saveFile, file);
                        UploadResult uploadResult = upload.upload(parameter);
                        updateProgress(70, 99);
                        saveFile.uploadDate = System.currentTimeMillis();
                        saveFile.hash = uploadResult.getData().getDataHash();
                        saveFile.nemHash = uploadResult.getTransactionHash();
                        saveFile.fileSize = file.length();
                        saveFile.modified = file.lastModified();
                        keePassDB.updateLocalFile(localFile, saveFile);
                        updateProgress(99, 99);
                        updateMessage("Status: sync from local to remote completed.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        updateProgress(0, 99);
                        updateMessage("Status: sync failed.");
                        ErrorDialog.showErrorFX(homeDlg, ex.getMessage());
                    }
                    setDisableButtons(false);
                }
                return null;
            }
        };
        return task;
    }

    @FXML
    void saveFile(ActionEvent event) {
        saveKeePassDB();
    }

    @FXML
    void showProfile(ActionEvent event) {
        try {
            UserProfileDialog dlg = new UserProfileDialog(keePassDB.getLocalAccount());
            dlg.openWindow(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveKeePassDB() {
        try {
            int ret = keePassDB.saveDB();
            switch (ret) {
                case 1:
                    setStatus("Save Done.");
                    break;
                case 0:
                    setStatus("Don't change anything.");
                    break;
                default:
                    setStatus("Something wrong.");
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set status text in status bar
     *
     * @param status
     */
    public void setStatus(String status) {
        this.statusProperty.set(CONST.STR_STATUS + status);
    }

    @Override
    public void handle(Event event) {
        if (event.getSource() instanceof ComboBox) {
            ComboBox comboBox = (ComboBox) event.getSource();
            if (comboBox.getId().equals("status-nodes")) {
                keePassDB.getLocalAccount().setConnectionIndex(comboBox.getSelectionModel().getSelectedIndex());
                if (serverCheck != null) {
                    serverCheck.cancel();
                    serverCheck.setPeriod(Duration.seconds(1));
                    serverCheck.restart();
                }
            }
        }
        super.handle(event);
    }

    /**
     * Set connection status: image, text in status bar
     *
     * @param connected
     */
    public void setConnectionStatus(Boolean connected) {
        if (connected) {
            setStatus(CONST.STR_CONNECTED);
        } else {
            setStatus(CONST.STR_DISCONNECTED);
        }
        this.statusBar.setImageStatus(connected);
    }

    @Override
    protected void onClosing(Event event) {
        if (keePassDB.isModified()) {
            AlertEx alert = new AlertEx(AlertType.CONFIRMATION,
                    "Save database changes before exiting " + CONST.APP_NAME + " ?", ButtonType.YES, ButtonType.NO);
            alert.setTitleEx(CONST.APP_NAME);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES) {
                saveKeePassDB();
            }
        }
        super.onClosing(event);
        IApp.exit(0);
    }
}
