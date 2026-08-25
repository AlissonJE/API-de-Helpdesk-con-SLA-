package com.helpdesk.entities;

import com.helpdesk.enums.Estado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Reto opcional: historial de cambios de estado por ticket.
 */
@Entity
@Table(name = "ticket_historial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    private Estado estadoAnterior;

    @Enumerated(EnumType.STRING)
    private Estado estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cambiado_por_id", nullable = false)
    private Usuario cambiadoPor;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
