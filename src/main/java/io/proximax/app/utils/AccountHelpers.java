package io.proximax.app.utils;

import java.io.File;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.proximax.sdk.model.account.Account;
import io.proximax.app.db.LocalAccount;
import java.util.ArrayList;
import java.util.List;
import io.proximax.sdk.model.account.Address;
import io.proximax.app.recovery.AccountInfo;

public class AccountHelpers {

    public static boolean isExistAccount(String fullName, String network) {
        String fileName = System.getProperty("user.home") + File.separator + CONST.APP_FOLDER + File.separator + "users" + File.separator + network + File.separator + fullName + ".wlt";
        return new File(fileName).exists();
    }

    public static String getHomeAccount(String fullName, String network) {
        return System.getProperty("user.home") + File.separator + CONST.APP_FOLDER + File.separator + "users" + File.separator + network + File.separator;
    }

    public static boolean isExistAccountDB(String fullName, String network) {
        try {
            LocalAccount account = DBHelpers.getUser(fullName, network);
            if (account != null) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static void initUserHome() {
        new File(System.getProperty("user.home") + File.separator + CONST.APP_FOLDER + File.separator + "node").mkdirs();
        for (String network : NetworkUtils.NETWORK_SUPPORT) {
            new File(System.getProperty("user.home") + File.separator + CONST.APP_FOLDER + File.separator + "users" + File.separator + network).mkdirs();
        }
        System.setProperty("user.dir", System.getProperty("user.home") + File.separator + CONST.APP_FOLDER);
        System.setProperty("vertx.httpServiceFactory.cacheDir", System.getProperty("user.dir"));
    }

    public static List<String> getAccounts() {
        List<String> list = new ArrayList<>();
        for (String network : NetworkUtils.NETWORK_SUPPORT) {
            String appDir = System.getProperty("user.home") + File.separator + CONST.APP_FOLDER
                    + File.separator + "users" + File.separator + network;
            File file = new File(appDir);
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    String fileName = child.getName();
                    if (child.isFile() && fileName.endsWith(".wlt")) {
                        String userName = fileName.substring(0, fileName.lastIndexOf(".wlt"));
                        if (isExistAccountDB(userName, network)) {
                            list.add(network + File.separator + userName);
                        }
                    }
                }
            }
        }
        return list;
    }

    public static void addAccountDB(String fullName, String password, String network, String encodedPrivateKey,
            String encodedPublicKey, String encodedAddress) throws Exception {
        LocalAccount account = new LocalAccount(fullName, password, network, encodedPrivateKey, encodedPublicKey, encodedAddress, CONST.DB_VERSION);
        addAccountDB(account);
    }

    public static void addAccountDB(LocalAccount account) throws Exception {
        if (!isExistAccount(account.fullName, account.network)) {
            DBHelpers.createUserDB(account.fullName, account.network);
            String password = CryptoUtils.encryptToBase64String(account.password.getBytes(), CONST.ENC_STRING);
            DBHelpers.addUser(account.fullName, password, account.network, account.privateKey, account.publicKey, account.address);
        }
    }

    public static void updateAccountDB(String fullName, String password, String network, String encodedPrivateKey,
            String encodedPublicKey, String encodedAddress) throws Exception {
        LocalAccount account = new LocalAccount(fullName, password, network, encodedPrivateKey, encodedPublicKey, encodedAddress, CONST.DB_VERSION);
        updateAccountDB(account);
    }

    public static void updateAccountDB(LocalAccount account) throws Exception {
        String password = CryptoUtils.encryptToBase64String(account.password.getBytes(), CONST.ENC_STRING);
        DBHelpers.updateUser(account.fullName, password, account.network, account.privateKey, account.publicKey, account.address);
    }

    public static LocalAccount loginDB(String fullName, String network, String password) throws Exception {
        LocalAccount account = DBHelpers.getUser(fullName, network);
        if (account != null) {
            String enc_password = CryptoUtils.encryptToBase64String(password.getBytes(), CONST.ENC_STRING);
            if (account.password.equals(enc_password)) {
                if (account.status == 0 || account.status == 1) {
                    boolean bDone = false;
                    do {
                        try {
                            int ret = account.connectNextNode();
                            if (ret == -1) {
                                break;
                            } else if (ret == 1) {
                                bDone = NetworkUtils.activeAccount(account);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            bDone = false;
                        }
                    } while (!bDone);
                }
                return account;
            }
        }
        return null;
    }

    public static LocalAccount login(String fullName, String network, String password) throws Exception {
        LocalAccount account = DBHelpers.getUser(fullName, network);
        if (account != null) {
            String enc_password = CryptoUtils.encryptToBase64String(password.getBytes(), CONST.ENC_STRING);
            if (account.password.equals(enc_password)) {
                return account;
            }
        }
        return null;
    }

    /**
     * get public Key from address.
     *
     * @param apiUrl
     * @param apiPort
     * @param addressString the address string
     * @return the public key from address
     */
    public static String getPublicKeyFromAddress(String apiUrl, int apiPort, String addressString) {
        String queryResult = NetworkUtils.sendGetHTTP(apiUrl, apiPort, CONST.URL_ACCOUNT_GET + File.separator + addressString);
        Gson gson = new Gson();
        JsonObject queryAccount = gson.fromJson(queryResult, JsonObject.class);
        return queryAccount.get("account").getAsJsonObject().get("publicKey").getAsString();
    }

    /**
     * Test if a string is hexadecimal
     *
     * @param str - A string to test
     *
     * @return True if correct, false otherwise
     */
    public static boolean isHexadecimal(String str) {
        return str.matches("^(0x|0X)?[a-fA-F0-9]+$");
    }

    /**
     * Check if a private key is valid
     *
     * @param privateKey A private key
     * @return True if valid, false otherwise
     */
    public static boolean isPrivateKeyValid(String privateKey) {
        if (privateKey.length() != 64 && privateKey.length() != 66) {
            return false;
        } else if (!isHexadecimal(privateKey)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if a public key is valid
     *
     * @param publicKey A public key
     *
     * @return True if valid, false otherwise
     */
    public static boolean isPublicKeyValid(String publicKey) {
        if (publicKey.length() != 64) {
            return false;
        } else if (!isHexadecimal(publicKey)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if a address is valid
     *
     * @param address A address
     *
     * @return True if valid, false otherwise
     */
    public static boolean isAddressValid(String address) {
        try {
            Address addr = Address.createFromRawAddress(address);
            return addr != null;
        } catch (Exception ex) {
        }
        return false;
    }

    public static LocalAccount createAccount(String fullName, String network, String password, String privateKey) throws Exception {
        Account nemAccount = null;
        if (StringUtils.isEmpty(privateKey)) { //create new account
            nemAccount = Account.generateNewAccount(NetworkUtils.getNetworkType(network));
        } else {
            if (isPrivateKeyValid(privateKey)) {
                nemAccount = Account.createFromPrivateKey(privateKey, NetworkUtils.getNetworkType(network));
            } else {
                throw new Exception("Provided private key is not valid!");
            }
        }
        if (nemAccount != null) {
            AccountHelpers.addAccountDB(new LocalAccount(fullName, password, network, nemAccount.getPrivateKey(), nemAccount.getPublicKey(), nemAccount.getAddress().plain(), CONST.DB_VERSION));
            return loginDB(fullName, network, password);
        }
        return null;
    }

    public static String formatAddressPretty(String address) {
        return new StringBuilder().append(address.substring(0, 6)).
                append("-").
                append(address.substring(6, 6 + 6)).
                append("-").
                append(address.substring(6 * 2, 6 * 2 + 6)).
                append("-").
                append(address.substring(6 * 3, 6 * 3 + 6)).
                append("-").
                append(address.substring(6 * 4, 6 * 4 + 6)).
                append("-").
                append(address.substring(6 * 5, 6 * 5 + 6)).
                append("-").
                append(address.substring(6 * 6, 6 * 6 + 4)).toString();
    }

    public static AccountInfo getAccountInfo(LocalAccount localAccount) {
        AccountInfo account = null;
        try {
            account = DBHelpers.getAccountInfo(localAccount.fullName, localAccount.network);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return account;
    }

    public static void updateAccountInfo(LocalAccount localAccount, AccountInfo accountInfo) {
        try {
            DBHelpers.updateAccountInfo(localAccount.fullName, localAccount.network, accountInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
