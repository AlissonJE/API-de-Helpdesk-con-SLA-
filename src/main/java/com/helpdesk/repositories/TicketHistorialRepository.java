package com.helpdesk.repositories;

import com.helpdesk.entities.Ticket;
import com.helpdesk.entities.TicketHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistorialRepository extends JpaRepository<TicketHistorial, Long> {
    List<TicketHistorial> findByTicketOrderByFechaAsc(Ticket ticket);
}
