package com.helpdesk.controllers;

import com.helpdesk.dto.EstadoUpdateRequest;
import com.helpdesk.dto.TicketRequest;
import com.helpdesk.dto.TicketResponse;
import com.helpdesk.entities.Ticket;
import com.helpdesk.entities.Usuario;
import com.helpdesk.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final com.helpdesk.repositories.UsuarioRepository usuarioRepository;

    private Usuario usuarioActual(User principal) {
        return usuarioRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new com.helpdesk.exceptions.RecursoNoEncontradoException("Usuario autenticado no encontrado"));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> crear(@Valid @RequestBody TicketRequest request,
                                                 @AuthenticationPrincipal User principal) {
        Ticket ticket = ticketService.crear(request, usuarioActual(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.fromEntity(ticket));
    }

    @GetMapping("/mios")
    public ResponseEntity<List<TicketResponse>> misTickets(@AuthenticationPrincipal User principal) {
        List<TicketResponse> tickets = ticketService.listarMios(usuarioActual(principal))
                .stream().map(TicketResponse::fromEntity).toList();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> obtener(@PathVariable Long id, @AuthenticationPrincipal User principal) {
        Ticket ticket = ticketService.obtenerPorId(id, usuarioActual(principal));
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SOPORTE','ADMIN')")
    public ResponseEntity<Page<TicketResponse>> listarTodos(Pageable pageable) {
        Page<TicketResponse> pagina = ticketService.listarTodos(pageable).map(TicketResponse::fromEntity);
        return ResponseEntity.ok(pagina);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('SOPORTE','ADMIN')")
    public ResponseEntity<TicketResponse> cambiarEstado(@PathVariable Long id,
                                                          @Valid @RequestBody EstadoUpdateRequest request,
                                                          @AuthenticationPrincipal User principal) {
        Ticket ticket = ticketService.cambiarEstado(id, request.getEstado(), usuarioActual(principal));
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    @GetMapping("/vencidos")
    @PreAuthorize("hasAnyRole('SOPORTE','ADMIN')")
    public ResponseEntity<List<TicketResponse>> vencidos() {
        List<TicketResponse> tickets = ticketService.listarVencidos()
                .stream().map(TicketResponse::fromEntity).toList();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<?> historial(@PathVariable Long id, @AuthenticationPrincipal User principal) {
        ticketService.obtenerPorId(id, usuarioActual(principal));
        return ResponseEntity.ok(ticketService.historial(id));
    }
}
