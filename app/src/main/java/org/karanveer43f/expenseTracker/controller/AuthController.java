package org.karanveer43f.expenseTracker.controller;

import org.karanveer43f.expenseTracker.entities.RefreshToken;
import org.karanveer43f.expenseTracker.entities.UserInfo;
import org.karanveer43f.expenseTracker.model.UserInfoDto;
import org.karanveer43f.expenseTracker.response.JwtResponseDTO;
import org.karanveer43f.expenseTracker.service.JwtService;
import org.karanveer43f.expenseTracker.service.RefreshTokenService;
import org.karanveer43f.expenseTracker.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AuthController {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("/auth/v1/signup")
    public ResponseEntity signup(@RequestBody UserInfoDto userInfoDto){
        try{
            Boolean isSignedUp = userDetailsService.signUpUser(userInfoDto);
            if(Boolean.FALSE.equals(isSignedUp)){
                return new ResponseEntity("User with email " + userInfoDto.getUsername() + " already exists", null, HttpStatus.BAD_REQUEST);
            }

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUsername());
            String jwtToken = jwtService.GenerateToken(userInfoDto.getUsername());
            return new ResponseEntity<>(JwtResponseDTO
                    .builder()
                    .accessToken(jwtToken)
                    .token(refreshToken.getToken())
                    .build(
            ), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity("Exception in User Service" + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
