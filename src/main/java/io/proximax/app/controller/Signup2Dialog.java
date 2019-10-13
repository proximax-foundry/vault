package io.proximax.app.controller;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import io.proximax.app.db.LocalAccount;
import io.proximax.app.recovery.AccountInfo;
import io.proximax.app.recovery.AccountStatus;
import io.proximax.app.recovery.ProxiLicenseHttp;
import io.proximax.app.utils.AccountHelpers;
import io.proximax.app.utils.CONST;
import io.reactivex.Observable;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 *
 * @author thcao
 */
public class Signup2Dialog extends AbstractController {

    private LocalAccount localAccount = null;

    @FXML
    JFXComboBox<String> question1Cbx;
    @FXML
    JFXComboBox<String> question2Cbx;
    @FXML
    JFXComboBox<String> question3Cbx;
    @FXML
    JFXTextField emailField;
    @FXML
    JFXTextField answer1Field;
    @FXML
    JFXTextField answer2Field;
    @FXML
    JFXTextField answer3Field;
    @FXML
    private Label errorLbl;

    private ProxiLicenseHttp proxiHttp = null;

    public Signup2Dialog(LocalAccount localAccount) {
        super(true);
        this.localAccount = localAccount;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        question1Cbx.setItems(FXCollections.observableArrayList());
        question1Cbx.getItems().addAll(CONST.SECURE_QUESTIONS);
        question2Cbx.setItems(FXCollections.observableArrayList());
        question2Cbx.getItems().addAll(CONST.SECURE_QUESTIONS);
        question3Cbx.setItems(FXCollections.observableArrayList());
        question3Cbx.getItems().addAll(CONST.SECURE_QUESTIONS);

        AccountInfo accountInfo = AccountHelpers.getAccountInfo(localAccount);
        if (accountInfo != null) {
            question1Cbx.setValue(accountInfo.getQuestion1());
            question2Cbx.setValue(accountInfo.getQuestion2());
            question3Cbx.setValue(accountInfo.getQuestion3());
            answer1Field.setText(accountInfo.getAnswer1());
            answer2Field.setText(accountInfo.getAnswer2());
            answer3Field.setText(accountInfo.getAnswer3());
            emailField.setText(accountInfo.getEmail());
        }
        try {
            proxiHttp = new ProxiLicenseHttp("http://dev-proxi-service.proximax.io:8080/LicenseServer");
        } catch (Exception ex) {
            proxiHttp = null;
        }
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && proxiHttp != null) {
                try {
                    String email = emailField.getText();
                    if (validEmail(email)) {
                        errorLbl.setText("");
                        AccountInfo accountInfo1 = proxiHttp.getAccountInfo(email).toFuture().get();
                        if (accountInfo1 != null) {
                            if (!accountInfo1.compare(email, localAccount.privateKey, localAccount.publicKey, localAccount.address)) {
                                errorLbl.setText("Error: email existed");
                                emailField.requestFocus();
                            }
                        }
                    } else {
                        errorLbl.setText("Error: invalid email");
                        emailField.requestFocus();
                    }
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }

            }
        }
        );
    }

    @FXML
    protected void saveBtn(ActionEvent event) {
        if (!validEmail(emailField.getText())) {
            errorLbl.setText("Error: invalid email");
            emailField.requestFocus();
            return;
        }
        if (question1Cbx.getSelectionModel().getSelectedIndex() == -1) {
            errorLbl.setText("Error: question 1 is required");
            question1Cbx.requestFocus();
            return;
        }
        if (question2Cbx.getSelectionModel().getSelectedIndex() == -1) {
            errorLbl.setText("Error: question 2 is required");
            question2Cbx.requestFocus();
            return;
        }
        if (question3Cbx.getSelectionModel().getSelectedIndex() == -1) {
            errorLbl.setText("Error: question 3 is required");
            question3Cbx.requestFocus();
            return;
        }
        if (question1Cbx.getSelectionModel().getSelectedIndex() == question2Cbx.getSelectionModel().getSelectedIndex()
                || question1Cbx.getSelectionModel().getSelectedIndex() == question3Cbx.getSelectionModel().getSelectedIndex()
                || question2Cbx.getSelectionModel().getSelectedIndex() == question3Cbx.getSelectionModel().getSelectedIndex()) {
            errorLbl.setText("Error: questions are the same");
            return;
        }
        if (answer1Field.getText().isEmpty()) {
            errorLbl.setText("Error: answer cannot empty");
            answer1Field.requestFocus();
            return;
        }
        if (answer2Field.getText().isEmpty()) {
            errorLbl.setText("Error: answer cannot empty");
            answer2Field.requestFocus();
            return;
        }
        if (answer3Field.getText().isEmpty()) {
            errorLbl.setText("Error: answer cannot empty");
            answer3Field.requestFocus();
            return;
        }
        if (answer1Field.getText().equals(answer2Field.getText())
                || answer1Field.getText().equals(answer3Field.getText())
                || answer3Field.getText().equals(answer2Field.getText())) {
            errorLbl.setText("Error: answers are the same");
            return;
        }

        AccountInfo accountInfo = new AccountInfo(localAccount.fullName, emailField.getText(),
                question1Cbx.getSelectionModel().getSelectedItem(), answer1Field.getText(),
                question2Cbx.getSelectionModel().getSelectedItem(), answer2Field.getText(),
                question3Cbx.getSelectionModel().getSelectedItem(), answer3Field.getText(),
                localAccount.privateKey, localAccount.publicKey, localAccount.address);
        AccountHelpers.updateAccountInfo(localAccount, accountInfo);
        //update to server
        try {
            AccountStatus status = proxiHttp.saveAccountInfo(accountInfo).toFuture().get();
            if ("Success".equals(status.getStatus())) {
                close();
            } else {
                errorLbl.setText("Error: " + status.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private boolean validEmail(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    protected void dispose() {
    }

    @Override
    public String getTitle() {
        return CONST.SIGNUP2_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.SIGNUP2_FXML;
    }

}
