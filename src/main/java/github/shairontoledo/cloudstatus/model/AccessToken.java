package github.shairontoledo.cloudstatus.model;


public class AccessToken extends BaseModel {
    private String identification;
    private String token;

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
