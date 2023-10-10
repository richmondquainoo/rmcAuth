package com.example.authvento.responseHandler;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RegisterResponse {
    private String firstname;
    private String lastname;
    private String email;
    private Integer otp;
    private String dateCreated;
}
