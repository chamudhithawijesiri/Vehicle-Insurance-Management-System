package com.autoshield.enums;

// Epic 5, Section 6: New -> Assigned -> In Progress -> Waiting for Customer (if
// required) -> Resolved -> Closed.
/**
 * Defines the TicketStatus values used by the application.
 */
public enum TicketStatus {
    NEW,
    ASSIGNED,
    IN_PROGRESS,
    WAITING_FOR_CUSTOMER,
    RESOLVED,
    CLOSED
}