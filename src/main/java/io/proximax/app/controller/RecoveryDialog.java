package io.proximax.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import io.proximax.app.recovery.AccountInfo;
import io.proximax.app.recovery.ProxiLicenseHttp;
import io.proximax.app.utils.CONST;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

/**
 *
 * @author thcao
 */
public class RecoveryDialog extends AbstractController {

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
    @FXML
    private JFXButton saveBtn;

    private ProxiLicenseHttp proxiHttp = null;

    public RecoveryDialog() {
        super(true);
    }

    private AccountInfo accountInfo = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveBtn.setText("RECOVERY");
        question1Cbx.setItems(FXCollections.observableArrayList());
        question1Cbx.getItems().addAll(CONST.SECURE_QUESTIONS);
        question2Cbx.setItems(FXCollections.observableArrayList());
        question2Cbx.getItems().addAll(CONST.SECURE_QUESTIONS);
        question3Cbx.setItems(FXCollections.observableArrayList());
        question3Cbx.getItems().addAll(CONST.SECURE_QUESTIONS);

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
                        accountInfo = proxiHttp.getAccountInfo(email).toFuture().get();
                    } else {
                        errorLbl.setText("Error: invalid email");
                        emailField.requestFocus();
                    }
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
            }
        });
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
        if (accountInfo == null) {
            errorLbl.setText("Error: cannot find your key in ProximaX Central Data");
            return;
        }
        if (accountInfo.getQuestion1().equals(question1Cbx.getSelectionModel().getSelectedItem())
                && accountInfo.getQuestion2().equals(question2Cbx.getSelectionModel().getSelectedItem())
                && accountInfo.getQuestion3().equals(question3Cbx.getSelectionModel().getSelectedItem())
                && accountInfo.getAnswer1().equals(answer1Field.getText())
                && accountInfo.getAnswer2().equals(answer2Field.getText())
                && accountInfo.getAnswer3().equals(answer3Field.getText())) {
            setButtonType(ButtonType.OK);
            close();
        } else {
            errorLbl.setText("Error: your questions and answers not match");
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

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

}
