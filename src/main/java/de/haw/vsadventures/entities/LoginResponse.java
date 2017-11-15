package de.haw.vsadventures.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class LoginResponse {
    String message;
    String token;
    //String valid_till;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

   /* public String getValid_till() {
        return valid_till;
    }

    public void setValid_till(String valid_till) {
        this.valid_till = valid_till;
    }*/

}
