package io.proximax.app.db;

import io.proximax.app.utils.NetworkUtils;

/**
 *
 * @author thcao
 */
public class IpfsConnection extends io.proximax.connection.IpfsConnection {

    private final String schema;

    public IpfsConnection(String apiHost, int apiPort) {
        super(apiHost, apiPort);
        this.schema = "http";
    }

    public IpfsConnection(String schema, String apiHost, int apiPort) {
        super(apiHost, apiPort);
        this.schema = schema;
    }

    public String getDownloadUrl() {
        if (getApiHost().equals(NetworkUtils.LOCALHOST)) {
            return NetworkUtils.IPFS_LOCALHOST_URL;
        } else {
            return String.format("%s://%s/ipfs/", schema, getApiHost());
        }
    }

    public boolean isLocalHost() {
        return (getApiHost().equalsIgnoreCase("127.0.0.1") || getApiHost().equalsIgnoreCase("localhost"));
    }
}
