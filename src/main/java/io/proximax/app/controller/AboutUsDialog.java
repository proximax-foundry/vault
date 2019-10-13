package io.proximax.app.controller;

import io.proximax.app.core.ui.IApp;
import io.proximax.app.utils.CONST;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 *
 * @author thcao
 */
public class AboutUsDialog extends AbstractController {

    @FXML
    private Label titleLbl;
    @FXML
    private Label msgLbl;
    
    private String appVer = "";

    public AboutUsDialog() {
        super(true);
    }

    @Override
    protected void initialize() {
        String version = "2.0.1";
        String buildTime = CONST.SDF.format(new Date());
        String jdk = "1.8.0_191";

        Manifest manifest = IApp.getManifest();
        if (manifest != null) {
            Attributes mainAttributes = manifest.getMainAttributes();
            version = mainAttributes.getValue("Implementation-Version");
            buildTime = mainAttributes.getValue("Build-Time");
            jdk = mainAttributes.getValue("Build-Jdk");

        }
        String msg = "Product Version\n";
        appVer = CONST.APP_NAME + "-" + version;
        msg += "    " + CONST.APP_NAME + " " + version + "\n\n";
        msg += "Build Information \n";
        msg += "    Version " + version + "\n";
        msg += "    Date: " + buildTime + "\n";
        msg += "    Java Version: " + jdk + "\n\n\n";
        msg += "Copyright (c) 2019, ProximaX";
        msgLbl.setText(msg);
        String title = "ABOUT " + CONST.APP_NAME;
        titleLbl.setText(title.toUpperCase());
    }

    @Override
    protected void dispose() {
    }

    @Override
    public String getTitle() {
        return CONST.ABOUTDLG_TITLE;
    }

    @Override
    public String getFXML() {
        return CONST.ABOUTDLG_FXML;
    }

    public static void showAbout(AbstractController parent) {
        try {
            AboutUsDialog dlg = new AboutUsDialog();
            dlg.setParent(parent);
            dlg.openWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void showAboutSafe(AbstractController parent) {
        IApp.runSafe(() -> {
            try {
                AboutUsDialog dlg = new AboutUsDialog();
                dlg.setParent(parent);
                dlg.openWindow();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

    }
    
    @FXML
    protected void feedbackBtn(ActionEvent event) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.MAIL)) {
                try {
                    String url = "mailto:support@proximax.io?subject=" + URLEncoder.encode(appVer + "-Feedback", "UTF-8");
                    System.out.println("Url: " + url);
                    URI mailto = new URI(url);
                    desktop.mail(mailto);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
