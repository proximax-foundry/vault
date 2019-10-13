package io.proximax.app.db;

import io.proximax.sdk.model.account.Account;
import io.proximax.connection.ConnectionConfig;
import io.proximax.connection.IpfsConnection;
import io.proximax.app.utils.AccountHelpers;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.CryptoUtils;
import io.proximax.app.utils.NetworkUtils;
import io.proximax.connection.BlockchainNetworkConnection;
import io.proximax.utils.NemUtils;
import java.util.List;

public final class LocalAccount {

    public String fullName;
    public String password;
    public String network;
    public String privateKey;
    public String publicKey;
    public String address;
    public String licenseKey;
    public int status;
    public int type;
    public double xpx;
    public double used;
    public double capacity;

    public Account nemAccount = null;
    public String version;
    public ConnectionConfig connectionConfig = null;
    private int nodeIndex = -1;

    public LocalAccount(String fullName, String password, String network, String privateKey, String publicKey, String address, String version) {
        this.fullName = fullName;
        this.password = password;
        this.network = network;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.version = version;
        NetworkConfiguration netconf = getNetworkConfiguration();
        if (netconf != null) {
            nemAccount = new NemUtils(netconf.getNetworkType()).getAccount(privateKey);
        }
    }

    public LocalAccount(String fullName, String password, String network, String privateKey, String publicKey, String address, String version, int type, int status) {
        this.fullName = fullName;
        this.password = password;
        this.network = network;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.version = version;
        this.type = type;
        this.status = status;
        NetworkConfiguration netconf = getNetworkConfiguration();
        if (netconf != null) {
            nemAccount = new NemUtils(netconf.getNetworkType()).getAccount(privateKey);
        }
    }

    public boolean createConnection() {
        if (isConnected()) {
            return true;
        }
        boolean bDone = false;
        do {
            try {
                int ret = connectNextNode();
                if (ret == -1) {
                    break;
                } else if (ret == 1) {
                    bDone = true;
                }
            } catch (Exception ex) {
                bDone = false;
            }
        } while (!bDone);
        return bDone;
    }

    public void setConnectionIndex(int idx) {
        disconnect();
        nodeIndex = idx;
    }

    public boolean connectToNode(int idx) {
        try {
            disconnect();
            NetworkConfiguration netconf = getNetworkConfiguration();
            if (NetworkUtils.testNode(netconf.name, netconf.getApiUrl(idx))) {
                nodeIndex = idx;
                connectionConfig = netconf.createWithIpfsConnection(nodeIndex);
                return isConnected();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public int connectNextNode() {
        int idx = nodeIndex;
        NetworkConfiguration netconf = getNetworkConfiguration();
        if (netconf == null) {
            netconf = NetworkUtils.getNetworkConfiguration(network);
        }
        if (idx < netconf.nodeList.size() - 1) {
            idx++;
        } else {
            return -1;
        }
        if (connectToNode(idx)) {
            return 1;
        }
        nodeIndex = idx;
        return 0;
    }

    public boolean testNode() {
        NetworkConfiguration netconf = getNetworkConfiguration();
        if (NetworkUtils.testNode(netconf.name, netconf.getApiUrl(nodeIndex))) {
            if (!isConnected()) {
                connectionConfig = netconf.createWithIpfsConnection(nodeIndex);
            }
            return isConnected();
        }
        return false;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public String getCurrentNode() {
        NetworkConfiguration netconf = getNetworkConfiguration();
        return netconf.getNodes().get(nodeIndex);
    }

    public String getApiHost() {
        return connectionConfig.getBlockchainNetworkConnection().getApiHost();
    }

    public String getApiUrl() {
        return connectionConfig.getBlockchainNetworkConnection().getApiUrl();
    }

    public int getApiPort() {
        return connectionConfig.getBlockchainNetworkConnection().getApiPort();
    }

    public String getNetwork() {
        return network.toLowerCase();
    }

    public void disconnect() {
        connectionConfig = null;
    }

    public List<String> getNodes() {
        NetworkConfiguration netconf = getNetworkConfiguration();
        return netconf.getNodes();
    }

    public int getCurrentNodeIndex() {
        return nodeIndex;
    }

    public boolean isConnected() {
        return (connectionConfig != null);
    }

    public String getPeerId() {
        try {
            return ((IpfsConnection) connectionConfig.getFileStorageConnection()).getIpfs().config.show().get("Identity").toString();
        } catch (Exception ex) {
        }
        return "No Connection";
    }

    public String toString() {
        return new String().format("usr: %s-pass: %s-net: %s-pri: %s-pub: %s-add: %s", fullName, password, network, privateKey, publicKey, address);
    }

    public String getAddressPretty() {
        return AccountHelpers.formatAddressPretty(address);
    }

    public NetworkConfiguration getNetworkConfiguration() {
        return NetworkUtils.getNetworkConfiguration(network);
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public String getEncPassword() {
        try {
            return CryptoUtils.decryptToBase64String(password, CONST.ENC_STRING);
        } catch (Exception ex) {
        }
        return null;
    }
    
    public BlockchainNetworkConnection getBlockchainNetworkConnection() {
        return connectionConfig.getBlockchainNetworkConnection();
    }

}
