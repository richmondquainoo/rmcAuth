package com.example.authvento.service;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.example.authvento.model.OtpModel;
import com.example.authvento.repository.OtpRepository;
import com.example.authvento.user.User;
import com.example.authvento.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

@Service
@RequiredArgsConstructor
public class OTPService {

    private UserRepository repository;
    @Autowired
    private OtpRepository otpRepository;

    private static final Integer EXPIRE_MINS = 4;
    private final LoadingCache<String, Integer> otpCache;
    public OTPService(){
        super();
        otpCache = CacheBuilder.newBuilder().
                expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public int generateOTP(String key){
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        otpCache.put(key, otp);
        return otp;
    }

    public OtpModel fetchOtpByEmail(String email){
       return otpRepository.findLatestOTPByEmail(email);

    }

    public void clearOTP(String key){
        otpCache.invalidate(key);
    }


    private long computeDifferenceInMinutes(Date startDate, Date endDate) throws ParseException {
        long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
        long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
        System.out.println("No. of minutes between dates for: {}" +diff);
        return diff;
    }
}