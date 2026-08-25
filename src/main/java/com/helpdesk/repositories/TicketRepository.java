package com.helpdesk.repositories;

import com.helpdesk.enums.Estado;
import com.helpdesk.entities.Ticket;
import com.helpdesk.entities.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreadoPor(Usuario usuario);

    Page<Ticket> findAll(Pageable pageable);

    List<Ticket> findByEstadoNotAndSlaVenceEnBefore(Estado estado, LocalDateTime fecha);

    long countByEstado(Estado estado);
}
