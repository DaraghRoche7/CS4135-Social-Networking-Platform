package com.studyhub.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.userservice.dto.AuthResponse;
import com.studyhub.userservice.dto.LoginRequest;
import com.studyhub.userservice.dto.RegisterRequest;
import com.studyhub.userservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;

    @Test
    void register_withValidULEmail_returns201AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice Student");
        request.setEmail("alice@studentmail.ul.ie");
        request.setPassword("password123");
        AuthResponse response = new AuthResponse("jwt-token", "refresh-token", UUID.randomUUID(), "Alice Student", "alice@studentmail.ul.ie", "STUDENT");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("alice@studentmail.ul.ie"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void register_withNonULEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@gmail.com");
        request.setPassword("password123");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void register_withShortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@studentmail.ul.ie");
        request.setPassword("short");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withValidCredentials_returns200AndToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@studentmail.ul.ie");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse("jwt-token", "refresh-token", UUID.randomUUID(), "Alice", "alice@studentmail.ul.ie", "STUDENT");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@studentmail.ul.ie");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withBlankName_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("");
        request.setEmail("alice@studentmail.ul.ie");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }
}