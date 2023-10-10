package com.example.authvento.model;

import com.example.authvento.user.User;
import org.springframework.http.HttpStatus;

import java.util.Optional;

public class AppUserResponseModel {

    private boolean Error;
    private Optional<OtpModel> Data;
    private HttpStatus Code;
    private String Message;
    private String Token;

    public AppUserResponseModel(boolean Error,
                                 Optional<OtpModel> Data,
                                 HttpStatus Code,
                                 String Message,
                                 String Token
    )
    {
        this.Code = Code;
        this.Data = Data;
        this.Message = Message;
        this.Error = Error;
        this.Token = Token;
    }

    public HttpStatus getCode ()
    {
        return Code;
    }

    public Optional<OtpModel> getData()
    {
        return Data;
    }

    public boolean getError()
    {
        return Error;
    }

    public String getMessage() {
        return Message;
    }

    public String getToken() {
        return Token;
    }

    public void setCode(HttpStatus code) {
        Code = code;
    }

    public void setData(Optional<OtpModel> data) {
        Data = data;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public void setError(boolean error) {
        Error = error;
    }

    public void setToken(String token) {
        Token = token;
    }

}
