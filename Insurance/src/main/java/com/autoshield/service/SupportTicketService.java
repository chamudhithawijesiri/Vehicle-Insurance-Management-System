package com.autoshield.service;

import com.autoshield.enums.TicketCategory;
import com.autoshield.enums.TicketPriority;
import com.autoshield.enums.TicketStatus;
import com.autoshield.model.SupportTicket;
import com.autoshield.model.SupportTicketResponse;
import com.autoshield.model.User;

import java.util.List;
/**
 * Provides the SupportTicketService functionality.
 */

public interface SupportTicketService {

    // US-01/02/03: customer raises a ticket and gets back a ticket number.
    SupportTicket submitTicket(User customer, String subject, TicketCategory category,
                               String description, String referenceInfo);

    // US-04/09: customer-facing views.
/**
     * Executes the getTicketsForUser operation.
     */
    List<SupportTicket> getTicketsForUser(User customer);

/**
     * Executes the getById operation.
     */
    SupportTicket getById(Long id);

/**
     * Executes the getByTicketNumber operation.
     */
    SupportTicket getByTicketNumber(String ticketNumber);

    // US-05: officer/admin queue.
/**
     * Executes the getAllTickets operation.
     */
    List<SupportTicket> getAllTickets();

/**
     * Executes the getOpenTickets operation.
     */
    List<SupportTicket> getOpenTickets();

/**
     * Executes the getTicketsAssignedTo operation.
     */
    List<SupportTicket> getTicketsAssignedTo(User officer);

/**
     * Executes the assignTicket operation.
     */
    SupportTicket assignTicket(Long ticketId, User officer);

    // US-06: priority handling.
/**
     * Executes the updatePriority operation.
     */
    SupportTicket updatePriority(Long ticketId, TicketPriority priority);

    // US-05/06/07: officer moves the ticket through its lifecycle.
/**
     * Executes the updateStatus operation.
     */
    SupportTicket updateStatus(Long ticketId, TicketStatus status);

    // US-05/08: officer or customer posts a message on the ticket thread.
    // infoRequested is only meaningful when postedByStaff is true.
    SupportTicketResponse addResponse(Long ticketId, User author, boolean postedByStaff,
                                      String message, boolean infoRequested);

    // US-10: escalate an urgent/unresolved ticket to a Support Manager.
/**
     * Executes the escalate operation.
     */
    SupportTicket escalate(Long ticketId);
}