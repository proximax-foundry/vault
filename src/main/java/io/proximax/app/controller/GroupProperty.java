package io.proximax.app.controller;

import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.GroupContract;
import de.slackspace.openkeepass.domain.Times;
import de.slackspace.openkeepass.domain.TimesBuilder;
import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.DateTimeUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javafx.scene.control.TreeItem;

/**
 *
 * @author thcao
 */
public class GroupProperty implements GroupContract {

    private UUID uuid;

    private String name;

    private int iconId = 48;

    private Times times;

    private boolean isExpanded;

    private byte[] iconData;

    private UUID customIconUuid;

    private GroupProperty parent;

    protected ProxiKeePassImpl handle;

    private List<EntryProperty> entries = new ArrayList<>();
    private List<GroupProperty> groups = new ArrayList<>();

    public GroupProperty(Group group, GroupProperty parent) {
        this.parent = parent;
        this.uuid = group.getUuid();
        this.name = group.getName();
        this.iconId = group.getIconId();
        this.iconData = group.getIconData();
        this.customIconUuid = group.getCustomIconUuid();
        this.times = group.getTimes();
        this.isExpanded = group.isExpanded();
        if (parent != null) {
            this.handle = parent.handle;
        }

        cloneEntries(group);
        cloneGroups(group);
    }
    
    public GroupProperty(Group group, ProxiKeePassImpl handle) {
        this.parent = null;
        this.uuid = group.getUuid();
        this.name = group.getName();
        this.iconId = group.getIconId();
        this.iconData = group.getIconData();
        this.customIconUuid = group.getCustomIconUuid();
        this.times = group.getTimes();
        this.isExpanded = group.isExpanded();
        this.handle = handle;

        cloneEntries(group);
        cloneGroups(group);
    }

    public GroupProperty(GroupProperty parent) {
        this.parent = parent;
        this.uuid = UUID.randomUUID();
        this.times = DateTimeUtils.createTimesDefault();
    }

    public GroupProperty(GroupProperty group, GroupProperty parent) {
        this.parent = parent;
        this.name = group.getName();
        this.iconId = group.getIconId();
        this.iconData = group.getIconData();
        this.isExpanded = group.isExpanded();
        this.times = DateTimeUtils.createTimeBuilderDefault().expires(group.getTimes().expires()).expiryTime(group.getTimes().getExpiryTime()).build();
        this.uuid = UUID.randomUUID();
        cloneEntries(group);
        cloneGroups(group);
    }

    private void cloneEntries(Group group) {
        entries.clear();
        for (Entry entry : group.getEntries()) {
            EntryProperty e = new EntryProperty(entry, this);
            entries.add(e);
        }
    }

    private void cloneEntries(GroupProperty group) {
        entries.clear();
        for (EntryProperty entry : group.getEntriesProperty()) {
            EntryProperty e = new EntryProperty(entry, this);
            entries.add(e);
        }
    }

    public void addGroup(GroupProperty group) {
        group.setHandle(handle);
        groups.add(group);        
        markModified();
    }

    public void addEntry(EntryProperty entry) {
        entries.add(entry);
        markModified();
    }

    public void removeEntry(EntryProperty entry) {
        entries.remove(entry);
        markModified();
    }

    public void removeGroup(GroupProperty group) {
        groups.remove(group);
        markModified();
    }

    private void cloneGroups(Group group) {
        groups.clear();
        for (Group g : group.getGroups()) {
            GroupProperty gp = new GroupProperty(g, this);
            groups.add(gp);
        }
    }

    private void cloneGroups(GroupProperty group) {
        groups.clear();
        for (GroupProperty g : group.getGroupsProperty()) {
            GroupProperty gp = new GroupProperty(g, this);
            groups.add(gp);
        }
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getIconId() {
        return iconId;
    }

    @Override
    public Times getTimes() {
        return times;
    }

    @Override
    public boolean isExpanded() {
        return isExpanded;
    }

    @Override
    public byte[] getIconData() {
        return iconData;
    }

    @Override
    public UUID getCustomIconUuid() {
        return customIconUuid;
    }

    public void setCustomIconUuid(UUID customIconUuid) {
        this.customIconUuid = customIconUuid;
        markModified();
    }

    @Override
    public List<Entry> getEntries() {
        List<Entry> entryList = new ArrayList<>();
        entries.forEach((e) -> {
            entryList.add(e.build());
        });
        return entryList;
    }

    @Override
    public List<Group> getGroups() {
        List<Group> groupList = new ArrayList<>();
        groups.forEach((g) -> {
            groupList.add(g.build());
        });
        return groupList;
    }

    public TreeItem<GroupProperty> createTreeItem() {
        TreeItem<GroupProperty> treeItem = new TreeItem<>(this);
        treeItem.setExpanded(true);
        groups.forEach((g) -> {
            treeItem.getChildren().add(g.createTreeItem());
        });
        return treeItem;
    }

    public List<EntryProperty> getEntriesProperty() {
        return entries;
    }

    public List<GroupProperty> getGroupsProperty() {
        return groups;
    }

    public GroupProperty getParent() {
        return parent;
    }

    public String getCreationTime() {
        String str = "None";
        if (getTimes() != null && getTimes().getCreationTime() != null) {
            str = CONST.SDF.format(getTimes().getCreationTime().getTime());
        }
        return str;
    }

    public String getLastModificationTime() {
        String str = "None";
        if (getTimes() != null && getTimes().getLastModificationTime() != null) {
            str = CONST.SDF.format(getTimes().getLastModificationTime().getTime());
        }
        return str;
    }

    public String getLocationChanged() {
        String str = "None";
        if (getTimes() != null && getTimes().getLocationChanged() != null) {
            str = CONST.SDF.format(getTimes().getLocationChanged().getTime());
        }
        return str;
    }

    public String getExpiryTime() {
        String str = "None";
        if (getTimes() != null && getTimes().expires() && getTimes().getLastModificationTime() != null) {
            str = CONST.SDF.format(getTimes().getLastModificationTime().getTime());
        }
        return str;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String toStringDebug() {
        return String.format("Group: %s,Creation Time: %s, Last Modification Time: %s, Expiry Time: %s",
                getName(), getCreationTime(), getLastModificationTime(), getExpiryTime());
    }

    public void printDebug(int i) {
        for (int j = 0; j < i; j++) {
            System.out.print("\t");
        }
        System.out.println("Group[" + i + "]: " + toString());
        for (EntryProperty e : entries) {
            for (int j = 0; j < i; j++) {
                System.out.print("\t");
            }
            System.out.println("--> Entry: " + e.toString());
        }
        for (GroupProperty g : groups) {
            g.printDebug(i + 1);
        }
    }

    public void setName(String name) {
        if (null == name || name.isEmpty()) {
            return;
        }

        if (!name.equals(this.name)) {
            this.name = name;
            markModified();
        }
    }

    public void setExpiryDate(boolean expires, Calendar expiryTime) {
        TimesBuilder timeBuilder = DateTimeUtils.createTimeBuilder(times);
        Times newTimes = null;
        if (times.expires() != expires) {
            timeBuilder.expires(expires);
            newTimes = timeBuilder.build();
        }
        if (expires) {
            if (!times.getExpiryTime().equals(expiryTime)) {
                timeBuilder.expiryTime(expiryTime);
                newTimes = timeBuilder.build();
            }
        }
        if (newTimes != null) {
            times = newTimes;
            markModified();
        }
    }

    public void setIconId(int iconId) {
        if (this.iconId != iconId) {
            this.iconId = iconId;
            setIconData(IApp.getIconData(iconId));
            markModified();
        }
    }

    public void setIconData(byte[] iconData) {
        this.iconData = iconData;
        markModified();
    }

    protected void markModified() {
        if (handle != null) {
            handle.markModified();
        }
    }

    public boolean isModified() {
        if (handle != null) {
            return handle.isModified();
        }
        return false;
    }

    public boolean checkingModify() {
        return isModified();
    }

    public void refresh() {
        if (handle != null) {
            handle.refresh();
        }
    }

    public Group build() {
        return new Group(this);
    }

    public void setHandle(ProxiKeePassImpl proxiKeePassDB) {
        this.handle = proxiKeePassDB;
    }

    public ProxiKeePassImpl getHandle() {
        return handle;
    }
}
