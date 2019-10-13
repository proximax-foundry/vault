package io.proximax.app.recovery;

/**
 *
 * @author thcao
 */
public class AccountInfo {

    private String userName = null;
    private String email = null;
    private String question1 = null;
    private String answer1 = null;
    private String question2 = null;
    private String answer2 = null;
    private String question3 = null;
    private String answer3 = null;
    private String privateKey = null;
    private String publicKey = null;
    private String address = null;

    AccountInfo() {
    }

    public AccountInfo(String userName, String email, String question1, String answer1, String question2, String answer2, String question3, String answer3, String privateKey, String publicKey, String address) {
        this.userName = userName;
        this.email = email;
        this.question1 = question1;
        this.question2 = question2;
        this.question3 = question3;
        this.address = address;
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.answer3 = answer3;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setQuestion1(String question1) {
        this.question1 = question1;
    }

    public String getAddress() {
        return address;
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

    public String getAnswer1() {
        return answer1;
    }

    public String getAnswer2() {
        return answer2;
    }

    public String getAnswer3() {
        return answer3;
    }

    public String getEmail() {
        return email;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getQuestion1() {
        return question1;
    }

    public String getQuestion2() {
        return question2;
    }

    public String getQuestion3() {
        return question3;
    }

    public String getUserName() {
        return userName;
    }

    public boolean compare(String email, String privateKey, String publicKey, String address) {
        return this.email.equals(email) && this.privateKey.equals(privateKey) && this.publicKey.equals(publicKey) && this.address.equals(address);
    }

}
