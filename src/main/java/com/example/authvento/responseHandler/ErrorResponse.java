package com.example.authvento.responseHandler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ErrorResponse {
    private boolean Error;
    private HttpStatus Code;
    private String Message;

    public ErrorResponse(
            boolean Error,
            HttpStatus Code,
            String Message
    ){
        this.Code = Code;
        this.Message = Message;
        this.Error = Error;
    }


}
