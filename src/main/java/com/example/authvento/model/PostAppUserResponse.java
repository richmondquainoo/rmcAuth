package com.example.authvento.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class PostAppUserResponse {
    private boolean Error;
    private AppUser Data;
    private HttpStatus Code;
    private String Message;

    public PostAppUserResponse(
            boolean Error,
            AppUser Data,
            HttpStatus Code,
            String Message
    ){
        this.Code = Code;
        this.Data = Data;
        this.Message = Message;
        this.Error = Error;
    }


}
