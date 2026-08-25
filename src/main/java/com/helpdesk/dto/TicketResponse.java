package com.helpdesk.dto;

import com.helpdesk.enums.Estado;
import com.helpdesk.enums.Prioridad;
import com.helpdesk.entities.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private Estado estado;
    private LocalDateTime creadoEn;
    private LocalDateTime slaVenceEn;
    private boolean vencido;
    private String creadoPorEmail;

    public static TicketResponse fromEntity(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId())
                .titulo(t.getTitulo())
                .descripcion(t.getDescripcion())
                .prioridad(t.getPrioridad())
                .estado(t.getEstado())
                .creadoEn(t.getCreadoEn())
                .slaVenceEn(t.getSlaVenceEn())
                .vencido(t.isVencido())
                .creadoPorEmail(t.getCreadoPor().getEmail())
                .build();
    }
}
