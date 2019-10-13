package io.proximax.app.db;

import io.proximax.app.utils.CONST;
import io.proximax.app.utils.StringUtils;
import java.text.SimpleDateFormat;

/**
 *
 * @author thcao
 */
public class LocalFile {

    public int id;
    public String fileName;
    public String filePath;
    public long modified;
    public long fileSize;
    public long uploadDate;
    public String hash;
    public String nemHash;
    public String publicKey;
    public String address;
    public String privateKey;
    public String password;
    public String category;
    public String desc;
    public int uType;
    public String shared;
    public String metadata;
    public int rev;
    public int fileId;
    public int status;
    public long createdDate;
    public long updatedDate;

    public boolean isFolder;

    public LocalFile() {
        this.id = 0;
        this.fileName = "";
        this.filePath = "";
        this.modified = 0;
        this.fileSize = 0;
        this.uploadDate = 0;
        this.hash = "";
        this.nemHash = "";
        this.publicKey = "";
        this.address = "";
        this.category = "";
        this.desc = "";
        this.password = "";
        this.privateKey = "";
        this.uType = 0;
        this.shared = "";
        this.metadata = "";
        this.fileId = id;
        this.rev = 0;
        this.status = 0;
        this.isFolder = false;
    }

    public LocalFile(boolean isFolder) {
        this.id = 0;
        this.fileName = "";
        this.filePath = "";
        this.modified = 0;
        this.fileSize = 0;
        this.uploadDate = 0;
        this.hash = "";
        this.nemHash = "";
        this.publicKey = "";
        this.category = "";
        this.desc = "";
        this.password = "";
        this.privateKey = "";
        this.address = "";
        this.uType = 0;
        this.shared = "";
        this.metadata = "";
        this.fileId = id;
        this.rev = 0;
        this.status = 0;
        this.isFolder = isFolder;
    }

    public LocalFile(LocalFile localFile) {
        clone(localFile);
    }

    public boolean isSecure() {
        return (uType != CONST.UTYPE_PUBLIC);
    }

    @Override
    public String toString() {
        return new StringBuilder("/id[").append(id).append("]/f[").append(fileName).append("]/s[").append(uType).append("]/d[").append(modified).append("]/h[").append(hash).append("]/n[").append(nemHash).append("]").toString();
    }

    public String getTitle() {
        return String.format("%s %s", new SimpleDateFormat("M/d/yy").format(modified), fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public String getDesc() {
        if (StringUtils.isEmpty(desc)) {
            desc = fileName;
        }
        if (desc.length() > CONST.MAX_LENGTH) {
            return desc.substring(0, CONST.MAX_LENGTH);
        }
        return desc;
    }

    public String getModified() {
        return String.format("%s", new SimpleDateFormat("MMM dd").format(modified));
    }

    @Override
    public boolean equals(Object obj) {
        LocalFile localFile = (LocalFile) obj;
        if (localFile.id == 0 && id == 0) {
            return this == obj;
        } else {
            return localFile.id == id;
        }
    }

    public String getFilenamePretty() {
        return fileName.replace(".rtfx", "");
    }

    public final LocalFile clone(LocalFile localFile) {
        this.id = localFile.id;
        this.fileName = localFile.fileName;
        this.filePath = localFile.filePath;
        this.modified = localFile.modified;
        this.fileSize = localFile.fileSize;
        this.uploadDate = localFile.uploadDate;
        this.hash = localFile.hash;
        this.nemHash = localFile.nemHash;
        this.address = localFile.address;
        this.publicKey = localFile.publicKey;
        this.category = localFile.category;
        this.desc = localFile.desc;
        this.password = localFile.password;
        this.privateKey = localFile.privateKey;
        this.uType = localFile.uType;
        this.shared = localFile.shared;
        this.metadata = localFile.metadata;
        this.fileId = localFile.fileId;
        this.rev = localFile.rev;
        this.status = localFile.status;
        this.updatedDate = localFile.updatedDate;
        this.createdDate = localFile.createdDate;
        this.isFolder = localFile.isFolder;
        return this;
    }

}
