package authservice.controller;

import authservice.entities.RefreshToken;
import authservice.request.AuthRequestDTO;
import authservice.request.RefreshTokenDTO;
import authservice.response.ErrorResponse;
import authservice.response.JwtResponseDTO;
import authservice.service.JwtService;
import authservice.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
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
            Authentication authentication;
            try{
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
                );
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Bad Credentials: " + e.getMessage());
            }

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
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Exception in Authentication" + e.getMessage());
        }
    }

//    @PostMapping("auth/v1/refreshToken")
//    public ResponseEntity<JwtResponseDTO> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO){
//        System.out.println("Refreshing token for " + refreshTokenDTO.getToken());
//        try{
//            Optional<RefreshToken> refreshToken = refreshTokenService.findByToken(refreshTokenDTO.getToken());
//            if(refreshToken.isEmpty()){
//                return ResponseEntity.badRequest().body(null);
//            }
//            refreshToken = Optional.ofNullable(refreshTokenService.verifyExpiration(refreshToken.get()));
//
//            String username = refreshToken.get().getUserInfo().getUsername();
//            String accessToken = jwtService.GenerateToken(username);
//
//
//            return new ResponseEntity<>(JwtResponseDTO.builder()
//                    .accessToken(accessToken)
//                    .token(refreshTokenDTO.getToken())
//                    .build(), null, HttpStatus.OK);
//
//        }catch (Exception e){
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body(null);
//        }

//        return refreshTokenService
//                .findByToken(refreshTokenDTO.getToken())
//                .map(refreshTokenService::verifyExpiration)
//                .map(RefreshToken::getUserInfo)
//                .map(userInfo -> {
//                    System.out.println("Generating new token for " + userInfo.getUsername());
//                    String accessToken = jwtService.GenerateToken(userInfo.getUsername());
//                    return JwtResponseDTO.builder()
//                            .accessToken(accessToken)
//                            .token(refreshTokenDTO.getToken())
//                            .build();
//                }).orElseThrow(()-> new RuntimeException("Refresh token is not in database!"));
//
//    }

    @PostMapping("/auth/v1/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO dto) {
        try {
            JwtResponseDTO response = refreshTokenService.refreshToken(dto.getToken());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage(), 403, System.currentTimeMillis()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Something went wrong", 500, System.currentTimeMillis()));
        }
    }
}
