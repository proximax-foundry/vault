package io.proximax.app.controller;

import de.slackspace.openkeepass.domain.Attachment;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.EntryBuilder;
import de.slackspace.openkeepass.domain.EntryContract;
import de.slackspace.openkeepass.domain.History;
import de.slackspace.openkeepass.domain.Property;
import de.slackspace.openkeepass.domain.Times;
import de.slackspace.openkeepass.domain.TimesBuilder;
import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.DateTimeUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author thcao
 */
public class EntryProperty implements EntryContract {

    private GroupProperty parent;

    private UUID uuid;

    private History history;

    private String title;

    private String username;

    private String password;

    private String notes;

    private String url;

    private Entry originalEntry;

    private int iconId;

    private byte[] iconData;

    private UUID customIconUUID;

    private Times times;

    private List<String> tags = new ArrayList<String>();

    private String foregroundColor;

    private String backgroundColor;

    private List<Property> customPropertyList = new ArrayList<Property>();

    private List<Attachment> attachmentList = new ArrayList<Attachment>();

    public EntryProperty(Entry entry, GroupProperty parent) {
        this.parent = parent;
        this.originalEntry = entry;
        this.uuid = entry.getUuid();
        this.title = entry.getTitle();
        this.history = entry.getHistory();
        this.username = entry.getUsername();
        this.password = entry.getPassword();
        this.notes = entry.getNotes();
        this.url = entry.getUrl();
        this.iconId = entry.getIconId();
        this.iconData = entry.getIconData();
        this.customIconUUID = entry.getCustomIconUuid();
        this.customPropertyList.addAll(entry.getCustomProperties());
        this.times = entry.getTimes();
        this.tags = entry.getTags();
        this.foregroundColor = entry.getForegroundColor();
        this.backgroundColor = entry.getBackgroundColor();
        this.attachmentList.addAll(entry.getAttachments());
    }

    public EntryProperty(GroupProperty parent) {
        this.parent = parent;
        this.uuid = UUID.randomUUID();
        this.times = DateTimeUtils.createTimesDefault();
    }

    public EntryProperty(EntryProperty entry) {
        this.parent = entry.parent;
        this.originalEntry = entry.originalEntry;
        this.title = entry.getTitle();
        this.history = entry.getHistory();
        this.username = entry.getUsername();
        this.password = entry.getPassword();
        this.notes = entry.getNotes();
        this.url = entry.getUrl();
        this.iconId = entry.getIconId();
        this.iconData = entry.getIconData();
        this.customIconUUID = entry.getCustomIconUUID();
        this.customPropertyList.addAll(entry.getCustomProperties());
        this.tags = entry.getTags();
        this.foregroundColor = entry.getForegroundColor();
        this.backgroundColor = entry.getBackgroundColor();
        this.attachmentList.addAll(entry.getAttachments());

        this.uuid = UUID.randomUUID();
        this.times = DateTimeUtils.createTimeBuilderDefault().expires(entry.getTimes().expires()).expiryTime(entry.getTimes().getExpiryTime()).build();
    }

    public EntryProperty(EntryProperty entry, GroupProperty parent) {
        this.parent = parent;
        this.originalEntry = entry.originalEntry;
        this.title = entry.getTitle();
        this.history = entry.getHistory();
        this.username = entry.getUsername();
        this.password = entry.getPassword();
        this.notes = entry.getNotes();
        this.url = entry.getUrl();
        this.iconId = entry.getIconId();
        this.iconData = entry.getIconData();
        this.customIconUUID = entry.getCustomIconUUID();
        this.customPropertyList.addAll(entry.getCustomProperties());
        this.tags = entry.getTags();
        this.foregroundColor = entry.getForegroundColor();
        this.backgroundColor = entry.getBackgroundColor();
        this.attachmentList.addAll(entry.getAttachments());

        this.uuid = UUID.randomUUID();
        this.times = DateTimeUtils.createTimeBuilderDefault().expires(entry.getTimes().expires()).expiryTime(entry.getTimes().getExpiryTime()).build();
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public byte[] getIconData() {
        return iconData;
    }

    @Override
    public int getIconId() {
        return iconId;
    }

    @Override
    public UUID getCustomIconUUID() {
        return customIconUUID;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public List<Property> getCustomPropertyList() {
        return customPropertyList;
    }

    @Override
    public History getHistory() {
        return history;
    }

    @Override
    public Times getTimes() {
        return times;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public String getForegroundColor() {
        return foregroundColor;
    }

    @Override
    public String getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public List<Attachment> getAttachmentList() {
        return attachmentList;
    }

    public String getCreationTime() {
        String str = "";
        if (getTimes() != null && getTimes().getCreationTime() != null) {
            str = CONST.SDF.format(getTimes().getCreationTime().getTime());
        }
        return str;
    }

    public String getLastModificationTime() {
        String str = "";
        if (getTimes() != null && getTimes().getLastModificationTime() != null) {
            str = CONST.SDF.format(getTimes().getLastModificationTime().getTime());
        }
        return str;
    }

    public String getLocationChanged() {
        String str = "";
        if (getTimes() != null && getTimes().getLocationChanged() != null) {
            str = CONST.SDF.format(getTimes().getLocationChanged().getTime());
        }
        return str;
    }

    public String getExpiryTime() {
        String str = "";
        if (getTimes() != null && getTimes().expires() && getTimes().getExpiryTime() != null) {
            str = CONST.SDF.format(getTimes().getExpiryTime().getTime());
        }
        return str;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public String toStringDebug() {
        return String.format("Group: %s, Title: %s, User Name: %s, Password: ********, URL: %s, Creation Time: %s, Last Modification Time: %s, Expiry Time: %s, UUID: %s",
                parent.getName(), getTitle(), getUsername(), getUrl(), getCreationTime(), getLastModificationTime(), getExpiryTime(), getUuid().toString());
    }

    public Entry build() {
        return new Entry(this);
    }

    /**
     * Builds a new entry and place the original one in the history list.
     *
     * @return the new entry.
     */
    public Entry buildWithHistory() {
        if (originalEntry == null) {
            throw new IllegalArgumentException("originalEntry is not set");
        }

        if (history == null) {
            history = new History();
        }

        Entry entryWithoutHistory = new EntryBuilder(originalEntry).history(new History()).build();
        history.getHistoricEntries().add(entryWithoutHistory);
        return build();
    }

    public String getEncPassword() {
        return "**********";
    }

    private void markModified() {
        if (parent != null) {
            parent.markModified();
        }
    }

    public boolean isModified() {
        if (parent != null) {
            return parent.isModified();
        }
        return false;
    }

    public void refresh() {
        if (parent != null) {
            parent.refresh();
        }
    }

    public void setTitle(String str) {
        if (null == str || str.isEmpty()) {
            return;
        }

        if (!str.equals(this.title)) {
            this.title = str;
            markModified();
        }
    }

    public void setUsername(String str) {
        if (null == str || str.isEmpty()) {
            return;
        }

        if (!str.equals(this.username)) {
            this.username = str;
            markModified();
        }
    }

    public void setPassword(String str) {
        if (null == str || str.isEmpty()) {
            return;
        }

        if (!str.equals(this.password)) {
            this.password = str;
            markModified();
        }
    }

    public void setUrl(String str) {
        if (null == str || str.isEmpty()) {
            return;
        }

        if (!str.equals(this.url)) {
            this.url = str;
            markModified();
        }
    }

    public void setNotes(String str) {
        if (null == str || str.isEmpty()) {
            return;
        }

        if (!str.equals(this.notes)) {
            this.notes = str;
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
            if (!expiryTime.equals(times.getExpiryTime())) {
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
        if (this.iconData != iconData) {
            this.iconData = iconData;
            markModified();
        }
    }

    public void setCustomIconUUID(UUID customIconUUID) {
        if (!customIconUUID.equals(this.customIconUUID)) {
            this.customIconUUID = customIconUUID;
            markModified();
        }
    }

    public GroupProperty getParent() {
        return parent;
    }

    public List<Property> getCustomProperties() {
        return customPropertyList;
    }

    public List<Attachment> getAttachments() {
        return attachmentList;
    }

}
