package com.example.authvento.auth;

import com.example.authvento.config.JwtService;
import com.example.authvento.model.OtpModel;
import com.example.authvento.repository.OtpRepository;
import com.example.authvento.user.User;
import com.example.authvento.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final OtpRepository otpRepository;

  public OtpModel register(OtpModel otpModel) {
    OtpModel customUser = otpRepository.save(otpModel);
    System.out.println("USER TO BE SAVED: " +customUser);
    return customUser;
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


}
