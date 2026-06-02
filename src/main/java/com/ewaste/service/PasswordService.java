package com.ewaste.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ewaste.entity.User;
import com.ewaste.repository.UserRepository;

@Service
public class PasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // STEP 1: Generate OTP
    public String generateOtp(String email){

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isEmpty()){
            return "Email not registered";
        }

        User user = optionalUser.get();

        int otp = (int)(Math.random()*9000)+1000;

        user.setResetOtp(String.valueOf(otp));
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);


        return "OTP generated successfully";
    }

    // STEP 2: Verify OTP
    public String verifyOtp(String email, String otp){

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isEmpty()){
            return "User not found";
        }

        User user = optionalUser.get();

        if(user.getResetOtp() == null){
            return "OTP not generated";
        }

        if(!user.getResetOtp().equals(otp)){
            return "Invalid OTP";
        }

        if(user.getOtpExpiry().isBefore(LocalDateTime.now())){
            return "OTP expired";
        }

        return "OTP verified successfully";
    }

    // STEP 3: Reset Password
    public String resetPassword(String email, String newPassword){

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isEmpty()){
            return "User not found";
        }

        User user = optionalUser.get();

        user.setPassword(passwordEncoder.encode(newPassword));

        user.setResetOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);

        return "Password reset successful";
    }

}
