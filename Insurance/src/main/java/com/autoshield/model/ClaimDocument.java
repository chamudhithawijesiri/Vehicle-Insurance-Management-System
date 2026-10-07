package com.autoshield.model;

import jakarta.persistence.*;
/**
 * Provides the ClaimDocument functionality.
 */

@Entity
@Table(name = "claim_documents")
public class ClaimDocument {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/** The claim value. */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;
/** The fileName value. */

    @Column(name = "file_name", nullable = false)
    private String fileName;

    // Matches the pattern already used for Vehicle.vehicleImages: the actual
    // file content/reference is kept as a string (e.g. base64 data URL) rather
    // than wiring up real file storage, which is out of scope for the MVP.
    @Column(name = "file_data", length = 5000000)
    private String fileData;
/** The documentType value. */

    @Column(name = "document_type")
    private String documentType; // e.g. "Police Report", "Vehicle Images", "Garage Quotation", "NIC Copy"
/**
 * Executes the ClaimDocument operation.
 */

    public ClaimDocument() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}