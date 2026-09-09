package com.parquecafe.accesoapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String rol;
    private String nombreMostrar;
}