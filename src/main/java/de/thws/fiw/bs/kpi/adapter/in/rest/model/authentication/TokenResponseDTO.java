package de.thws.fiw.bs.kpi.adapter.in.rest.model.authentication;

public class TokenResponseDTO {

    String accessToken;

    public TokenResponseDTO() {
    }
    public TokenResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {}
}
