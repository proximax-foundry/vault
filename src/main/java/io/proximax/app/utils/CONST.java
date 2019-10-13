package io.proximax.app.utils;

import io.proximax.app.core.ui.IApp;
import java.text.SimpleDateFormat;

/**
 *
 * @author thcao
 */
public class CONST {

    public static IApp IAPP;    
    public static final String APP_NAME = "ProximaX Vault";
    public static final String APP_FOLDER = "vault";
    public static final char[] ENC_STRING = {'P', 'R', 'O', 'X', 'I', 'M', 'A', 'X'};
    public static final String DB_VERSION = "1.0.0";
    public static final String[] UPLOAD_TYPES = {"Encrypt with keys", "Encrypt with password", "Public"};
    public static final int UTYPE_SECURE_NEMKEYS = 0;
    public static final int UTYPE_SECURE_PASSWORD = 1;
    public static final int UTYPE_PUBLIC = 2;
    public static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static final String IMAGE_PATH = "/fxml/css/%s/img/";
    public static final String IMAGE_GREEN = "green.png";
    public static final String IMAGE_RED = "red.png";    
    public static final String STR_STATUS = "Status: ";
    public static final String STR_CONNECTED = "Connected";
    public static final String STR_DISCONNECTED = "Disconnected";
    public static final String ERRORDLG_TITLE = "Error Message";
    public static final String ERRORDLG_FXML = "ErrorDialog.fxml";
    public static final int FILE_STATUS_NOR = 0;
    public static final int FILE_STATUS_NEW = 1;
    public static final int FILE_STATUS_DEL = 2;
    public static final String[] THEMES = {"Light", "Dark"};
    public static final String CSS_PATH = "/fxml/css/";
    public static final String CSS_THEME = "/fxml/css/%s.css";
    public static final String APP_ICON = "vault-icon.png";
    public static final int MAX_LENGTH = 46;
    public static final String URL_ACCOUNT_GET = "/account";
    public static final String DB_USER_TABLE = "PROXIMAX_USER";
    public static final String DB_FILE_TABLE = "PROXIMAX_FILES";
    public static final String DB_SHARE_TABLE = "PROXIMAX_SHARES";
    public static final String DB_FRIEND_TABLE = "PROXIMAX_FRIEND";
    public static final String USERDLG_TITLE = "User Profile";
    public static final String VIEW_COLUMNS_DIALOG_TITLE = "View Columns";
    public static final String USERDLG_FXML = "UserProfile.fxml";
    public static final String VIEW_COLUMNS_DIALOG = "ViewColumnsDialog.fxml";
    public static final String DASHBOARD_TITLE = APP_NAME;
    public static final String DASHBOARD_FXML = "Dashboard.fxml";
    public static final String ENTRY_TITLE = APP_NAME + " - Entry";
    public static final String ENTRY_FXML = "EntryDialog.fxml";
    public static final String LOGIN_TITLE = APP_NAME + " - Login";
    public static final String LOGIN_FXML = "LoginDialog.fxml";
    public static final String SIGNUP_FXML = "SignupDialog.fxml";
    public static final String GROUP_TITLE = APP_NAME + " - Group";
    public static final String GROUP_FXML = "GroupDialog.fxml";
    public static final String ICON_PATH = "/fxml/icons/%d.png";
    public static final int GROUP_ICON = 48;
    public static final int ENTRY_ICON = 0;
    public static final String FXML_DEFAULT_PATH = "/fxml/ui/";
    public static final String FXML_PATH = "/fxml/ui/";
    public static final String ICONDLG_TITLE = APP_NAME + " - Icons";
    public static final String ICONDLG_FXML = "IconDialog.fxml";
    public static final String DB_NETWORK_TABLE = "PROXIMAX_NETWORK";
    public static final String DB_COMMON_TABLE = "PROXIMAX_COMMON";
    public static final String DB_SUPPORT_TABLE = "PROXIMAX_SUPPORT";
    public static final String DB_APP_CONFIGURATION = "proximax.properties";
    public static final String PASSWORD_TITLE = APP_NAME + " - Generator Password";
    public static final String PASSWORD_FXML = "PasswordDialog.fxml";
    public static final String NETWORKDLG_TITLE = APP_NAME + " - Network Configuration";
    public static final String NETWORKDLG_FXML = "NetworkDialog.fxml";
    public static final String ABOUTDLG_TITLE = APP_NAME + " - About";
    public static final String ABOUTDLG_FXML = "AboutUs.fxml";
    
    public static final String SIGNUP2_TITLE = APP_NAME + " - Secure Questions";
    public static final String SIGNUP2_FXML = "Signup2.fxml";
    
    public static final String[] SECURE_QUESTIONS = {
        "What was your childhood nickname?",
        "In what city did you meet your spouse/significant other?",
        "What is the name of your favorite childhood friend? ",
        "What street did you live on in third grade?",
        "What is your oldest sibling’s birthday month and year? (e.g., January 1900)",
        "What is the middle name of your youngest child?",
        "What is your oldest sibling's middle name?",
        "What school did you attend for sixth grade?",
        "What was your childhood phone number including area code? (e.g., 000-000-0000)",
        "What is your oldest cousin's first and last name?",
        "What was the name of your first stuffed animal?",
        "In what city or town did your mother and father meet?",
        "Where were you when you had your first kiss? ",
        "What is the first name of the boy or girl that you first kissed?",
        "What was the last name of your third grade teacher?",
        "In what city does your nearest sibling live?",
        "What is your youngest brother’s birthday month and year? (e.g., January 1900)",
        "What is your maternal grandmother's maiden name?",
        "In what city or town was your first job?",
        "What is the name of the place your wedding reception was held?",
        "What is the name of a college you applied to but didn't attend?",
        "Where were you when you first heard about 9/11?"};

}
