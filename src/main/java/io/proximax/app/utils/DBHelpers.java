package io.proximax.app.utils;

import io.proximax.app.db.LocalAccount;
import io.proximax.app.db.ShareFile;
import io.proximax.app.db.LocalFile;
import io.proximax.app.db.NetworkConfiguration;
import io.proximax.app.recovery.AccountInfo;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author thcao
 */
public class DBHelpers {

    public static Connection createConnection(String userName, String network) throws SQLException {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            String dbName = System.getProperty("user.home") + "/" + CONST.APP_FOLDER
                    + "/users/" + network + "/" + userName + ".wlt";
            c = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            c.setAutoCommit(false);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return c;
    }

    public static void createUserDB(String userName, String network) throws SQLException {

        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        String sql = "CREATE TABLE " + CONST.DB_USER_TABLE
                + " (ID          INTEGER PRIMARY KEY     AUTOINCREMENT,"
                + " USERNAME    CHAR(255) NOT NULL, "
                + " PASSWORD    CHAR(255) NOT NULL, "
                + " NETWORK     CHAR(255) NOT NULL, "
                + " ADDRESS     CHAR(255), "
                + " PUBLIC_KEY  CHAR(255), "
                + " PRIVATE_KEY CHAR(255), "
                + " LICENSE_KEY  CHAR(255), "
                + " XPX          REAL NOT NULL DEFAULT 0, "
                + " USED         REAL NOT NULL DEFAULT 0, "
                + " CAPACITY     REAL NOT NULL DEFAULT 5368709120, "
                + " TYPE         INTEGER NOT NULL DEFAULT 0, "
                + " STATUS       INTEGER NOT NULL DEFAULT 0, "
                + " EMAIL     CHAR(255), "
                + " QUESTION1     CHAR(255), "
                + " ANSWER1     CHAR(255), "
                + " QUESTION2     CHAR(255), "
                + " ANSWER2     CHAR(255), "
                + " QUESTION3     CHAR(255), "
                + " ANSWER3     CHAR(255), "
                + " VERSION CHAR(255))";
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE " + CONST.DB_FILE_TABLE
                + " (ID          INTEGER PRIMARY KEY     AUTOINCREMENT,"
                + " FILENAME    CHAR(1024)    NOT NULL, "
                + " FILEPATH    CHAR(1024)    NOT NULL, "
                + " MODIFIED    INTEGER, "
                + " FILESIZE    INTEGER, "
                + " UPLOAD_DATE INTEGER, "
                + " HASH        CHAR(255) NOT NULL, "
                + " NEM_HASH    CHAR(255) NOT NULL, "
                + " CATEGORY    CHAR(1024),"
                + " DESC        CHAR(255), "
                + " PASSWORD    CHAR(255), "
                + " ADDRESS     CHAR(255), "
                + " PUBLIC_KEY  CHAR(255), "
                + " PRIVATE_KEY CHAR(255),"
                + " UTYPE       INTEGER,"
                + " SHARED      TEXT,"
                + " METADATA    TEXT,"
                + " REV         INTEGER,"
                + " FILE_ID     INTEGER,"
                + " CREATED_DATE INTEGER,"
                + " UPDATED_DATE INTEGER,"
                + " STATUS     INTEGER NOT NULL DEFAULT 0"
                + ")";
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE " + CONST.DB_SHARE_TABLE
                + " (ID          INTEGER PRIMARY KEY     AUTOINCREMENT,"
                + " FILE_ID     INTEGER   NOT NULL, "
                + " USERNAME    CHAR(255), "
                + " ADDRESS     CHAR(255),"
                + " SHARE_DATE  INTEGER, "
                + " HASH        CHAR(255) NOT NULL, "
                + " NEM_HASH    CHAR(255) NOT NULL, "
                + " PASSWORD    CHAR(255), "
                + " SHARE_TYPE  INTEGER, "
                + " STATUS      INTEGER NOT NULL DEFAULT 0, "
                + " CONSTRAINT \"share_file_fk\" FOREIGN KEY (\"FILE_ID\") REFERENCES " + CONST.DB_FILE_TABLE + " (\"ID\")"
                + ")";
        stmt.executeUpdate(sql);
        stmt.close();
        c.commit();
        c.close();
    }

    public static void addUser(String userName, String password, String network, String privateKey, String publicKey, String address) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        String sql = "INSERT INTO " + CONST.DB_USER_TABLE
                + " (USERNAME,PASSWORD,NETWORK,ADDRESS,PUBLIC_KEY,PRIVATE_KEY, VERSION) "
                + "VALUES (\"" + userName + "\",\"" + password + "\",\"" + network + "\",\"" + address + "\",\"" + publicKey + "\",\"" + privateKey + "\",\"" + CONST.DB_VERSION + "\");";
        stmt.executeUpdate(sql);
        stmt.close();
        c.commit();
        c.close();
    }

    public static void updateUser(String userName, String password, String network, String privateKey, String publicKey, String address) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        String sql = "UPDATE " + CONST.DB_USER_TABLE
                + " SET PASSWORD=\"" + password + "\",ADDRESS=\"" + address + "\",PUBLIC_KEY=\"" + publicKey + "\",PRIVATE_KEY=\"" + privateKey + "\"";
        stmt.executeUpdate(sql);
        stmt.close();
        c.commit();
        c.close();
    }

    public static void updateUserType(String userName, String network, int type) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        String sql = "UPDATE " + CONST.DB_USER_TABLE + " SET TYPE=" + type;
        stmt.executeUpdate(sql);
        stmt.close();
        c.commit();
        c.close();
    }

    public static void updateUserLicense(String userName, String network, String licenseKey) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        String sql = "UPDATE " + CONST.DB_USER_TABLE + " SET LICENSE_KEY=\"" + licenseKey + "\"";
        stmt.executeUpdate(sql);
        stmt.close();
        c.commit();
        c.close();
    }

    public static void updateUserStatus(String userName, String network, int status) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        String sql = "UPDATE " + CONST.DB_USER_TABLE + " SET STATUS=" + status;
        stmt.executeUpdate(sql);
        stmt.close();
        c.commit();
        c.close();
    }

    public static void addFile(String userName, String network, LocalFile localFile) throws SQLException {
        List<LocalFile> files = getFiles(userName, network, localFile.fileName, localFile.hash, localFile.nemHash);
        if (!files.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO " + CONST.DB_FILE_TABLE
                + " (FILENAME,FILEPATH,MODIFIED,FILESIZE,UPLOAD_DATE,HASH,NEM_HASH,CATEGORY,DESC,PASSWORD,ADDRESS,PUBLIC_KEY,PRIVATE_KEY,UTYPE,SHARED,METADATA,REV,FILE_ID,CREATED_DATE,UPDATED_DATE,STATUS) VALUES ("
                + "\"" + localFile.fileName + "\","
                + "\"" + localFile.filePath + "\","
                + localFile.modified + ","
                + localFile.fileSize + ","
                + localFile.uploadDate + ","
                + "\"" + localFile.hash + "\","
                + "\"" + localFile.nemHash + "\","
                + "\"" + localFile.category + "\","
                + "\"" + localFile.desc + "\","
                + "\"" + localFile.password + "\","
                + "\"" + localFile.address + "\","
                + "\"" + localFile.publicKey + "\","
                + "\"" + localFile.privateKey + "\","
                + localFile.uType + ","
                + "\"" + localFile.shared + "\","
                + "\"" + localFile.metadata + "\","
                + localFile.rev + ","
                + localFile.fileId + ","
                + System.currentTimeMillis() + ","
                + System.currentTimeMillis() + ","
                + localFile.status + ");";
        Connection c = createConnection(userName, network);
        PreparedStatement stmt = c.prepareStatement(sql);
        stmt.executeUpdate();
        stmt.close();
        if (localFile.fileId == 0) {
            Statement stmt1 = c.createStatement();
            ResultSet rs = stmt1.executeQuery("select last_insert_rowid()");
            long id = 0;
            if (rs.next()) {
                id = rs.getLong(1);
            }
            localFile.id = (int) id;
            stmt1.close();
            if (id > 0) {
                PreparedStatement stmt2 = c.prepareStatement("UPDATE " + CONST.DB_FILE_TABLE
                        + " SET FILE_ID=" + id + " WHERE id=" + id);
                stmt2.executeUpdate();
                stmt2.close();
            }
        }
        if (localFile.id == 0) {
            Statement stmt1 = c.createStatement();
            ResultSet rs = stmt1.executeQuery("select last_insert_rowid()");
            long id = 0;
            if (rs.next()) {
                id = rs.getLong(1);
            }
            localFile.id = (int) id;
            stmt1.close();
        }
        c.commit();
        c.close();
    }

    public static LocalAccount getUser(String userName, String network) throws SQLException {
        Connection c = createConnection(userName, network);
        LocalAccount account = null;
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_USER_TABLE);
        while (rs.next()) {
            account = new LocalAccount(rs.getString("USERNAME"),
                    rs.getString("PASSWORD"),
                    rs.getString("NETWORK"),
                    rs.getString("PRIVATE_KEY"),
                    rs.getString("PUBLIC_KEY"),
                    rs.getString("ADDRESS"),
                    rs.getString("VERSION"),
                    rs.getInt("TYPE"),
                    rs.getInt("STATUS"));
            account.licenseKey = rs.getString("LICENSE_KEY");
            break;
        }
        stmt.close();
        c.close();
        return account;
    }

    public static List<LocalFile> getOFiles(String userName, String network) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_FILE_TABLE);
        List<LocalFile> list = new ArrayList<LocalFile>();
        while (rs.next()) {
            LocalFile localFile = new LocalFile();
            localFile.id = rs.getInt("ID");
            localFile.fileName = rs.getString("FILENAME");
            localFile.filePath = rs.getString("FILEPATH");
            localFile.modified = rs.getLong("MODIFIED");
            localFile.fileSize = rs.getLong("FILESIZE");
            localFile.uploadDate = rs.getLong("UPLOAD_DATE");
            localFile.hash = rs.getString("HASH");
            localFile.nemHash = rs.getString("NEM_HASH");
            localFile.publicKey = rs.getString("PUBLIC_KEY");
            localFile.address = rs.getString("ADDRESS");
            localFile.category = rs.getString("CATEGORY");
            localFile.desc = rs.getString("DESC");
            localFile.password = rs.getString("PASSWORD");
            localFile.privateKey = rs.getString("PRIVATE_KEY");
            localFile.uType = rs.getInt("UTYPE");
            localFile.shared = rs.getString("SHARED");
            localFile.metadata = rs.getString("METADATA");
            localFile.rev = rs.getInt("REV");
            localFile.fileId = rs.getInt("FILE_ID");
            localFile.status = rs.getInt("STATUS");
            localFile.updatedDate = rs.getLong("UPDATED_DATE");
            localFile.createdDate = rs.getLong("CREATED_DATE");
            list.add(localFile);
        }
        stmt.close();
        c.close();
        return list;
    }

    public static void updateFileStatus(String userName, String network, long timeout) throws SQLException {
        Connection c = createConnection(userName, network);
        PreparedStatement stmt = c.prepareStatement("UPDATE " + CONST.DB_FILE_TABLE + " SET STATUS=" + CONST.FILE_STATUS_NOR + " WHERE STATUS=" + CONST.FILE_STATUS_NEW
                + " AND UPLOAD_DATE<" + timeout);
        stmt.executeUpdate();
        stmt.close();
        c.commit();
        c.close();
    }

    public static List<LocalFile> getFiles(String userName, String network) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_FILE_TABLE + " WHERE STATUS=" + CONST.FILE_STATUS_NOR + " OR STATUS=" + CONST.FILE_STATUS_NEW + " ORDER BY MODIFIED DESC");
        List<LocalFile> list = new ArrayList<LocalFile>();
        while (rs.next()) {
            LocalFile localFile = new LocalFile();
            localFile.id = rs.getInt("ID");
            localFile.fileName = rs.getString("FILENAME");
            localFile.filePath = rs.getString("FILEPATH");
            localFile.modified = rs.getLong("MODIFIED");
            localFile.fileSize = rs.getLong("FILESIZE");
            localFile.uploadDate = rs.getLong("UPLOAD_DATE");
            localFile.hash = rs.getString("HASH");
            localFile.nemHash = rs.getString("NEM_HASH");
            localFile.publicKey = rs.getString("PUBLIC_KEY");
            localFile.address = rs.getString("ADDRESS");
            localFile.category = rs.getString("CATEGORY");
            localFile.desc = rs.getString("DESC");
            localFile.password = rs.getString("PASSWORD");
            localFile.privateKey = rs.getString("PRIVATE_KEY");
            localFile.uType = rs.getInt("UTYPE");
            localFile.shared = rs.getString("SHARED");
            localFile.metadata = rs.getString("METADATA");
            localFile.rev = rs.getInt("REV");
            localFile.fileId = rs.getInt("FILE_ID");
            localFile.status = rs.getInt("STATUS");
            localFile.updatedDate = rs.getLong("UPDATED_DATE");
            localFile.createdDate = rs.getLong("CREATED_DATE");
            list.add(localFile);
        }
        stmt.close();
        c.close();
        return list;
    }

    public static List<LocalFile> getFiles(String userName, String network, String fileName, String hash, String nemHash) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_FILE_TABLE + " WHERE FILENAME=\"" + fileName + "\" AND HASH=\"" + hash + "\" AND NEM_HASH=\"" + nemHash + "\"");
        List<LocalFile> list = new ArrayList<LocalFile>();
        while (rs.next()) {
            LocalFile localFile = new LocalFile();
            localFile.id = rs.getInt("ID");
            localFile.fileName = rs.getString("FILENAME");
            localFile.filePath = rs.getString("FILEPATH");
            localFile.modified = rs.getLong("MODIFIED");
            localFile.fileSize = rs.getLong("FILESIZE");
            localFile.uploadDate = rs.getLong("UPLOAD_DATE");
            localFile.hash = rs.getString("HASH");
            localFile.nemHash = rs.getString("NEM_HASH");
            localFile.publicKey = rs.getString("PUBLIC_KEY");
            localFile.address = rs.getString("ADDRESS");
            localFile.category = rs.getString("CATEGORY");
            localFile.desc = rs.getString("DESC");
            localFile.password = rs.getString("PASSWORD");
            localFile.privateKey = rs.getString("PRIVATE_KEY");
            localFile.uType = rs.getInt("UTYPE");
            localFile.shared = rs.getString("SHARED");
            localFile.metadata = rs.getString("METADATA");
            localFile.rev = rs.getInt("REV");
            localFile.fileId = rs.getInt("FILE_ID");
            localFile.status = rs.getInt("STATUS");
            localFile.updatedDate = rs.getLong("UPDATED_DATE");
            localFile.createdDate = rs.getLong("CREATED_DATE");
            list.add(localFile);
        }
        stmt.close();
        c.close();
        return list;
    }

    public static boolean isFileExisted(String userName, String network, String fileName, long lastModified, int uType) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_FILE_TABLE + " WHERE FILENAME=\"" + fileName + "\" AND MODIFIED=" + lastModified + " AND UTYPE=" + uType);
        boolean bExisted = false;
        while (rs.next()) {
            bExisted = true;
            break;
        }
        stmt.close();
        c.close();
        return bExisted;
    }

    public static void shareLocalFile(String userName, String network, ShareFile shareFile) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        String sql = "INSERT INTO " + CONST.DB_SHARE_TABLE
                + " (FILE_ID, USERNAME, ADDRESS, SHARE_DATE, HASH, NEM_HASH, PASSWORD,SHARE_TYPE,STATUS) VALUES ("
                + shareFile.fileId + ","
                + "\"" + shareFile.userName + "\","
                + "\"" + shareFile.address + "\","
                + shareFile.shareDate + ","
                + "\"" + shareFile.hash + "\","
                + "\"" + shareFile.nemHash + "\","
                + "\"" + shareFile.password + "\","
                + shareFile.shareType + ","
                + shareFile.status + ")";

        stmt.executeUpdate(sql);
        stmt.close();
        c.commit();
        c.close();
    }

    public static LocalFile getFile(String userName, String network, int id) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_FILE_TABLE
                + " WHERE ID=" + id);
        LocalFile localFile = null;
        while (rs.next()) {
            localFile = new LocalFile();
            localFile.id = rs.getInt("ID");
            localFile.fileName = rs.getString("FILENAME");
            localFile.filePath = rs.getString("FILEPATH");
            localFile.modified = rs.getLong("MODIFIED");
            localFile.fileSize = rs.getLong("FILESIZE");
            localFile.uploadDate = rs.getLong("UPLOAD_DATE");
            localFile.hash = rs.getString("HASH");
            localFile.nemHash = rs.getString("NEM_HASH");
            localFile.publicKey = rs.getString("PUBLIC_KEY");
            localFile.address = rs.getString("ADDRESS");
            localFile.category = rs.getString("CATEGORY");
            localFile.desc = rs.getString("DESC");
            localFile.password = rs.getString("PASSWORD");
            localFile.privateKey = rs.getString("PRIVATE_KEY");
            localFile.uType = rs.getInt("UTYPE");
            localFile.shared = rs.getString("SHARED");
            localFile.metadata = rs.getString("METADATA");
            localFile.rev = rs.getInt("REV");
            localFile.fileId = rs.getInt("FILE_ID");
            localFile.status = rs.getInt("STATUS");
            localFile.updatedDate = rs.getLong("UPDATED_DATE");
            localFile.createdDate = rs.getLong("CREATED_DATE");
            break;
        }
        stmt.close();
        c.close();
        return localFile;
    }

    public static void updateFile(String userName, String network, LocalFile oldFile, LocalFile newFile) throws SQLException {
        newFile.rev = oldFile.rev + 1;
        newFile.status = CONST.FILE_STATUS_NEW;
        if (oldFile.fileId == oldFile.id) {
            newFile.fileId = oldFile.id;
        } else {
            newFile.fileId = oldFile.fileId;
        }
        addFile(userName, network, newFile);
        delFile(userName, network, oldFile);
    }

    public static void delFile(String userName, String network, LocalFile localFile) throws SQLException {
        Connection c = createConnection(userName, network);
        PreparedStatement stmt = c.prepareStatement("UPDATE " + CONST.DB_FILE_TABLE
                + " SET STATUS=" + CONST.FILE_STATUS_DEL + " WHERE id=" + localFile.id);
        stmt.executeUpdate();
        stmt.close();
        c.commit();
        c.close();
    }

    public static List<LocalFile> getDelFiles(String userName, String network) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_FILE_TABLE
                + " WHERE STATUS=" + CONST.FILE_STATUS_DEL);
        List<LocalFile> list = new ArrayList<LocalFile>();
        while (rs.next()) {
            LocalFile localFile = new LocalFile();
            localFile.id = rs.getInt("ID");
            localFile.fileName = rs.getString("FILENAME");
            localFile.filePath = rs.getString("FILEPATH");
            localFile.modified = rs.getLong("MODIFIED");
            localFile.fileSize = rs.getLong("FILESIZE");
            localFile.uploadDate = rs.getLong("UPLOAD_DATE");
            localFile.hash = rs.getString("HASH");
            localFile.nemHash = rs.getString("NEM_HASH");
            localFile.address = rs.getString("ADDRESS");
            localFile.publicKey = rs.getString("PUBLIC_KEY");
            localFile.category = rs.getString("CATEGORY");
            localFile.desc = rs.getString("DESC");
            localFile.password = rs.getString("PASSWORD");
            localFile.privateKey = rs.getString("PRIVATE_KEY");
            localFile.uType = rs.getInt("UTYPE");
            localFile.shared = rs.getString("SHARED");
            localFile.metadata = rs.getString("METADATA");
            localFile.rev = rs.getInt("REV");
            localFile.fileId = rs.getInt("FILE_ID");
            localFile.status = rs.getInt("STATUS");
            localFile.updatedDate = rs.getLong("UPDATED_DATE");
            localFile.createdDate = rs.getLong("CREATED_DATE");
            list.add(localFile);
        }
        stmt.close();
        c.close();
        return list;
    }

    public static void updateFile(String userName, String network, String nemHash, int status) throws SQLException {
        Connection c = createConnection(userName, network);
        PreparedStatement stmt = c.prepareStatement("UPDATE " + CONST.DB_FILE_TABLE
                + " SET STATUS=" + status + " WHERE NEM_HASH=\"" + nemHash + "\"");
        stmt.executeUpdate();
        stmt.close();
        c.commit();
        c.close();
    }

    public static LocalFile getLatest(String userName, String network) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_FILE_TABLE + " WHERE STATUS=" + CONST.FILE_STATUS_NOR + " OR STATUS=" + CONST.FILE_STATUS_NEW + " ORDER BY MODIFIED DESC");
        LocalFile localFile = null;
        while (rs.next()) {
            localFile = new LocalFile();
            localFile.id = rs.getInt("ID");
            localFile.fileName = rs.getString("FILENAME");
            localFile.filePath = rs.getString("FILEPATH");
            localFile.modified = rs.getLong("MODIFIED");
            localFile.fileSize = rs.getLong("FILESIZE");
            localFile.uploadDate = rs.getLong("UPLOAD_DATE");
            localFile.hash = rs.getString("HASH");
            localFile.nemHash = rs.getString("NEM_HASH");
            localFile.publicKey = rs.getString("PUBLIC_KEY");
            localFile.address = rs.getString("ADDRESS");
            localFile.category = rs.getString("CATEGORY");
            localFile.desc = rs.getString("DESC");
            localFile.password = rs.getString("PASSWORD");
            localFile.privateKey = rs.getString("PRIVATE_KEY");
            localFile.uType = rs.getInt("UTYPE");
            localFile.shared = rs.getString("SHARED");
            localFile.metadata = rs.getString("METADATA");
            localFile.rev = rs.getInt("REV");
            localFile.fileId = rs.getInt("FILE_ID");
            localFile.status = rs.getInt("STATUS");
            localFile.updatedDate = rs.getLong("UPDATED_DATE");
            localFile.createdDate = rs.getLong("CREATED_DATE");
            break;
        }
        stmt.close();
        c.close();
        return localFile;
    }

    public static Connection createConnection() throws SQLException {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            String dbName = System.getProperty("user.home") + File.separator + CONST.APP_FOLDER + File.separator + CONST.DB_APP_CONFIGURATION;
            if (!new File(dbName).exists()) {
                try {
                    URL configFile = DBHelpers.class.getResource("/" + CONST.DB_APP_CONFIGURATION);
                    FileUtils.copyURLToFile(configFile, new File(dbName));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            c = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            c.setAutoCommit(false);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return c;
    }

    public static Connection createConnectionFromResource() throws SQLException {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            String dbName = DBHelpers.class.getResource("/" + CONST.DB_APP_CONFIGURATION).toString();
            c = DriverManager.getConnection("jdbc:sqlite::resource:" + dbName);
            c.setAutoCommit(false);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return c;
    }

    public static int getAppConfigVerFromRes() throws SQLException {
        Map<String, String> configs = getAppConfiguration(createConnectionFromResource());
        return StringUtils.parseInt(configs.get("version"), 0);
    }

    public static int getAppConfigVerFromLocal() throws SQLException {
        Map<String, String> configs = getAppConfiguration();
        return StringUtils.parseInt(configs.get("version"), 0);
    }

    public static Map<String, String> getAppConfiguration() throws SQLException {
        return getAppConfiguration(createConnection());
    }

    public static Map<String, String> getAppConfiguration(Connection c) throws SQLException {
        Map<String, String> configs = new HashMap<>();
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_COMMON_TABLE);
        while (rs.next()) {
            configs.put(rs.getString("PARAM"), rs.getString("VALUE"));
        }
        stmt.close();
        c.close();
        return configs;
    }

    public static Map<String, NetworkConfiguration> getNetworkConfiguration(int status) throws SQLException {
        Connection c = createConnection();
        Statement stmt = c.createStatement();
        String str;
        if (status >= 0) {
            str = "SELECT * FROM " + CONST.DB_NETWORK_TABLE + " WHERE STATUS=" + status;
        } else {
            str = "SELECT * FROM " + CONST.DB_NETWORK_TABLE;
        }
        ResultSet rs = stmt.executeQuery(str);
        NetworkConfiguration netconf = null;
        Map<String, NetworkConfiguration> configs = new HashMap<>();
        while (rs.next()) {
            netconf = new NetworkConfiguration(rs.getInt("ID"), rs.getString("NAME"), rs.getInt("VALUE"), rs.getInt("STATUS"), rs.getString("NODES"), rs.getString("IPFS"));
            configs.put(netconf.name, netconf);
        }
        stmt.close();
        c.close();
        return configs;
    }

    public static NetworkConfiguration getNetworkConfiguration(String name) throws SQLException {
        Connection c = createConnection();
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_NETWORK_TABLE + " WHERE NAME=\"" + name + "\"");
        NetworkConfiguration netconf = null;
        while (rs.next()) {
            netconf = new NetworkConfiguration(rs.getInt("ID"), rs.getString("NAME"), rs.getInt("VALUE"), rs.getInt("STATUS"), rs.getString("NODES"), rs.getString("IPFS"));
            break;
        }
        stmt.close();
        c.close();
        return netconf;
    }    

    public static void updateNetworkConfiguration(NetworkConfiguration config) throws SQLException {
        Connection c = createConnection();
        String nodes = "";
        for (String node : config.getNodes()) {
            nodes += node + ",";
        }
        String ipfss = "";
        for (String ipfs : config.getIpfses()) {
            ipfss += ipfs + ",";
        }
        PreparedStatement stmt = c.prepareStatement("UPDATE " + CONST.DB_NETWORK_TABLE
                + " SET STATUS=" + config.status + ","
                + "NODES=\"" + nodes + "\","
                + "IPFS=\"" + ipfss + "\""
                + " WHERE ID=" + config.id);
        stmt.executeUpdate();
        stmt.close();
        c.commit();
        c.close();

    }
    
    public static AccountInfo getAccountInfo(String userName, String network) throws SQLException {
        Connection c = createConnection(userName, network);
        AccountInfo account = null;
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + CONST.DB_USER_TABLE);
        while (rs.next()) {
            account = new AccountInfo(rs.getString("USERNAME"),
                    rs.getString("EMAIL"),
                    rs.getString("QUESTION1"),
                    rs.getString("ANSWER1"),
                    rs.getString("QUESTION2"),
                    rs.getString("ANSWER2"),
                    rs.getString("QUESTION3"),
                    rs.getString("ANSWER3"),
                    rs.getString("PRIVATE_KEY"),
                    rs.getString("PUBLIC_KEY"),
                    rs.getString("ADDRESS"));
            break;
        }
        stmt.close();
        c.close();
        return account;
    }

    public static void updateAccountInfo(String userName, String network, AccountInfo account) throws SQLException {
        Connection c = createConnection(userName, network);
        Statement stmt = c.createStatement();
        String sql = "UPDATE " + CONST.DB_USER_TABLE + " SET EMAIL=\"" + account.getEmail() + "\""
                + ",QUESTION1=\"" + account.getQuestion1() + "\""
                + ",QUESTION2=\"" + account.getQuestion2() + "\""
                + ",QUESTION3=\"" + account.getQuestion3() + "\""
                + ",ANSWER1=\"" + account.getAnswer1() + "\""
                + ",ANSWER2=\"" + account.getAnswer2() + "\""
                + ",ANSWER3=\"" + account.getAnswer3() + "\"";
        stmt.executeUpdate(sql);
        stmt.close();
        c.commit();
        c.close();
    }

}
