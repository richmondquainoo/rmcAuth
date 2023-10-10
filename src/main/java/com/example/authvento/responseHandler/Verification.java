package com.example.authvento.responseHandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;


@Data
@NoArgsConstructor
@ToString
public class Verification implements Serializable {

    private String message;
    private boolean verificationStatus;


    public Verification(String message, boolean verificationStatus) {
        this.message = message;
        this.verificationStatus = verificationStatus;
    }
}
