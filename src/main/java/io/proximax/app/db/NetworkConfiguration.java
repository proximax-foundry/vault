package io.proximax.app.db;

import io.proximax.sdk.model.blockchain.NetworkType;
import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.NetworkUtils;
import io.proximax.app.utils.StringUtils;
import io.proximax.connection.BlockchainNetworkConnection;
import io.proximax.connection.ConnectionConfig;
import io.proximax.connection.HttpProtocol;
import io.proximax.model.BlockchainNetworkType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thcao
 */
public class NetworkConfiguration {

    public int id;
    public int value;
    public List<BlockchainNetworkConnection> nodeList = new ArrayList<>();
    public List<IpfsConnection> ipfsList = new ArrayList<>();
    public int status;
    public String name;
    public String enc1;
    public String enc2;
    public String enc3;
    public boolean modified;
    private String remoteUrl;

    public NetworkConfiguration(int id, String name, int value, int status, String nodes, String ipfs) {
        this.id = id;
        this.value = value;
        this.name = name;
        this.status = status;
        //only parse network active
        if (status == 1) {
            parseNodes(nodes);
            parseIpfs(ipfs);
        }
    }

    private void parseNodes(String str) {
        if (!StringUtils.isEmpty(str)) {
            String[] nodeArr = str.split(",");
            for (String n : nodeArr) {
                addNode(n);
            }
        }
    }

    public boolean addNode(String url) {
        try {
            String host;
            int port = NetworkUtils.NODE_DEFAULT_PORT;
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            URI u = new URI(url);
            host = u.getHost();
            port = u.getPort();
            if (port == -1) {
                port = NetworkUtils.NODE_DEFAULT_PORT;
            }
            BlockchainNetworkConnection blNode = new BlockchainNetworkConnection(
                    getBlockchainNetworkType(),
                    host,
                    port,
                    HttpProtocol.HTTP);
            return nodeList.add(blNode);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public BlockchainNetworkConnection removeNode(int idx) {
        return nodeList.remove(idx);
    }

    public IpfsConnection removeIpfs(int idx) {
        return ipfsList.remove(idx);
    }

    public void moveUpNode(int idx) {
        if (idx > 0) {
            BlockchainNetworkConnection curItem = nodeList.get(idx);
            BlockchainNetworkConnection prevItem = nodeList.set(idx - 1, curItem);
            nodeList.set(idx, prevItem);
        }
    }

    public void moveUpIpfs(int idx) {
        if (idx > 0) {
            IpfsConnection curItem = ipfsList.get(idx);
            IpfsConnection prevItem = ipfsList.set(idx - 1, curItem);
            ipfsList.set(idx, prevItem);
        }
    }

    private void parseIpfs(String str) {
        boolean bLocal = false;
        if (!StringUtils.isEmpty(str)) {
            String[] ipfsArr = str.split(",");
            for (String n : ipfsArr) {
                IpfsConnection conn = addIpfs(n);
                if (conn.isLocalHost()) {
                    bLocal = true;
                }
            }
        } else {
            for (String n : NetworkUtils.IPFS_SERVER) {
                IpfsConnection conn = addIpfs(n);
                if (conn.isLocalHost()) {
                    bLocal = true;
                }
            }
        }
        if (IApp.isLocalIPFS() && !bLocal) {
            ipfsList.add(0, new IpfsConnection(NetworkUtils.LOCALHOST, NetworkUtils.IPFS_PORT));
        }
    }

    public IpfsConnection addIpfs(String url) {
        try {
            String host;
            int port = NetworkUtils.IPFS_PORT;
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            URI u = new URI(url);
            host = u.getHost();
            port = u.getPort();
            if (port == -1) {
                port = NetworkUtils.IPFS_PORT;
            }
            IpfsConnection conn = new IpfsConnection(u.getScheme(), host, port);
            ipfsList.add(conn);
            return conn;
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        return null;

    }

    public List<String> getNodes() {
        List<String> arrNode = new ArrayList<>();
        for (BlockchainNetworkConnection blNode : nodeList) {
            arrNode.add(blNode.getApiHost());
        }
        return arrNode;
    }

    public List<String> getIpfses() {
        List<String> arrIpfs = new ArrayList<>();
        for (IpfsConnection ipfs : ipfsList) {
            arrIpfs.add(ipfs.getApiHost());
        }
        return arrIpfs;
    }

    public BlockchainNetworkConnection getNode(int idx) {
        return nodeList.get(idx);
    }

    public IpfsConnection getIpfs(int idx) {
        return ipfsList.get(idx);
    }

    public String getApiUrl(int idx) {
        return nodeList.get(idx).getApiUrl();
    }

    public String getApiHost(int idx) {
        return nodeList.get(idx).getApiHost();
    }

    public int getApiPort(int idx) {
        return nodeList.get(idx).getApiPort();
    }

    public String getIpfsHost(int idx) {
        String url;
        if (ipfsList.isEmpty()) {
            url = NetworkUtils.getIpfs(idx);
        } else {
            return ipfsList.get(idx).getApiHost();
        }
        try {
            URI a = new URI(url);
            return a.getHost();
        } catch (Exception ex) {
        }
        return url;
    }

    public int getIpfsPort(int idx) {
        String url;
        if (ipfsList.isEmpty()) {
            url = NetworkUtils.getIpfs(idx);
        } else {
            return ipfsList.get(idx).getApiPort();
        }
        try {
            URI a = new URI(url);
            return a.getPort();
        } catch (Exception ex) {
        }
        return NetworkUtils.IPFS_PORT;
    }

    public NetworkType getNetworkType() {
        return NetworkUtils.getNetworkType(name);
    }

    public BlockchainNetworkType getBlockchainNetworkType() {
        return NetworkUtils.getBlockchainNetworkType(name);
    }

    public ConnectionConfig createWithIpfsConnection(int idx) {
        return ConnectionConfig.createWithLocalIpfsConnection(
                getNode(idx),
                getIpfs(0));
    }

    public String getDownloadUrl(int idx) {
        return getIpfs(idx).getDownloadUrl();
    }

    public String getDownloadUrl() {
        if (StringUtils.isEmpty(remoteUrl)) {
            for (IpfsConnection conn : ipfsList) {
                if (!conn.isLocalHost()) {
                    remoteUrl = conn.getDownloadUrl();
                    return remoteUrl;
                }
            }
        }
        if (StringUtils.isEmpty(remoteUrl)) {
            return NetworkUtils.IPFS_LOCALHOST_URL;
        } else {
            return remoteUrl;
        }
    }
}
