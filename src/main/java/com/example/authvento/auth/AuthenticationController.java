package com.example.authvento.auth;

import com.example.authvento.config.JwtService;
import com.example.authvento.model.OtpModel;
import com.example.authvento.model.ResetUserModel;
import com.example.authvento.model.UserModel;
import com.example.authvento.rabbitmq.Email;
import com.example.authvento.rabbitmq.RabbitMQConfig;
import com.example.authvento.rabbitmq.RabbitMQMessageConsumer;
import com.example.authvento.rabbitmq.RabbitMQMessageProducer;
import com.example.authvento.repository.OtpRepository;
import com.example.authvento.repository.UserModelRepository;
import com.example.authvento.responseHandler.RegisterResponse;
import com.example.authvento.responseHandler.ResponseHandler;
import com.example.authvento.responseHandler.ResponseObject;
import com.example.authvento.responseHandler.Verification;
import com.example.authvento.service.EmailService;
import com.example.authvento.service.OTPService;
import com.example.authvento.user.Role;
import com.example.authvento.user.User;
import com.example.authvento.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

  private final UserRepository repository;
  private final AuthenticationService service;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final OTPService otpService;
  private final EmailService emailService;
  private final OtpRepository otpRepository;
  private final UserModelRepository userModelRepository;
  private final RabbitMQMessageProducer rabbitMQMessageProducer;



  private static final String topicExchange = "test-exchange";
  private static final String authEmailQueue = "test-queue";
  private static final String authEmailRoutingKey="test-route";

  private static final String getTopicExchangeVerifyOtp="verify-otp-exchange";
  private static final String verifyOtpRoutingKey="otp-queue";


  @PostMapping("/register")
  @Operation(summary = "Register the user", description = "This endpoint registers the user into the system")
  @ApiResponses(
          value = {
                  @ApiResponse(responseCode = "201", description = "Register the details of the user",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = User.class)
                                  )
                          }
                  )
          }
  )
  public ResponseEntity<?> registerAppUser(@RequestBody User request) throws MessagingException {
    Date date = new Date();
    //Check whether user exists in the database first.
    Optional<User> appUserExist = repository.findByEmail(request.getEmail());
    if(appUserExist.isPresent()) {
      return ResponseHandler.ErrorResponse(
              "User already exist",
              HttpStatus.NOT_ACCEPTABLE,
              true
      );
    }
    //Generate OTP
      int userOTP = otpService.generateOTP(request.getEmail());
      var user = User.builder()
              .firstname(request.getFirstname())
              .lastname(request.getLastname())
              .email(request.getEmail())
              .password(request.getPassword())
              .build();
      var jwtToken = jwtService.generateToken((UserDetails) user);

        //Set the values to the otpModel
        OtpModel otpModel = new OtpModel();
        otpModel.setFirstname(request.getFirstname());
        otpModel.setLastname(request.getLastname());
        otpModel.setEmail(request.getEmail());
        otpModel.setOtp(userOTP);
        otpModel.setPassword(passwordEncoder.encode(request.getPassword()));
        otpModel.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));

        //Set the values to the registerResponseModel
        RegisterResponse regModel = new RegisterResponse();
        regModel.setFirstname(request.getFirstname());
        regModel.setLastname(request.getLastname());
        regModel.setEmail(request.getEmail());
        regModel.setOtp(userOTP);
        regModel.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));

        //Send OTP to customer via email
        Email email = new Email();
        email.setMessageType("Admin_Account_Created");
        email.setRecipientEmail(request.getEmail());
        email.setRecipientName(request.getFirstname());
        email.setSender("RMC-app");
        email.setSubject("Account Verification");
        String emailEncoded = Base64.getEncoder().encodeToString(request.getEmail().getBytes());
        String url = "test.com";
        email.setUrl(url);
        email.setMessage("Hello, Welcome to RMC app. Please enter the OTP you have received to verify your account: "+ userOTP);
        rabbitMQMessageProducer.publish(email,topicExchange,authEmailRoutingKey);
//        rabbitMQMessageConsumer.consumeAuthEmailRoutingKeyMessage(request.getFirstname(),userOTP, request.getEmail());


        //Save the otp model to be able to do otp validation
        OtpModel savedOtpModel = service.register(otpModel);
        return ResponseHandler.registerResponseModel(
                "User has been successfully created",
                HttpStatus.CREATED,
                Optional.of(regModel),
                false,
                jwtToken
        );
  }


  @PostMapping("/verify")
  public ResponseEntity<?> verifyOtp(@RequestBody OtpModel req) {
    try {
      log.info("FIND LATEST USER BY EMAIL: " + userModelRepository.findLatestByEmail(req.getEmail())  );
      if(userModelRepository.findLatestByEmail(req.getEmail()) != null){
        return new ResponseEntity<>(new Verification("User already exists", false), HttpStatus.BAD_REQUEST);
      }
      else{
        log.info("The request body: ", req.getEmail());
        OtpModel otpModel = otpService.fetchOtpByEmail(req.getEmail());
        Optional<User> userModel = repository.findByEmail(req.getEmail());
//      log.info(String.valueOf(systemOtp));
        if (Objects.equals(req.getOtp(), otpModel.getOtp())) {
          Date date = new Date();
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
          Date startDate = sdf.parse(otpModel.getDateCreated());
          Date endDate = sdf.parse(sdf.format(date));
          long diff = computeDifferenceInMinutes(startDate, endDate);

          //Otp expires after 25 mins
          if (diff < 25) {
            System.out.println("THE DIFF 2: " + diff);
            UserModel user = new UserModel();
            user.setFirstName(req.getFirstname());
            user.setLastName(req.getLastname());
            user.setEmail(req.getEmail());
            user.setPassword(otpModel.getPassword());
            user.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));

            log.info("user model to be saved{} :", user);

            Email email = new Email();
            email.setMessageType("Mobile_Account_Created");
            email.setRecipientEmail(req.getEmail());
            email.setRecipientName(req.getFirstname());
            email.setSender("RMC-app");
            email.setSubject("Account Verification Confirmation");
            String emailEncoded = Base64.getEncoder().encodeToString(req.getEmail().getBytes());
            String url = "test.com";
            email.setUrl(url);
            email.setMessage("Your account verification is successful. Have a wonderful experience on our application. Jesus is Alive!");
            rabbitMQMessageProducer.publish(email,topicExchange,authEmailRoutingKey);

            userModelRepository.save(user);

            return new ResponseEntity<>(new Verification("Valid OTP", true), HttpStatus.OK);
          } else {
            return new ResponseEntity<>(new Verification("Verification failed due to OTP expiration", false), HttpStatus.OK);
          }
        } else {
          return new ResponseEntity<>(new Verification("Verification failed. Wrong OTP", false), HttpStatus.NOT_FOUND);
        }
      }
    }catch (Exception e){
      log.error("otp verification error: {}",e.getMessage());
      return new ResponseEntity<>(new Verification("Verification failed.",false), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/authenticate")
  public ResponseEntity<?> authenticate(@RequestBody User req){
    log.error("User request body: {}", req);
    try {
      log.error("User request body: {}", req);
      boolean authenticated =  service.isAuthenticated(req.getEmail(), req.getPassword());
      if(authenticated){
        Optional<UserModel> user = service.findByUsername(req.getEmail());
        user.get().setPassword("****");
        return ResponseHandler.handleResponse(
                "Authentication successful",
                HttpStatus.OK,
                user
        );
      }
      return ResponseHandler.ErrorResponse(
              "Authentication failed",
              HttpStatus.OK,
              true
      );
    }catch (Exception e){
      log.error("Authentication error: {}",e.getMessage());
      return ResponseHandler.ErrorResponse(
              "Server Error",
              HttpStatus.OK,
              true
      );
    }
  }

  @PostMapping("/resetPassword")
  public ResponseEntity<?> resetPassword(@RequestBody UserModel req){
    try {
      Date date = new Date();
      log.info("THE USER EMAIL :" , req.getEmail());
      //Find user by the email supplied
      Optional<UserModel> findUser = service.findByUsername(req.getEmail());
      log.info("THE USER IN THE DB :" , findUser);
      //Generate OTP
      int userOTP = otpService.generateOTP(req.getEmail());
      log.info("THE OTP:" , userOTP);
      //Send otp via mail with messaging queue.
      Email email = new Email();
      email.setMessageType("Password_Reset_OTP");
      email.setRecipientEmail(req.getEmail());
      email.setRecipientName(req.getFirstName());
      email.setSender("RMC-app");
      email.setSubject("Password Reset");
      String emailEncoded = Base64.getEncoder().encodeToString(req.getEmail().getBytes());
      String url = "test.com";
      email.setUrl(url);
      email.setMessage("Hello, "+req.getFirstName()+ " please enter the otp received to initiate password reset: "+ userOTP);
      rabbitMQMessageProducer.publish(email,topicExchange,authEmailRoutingKey);

      OtpModel otpModel = new OtpModel();
      otpModel.setFirstname(findUser.get().getFirstName());
      otpModel.setLastname(findUser.get().getLastName());
      otpModel.setEmail(findUser.get().getEmail());
      otpModel.setOtp(userOTP);
      otpModel.setPassword(passwordEncoder.encode(findUser.get().getPassword()));
      otpModel.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));

      //Save the otp model to be able to do otp validation
      OtpModel savedOtpModel = service.register(otpModel);

        return ResponseHandler.handleResponse(
                "Password initiated successfully",
                HttpStatus.OK,
                otpModel
        );

    }catch (Exception e){
      log.error("Password Reset error: {}",e.getMessage());
      return ResponseHandler.handleResponse(
              "Server Error",
              HttpStatus.INTERNAL_SERVER_ERROR,
              null
      );
    }
  }

  @PostMapping("/verify-password-reset")
  public ResponseEntity<?> verifyOtpReset(@RequestBody OtpModel req) {
    try {
      log.info("FIND LATEST USER BY EMAIL: " + userModelRepository.findLatestByEmail(req.getEmail())  );
      if(userModelRepository.findLatestByEmail(req.getEmail()) == null){
        return new ResponseEntity<>(new Verification("User does not exists", false), HttpStatus.BAD_REQUEST);
      }
      else{
        log.info("The request body: ", req.getEmail());
        ResetUserModel otpModel = otpService.fetchOtpForResetPassword(req.getEmail());
        Optional<User> userModel = repository.findByEmail(req.getEmail());
        //Find user by the email supplied
        Optional<UserModel> findUser = service.findByUsername(req.getEmail());
        if (Objects.equals(req.getOtp(), otpModel.getOtp())) {
          Date date = new Date();
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
          Date startDate = sdf.parse(otpModel.getDateCreated());
          Date endDate = sdf.parse(sdf.format(date));
          long diff = computeDifferenceInMinutes(startDate, endDate);

          //Otp expires after 25 mins
          if (diff < 25) {
            UserModel user = new UserModel();
            user.setFirstName(req.getFirstname());
            user.setLastName(req.getLastname());
            user.setEmail(req.getEmail());
            user.setPassword(otpModel.getPassword());
            user.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));

            log.info("user model to be saved{} :", user);

           return new ResponseEntity<>(new Verification("Valid OTP", true), HttpStatus.OK);
          } else {
            return new ResponseEntity<>(new Verification("Verification failed due to OTP expiration", false), HttpStatus.OK);
          }
        } else {
          return new ResponseEntity<>(new Verification("Verification failed. Wrong OTP", false), HttpStatus.NOT_FOUND);
        }
      }
    }catch (Exception e){
      log.error("otp verification error: {}",e.getMessage());
      return new ResponseEntity<>(new Verification("Verification failed.",false), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/update-password")
  public ResponseEntity<?>  updatePassword(@RequestBody UserModel req){
    try{
      Optional<UserModel> findUser = service.findByUsername(req.getEmail());
      if(findUser.isPresent()){
        req.setId(findUser.get().getId());
        service.save(req);
        Email email = new Email();
        email.setMessageType("Password_Reset_OTP");
        email.setRecipientEmail(req.getEmail());
        email.setRecipientName(req.getFirstName());
        email.setSender("RMC-app");
        email.setSubject("Password Reset");
        String emailEncoded = Base64.getEncoder().encodeToString(req.getEmail().getBytes());
        String url = "test.com";
        email.setUrl(url);
        email.setMessage("Your password has been updated successfully. Jesus is Alive!");
        rabbitMQMessageProducer.publish(email,topicExchange,authEmailRoutingKey);

        return ResponseHandler.handleResponse(
                "Password reset successful",
                HttpStatus.OK,
                req
        );
      }
      return ResponseHandler.handleResponse(
              "Failed to update password",
              HttpStatus.NOT_FOUND,
              null
      );

    }catch (Exception e){
      return ResponseHandler.handleResponse(
              "Server Error",
              HttpStatus.INTERNAL_SERVER_ERROR,
              null
      );
    }
  }


  @PostMapping("/delete")
  public ResponseEntity<?> delete(@RequestBody UserModel req){
    try {
      Optional<UserModel> findUser = service.findByUsername(req.getEmail());
      if(findUser.isPresent()){
        service.delete(req.getEmail());
//                if(auth != null){
//                    Email email = new Email();
//                    email.setMessageType("Password_Reset_Successful");
//                    email.setRecipientEmail(auth.getEmail());
//                    String emailEncoded = Base64.getEncoder().encodeToString(auth.getEmail().getBytes());
//                    String url = accountLoginUrl+emailEncoded;
//                    email.setUrl(url);
//                    email.setSubject("Password Reset Successful");
//                    email.setMessage("Hello, Your password reset was successful. Please log into your account by using the button below.");
//                    rabbitMQMessageProducer.publish(email,topicExchange,authEmailRoutingKey);
//                }
        return new ResponseEntity<>(new ResponseObject(HttpStatus.OK.value(),"Account deletion successful", req),HttpStatus.OK);
      }
      return new ResponseEntity<>(new ResponseObject(HttpStatus.NOT_FOUND.value(),"Account deletion failed: No user with this email found", null),HttpStatus.NOT_FOUND);
    }catch (Exception e){
      log.error("Account Deletion error: {}",e.getMessage());
      return new ResponseEntity<>(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Server Error", e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }



  private Auth getData(String entity) throws UnsupportedEncodingException {
    Auth auth;
    String str = URLDecoder.decode(entity, StandardCharsets.UTF_8.toString());
    log.info("decoded string: {}",str);
    Map<String, String> map = decodeUrl(entity);
    log.info("Map value: {}",map);
    final ObjectMapper mapper = new ObjectMapper();
    auth = mapper.convertValue(map,Auth.class);
    return auth;
  }


  private String buildEmail(String name, Integer generatedOtp) {
    return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
            "\n" +
            "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
            "\n" +
            "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
            "    <tbody><tr>\n" +
            "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b1c0c\">\n" +
            "        \n" +
            "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
            "          <tbody><tr>\n" +
            "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
            "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
            "                  <tbody><tr>\n" +
            "                    <td style=\"padding-left:10px\">\n" +
            "                  \n" +
            "                    </td>\n" +
            "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
            "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
            "                    </td>\n" +
            "                  </tr>\n" +
            "                </tbody></table>\n" +
            "              </a>\n" +
            "            </td>\n" +
            "          </tr>\n" +
            "        </tbody></table>\n" +
            "        \n" +
            "      </td>\n" +
            "    </tr>\n" +
            "  </tbody></table>\n" +
            "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
            "    <tbody><tr>\n" +
            "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
            "      <td>\n" +
            "        \n" +
            "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
            "                  <tbody><tr>\n" +
            "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
            "                  </tr>\n" +
            "                </tbody></table>\n" +
            "        \n" +
            "      </td>\n" +
            "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
            "    </tr>\n" +
            "  </tbody></table>\n" +
            "\n" +
            "\n" +
            "\n" +
            "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
            "    <tbody><tr>\n" +
            "      <td height=\"30\"><br></td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
            "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
            "        \n" +
            "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p>" +
            "               <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please enter the OTP you have received to verify your account: "+generatedOtp+" </p>"
            +"        \n" +
            "      </td>\n" +
            "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "      <td height=\"30\"><br></td>\n" +
            "    </tr>\n" +
            "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
            "\n" +
            "</div></div>";
  }


  private long computeDifferenceInMinutes(Date startDate, Date endDate) throws ParseException {
    long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
    long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
    System.out.println("No. of minutes between dates for: {}" +diff);
    return diff;
  }


  private Map<String, String> decodeUrl(String s) {
    Map<String, String> params = new HashMap<String, String>();
    if (s != null) {
      String array[] = s.split("&");
      for (String parameter : array) {
        String v[] = parameter.split("=");
        if (v.length > 1) {
          params.put(URLDecoder.decode(v[0]), v.length > 1 ? URLDecoder.decode(v[1]) : null);
        }
      }
    }
    return params;
  }




  @PostMapping("/sendEmail")
  public ResponseEntity<Email> sendEmail(@RequestBody Email email){
    email.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    try{
      log.error("*************** send email post request");
      emailService.sendHTMLEmail(email);
      email.setDeliveryStatus(200);
      email.setDeliveryMessage("Sent");
      return new ResponseEntity<>(emailService.save(email), HttpStatus.OK);
    }catch (Exception e){
      log.error("Error sending email: {}",e.getMessage());
      email.setDeliveryStatus(500);
      email.setDeliveryMessage("An error has occurred");
      return new ResponseEntity<>(emailService.save(email),HttpStatus.OK);
    }
  }

}


