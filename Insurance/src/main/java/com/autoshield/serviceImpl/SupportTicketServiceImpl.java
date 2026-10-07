package com.autoshield.serviceImpl;

import com.autoshield.enums.NotificationType;
import com.autoshield.enums.TicketCategory;
import com.autoshield.enums.TicketPriority;
import com.autoshield.enums.TicketStatus;
import com.autoshield.model.SupportTicket;
import com.autoshield.model.SupportTicketResponse;
import com.autoshield.model.User;
import com.autoshield.repository.SupportTicketRepository;
import com.autoshield.repository.SupportTicketResponseRepository;
import com.autoshield.service.NotificationService;
import com.autoshield.service.SupportTicketService;
import com.autoshield.util.TicketNumberGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Provides the SupportTicketServiceImpl functionality.
 */

@Service
public class SupportTicketServiceImpl implements SupportTicketService {
/** The ticketRepository value. */

    private final SupportTicketRepository ticketRepository;
/** The responseRepository value. */
    private final SupportTicketResponseRepository responseRepository;
/** The ticketNumberGenerator value. */
    private final TicketNumberGenerator ticketNumberGenerator;
/** The notificationService value. */
    private final NotificationService notificationService;
/**
 * Executes the SupportTicketServiceImpl operation.
 */

    public SupportTicketServiceImpl(SupportTicketRepository ticketRepository,
                                    SupportTicketResponseRepository responseRepository,
                                    TicketNumberGenerator ticketNumberGenerator,
                                    NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.responseRepository = responseRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.notificationService = notificationService;
    }
/**
 * Processes the submitTicket operation.
 */

    @Override
    public SupportTicket submitTicket(User customer, String subject, TicketCategory category,
                                      String description, String referenceInfo) {
        SupportTicket ticket = new SupportTicket();
        ticket.setTicketNumber(ticketNumberGenerator.generate());
        ticket.setCustomer(customer);
        ticket.setSubject(subject);
        ticket.setCategory(category);
        ticket.setDescription(description);
        ticket.setReferenceInfo(referenceInfo);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setPriority(TicketPriority.MEDIUM);
        SupportTicket saved = ticketRepository.save(ticket);

        // US-07: acknowledge receipt straight away.
        notificationService.notify(
                customer,
                NotificationType.SUPPORT_TICKET,
                "Support ticket " + saved.getTicketNumber() + " received",
                "We have received your request and will respond shortly."
        );
        return saved;
    }

    @Override
    public List<SupportTicket> getTicketsForUser(User customer) {
        return ticketRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    @Override
    public SupportTicket getById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    @Override
    public SupportTicket getByTicketNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber).orElse(null);
    }

    @Override
    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<SupportTicket> getOpenTickets() {
        return ticketRepository.findByStatusNotOrderByPriorityDescCreatedAtAsc(TicketStatus.CLOSED);
    }

    @Override
    public List<SupportTicket> getTicketsAssignedTo(User officer) {
        return ticketRepository.findByAssignedToAndStatusNotOrderByCreatedAtAsc(officer, TicketStatus.CLOSED);
    }
/**
 * Executes the assignTicket operation.
 */

    @Override
    public SupportTicket assignTicket(Long ticketId, User officer) {
        SupportTicket ticket = requireTicket(ticketId);
        ticket.setAssignedTo(officer);
        if (ticket.getStatus() == TicketStatus.NEW) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }
/**
 * Processes the updatePriority operation.
 */

    @Override
    public SupportTicket updatePriority(Long ticketId, TicketPriority priority) {
        SupportTicket ticket = requireTicket(ticketId);
        ticket.setPriority(priority);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }
/**
 * Processes the updateStatus operation.
 */

    @Override
    public SupportTicket updateStatus(Long ticketId, TicketStatus status) {
        SupportTicket ticket = requireTicket(ticketId);
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        if (status == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        SupportTicket saved = ticketRepository.save(ticket);

        // US-07: notify the customer whenever the ticket status changes.
        String statusLabel = switch (status) {
            case ASSIGNED -> "has been assigned to a customer service officer";
            case IN_PROGRESS -> "is now being worked on";
            case WAITING_FOR_CUSTOMER -> "needs more information from you — please check the ticket";
            case RESOLVED -> "has been resolved";
            case CLOSED -> "has been closed";
            default -> "was updated";
        };
        notificationService.notify(
                saved.getCustomer(),
                NotificationType.SUPPORT_TICKET,
                "Ticket " + saved.getTicketNumber() + " " + statusLabel,
                null
        );
        return saved;
    }
/**
 * Processes the addResponse operation.
 */

    @Override
    public SupportTicketResponse addResponse(Long ticketId, User author, boolean postedByStaff,
                                             String message, boolean infoRequested) {
        SupportTicket ticket = requireTicket(ticketId);

        SupportTicketResponse response = new SupportTicketResponse();
        response.setTicket(ticket);
        response.setAuthor(author);
        response.setFromStaff(postedByStaff);
        response.setMessage(message);
        response.setInfoRequested(infoRequested);
        SupportTicketResponse savedResponse = responseRepository.save(response);

        ticket.setUpdatedAt(LocalDateTime.now());

        if (postedByStaff) {
            // US-08: officer asking for more details moves the ticket to
            // "Waiting for Customer" until they reply.
            ticket.setStatus(infoRequested ? TicketStatus.WAITING_FOR_CUSTOMER : TicketStatus.IN_PROGRESS);
            ticketRepository.save(ticket);
            notificationService.notify(
                    ticket.getCustomer(),
                    NotificationType.SUPPORT_TICKET,
                    "New reply on ticket " + ticket.getTicketNumber(),
                    infoRequested ? "The officer has requested more information from you."
                            : "A customer service officer has responded to your ticket."
            );
        } else {
            // Customer replying (e.g. supplying the requested info) puts the
            // ticket back in the officer's active queue.
            if (ticket.getStatus() == TicketStatus.WAITING_FOR_CUSTOMER) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
                ticketRepository.save(ticket);
            }
        }

        return savedResponse;
    }
/**
 * Executes the escalate operation.
 */

    @Override
    public SupportTicket escalate(Long ticketId) {
        SupportTicket ticket = requireTicket(ticketId);
        // US-10: escalation raises priority and clears the current assignment
        // so a Support Manager (ROLE_ADMIN) can pick it up.
        ticket.setPriority(TicketPriority.CRITICAL);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    private SupportTicket requireTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Support ticket not found: " + ticketId));
    }
}