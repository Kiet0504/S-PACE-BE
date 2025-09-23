package com.example.S_PACE.dto.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String avatar;
}
