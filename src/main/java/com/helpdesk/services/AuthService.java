package com.helpdesk.services;

import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.LoginRequest;
import com.helpdesk.dto.RegistroRequest;
import com.helpdesk.exceptions.EmailYaRegistradoException;
import com.helpdesk.exceptions.TokenInvalidoException;
import com.helpdesk.entities.RefreshToken;
import com.helpdesk.enums.Rol;
import com.helpdesk.entities.Usuario;
import com.helpdesk.repositories.RefreshTokenRepository;
import com.helpdesk.repositories.UsuarioRepository;
import com.helpdesk.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public Usuario registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailYaRegistradoException(request.getEmail());
        }
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.USUARIO) // todo registro publico nace como USUARIO
                .build();
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new TokenInvalidoException("Usuario no encontrado"));

        String accessToken = jwtService.generarAccessToken(
                User.withUsername(usuario.getEmail()).password(usuario.getPassword())
                        .authorities("ROLE_" + usuario.getRol().name()).build(),
                usuario.getRol().name());

        String refreshToken = emitirRefreshToken(usuario);

        return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    @Transactional
    public AuthResponse refrescar(String refreshTokenValor) {
        RefreshToken tokenGuardado = refreshTokenRepository.findByToken(refreshTokenValor)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token invalido"));

        if (tokenGuardado.isRevocado()) {
            throw new TokenInvalidoException("Refresh token revocado");
        }
        if (tokenGuardado.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoException("Refresh token expirado");
        }

        Usuario usuario = tokenGuardado.getUsuario();

        String nuevoAccessToken = jwtService.generarAccessToken(
                User.withUsername(usuario.getEmail()).password(usuario.getPassword())
                        .authorities("ROLE_" + usuario.getRol().name()).build(),
                usuario.getRol().name());

        tokenGuardado.setRevocado(true);
        refreshTokenRepository.save(tokenGuardado);
        String nuevoRefreshToken = emitirRefreshToken(usuario);

        return AuthResponse.builder().accessToken(nuevoAccessToken).refreshToken(nuevoRefreshToken).build();
    }

    @Transactional
    public void logout(String refreshTokenValor) {
        RefreshToken tokenGuardado = refreshTokenRepository.findByToken(refreshTokenValor)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token invalido"));
        tokenGuardado.setRevocado(true);
        refreshTokenRepository.save(tokenGuardado);
    }

    private String emitirRefreshToken(Usuario usuario) {
        String valor = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(valor)
                .usuario(usuario)
                .expiraEn(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000))
                .revocado(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return valor;
    }
}
