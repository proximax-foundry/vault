package io.proximax.app.controller;

import io.proximax.app.db.PasswordAttributes;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.PasswordFactory;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author thcao
 */
public class PasswordDialog extends AbstractController {

    public String passwords;

    @FXML
    CheckBox capitalCheckBox;
    @FXML
    CheckBox smallCheckBox;
    @FXML
    CheckBox numbersCheckBox;
    @FXML
    CheckBox symbolsCheckBox;
    @FXML
    TextField lengthTextField;
    @FXML
    TextField quantityTextField;
    @FXML
    TextArea passwordsTA;
    @FXML
    Button saveButton;
    @FXML
    Button generateButton;

    public PasswordDialog() {
        super(true);
    }

    @FXML
    private void generatePasswords() {
        PasswordAttributes attributes = new PasswordAttributes(
                capitalCheckBox.isSelected(),
                smallCheckBox.isSelected(),
                numbersCheckBox.isSelected(),
                symbolsCheckBox.isSelected()
        );

        int length = 0;
        try {
            length = Integer.parseInt(lengthTextField.getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        int quantity = 0;
        try {
            quantity = Integer.parseInt(quantityTextField.getText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        List<String> passwordsArray = PasswordFactory.generateMultiplePasswords(attributes, length, quantity);
        passwords = "";
        if (length == 1) {
            passwords = passwordsArray.get(0);
        } else {
            for (String password : passwordsArray) {
                passwords += password + "\n";
            }
        }
        passwordsTA.setText(passwords);
    }

    @FXML
    public void saveToFile() {
        setButtonType(ButtonType.OK);
        close();
    }

    @FXML
    public void clearTA() {
        passwordsTA.clear();
    }

    @Override
    protected void dispose() {
    }

    @Override
    public String getTitle() {
        return CONST.PASSWORD_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.PASSWORD_FXML;
    }

}
