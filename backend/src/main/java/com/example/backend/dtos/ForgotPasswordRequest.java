package com.example.backend.dtos;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
}