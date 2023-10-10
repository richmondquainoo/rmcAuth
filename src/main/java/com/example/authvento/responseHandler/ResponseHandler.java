package com.example.authvento.responseHandler;

import com.example.authvento.model.*;
import com.example.authvento.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResponseHandler {
    public static ResponseEntity<Object> handleResponse(String message, HttpStatus status, Object responseObj){
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("message", message);
        responseMap.put("status", status.value());
        if (status == HttpStatus.OK || status == HttpStatus.CREATED) {
            responseMap.put("data", responseObj);
        } else {
            responseMap.put("errors", responseObj);
        }
        return new ResponseEntity<>(responseMap, status);
    }


    public static ResponseEntity<AppUserResponseModel> GetFetchUserResponse(String message, HttpStatus code, Optional<OtpModel> user, boolean error, String token) {
        AppUserResponseModel response = new AppUserResponseModel(
                error, user, code, message,token
        );
        return new ResponseEntity<>(response, code);
    }

    public static ResponseEntity<RegUserResponseModel> registerResponseModel(String message, HttpStatus code, Optional<RegisterResponse> user, boolean error, String token) {
        RegUserResponseModel response = new RegUserResponseModel(
                error, user, code, message,token
        );
        return new ResponseEntity<>(response, code);
    }

    public static ResponseEntity<PostAppUserResponse> PostUserResponse(String message, HttpStatus code, AppUser appUser, boolean error) {
        PostAppUserResponse response = new PostAppUserResponse(
                error, appUser, code, message
        );
        return new ResponseEntity<>(response, code);
    }

    public static ResponseEntity<ErrorResponse> ErrorResponse(String message, HttpStatus code, boolean error) {
        ErrorResponse response = new ErrorResponse(
                error, code, message
        );
        return new ResponseEntity<>(response, code);
    }


}
