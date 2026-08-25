package com.helpdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasResponse {
    private long totalTickets;
    private Map<String, Long> ticketsPorEstado;
    private long ticketsVencidos;
    private double porcentajeCumplimientoSla;
}
