package org.karanveer43f.expenseTracker.service;

import lombok.extern.slf4j.Slf4j;
import org.karanveer43f.expenseTracker.entities.RefreshToken;
import org.karanveer43f.expenseTracker.entities.UserInfo;
import org.karanveer43f.expenseTracker.exceptions.RefreshTokenException;
import org.karanveer43f.expenseTracker.repository.RefreshTokenRepository;
import org.karanveer43f.expenseTracker.repository.UserRepository;
import org.karanveer43f.expenseTracker.response.JwtResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {

    @Autowired RefreshTokenRepository refreshTokenRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtService jwtService;

     public boolean isValid(String token){
        return refreshTokenRepository.findByToken(token).isPresent();
    }

    public JwtResponseDTO refreshToken(String token) throws RefreshTokenException {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));

        verifyExpiration(refreshToken);

        String username = refreshToken.getUserInfo().getUsername();
        String accessToken = jwtService.GenerateToken(username);

        return JwtResponseDTO.builder()
                .accessToken(accessToken)
                .token(token)
                .build();
    }

    public RefreshToken createRefreshToken(String username){
        UserInfo user = userRepository.findByUsername(username);

        Optional<RefreshToken> existing = refreshTokenRepository.findByUserInfo(user);

        if(existing.isPresent()){
            RefreshToken token = existing.get();
            token.setToken(UUID.randomUUID().toString());
            token.setExpiryDate(Instant.now().plusMillis(6000000));
            return refreshTokenRepository.save(token);
        }

        RefreshToken newToken = RefreshToken.builder()
                .userInfo(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(6000000))
                .build();

        return refreshTokenRepository.save(newToken);
    }

    public void verifyExpiration (RefreshToken refreshToken) throws RefreshTokenException {
         if(refreshToken.getExpiryDate().compareTo(Instant.now()) < 0){
             refreshTokenRepository.delete(refreshToken);
             throw new RefreshTokenException(refreshToken.getToken() +  " token was expired. Please login again" );
         }
         log.debug("Token is valid: " + refreshToken.getToken());
    }

    public Optional<RefreshToken> findByToken(String token){
         return refreshTokenRepository.findByToken(token);
    }

}
