package io.proximax.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import io.proximax.app.db.LocalAccount;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.NetworkUtils;
import io.proximax.app.utils.StringUtils;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 *
 * @author thcao
 */
public class UserProfileDialog extends AbstractController {

    private LocalAccount localAccount = null;

    @FXML
    JFXTextField nameField;

    @FXML
    JFXTextField addressField;

    @FXML
    JFXTextField publicField;

    @FXML
    JFXTextField privateField;

    @FXML
    JFXTextField peerField;

    @FXML
    JFXTextField storageField;

    @FXML
    JFXTextField xpxField;

    @FXML
    JFXButton xpxBtn;

    public UserProfileDialog(LocalAccount localAccount) {
        super(true);
        this.localAccount = localAccount;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameField.setText(localAccount.fullName);
        addressField.setText(localAccount.getAddressPretty());
        publicField.setText(localAccount.publicKey);
        privateField.setText(localAccount.privateKey);
        String peerId = localAccount.getPeerId();
        String quest = "Account doesn't have any balance, do you want to get xpx?";
        xpxBtn.setText("GET XPX");
        if ("MAIN_NET".equals(localAccount.getNetwork())) {
            quest = "Account doesn't have any balance, do you want to buy xpx?";
            xpxBtn.setText("BUY XPX");
        }
        if (!StringUtils.isEmpty(peerId)) {
            int idx = peerId.indexOf("{PeerID=");
            if (idx != -1 && peerId.length() > 8) {
                peerField.setText(peerId.substring(idx + 8, peerId.lastIndexOf("}")));
            } else {
                peerField.setText(peerId);
            }
        }
        double GB = 1024 * 1024 * 1024;
        storageField.setText(String.format("%.2f GB of %.2f GB used", localAccount.used / GB, localAccount.capacity / GB));
        long xpx = NetworkUtils.getXPX(localAccount);
        xpxField.setText(String.format("%d xpx", xpx));
        if (xpx == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING, quest, ButtonType.YES, ButtonType.NO);
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(getMainApp().getIcon());
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                xpxBtn(null);
            }
        }
    }

    @FXML
    protected void saveBtn(ActionEvent event) {
        try {
            Signup2Dialog dlg = new Signup2Dialog(localAccount);
            dlg.openWindow(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    protected void xpxBtn(ActionEvent event) {
        String url = "http://bctestnetfaucet.xpxsirius.io/#/";
        if ("MAIN_NET".equals(localAccount.getNetwork())) {
            url = "https://www.proximax.io/xpx";
        }
        final String xpxUrl = url;
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(xpxUrl));
                } catch (Exception ex) {
                }
            }).start();
        }
    }

    @Override
    protected void dispose() {
    }

    @Override
    public String getTitle() {
        return CONST.USERDLG_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.USERDLG_FXML;
    }

}
