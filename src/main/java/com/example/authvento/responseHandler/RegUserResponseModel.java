package com.example.authvento.responseHandler;

import com.example.authvento.user.User;
import org.springframework.http.HttpStatus;

import java.util.Optional;

public class RegUserResponseModel {

    private boolean Error;
    private Optional<RegisterResponse> Data;
    private HttpStatus Code;
    private String Message;
    private String Token;

    public RegUserResponseModel(boolean Error,
                                Optional<RegisterResponse> Data,
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

    public Optional<RegisterResponse> getData()
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

    public void setData(Optional<RegisterResponse> data) {
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
