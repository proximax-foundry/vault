package io.proximax.app.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.proximax.app.db.LocalAccount;
import io.proximax.app.db.ShareFile;
import io.proximax.app.db.LocalFile;
import io.proximax.download.DownloadParameter;
import io.proximax.upload.ByteArrayParameterData;
import io.proximax.upload.FileParameterData;
import io.proximax.upload.UploadParameter;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.MimetypesFileTypeMap;

/**
 *
 * @author Administrator
 */
public class LocalFileHelpers {

    public static boolean isExisted(LocalAccount localAccount, File file, int shareType) {
        try {
            return DBHelpers.isFileExisted(localAccount.fullName, localAccount.network, file.getName(), file.lastModified(), shareType);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void addFile(String fullName, String network, LocalFile localFile) {
        try {
            DBHelpers.addFile(fullName, network, localFile);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static List<LocalFile> getFiles(String fullName, String network) {
        try {
            DBHelpers.updateFileStatus(fullName, network, System.currentTimeMillis() - 3 * 60 * 1000); //timeout 3p
            return DBHelpers.getFiles(fullName, network);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static DownloadParameter createDownloadParameter(LocalFile localFile) {
        switch (localFile.uType) {
            case CONST.UTYPE_SECURE_NEMKEYS:
                return DownloadParameter.create(localFile.nemHash)
                        .withNemKeysPrivacy(localFile.privateKey, localFile.publicKey)
                        .build();
            case CONST.UTYPE_SECURE_PASSWORD:
                return DownloadParameter.create(localFile.nemHash)
                        .withPasswordPrivacy(localFile.password)
                        .build();
            default:
                return DownloadParameter.create(localFile.nemHash)
                        .build();
        }
    }

    public static void shareLocalFile(LocalAccount localAccount, ShareFile shareFile) {
        try {
            DBHelpers.shareLocalFile(localAccount.fullName, localAccount.network, shareFile);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static String getContentType(String fileName) {
        String mimeType = null;
        if (fileName.endsWith(".html")) {
            mimeType = "text/html";
        } else if (fileName.endsWith(".css")) {
            mimeType = "text/css";
        } else if (fileName.endsWith(".js")) {
            mimeType = "application/javascript";
        } else if (fileName.endsWith(".gif")) {
            mimeType = "image/gif";
        } else if (fileName.endsWith(".png")) {
            mimeType = "image/png";
        } else if (fileName.endsWith(".txt") || fileName.endsWith(".log")) {
            mimeType = "text/plain";
        } else if (fileName.endsWith(".xml")) {
            mimeType = "application/xml";
        } else if (fileName.endsWith(".json")) {
            mimeType = "application/json";
        } else {
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            mimeType = mimeTypesMap.getContentType(fileName);
        }
        return mimeType;
    }

    public static String getContentType(File file) {
        return getContentType(file.getPath());
    }

    public static UploadParameter createUploadFileParameter(LocalAccount localAccount, LocalFile localFile, File uploadFile) throws IOException {
        Map<String, String> metaData = null;
        if (StringUtils.isEmpty(localFile.metadata)) {
            metaData = createMetaData(localAccount, localFile);
            localFile.metadata = metaData.toString();
        } else {
            Gson gson = new Gson();
            metaData = gson.fromJson(localFile.metadata, new TypeToken<Map<String, String>>() {
            }.getType());
        }
        switch (localFile.uType) {
            case CONST.UTYPE_SECURE_NEMKEYS:
                return UploadParameter
                        .createForFileUpload(
                                FileParameterData.create(
                                        uploadFile,
                                        "Uploaded by " + CONST.APP_NAME,
                                        localFile.fileName,
                                        new MimetypesFileTypeMap().getContentType(uploadFile),
                                        metaData),
                                localFile.privateKey)
                        .withRecipientAddress(localFile.address)
                        .withNemKeysPrivacy(localFile.privateKey, localFile.publicKey)
                        .build();
            case CONST.UTYPE_SECURE_PASSWORD:
                return UploadParameter
                        .createForFileUpload(
                                FileParameterData.create(
                                        uploadFile,
                                        "Uploaded by " + CONST.APP_NAME,
                                        localFile.fileName,
                                        new MimetypesFileTypeMap().getContentType(uploadFile),
                                        metaData),
                                localFile.privateKey)
                        .withRecipientAddress(localFile.address)
                        .withPasswordPrivacy(localFile.password)
                        .build();
            default:
                return UploadParameter
                        .createForFileUpload(
                                FileParameterData.create(
                                        uploadFile,
                                        "Uploaded by " + CONST.APP_NAME,
                                        localFile.fileName,
                                        new MimetypesFileTypeMap().getContentType(uploadFile),
                                        metaData),
                                localFile.privateKey)
                        .withRecipientAddress(localFile.address)
                        .build();
        }
    }

    public static void updateFile(String fullName, String network, LocalFile oldFile, LocalFile newFile) {
        try {
            DBHelpers.updateFile(fullName, network, oldFile, newFile);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static List<LocalFile> getDelFiles(String fullName, String network) {
        try {
            return DBHelpers.getDelFiles(fullName, network);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> createMetaData(LocalAccount localAccount, LocalFile localFile) {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("file", localFile.fileName);
        metaData.put("app", CONST.APP_FOLDER);
        metaData.put("user", localAccount.fullName);
        metaData.put("network", localAccount.network);
        metaData.put("utype", "" + localFile.uType);
        metaData.put("size", "" + localFile.fileSize);
        return metaData;
    }

    public static void updateFileFromTransaction(String fullName, String network, String nemHash, int status) {
        try {
            DBHelpers.updateFile(fullName, network, nemHash, status);
        } catch (SQLException ex) {
        }
    }

    public static UploadParameter createUploadBinaryParameter(LocalAccount localAccount, LocalFile localFile, byte[] data) {
        Map<String, String> metaData = null;
        if (StringUtils.isEmpty(localFile.metadata)) {
            metaData = createMetaData(localAccount, localFile);
            localFile.metadata = metaData.toString();
        } else {
            Gson gson = new Gson();
            metaData = gson.fromJson(localFile.metadata, new TypeToken<Map<String, String>>() {
            }.getType());
        }
        switch (localFile.uType) {
            case CONST.UTYPE_SECURE_NEMKEYS:
                return UploadParameter
                        .createForByteArrayUpload(
                                ByteArrayParameterData.create(
                                        data,
                                        "Uploaded by " + CONST.APP_NAME,
                                        localFile.fileName,
                                        "",
                                        metaData),
                                localFile.privateKey)
                        .withNemKeysPrivacy(localFile.privateKey, localFile.publicKey)
                        .build();
            case CONST.UTYPE_SECURE_PASSWORD:
                return UploadParameter
                        .createForByteArrayUpload(
                                ByteArrayParameterData.create(
                                        data,
                                        "Uploaded by " + CONST.APP_NAME,
                                        localFile.fileName,
                                        "",
                                        metaData),
                                localFile.privateKey)
                        .withPasswordPrivacy(localFile.password)
                        .build();
            default:
                return UploadParameter
                        .createForByteArrayUpload(
                                ByteArrayParameterData.create(
                                        data,
                                        "Uploaded by " + CONST.APP_NAME,
                                        localFile.fileName,
                                        "",
                                        metaData),
                                localFile.privateKey)
                        .build();
        }
    }

    public static LocalFile getLatest(String userName, String network) throws Exception {
        return DBHelpers.getLatest(userName, network);
    }

}
