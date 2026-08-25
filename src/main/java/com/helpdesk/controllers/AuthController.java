package com.helpdesk.controllers;

import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.LoginRequest;
import com.helpdesk.dto.RefreshRequest;
import com.helpdesk.dto.RegistroRequest;
import com.helpdesk.entities.Usuario;
import com.helpdesk.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new UsuarioResumen(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol().name())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refrescar(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    private record UsuarioResumen(Long id, String nombre, String email, String rol) {}
}
