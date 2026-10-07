package com.autoshield.repository;

import com.autoshield.enums.TicketStatus;
import com.autoshield.model.SupportTicket;
import com.autoshield.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
/**
 * Provides the SupportTicketRepository functionality.
 */

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

/**
     * Executes the findByTicketNumber operation.
     */
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    // US-04/US-09: customer views their own tickets, newest first.
/**
     * Executes the findByCustomerOrderByCreatedAtDesc operation.
     */
    List<SupportTicket> findByCustomerOrderByCreatedAtDesc(User customer);

    // US-05: officer's queue of everything not yet closed.
/**
     * Executes the findByStatusNotOrderByPriorityDescCreatedAtAsc operation.
     */
    List<SupportTicket> findByStatusNotOrderByPriorityDescCreatedAtAsc(TicketStatus status);

/**
     * Executes the findByAssignedToAndStatusNotOrderByCreatedAtAsc operation.
     */
    List<SupportTicket> findByAssignedToAndStatusNotOrderByCreatedAtAsc(User assignedTo, TicketStatus status);

/**
     * Executes the findAllByOrderByCreatedAtDesc operation.
     */
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}