package io.proximax.app.controller;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import io.proximax.app.core.ui.IApp;
import io.proximax.app.db.LocalAccount;
import io.proximax.app.recovery.AccountInfo;
import io.proximax.app.utils.AccountHelpers;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.NetworkUtils;
import io.proximax.app.utils.StringUtils;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

/**
 *
 * @author Marvin
 */
public class LoginDialog extends AbstractController {

    @FXML
    private JFXPasswordField passwordField;
    @FXML
    private JFXPasswordField confirmField;
    @FXML
    private JFXTextField passField;
    @FXML
    private ToggleButton viewBtn;
    @FXML
    private Label confirmLbl;
    @FXML
    private JFXComboBox<String> networkCbx;
    @FXML
    private CheckBox prvkeyChk;
    @FXML
    private JFXTextField prvkeyField;
    @FXML
    private CheckBox termsChk;
    @FXML
    private Button loginBtn;

    @FXML
    private Label errorLbl;

    @FXML
    private Button btnRecovery;

    private AccountInfo accountInfo = null;

    private String userName = System.getProperty("user.name");

    private boolean isLogin = false;

    private String network = NetworkUtils.NETWORK_DEFAULT;

    public LoginDialog() {
        super(false);
        isLogin = AccountHelpers.isExistAccount(userName, network);
    }

    public LoginDialog(String network) {
        super(false);
        this.network = network;
        isLogin = AccountHelpers.isExistAccount(userName, network);
    }

    @Override
    public void initialize() {
        ObservableList<String> obList = FXCollections.observableList(NetworkUtils.NETWORK_SUPPORT);
        networkCbx.setItems(obList);
        networkCbx.setValue(network);
        if (NetworkUtils.NETWORKS.size() <= 1) {
            networkCbx.setDisable(true);
        }

        networkCbx.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!network.equals(newValue)) {
                if (isLogin) {
                    boolean needLogin = AccountHelpers.isExistAccount(userName, newValue);
                    if (!needLogin) {
                        try {
                            LoginDialog dlg = new LoginDialog(newValue);
                            dlg.openWindow();
                            hide();
                        } catch (Exception ex) {
                        }
                    }
                } else {
                    boolean needLogin = AccountHelpers.isExistAccount(userName, newValue);
                    if (needLogin) {
                        try {
                            LoginDialog dlg = new LoginDialog(newValue);
                            dlg.openWindow();
                            hide();
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        });
        passField.setManaged(false);
        passField.setVisible(false);
        passField.managedProperty().bind(viewBtn.selectedProperty());
        passField.visibleProperty().bind(viewBtn.selectedProperty());
        passwordField.managedProperty().bind(viewBtn.selectedProperty().not());
        passwordField.visibleProperty().bind(viewBtn.selectedProperty().not());
        passField.textProperty().bindBidirectional(passwordField.textProperty());
        if (!isLogin) {
            prvkeyField.disableProperty().bind(prvkeyChk.selectedProperty().not());
            btnRecovery.disableProperty().bind(prvkeyChk.selectedProperty().not());
            loginBtn.disableProperty().bind(termsChk.selectedProperty().not());
            confirmField.disableProperty().bind(viewBtn.selectedProperty());
            passwordField.setTooltip(new Tooltip("Password"));
            passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    if (passwordField.textProperty().length().lessThan(10).get()) {
                        errorLbl.setText("Error: password at least 10 characters");
                        passwordField.requestFocus();
                    } else {
                        errorLbl.setText("");
                    }
                }
            });
            passField.setTooltip(new Tooltip("Password"));
            passField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    if (passField.textProperty().length().lessThan(10).get()) {
                        errorLbl.setText("Error: password at least 10 characters");
                        passField.requestFocus();
                    } else {
                        errorLbl.setText("");
                    }
                }
            });
            confirmField.setTooltip(new Tooltip("Confirm Password"));
            confirmField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    if (passwordField.textProperty().isEqualTo(confirmField.textProperty()).not().get()) {
                        errorLbl.setText("Error: password and confirm password not match");
                        confirmField.requestFocus();
                    } else {
                        errorLbl.setText("");
                    }
                }
            });
        }
    }

    @FXML
    void networkBtn(ActionEvent event) {
        try {
            NetworkDialog dlg = new NetworkDialog();
            dlg.setParent(this);
            dlg.openWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void loginBtn(ActionEvent event) {
        try {
            String network = networkCbx.getValue();
            String password = passwordField.getText();
            if (!StringUtils.isEmpty(userName)) {
                LocalAccount account = AccountHelpers.login(userName, network, password);
                if (account != null) {
                    passwordField.setText("");
                    hide();
                    HomeDialog homeDlg = new HomeDialog(account);
                    homeDlg.openWindow(this);
                } else {
                    ErrorDialog.showError(this, "Invalid password");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorDialog.showError(this, e.getMessage());
        }

    }

    @FXML
    void signupBtn(ActionEvent event) {
        try {
            String network = networkCbx.getValue();
            String password = passwordField.getText();

            if (password.length() < 10) {
                ErrorDialog.showError(this, "Minimum length for password is 10");
                passwordField.requestFocus();
                return;
            }
            if (!viewBtn.isSelected()) {
                String confirm = confirmField.getText();
                if (!password.equals(confirm)) {
                    ErrorDialog.showError(this, "Password and confirm aren't identical");
                    confirmField.requestFocus();
                    return;
                }
            }
            String privateKey = null;
            if (prvkeyChk.isSelected()) {
                privateKey = prvkeyField.getText();
                //need search db in network
            }
            LocalAccount account = AccountHelpers.createAccount(userName, network, password, privateKey);
            if (account == null) {
                ErrorDialog.showError(this, "Cannot create account");
                return;
            }
            if (accountInfo != null) {
                AccountHelpers.updateAccountInfo(account, accountInfo);
            }
            ProxiKeePassImpl.createDefaultKeePassFile(account);
            passwordField.setText("");
            hide();
            HomeDialog homeDlg = new HomeDialog(account);
            homeDlg.openWindow(this);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorDialog.showError(this, e.getMessage());
        }

    }

    @Override
    protected void dispose() {
        IApp.exit(0);
    }

    public static void showDialog(Stage stage) {
        try {
            LoginDialog dialog = new LoginDialog();
            dialog.setResizable(false);
            dialog.openWindow();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void recoveryBtn(ActionEvent event) {
        try {
            errorLbl.setText("");
            RecoveryDialog dlg = new RecoveryDialog();
            dlg.setParent(this);
            dlg.openWindow();
            accountInfo = null;
            if (dlg.getResultType() == ButtonType.OK) {
                accountInfo = dlg.getAccountInfo();
                prvkeyField.setText(dlg.getAccountInfo().getPrivateKey());
                passwordField.requestFocus();
            }
        } catch (Exception ex) {

        }

    }

    @Override
    public void show() {
        reloadTheme();
        super.show();
    }

    @Override
    public String getTitle() {
        return CONST.LOGIN_TITLE;
    }

    @Override
    public String getFXML() {
        if (isLogin) {
            return CONST.LOGIN_FXML;
        }
        return CONST.SIGNUP_FXML;
    }

}
