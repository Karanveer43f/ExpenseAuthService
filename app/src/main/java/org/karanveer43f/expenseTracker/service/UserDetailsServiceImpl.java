package org.karanveer43f.expenseTracker.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.karanveer43f.expenseTracker.entities.UserInfo;
import org.karanveer43f.expenseTracker.model.UserInfoDto;
import org.karanveer43f.expenseTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
@Data
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo =  userRepository.findByUsername(username);
        if(userInfo == null){
            throw  new UsernameNotFoundException("User not found with username: " + username);
        }
        return new CustomUserDetails(userInfo);
    }

    public UserInfo checkIfUserAlreadyExists(UserInfoDto userInfo){
        return userRepository.findByUsername(userInfo.getUsername());
    }

    public Boolean signUpUser(UserInfoDto userInfoDto){
        userInfoDto.setPassword(passwordEncoder.encode(userInfoDto.getPassword()));
        if(Objects.nonNull(checkIfUserAlreadyExists(userInfoDto))){
            return false;
        }

        String userId = UUID.randomUUID().toString();
        userRepository.save(new UserInfo(userId, userInfoDto.getUsername(), userInfoDto.getPassword(), new HashSet<>()));
        return true;
    }
}
