package io.proximax.app.controller;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Binaries;
import de.slackspace.openkeepass.domain.BinariesBuilder;
import de.slackspace.openkeepass.domain.CustomIcon;
import de.slackspace.openkeepass.domain.CustomIconBuilder;
import de.slackspace.openkeepass.domain.CustomIcons;
import de.slackspace.openkeepass.domain.CustomIconsBuilder;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.GroupBuilder;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileBuilder;
import de.slackspace.openkeepass.domain.Meta;
import de.slackspace.openkeepass.domain.MetaBuilder;
import de.slackspace.openkeepass.domain.Times;
import io.proximax.app.db.LocalAccount;
import io.proximax.app.db.LocalFile;
import io.proximax.app.utils.AccountHelpers;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.DateTimeUtils;
import io.proximax.app.utils.LocalFileHelpers;
import io.proximax.connection.ConnectionConfig;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author thcao
 */
public class ProxiKeePassImpl {

    private Meta meta = null;
    private LocalAccount localAccount = null;
    private LocalFile localFile = null;
    private boolean modified = false;
    private GroupProperty root = null;

    public ProxiKeePassImpl(LocalAccount localAccount) throws Exception {
        this.localAccount = localAccount;
        localFile = LocalFileHelpers.getLatest(localAccount.fullName, localAccount.network);
        initialize();
    }

    public String getKeePassDB() {
        return AccountHelpers.getHomeAccount(localAccount.fullName, localAccount.network) + localAccount.address + ".kdbx";
    }

    public boolean isExistKeePassDB() {
        return new File(getKeePassDB()).exists();
    }

    public static void createDefaultKeePassFile(LocalAccount localAccount) throws Exception {
        ProxiKeePassImpl keePass = new ProxiKeePassImpl(localAccount);
        keePass.saveDB();
    }

    private void initialize() throws Exception {
        Group rootG = null;
        if (!isExistKeePassDB()) {
            Times times = DateTimeUtils.createFullTimesDefault();
            rootG = new GroupBuilder(localAccount.fullName)
                    .addGroup(new GroupBuilder("General").iconId(48).times(times).build())
                    .addGroup(new GroupBuilder("Windows").iconId(38).times(times).build())
                    .addGroup(new GroupBuilder("Network").iconId(6).times(times).build())
                    .addGroup(new GroupBuilder("Internet").iconId(1).times(times).build())
                    .addGroup(new GroupBuilder("eMail").iconId(19).times(times).build())
                    .addGroup(new GroupBuilder("Banking").iconId(37).times(times).build())
                    .iconId(49).build();
            markModified();
            meta = buildMeta();
        } else {
            KeePassFile keePassDB = KeePassDatabase.getInstance(getKeePassDB()).openDatabase(localAccount.getEncPassword());
            rootG = keePassDB.getRoot().getGroups().get(0);
            meta = buildMeta(keePassDB.getMeta());
        }
        root = new GroupProperty(rootG, this);
    }

    public Meta getMeta() {
        return meta;
    }

    public boolean isConnected() {
        return localAccount.isConnected();
    }

    public LocalAccount getLocalAccount() {
        return localAccount;
    }

    public GroupProperty getRoot() {
        return root;
    }

    public LocalFile getLocalFile() {
        return localFile;
    }

    public boolean isModified() {
        return modified;
    }

    public void markModified() {
        modified = true;
    }

    public void refresh() {
        modified = false;
    }

    public CustomIcon getIconByUuid(UUID uuid) {
        return meta.getCustomIcons().getIconByUuid(uuid);
    }

    public void removeIconByUuid(UUID uuid) {
        CustomIcon icon = getIconByUuid(uuid);
        if (icon != null) {
            markModified();
            getIcons().remove(icon);
        }
    }

    public IconData addIcon(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = IOUtils.toByteArray(fis);
            UUID uuid = UUID.randomUUID();
            fis.close();
            CustomIcon icon = new CustomIconBuilder().uuid(uuid).data(data).build();
            if (icon != null) {
                markModified();
                getIcons().add(icon);
                return new IconData(icon.getUuid(), icon.getData());
            }
        } catch (Exception ex) {
        }
        return null;
    }

    public List<CustomIcon> getIcons() {
        return meta.getCustomIcons().getIcons();
    }

    public CustomIcons getCustomIcons() {
        return meta.getCustomIcons();
    }

    public LocalFile getLatestFile() {
        try {
            return LocalFileHelpers.getLatest(localAccount.fullName, localAccount.network);
        } catch (Exception ex) {
        }
        return null;
    }

    public ConnectionConfig getConnectionConfig() {
        return localAccount.connectionConfig;
    }

    public void addLocalFile(LocalFile localFile) {
        LocalFileHelpers.addFile(localAccount.fullName, localAccount.network, localFile);
    }

    public void updateLocalFile(LocalFile localFile, LocalFile saveFile) {
        LocalFileHelpers.updateFile(localAccount.fullName, localAccount.network, localFile, saveFile);
    }

    public int saveDB() {
        if (!isModified()) {
            return 0;
        }
        try {
            KeePassFile keePassDB = new KeePassFileBuilder(localAccount.fullName + ".kdbx")
                    .addTopGroups(root.build())
                    .withMeta(meta)
                    .build();
            KeePassDatabase.write(keePassDB, localAccount.getEncPassword(), getKeePassDB());
            refresh();
            return 1;
        } catch (Exception ex) {
        }
        return -1;
    }

    public List<IconData> addIcons(List<File> list) {
        if (list != null) {
            List<IconData> newList = new ArrayList<>();
            list.forEach((f) -> {
                newList.add(addIcon(f));
            });
            return newList;
        }
        return null;
    }

    private Meta buildMeta() {
        return new MetaBuilder(localAccount.fullName)
                .databaseDescription(CONST.APP_NAME + " by ProximaX")
                .customIcons(buildCustomIcons())
                .binaries(buildBinaries())
                .build();
    }

    private Meta buildMeta(Meta m) {
        if (m == null) {
            return buildMeta();
        }
        MetaBuilder builder = new MetaBuilder(m);
        builder.databaseDescription(CONST.APP_NAME + " by ProximaX");
        if (m.getCustomIcons() == null) {
            builder.customIcons(buildCustomIcons());
        }
        if (m.getBinaries() == null) {
            builder.binaries(buildBinaries());
        }
        return builder.build();
    }

    private CustomIcons buildCustomIcons() {
        CustomIcons cus = new CustomIconsBuilder().build();
        return cus;
    }

    private Binaries buildBinaries() {
        Binaries bin = new BinariesBuilder()
                .build();
        return bin;
    }

    public boolean isBefore(long modified) {
        File file = new File(getKeePassDB());
        if (modified >= file.lastModified()) {
            return true;
        }
        return false;
    }
}
