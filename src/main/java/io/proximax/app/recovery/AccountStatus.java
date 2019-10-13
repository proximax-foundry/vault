package io.proximax.app.recovery;

/**
 *
 * @author thcao
 */
public class AccountStatus {

    private String status = null;
    private String message = null;

    public AccountStatus(String status, String message) {
        this.message = message;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
