package com.example.authvento.responseHandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomResponse {
  private int status;
  private String message;
  private Object data;
}
