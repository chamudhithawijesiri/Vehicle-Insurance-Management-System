package com.autoshield.repository;

import com.autoshield.model.Claim;
import com.autoshield.model.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * Provides the ClaimDocumentRepository functionality.
 */

public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {
/**
     * Executes the findByClaim operation.
     */
    List<ClaimDocument> findByClaim(Claim claim);
}