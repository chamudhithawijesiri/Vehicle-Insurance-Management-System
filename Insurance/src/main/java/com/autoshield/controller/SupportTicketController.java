package com.autoshield.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.autoshield.enums.TicketCategory;
import com.autoshield.enums.TicketPriority;
import com.autoshield.enums.TicketStatus;
import com.autoshield.model.SupportTicket;
import com.autoshield.model.User;
import com.autoshield.service.SupportTicketService;
import com.autoshield.service.UserService;
/**
 * Provides the SupportTicketController functionality.
 */

@Controller
public class SupportTicketController {
/** The supportTicketService value. */

    private final SupportTicketService supportTicketService;
/** The userService value. */
    private final UserService userService;
/**
 * Executes the SupportTicketController operation.
 */

    public SupportTicketController(SupportTicketService supportTicketService, UserService userService) {
        this.supportTicketService = supportTicketService;
        this.userService = userService;
    }

    // ================= Customer side =================

    // US-01/02/03: submit a new request.
/**
 * Executes the newTicketForm operation.
 */
    @GetMapping("/support/new")
    public String newTicketForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", userService.findByEmail(userDetails.getUsername()));
        model.addAttribute("categories", TicketCategory.values());
        return "customer/submit-ticket";
    }
/**
 * Processes the submitTicket operation.
 */

    @PostMapping("/support/new")
    public String submitTicket(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("subject") String subject,
                               @RequestParam("category") TicketCategory category,
                               @RequestParam("description") String description,
                               @RequestParam(value = "referenceInfo", required = false) String referenceInfo) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User customer = userService.findByEmail(userDetails.getUsername());
        SupportTicket ticket = supportTicketService.submitTicket(customer, subject, category, description, referenceInfo);
        return "redirect:/support/tickets/" + ticket.getId();
    }

    // US-04/09: customer's own ticket list / history.
/**
 * Executes the myTickets operation.
 */
    @GetMapping("/support/tickets")
    public String myTickets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User customer = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", customer);
        model.addAttribute("tickets", supportTicketService.getTicketsForUser(customer));
        return "customer/support-tickets";
    }

    // US-04/08/09: ticket detail + conversation thread, shared by customer and staff.
/**
 * Executes the ticketDetails operation.
 */
    @GetMapping("/support/tickets/{id}")
    public String ticketDetails(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable("id") Long id, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        SupportTicket ticket = supportTicketService.getById(id);

        boolean isStaff = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // A customer may only view their own ticket; staff can view any.
        if (ticket == null || (!isStaff && !ticket.getCustomer().getId().equals(user.getId()))) {
            return "redirect:/support/tickets";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("ticket", ticket);
        model.addAttribute("isStaff", isStaff);
        return isStaff ? "admin/ticket-details" : "customer/ticket-details";
    }

    // US-08: customer supplies the additional information the officer asked for
    // (also usable by the customer to just add a follow-up message).
/**
 * Executes the customerReply operation.
 */
    @PostMapping("/support/tickets/{id}/reply")
    public String customerReply(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable("id") Long id,
                                @RequestParam("message") String message) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User customer = userService.findByEmail(userDetails.getUsername());
        SupportTicket ticket = supportTicketService.getById(id);
        if (ticket != null && ticket.getCustomer().getId().equals(customer.getId())) {
            supportTicketService.addResponse(id, customer, false, message, false);
        }
        return "redirect:/support/tickets/" + id;
    }

    // ================= Staff (Customer Service Officer / Admin) side =================
    // Per Epic 5's own note ("customer service office = ADMIN"), officer duties
    // are carried out by ROLE_ADMIN accounts in this system.

    // US-05: queue of open requests.
/**
 * Executes the officerQueue operation.
 */
    @GetMapping("/admin/support")
    public String officerQueue(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("currentUser", userService.findByEmail(userDetails.getUsername()));
        }
        model.addAttribute("tickets", supportTicketService.getOpenTickets());
        model.addAttribute("allTickets", supportTicketService.getAllTickets());
        return "admin/support-tickets";
    }

    // US-05: assign a ticket to the officer handling it.
/**
 * Executes the assignTicket operation.
 */
    @PostMapping("/admin/support/{id}/assign")
    public String assignTicket(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User officer = userService.findByEmail(userDetails.getUsername());
        supportTicketService.assignTicket(id, officer);
        return "redirect:/support/tickets/" + id;
    }

    // US-05/06/07: move the ticket through its status lifecycle.
/**
 * Processes the updateStatus operation.
 */
    @PostMapping("/admin/support/{id}/status")
    public String updateStatus(@PathVariable("id") Long id, @RequestParam("status") TicketStatus status) {
        supportTicketService.updateStatus(id, status);
        return "redirect:/support/tickets/" + id;
    }

    // US-06: set/change the priority level.
/**
 * Processes the updatePriority operation.
 */
    @PostMapping("/admin/support/{id}/priority")
    public String updatePriority(@PathVariable("id") Long id, @RequestParam("priority") TicketPriority priority) {
        supportTicketService.updatePriority(id, priority);
        return "redirect:/support/tickets/" + id;
    }

    // US-05/08: officer responds, optionally flagging that more info is needed.
/**
 * Executes the officerRespond operation.
 */
    @PostMapping("/admin/support/{id}/respond")
    public String officerRespond(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable("id") Long id,
                                 @RequestParam("message") String message,
                                 @RequestParam(value = "requestInfo", required = false) Boolean requestInfo) {
        User officer = userService.findByEmail(userDetails.getUsername());
        supportTicketService.addResponse(id, officer, true, message, Boolean.TRUE.equals(requestInfo));
        return "redirect:/support/tickets/" + id;
    }

    // US-10: escalate an urgent/unresolved ticket.
/**
 * Executes the escalateTicket operation.
 */
    @PostMapping("/admin/support/{id}/escalate")
    public String escalateTicket(@PathVariable("id") Long id) {
        supportTicketService.escalate(id);
        return "redirect:/support/tickets/" + id;
    }
}