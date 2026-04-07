package org.karanveer43f.expenseTracker.service;

import com.fasterxml.jackson.annotation.OptBoolean;
import org.karanveer43f.expenseTracker.entities.RefreshToken;
import org.karanveer43f.expenseTracker.entities.UserInfo;
import org.karanveer43f.expenseTracker.repository.RefreshTokenRepository;
import org.karanveer43f.expenseTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired RefreshTokenRepository refreshTokenRepository;

    @Autowired
    UserRepository userRepository;

     public boolean isValid(String token){
        return refreshTokenRepository.findByToken(token).isPresent();
    }

    public RefreshToken createRefreshToken(String username){
         UserInfo userInfoExtracted = userRepository.findByUsername(username);
         RefreshToken refreshToken = RefreshToken.builder()
                 .userInfo(userInfoExtracted)
                 .token(UUID.randomUUID().toString())
                 .expiryDate(Instant.now().plusMillis(6000000))
                 .build();
         refreshTokenRepository.save(refreshToken);
         return refreshToken;
    }

    public RefreshToken verifyExpiration (RefreshToken refreshToken){
         if(refreshToken.getExpiryDate().compareTo(Instant.now()) < 0){
             refreshTokenRepository.delete(refreshToken);
             throw new RuntimeException(refreshToken.getToken() +  " token was expired. Please login again");
         }
         return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token){
         return refreshTokenRepository.findByToken(token);
    }

}
