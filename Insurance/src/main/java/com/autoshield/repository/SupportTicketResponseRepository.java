package com.autoshield.repository;

import com.autoshield.model.SupportTicket;
import com.autoshield.model.SupportTicketResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * Provides the SupportTicketResponseRepository functionality.
 */

public interface SupportTicketResponseRepository extends JpaRepository<SupportTicketResponse, Long> {
/**
     * Executes the findByTicketOrderByCreatedAtAsc operation.
     */
    List<SupportTicketResponse> findByTicketOrderByCreatedAtAsc(SupportTicket ticket);
}