package com.example.authvento.auth;

import com.example.authvento.config.JwtService;
import com.example.authvento.model.OtpModel;
import com.example.authvento.model.ResetUserModel;
import com.example.authvento.model.UserModel;
import com.example.authvento.repository.OtpRepository;
import com.example.authvento.repository.ResetUserRepository;
import com.example.authvento.repository.UserModelRepository;
import com.example.authvento.user.User;
import com.example.authvento.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final OtpRepository otpRepository;
  private final UserModelRepository userModelRepository;
  private final ResetUserRepository resetUserRepository;

  public OtpModel register(OtpModel otpModel) {
    OtpModel customUser = otpRepository.save(otpModel);
    System.out.println("USER TO BE SAVED: " +customUser);
    return customUser;
  }


  public ResetUserModel savePasswordReset(ResetUserModel resetUserModel) {
    ResetUserModel resetUser = resetUserRepository.save(resetUserModel);
    System.out.println("USER TO BE SAVED FOR RESET: " +resetUser);
    return resetUser;
  }

  public OtpModel successRegister(OtpModel otpModel) {
    OtpModel customUser = otpRepository.save(otpModel);
    System.out.println("USER TO BE SAVED: " +customUser);
    return customUser;
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    var user = repository.findByEmail(request.getEmail())
        .orElseThrow();

    System.out.println("print the user: " + user);
    var jwtToken = jwtService.generateToken((UserDetails) user);
    return AuthenticationResponse.builder()
        .token(jwtToken)
            .user(user)
        .build();
  }

  public boolean isAuthenticated(String email, String password){
    Optional<UserModel> user = userModelRepository.findByEmail(email);
    log.info("user in authenticated: {}",user);
    if(user.isPresent() && user.get().getEmail().equals(email)){
      boolean matched = passwordEncoder.matches(password,user.get().getPassword());
      log.info("password match status: {}",matched);
      if(matched){
        return true;
      }else{
        return false;
      }
    }
    return false;
  }

  public Optional<UserModel> findByUsername(String email){
    return userModelRepository.findByEmail(email);
  }

//  public Optional<UserModel> findUserByID(String email){
//    return userModelRepository.findByEmail(email);
//  }


  public UserModel save(UserModel user){
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    return userModelRepository.save(user);
  }


  public void delete(String id){
    userModelRepository.deleteById(Long.valueOf(id));
  }


}
