package io.proximax.app.recovery;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author thcao
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-03-08T14:38:43.120Z")
public class AccountStatusDTO {

    @SerializedName("status")
    private String status = null;

    @SerializedName("message")
    private String message = null;

    @ApiModelProperty(value = "")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(value = "")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public AccountStatus getAccountStatus() {
        return new AccountStatus(status, message);
    }

}
