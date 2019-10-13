package io.proximax.app.main;

import io.proximax.app.controller.LoginDialog;
import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.AccountHelpers;
import io.proximax.app.utils.CONST;
import io.proximax.app.utils.StringUtils;
import java.util.Hashtable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Vault extends Application implements IApp {

    private Stage primaryStage = null;
    private int theme = 0;
    @SuppressWarnings("UseOfObsoleteCollectionType")
    private final Hashtable<String, Object> caches = new Hashtable<>();

    @Override
    public void setTheme(int theme) {
        this.theme = theme;
    }

    @Override
    public Image getIcon() {
        return new Image(getClass().getResourceAsStream(String.format(CONST.IMAGE_PATH, getCurrentTheme()) + CONST.APP_ICON));
    }

    @Override
    public Image getIcon16() {
        return new Image(getClass().getResourceAsStream(String.format(CONST.IMAGE_PATH, getCurrentTheme()) + CONST.APP_ICON), 16, 16, true, true);
    }

    @Override
    public Image getImageFromResource(String resUrl) {
        return new Image(getClass().getResourceAsStream(String.format(CONST.IMAGE_PATH, getCurrentTheme()) + resUrl));
    }

    @Override
    public Image getIcon(int iconId) {
        return new Image(getClass().getResourceAsStream(String.format(CONST.ICON_PATH, iconId)));
    }

    public String getString(String key) {
        return (String) caches.get(key);
    }

    public void putString(String key, String val) {
        caches.put(key, val);
    }

    @Override
    public String getCurrentDir() {
        String dir = (String) caches.get("latest.dir");
        if (StringUtils.isEmpty(dir)) {
            dir = System.getProperty("user.home");
        }
        return dir;
    }

    @Override
    public void saveCurrentDir(String sDir) {
        caches.put("latest.dir", sDir);
    }

    @Override
    public Image getImageFromResource(String resUrl, double w, double h) {
        return new Image(getClass().getResourceAsStream(String.format(CONST.IMAGE_PATH, getCurrentTheme()) + resUrl), w, h, true, true);
    }

    @Override
    public String getCurrentTheme() {
        return CONST.THEMES[theme];
    }

    @Override
    public String getCurrentThemeUrl() {
        return getClass().getResource(String.format(CONST.CSS_THEME, getCurrentTheme())).toExternalForm();
    }

    @Override
    public String getThemeUrl(int i) {
        return getClass().getResource(String.format(CONST.CSS_THEME, CONST.THEMES[i])).toExternalForm();
    }

    @Override
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        try {
            CONST.IAPP = this;
            this.primaryStage = primaryStage;
            AccountHelpers.initUserHome();
            Platform.setImplicitExit(false);
            primaryStage.getIcons().add(getIcon());
            LoginDialog.showDialog(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void dispose() {
    }

}
