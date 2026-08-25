package com.helpdesk.controllers;

import com.helpdesk.dto.EstadisticasResponse;
import com.helpdesk.dto.PromoverRequest;
import com.helpdesk.exceptions.RecursoNoEncontradoException;
import com.helpdesk.enums.Rol;
import com.helpdesk.entities.Usuario;
import com.helpdesk.repositories.UsuarioRepository;
import com.helpdesk.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final TicketService ticketService;

    @PostMapping("/soporte")
    @Transactional
    public ResponseEntity<?> promoverASoporte(@Valid @RequestBody PromoverRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con email " + request.getEmail()));
        usuario.setRol(Rol.SOPORTE);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new UsuarioResumen(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol().name()));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasResponse> estadisticas() {
        return ResponseEntity.ok(ticketService.estadisticas());
    }

    private record UsuarioResumen(Long id, String nombre, String email, String rol) {}
}
