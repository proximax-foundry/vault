package io.proximax.app.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import io.proximax.app.db.NetworkConfiguration;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.DBHelpers;
import io.proximax.app.utils.NetworkUtils;
import io.proximax.app.utils.StringUtils;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

/**
 *
 * @author thcao
 */
public class NetworkDialog extends AbstractController {

    @FXML
    private JFXComboBox<String> networkCbx;

    @FXML
    private JFXTextField nodeField;

    @FXML
    private JFXTextField ipfsField;

    @FXML
    private ListView<String> nodesLv;

    @FXML
    private JFXButton addNodeBtn;

    @FXML
    private JFXButton addIpfsBtn;

    @FXML
    private JFXListView<String> ipfsLv;

    private String network = NetworkUtils.NETWORK_DEFAULT;

    private Map<String, NetworkConfiguration> configs;

    private boolean modified = false;

    public NetworkDialog() {
        super(true);
    }

    @Override
    protected void initialize() {
        try {
            configs = DBHelpers.getNetworkConfiguration(-1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ObservableList<String> obList = FXCollections.observableList(NetworkUtils.NETWORK_SUPPORT);
        networkCbx.setItems(obList);
        networkCbx.setValue(network);
        onSelectedNetwork(network);

        networkCbx.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!network.equals(newValue)) {
                onSelectedNetwork(newValue);
            }
        });

        addNodeBtn.disableProperty().bind(nodeField.textProperty().isEmpty());
        addIpfsBtn.disableProperty().bind(ipfsField.textProperty().isEmpty());

    }

    @Override
    protected void dispose() {

    }

    @Override
    public String getTitle() {
        return CONST.NETWORKDLG_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.NETWORKDLG_FXML;
    }

    @FXML
    void saveBtn(ActionEvent event) {
        for (String net : NetworkUtils.NETWORK_SUPPORT) {
            NetworkConfiguration netconf = configs.get(net);
            if (netconf.modified) {
                try {
                    DBHelpers.updateNetworkConfiguration(netconf);
                    netconf.modified = false;
                } catch (Exception ex) {
                }
            }
        }
        if (modified) {
            NetworkUtils.loadNetworkConfig();
            modified = false;
            setButtonType(ButtonType.OK);
        } else {
            setButtonType(ButtonType.CLOSE);
        }
        close();
    }

    @FXML
    void addNodeBtn(ActionEvent event) {
        try {
            String str = nodeField.getText().trim();
            if (!StringUtils.isEmpty(str)) {
                if (!nodesLv.getItems().contains(str)) {
                    if (NetworkUtils.testNode(network, str)) {
                        NetworkConfiguration netconf = configs.get(network);
                        netconf.addNode(str);
                        nodesLv.getItems().add(str);
                        netconf.modified = true;
                        modified = true;
                    } else {
                        ErrorDialog.showError(this, "Invalid catapult server");
                    }
                }
            }
        } catch (Exception ex) {
            ErrorDialog.showError(this, "Invalid catapult server");
        }
    }

    @FXML
    void removeNodeBtn(ActionEvent event) {
        int idx = nodesLv.getSelectionModel().getSelectedIndex();
        if (idx != -1) {
            nodesLv.getItems().remove(idx);

            NetworkConfiguration netconf = configs.get(network);
            netconf.removeNode(idx);
            netconf.modified = true;
            modified = true;
        }
    }

    @FXML
    void upNodeBtn(ActionEvent event) {
        int idx = nodesLv.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            String item1 = nodesLv.getSelectionModel().getSelectedItem();
            String item2 = nodesLv.getItems().get(idx - 1);
            nodesLv.getItems().set(idx - 1, item1);
            nodesLv.getItems().set(idx, item2);
            nodesLv.getSelectionModel().select(idx - 1);

            NetworkConfiguration netconf = configs.get(network);
            netconf.moveUpNode(idx);
            netconf.modified = true;
            modified = true;
        }
    }

    @FXML
    void addIpfsBtn(ActionEvent event) {
        try {
            String str = ipfsField.getText().trim();
            if (!StringUtils.isEmpty(str)) {
                if (!ipfsLv.getItems().contains(str)) {
                    NetworkConfiguration netconf = configs.get(network);
                    if (netconf.addIpfs(str) != null) {
                        ipfsLv.getItems().add(str);
                        netconf.modified = true;
                        modified = true;
                    } else {
                        ErrorDialog.showError(this, "Invalid ipfs server");
                    }
                }
            }
        } catch (Exception ex) {
            ErrorDialog.showError(this, "Invalid ipfs server");
        }
    }

    @FXML
    void removeIpfsBtn(ActionEvent event) {
        int idx = ipfsLv.getSelectionModel().getSelectedIndex();
        if (idx != -1) {
            ipfsLv.getItems().remove(idx);
            NetworkConfiguration netconf = configs.get(network);
            netconf.removeIpfs(idx);
            netconf.modified = true;
            modified = true;
        }
    }

    @FXML
    void upIpfsBtn(ActionEvent event) {
        int idx = ipfsLv.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            String item1 = ipfsLv.getSelectionModel().getSelectedItem();
            String item2 = ipfsLv.getItems().get(idx - 1);
            ipfsLv.getItems().set(idx - 1, item1);
            ipfsLv.getItems().set(idx, item2);
            ipfsLv.getSelectionModel().select(idx - 1);

            NetworkConfiguration netconf = configs.get(network);
            netconf.moveUpIpfs(idx);
            netconf.modified = true;
            modified = true;
        }
    }

    private void onSelectedNetwork(String newNetwork) {
        try {
            network = newNetwork;
            NetworkConfiguration netconf = configs.get(newNetwork);
            ObservableList<String> nodeList = FXCollections.observableArrayList();
            nodeList.addAll(netconf.getNodes());
            nodesLv.setItems(nodeList);
            ObservableList<String> ipfsList = FXCollections.observableArrayList();
            ipfsList.addAll(netconf.getIpfses());
            ipfsLv.setItems(ipfsList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
