package com.proyecto.version1.auth.service;

import com.proyecto.version1.auth.request.AuthenticationRequest;
import com.proyecto.version1.auth.request.RefreshRequest;
import com.proyecto.version1.auth.request.RegistrationRequest;
import com.proyecto.version1.auth.response.AuthenticationResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    ResponseEntity<AuthenticationResponse> login(AuthenticationRequest request);

    void register(RegistrationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest req);
}
