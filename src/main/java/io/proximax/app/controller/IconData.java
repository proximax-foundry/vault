package io.proximax.app.controller;

import io.proximax.app.core.ui.IApp;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author thcao
 */
public class IconData {

    int iconId;
    UUID iconUUID = null;
    byte[] data;
    BooleanProperty isSelected = new SimpleBooleanProperty();

    public IconData(int iconId) {
        this.iconId = iconId;
        this.data = IApp.getIconData(iconId);
    }

    public IconData(UUID iconUUID, byte[] data) {
        this.iconUUID = iconUUID;
        this.data = data;
        iconId = -1;
    }

    public IconData(UUID iconUUID) {
        this.data = null;
        this.iconUUID = iconUUID;
        iconId = -1;
    }

}
