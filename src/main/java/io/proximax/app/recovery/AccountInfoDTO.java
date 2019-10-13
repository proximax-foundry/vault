package io.proximax.app.recovery;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author thcao
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-03-08T14:38:43.120Z")
public class AccountInfoDTO {

    @SerializedName("userName")
    private String userName = null;

    @SerializedName("email")
    private String email = null;

    @SerializedName("question1")
    private String question1 = null;

    @SerializedName("answer1")
    private String answer1 = null;

    @SerializedName("question2")
    private String question2 = null;

    @SerializedName("answer2")
    private String answer2 = null;

    @SerializedName("question3")
    private String question3 = null;

    @SerializedName("answer3")
    private String answer3 = null;

    @SerializedName("privateKey")
    private String privateKey = null;

    @SerializedName("publicKey")
    private String publicKey = null;

    @SerializedName("address")
    private String address = null;

    @ApiModelProperty(value = "")
    public String getUserName() {
        return userName;
    }

    @ApiModelProperty(value = "")
    public String getEmail() {
        return email;
    }

    @ApiModelProperty(value = "")
    public String getQuestion1() {
        return question1;
    }

    @ApiModelProperty(value = "")
    public String getAnswer1() {
        return answer1;
    }

    @ApiModelProperty(value = "")
    public String getQuestion2() {
        return question2;
    }

    @ApiModelProperty(value = "")
    public String getAnswer2() {
        return answer2;
    }

    @ApiModelProperty(value = "")
    public String getQuestion3() {
        return question3;
    }

    @ApiModelProperty(value = "")
    public String getAnswer3() {
        return answer3;
    }

    @ApiModelProperty(value = "")
    public String getAddress() {
        return address;
    }

    @ApiModelProperty(value = "")
    public String getPrivateKey() {
        return privateKey;
    }

    @ApiModelProperty(value = "")
    public String getPublicKey() {
        return publicKey;
    }

    public void setAnswer1(String answer1) {
        this.answer1 = answer1;
    }

    public void setAnswer2(String answer2) {
        this.answer2 = answer2;
    }

    public void setAnswer3(String answer3) {
        this.answer3 = answer3;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setQuestion1(String question1) {
        this.question1 = question1;
    }

    public void setQuestion2(String question2) {
        this.question2 = question2;
    }

    public void setQuestion3(String question3) {
        this.question3 = question3;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @ApiModelProperty(required = true, value = "")
    public AccountInfo getAccountInfo() {
        return new AccountInfo(userName, email, question1, answer1, question2, answer2, question3, answer3, privateKey, publicKey, address);
    }
}
