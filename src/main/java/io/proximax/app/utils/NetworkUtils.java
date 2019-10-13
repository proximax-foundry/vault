package io.proximax.app.utils;

import io.proximax.sdk.model.account.Account;
import io.proximax.sdk.model.account.AccountInfo;
import io.proximax.sdk.model.account.Address;
import io.proximax.sdk.model.blockchain.NetworkType;
import io.proximax.sdk.model.mosaic.Mosaic;
import io.proximax.sdk.model.transaction.Deadline;
import io.proximax.sdk.model.transaction.PlainMessage;
import io.proximax.sdk.model.transaction.SignedTransaction;
import io.proximax.sdk.model.transaction.TransferTransaction;
import io.proximax.app.db.LocalAccount;
import io.proximax.app.db.NetworkConfiguration;
import io.proximax.connection.BlockchainNetworkConnection;
import io.proximax.connection.ConnectionConfig;
import io.proximax.connection.HttpProtocol;
import io.proximax.connection.IpfsConnection;
import io.proximax.model.BlockchainNetworkType;
import io.proximax.sdk.AccountRepository;
import io.proximax.sdk.BlockchainApi;
import io.proximax.sdk.FeeCalculationStrategy;
import io.proximax.sdk.MosaicRepository;
import io.proximax.sdk.TransactionRepository;
import io.proximax.sdk.model.mosaic.MosaicId;
import io.proximax.sdk.model.mosaic.MosaicNames;
import io.proximax.sdk.model.mosaic.NetworkCurrencyMosaic;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import java.net.URL;
import java.net.URLDecoder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author thcao
 */
public class NetworkUtils {

    public static String NETWORK_DEFAULT = "TEST_NET";
    public static final String LOCALHOST = "127.0.0.1";
    public static final String BEARER_TOKEN = "11111";
    public static int NODE_DEFAULT_PORT = 3000;
    public static int NODE_DEFAULT_WSPORT = 7778;
    public static int CONFIG_VERSION = 0;
    public final static BigInteger XPX_10 = BigInteger.valueOf(100000);
    public static Map<String, NetworkConfiguration> NETWORKS = new HashMap<>();
    public static final List<String> NETWORK_SUPPORT = new ArrayList<>();
    public static final List<String> IPFS_SERVER = new ArrayList<>();
    public static final String IPFS_LOCALHOST_URL = "http://localhost:8080/ipfs/";
    public static int IPFS_PORT = 5001;

    static {
        //force override config - need remove when release 
        try {
            boolean bOverride = false;
            String dbName = System.getProperty("user.home") + File.separator + CONST.APP_FOLDER + File.separator + CONST.DB_APP_CONFIGURATION;
            if (new File(dbName).exists()) {
                int verRes = DBHelpers.getAppConfigVerFromRes();
                int verLocal = DBHelpers.getAppConfigVerFromLocal();
                if (verRes > verLocal) {
                    bOverride = true;
                }
            }
            if (bOverride) {
                URL configFile = NetworkUtils.class.getResource("/" + CONST.DB_APP_CONFIGURATION);
                FileUtils.copyURLToFile(configFile, new File(dbName));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initDefaultNetwork();
    }

    public static void initDefaultNetwork() {
        try {
            Map<String, String> configs = DBHelpers.getAppConfiguration();
            if (configs != null) {
                NETWORK_SUPPORT.addAll(Arrays.asList(configs.get("networks").split(",")));
                NETWORK_DEFAULT = configs.get("default");
                CONFIG_VERSION = StringUtils.parseInt(configs.get("version"), 0);
                NODE_DEFAULT_PORT = StringUtils.parseInt(configs.get("port"), 3000);
                NODE_DEFAULT_WSPORT = StringUtils.parseInt(configs.get("wsport"), 7778);
                IPFS_PORT = StringUtils.parseInt(configs.get("ipfsport"), 5001);
                IPFS_SERVER.addAll(Arrays.asList(configs.get("ipfs").split(",")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        loadNetworkConfig();
    }

    public static boolean isNetworkSupport(String network) {
        return NETWORKS.containsKey(network);
    }

    public static void loadNetworkConfig() {
        NETWORKS.clear();
        for (String network : NETWORK_SUPPORT) {
            try {
                NetworkConfiguration netconf = DBHelpers.getNetworkConfiguration(network);
                if (netconf.status > 0) {
                    put(network, netconf);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void put(String name, NetworkConfiguration network) {
        NETWORKS.put(name, network);
    }

    public static NetworkConfiguration get(String name) {
        return NETWORKS.get(name);
    }

    public static BlockchainNetworkType getBlockchainNetworkType(String network) {
        return BlockchainNetworkType.fromString(network);
    }

    public static NetworkConfiguration getNetworkConfiguration(String network) {
        return NETWORKS.get(network);
    }

    public static NetworkType getNetworkType(String network) {
        return NetworkType.valueOf(network);
    }

    public static long getXPXAmount(NetworkType networkType, String apiUrl, String privateKey) {
        try {
            final Account account = Account.createFromPrivateKey(privateKey, networkType);
            BlockchainApi blockchainApi = new BlockchainApi(new URL(apiUrl), networkType);
            return getXPXAmount(blockchainApi, account.getAddress());
        } catch (Exception ex) {
        }
        return 0;
    }

    public static long getXPXAmountWallet(NetworkType networkType, String apiUrl, String address) {
        try {
            BlockchainApi blockchainApi = new BlockchainApi(new URL(apiUrl), networkType);
            return getXPXAmount(blockchainApi, Address.createFromRawAddress(address));
        } catch (Exception ex) {
        }
        return 0;
    }

    public static long getXPXAmount(BlockchainApi blockchainApi, Address address) throws Exception {
        AccountRepository accountHttp = blockchainApi.createAccountRepository();
        AccountInfo accountInfo = accountHttp.getAccountInfo(address).toFuture().get();
        List<MosaicId> mosaicIds = new ArrayList<>();
        List<Mosaic> mosaics = accountInfo.getMosaics();
        for (Mosaic mo : mosaics) {
            mosaicIds.add(new MosaicId(mo.getId().getId()));
        }
        MosaicRepository mosaicHttp = blockchainApi.createMosaicRepository();
        List<MosaicNames> l = mosaicHttp.getMosaicNames(mosaicIds).toFuture().get();
        for (int i = 0; i < l.size(); i++) {
            MosaicNames mo = l.get(i);
            if (mo.getNames().contains(NetworkCurrencyMosaic.MOSAIC_NAMESPACE)) {
                return mosaics.get(i).getAmount().divide(BigDecimal.valueOf(Math.pow(10, NetworkCurrencyMosaic.DIVISIBILITY)).toBigInteger()).longValue();
            }
        }
        return 0;
    }

    public static long getXPX(LocalAccount localAccount) {
        NetworkConfiguration netconf = get(localAccount.network);
        try {
            //send thanks to supporter, public key will have in network
            return getXPXAmount(netconf.getNetworkType(), localAccount.getApiUrl(), localAccount.privateKey);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static boolean activeAccount1(LocalAccount localAccount) {
        NetworkConfiguration netconf = get(localAccount.network);
        try {
            //send thanks to supporter, public key will have in network
            sendPlainMessage(netconf.getNetworkType(), localAccount.getApiUrl(), localAccount.privateKey, netconf.enc1, "Thanks from" + localAccount.fullName);
            Thread.sleep(1000);
            localAccount.status = 1;

            DBHelpers.updateUserStatus(localAccount.fullName, localAccount.network, 1);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean activeAccount(LocalAccount localAccount) {
        if (localAccount.status == 0) {
            try {
                return activeAccount1(localAccount);
            } catch (Exception ex) {
                return false;
            }
        }
        return true;

    }

    public static String sendXPX(NetworkType networkType, String apiUrl, String senderPrivateKey, String recipientAddress, BigInteger xpxValue) throws Exception {
        final Account account = Account.createFromPrivateKey(senderPrivateKey, networkType);
        BlockchainApi blockchainApi = new BlockchainApi(new URL(apiUrl), networkType);

        //	10 XPX BigInteger.valueOf(100000)
        final TransferTransaction transferTransaction = blockchainApi.transact().transfer()
                .deadline(Deadline.create(1, ChronoUnit.HOURS))
                .to(Address.createFromRawAddress(recipientAddress))
                .mosaics(Arrays.asList(NetworkCurrencyMosaic.TEN))
                .message(PlainMessage.create(new String("[" + CONST.APP_NAME + "] - Welcome to Proximax - init 10 XPXs")))
                .networkType(networkType)
                .build();
        final SignedTransaction signedTransaction = blockchainApi.sign(transferTransaction, account);
        final TransactionRepository transactionHttp = (TransactionRepository) blockchainApi.createTransactionRepository();

        transactionHttp.announce(signedTransaction).toFuture().get();
        System.out.println("Transaction Hash: " + signedTransaction.getHash());
        return signedTransaction.getHash();
    }

    public static String sendPlainMessage(NetworkType networkType, String apiUrl, String senderPrivateKey, String recipientAddress, String message) throws Exception {
        final Account account = Account.createFromPrivateKey(senderPrivateKey, networkType);
        BlockchainApi blockchainApi = new BlockchainApi(new URL(apiUrl), networkType);
        final TransferTransaction transferTransaction = blockchainApi.transact().transfer()
                .deadline(Deadline.create(1, ChronoUnit.HOURS))
                .to(Address.createFromRawAddress(recipientAddress))
                //.mosaics(new ArrayList<Mosaic>())
                .mosaics(Arrays.asList(NetworkCurrencyMosaic.createAbsolute(BigInteger.ZERO)))
                .feeCalculationStrategy(FeeCalculationStrategy.ZERO)
                .message(PlainMessage.create(new String("[" + CONST.APP_NAME + "] ") + message))
                .networkType(networkType)
                .build();
        final SignedTransaction signedTransaction = blockchainApi.sign(transferTransaction, account);
        final TransactionRepository transactionHttp = (TransactionRepository) blockchainApi.createTransactionRepository();
        transactionHttp.announce(signedTransaction).toFuture().get();
        System.out.println("Transaction Hash: " + signedTransaction.getHash());
        return signedTransaction.getHash();
    }

    public static ConnectionConfig createConnectionConfig(String url, int port, String network, String ipfsUrl, int ipfsPort) {
        return ConnectionConfig.createWithLocalIpfsConnection(
                createBlockchainNetworkConnection(
                        NetworkUtils.getBlockchainNetworkType(network),
                        url,
                        port),
                createIpfsConnection(
                        ipfsUrl,
                        ipfsPort));
    }

    public static BlockchainNetworkConnection createBlockchainNetworkConnection(BlockchainNetworkType networkType, String apiUrl, int port) {
        return new BlockchainNetworkConnection(
                networkType,
                apiUrl,
                port,
                HttpProtocol.HTTP);
    }

    public static IpfsConnection createIpfsConnection(String ipfsUrl, int ipfsPort) {
        return new IpfsConnection(
                ipfsUrl,
                ipfsPort);
    }

    /**
     * Http Get.
     *
     * @param host
     * @param port
     * @param requestUrl the request url
     * @return the string
     */
    public static String sendGetHTTP(String host, int port, String requestUrl) {
        String result = "";
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = "http://" + host + ":" + port + requestUrl;
            url = URLDecoder.decode(url, "UTF-8");
            HttpGet method = new HttpGet(url);
            response = httpClient.execute(method);
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean testNode(String network, String url) {
        try {
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            URI u = new URI(url);
            int port = u.getPort();
            if (port == -1) {
                url = url + ":" + NetworkUtils.NODE_DEFAULT_PORT;
            }
            final BlockchainApi blockchainApi = new BlockchainApi(new URL(url), NetworkUtils.getNetworkType(network));
            final NetworkType networkType = blockchainApi.getNetworkType();
            if (getNetworkType(network) == networkType) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;

    }

    public static boolean testIpfs(String url) {
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
            IpfsConnection conn = new IpfsConnection(host, port);
            return (conn != null);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    public static int testAccount(LocalAccount localAccount) {
        try {
            if (localAccount.createConnection()) {
                AccountRepository accountHttp = (AccountRepository) localAccount.getBlockchainNetworkConnection().getBlockchainApi().createAccountRepository();
                AccountInfo accountInfo = accountHttp.getAccountInfo(Address.createFromRawAddress(localAccount.address)).toFuture().get();
                if (localAccount.address.equals(accountInfo.getAddress().plain())) {
                    return 1;
                }
                return 0;
            }
        } catch (ExecutionException ex1) {
            ex1.printStackTrace();
            String msg = ex1.getMessage();
            if (msg.contains("Not Found") || msg.contains("Conflict")) {
                return 0;
            }

        } catch (InterruptedException ex2) {
            ex2.printStackTrace();
        }
        return -1; //server issue

    }

    public static String getIpfs(int idx) {
        return IPFS_SERVER.get(idx);
    }

}
