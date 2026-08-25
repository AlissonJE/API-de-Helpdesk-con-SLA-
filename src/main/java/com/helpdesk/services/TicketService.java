package com.helpdesk.services;

import com.helpdesk.dto.EstadisticasResponse;
import com.helpdesk.dto.TicketRequest;
import com.helpdesk.exceptions.RecursoNoEncontradoException;
import com.helpdesk.entities.*;
import com.helpdesk.enums.*;
import com.helpdesk.repositories.TicketHistorialRepository;
import com.helpdesk.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketHistorialRepository ticketHistorialRepository;

    @Transactional
    public Ticket crear(TicketRequest request, Usuario usuarioActual) {
        LocalDateTime ahora = LocalDateTime.now();
        Ticket ticket = Ticket.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad())
                .estado(Estado.ABIERTO) // el cliente nunca define el estado inicial
                .creadoEn(ahora)
                .slaVenceEn(ahora.plusHours(request.getPrioridad().getHorasSla())) // regla de negocio del SLA
                .creadoPor(usuarioActual)
                .build();
        return ticketRepository.save(ticket);
    }

    public List<Ticket> listarMios(Usuario usuarioActual) {
        return ticketRepository.findByCreadoPor(usuarioActual);
    }

    public Ticket obtenerPorId(Long id, Usuario usuarioActual) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ticket no encontrado con id " + id));

        boolean esDueno = ticket.getCreadoPor().getId().equals(usuarioActual.getId());
        boolean tienePrivilegio = usuarioActual.getRol() == Rol.SOPORTE || usuarioActual.getRol() == Rol.ADMIN;

        if (!esDueno && !tienePrivilegio) {
            throw new AccessDeniedException("No puede consultar tickets de otro usuario");
        }
        return ticket;
    }

    public Page<Ticket> listarTodos(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    public List<Ticket> listarVencidos() {
        return ticketRepository.findByEstadoNotAndSlaVenceEnBefore(Estado.RESUELTO, LocalDateTime.now());
    }

    @Transactional
    public Ticket cambiarEstado(Long id, Estado nuevoEstado, Usuario usuarioActual) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ticket no encontrado con id " + id));

        Estado estadoAnterior = ticket.getEstado();
        ticket.setEstado(nuevoEstado);
        ticketRepository.save(ticket);

        TicketHistorial historial = TicketHistorial.builder()
                .ticket(ticket)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(nuevoEstado)
                .cambiadoPor(usuarioActual)
                .fecha(LocalDateTime.now())
                .build();
        ticketHistorialRepository.save(historial);

        return ticket;
    }

    public List<TicketHistorial> historial(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ticket no encontrado con id " + ticketId));
        return ticketHistorialRepository.findByTicketOrderByFechaAsc(ticket);
    }

    public EstadisticasResponse estadisticas() {
        long total = ticketRepository.count();
        Map<String, Long> porEstado = new HashMap<>();
        for (Estado e : Estado.values()) {
            porEstado.put(e.name(), ticketRepository.countByEstado(e));
        }
        long vencidos = ticketRepository.findByEstadoNotAndSlaVenceEnBefore(Estado.RESUELTO, LocalDateTime.now()).size();
        double cumplimiento = total == 0 ? 100.0 : (100.0 * (total - vencidos) / total);

        return EstadisticasResponse.builder()
                .totalTickets(total)
                .ticketsPorEstado(porEstado)
                .ticketsVencidos(vencidos)
                .porcentajeCumplimientoSla(Math.round(cumplimiento * 100.0) / 100.0)
                .build();
    }
}
