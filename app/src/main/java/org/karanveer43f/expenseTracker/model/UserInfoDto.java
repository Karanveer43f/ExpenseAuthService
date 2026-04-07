package org.karanveer43f.expenseTracker.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.karanveer43f.expenseTracker.entities.UserInfo;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserInfoDto extends UserInfo {

    private String lastName;
    private String userName;
    private Long phoneNumber;
    private String email;
}
