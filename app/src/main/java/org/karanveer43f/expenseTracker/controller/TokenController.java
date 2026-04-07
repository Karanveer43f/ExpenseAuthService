package org.karanveer43f.expenseTracker.controller;

import org.karanveer43f.expenseTracker.entities.RefreshToken;
import org.karanveer43f.expenseTracker.request.AuthRequestDTO;
import org.karanveer43f.expenseTracker.request.RefreshTokenDTO;
import org.karanveer43f.expenseTracker.response.JwtResponseDTO;
import org.karanveer43f.expenseTracker.service.JwtService;
import org.karanveer43f.expenseTracker.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.EntityManagerFactoryAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class TokenController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private EntityManagerFactoryAccessor entityManagerFactoryAccessor;

    @PostMapping("/auth/v1/login")
    public ResponseEntity AuthenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
            );

            if(authentication.isAuthenticated()){
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
                return new ResponseEntity( JwtResponseDTO
                        .builder()
                        .accessToken(jwtService.GenerateToken(authRequestDTO.getUsername()))
                        .token(refreshToken.getToken())
                        .build() ,null , HttpStatus.OK);
            }else {
                return new ResponseEntity("Invalid Credentials" , null , HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("Exception in Authentication" + e.getMessage());
        }
    }

    @PostMapping("auth/v1/refreshToken")
    public JwtResponseDTO refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO){
        return refreshTokenService
                .findByToken(refreshTokenDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.GenerateToken(userInfo.getUsername());
                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenDTO.getToken())
                            .build();
                }).orElseThrow(()-> new RuntimeException("Refresh token is not in database!"));
    }
}
