package com.fintech.expense_tracker;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 
 *Transaction model - represents a local transaction
 *
 *
 * Transaction Entity - maps to 'transactions' table in database
 * 
 * JPA Annotations:
 * @Entity - marks this as a JPA entity (database table)
 * @Table - specifies table name
 * @Id - marks primary key field
 * @Column - customizes column properties
 *
 *
 *This is POJO - Plain Old Java Object, used to:
 * - Accept JSON input from client
 * - Return JSON response to client
 *
*/

@Entity
@Table(name="transactions")
public class Transaction {
	
	 /**
     * Primary Key - unique identifier for each transaction
     * 
     * @Id - marks as primary key
     * Strategy: Manually assigned (we generate TX0001, TX0002...)
     */
	
	@Id
	@Column(name = "transaction_id", nullable = false, length = 50)
	private String transactionId;
	
	@Column(name = "from_account", nullable = false, length = 20)
	@NotBlank(message = "Source account cannot be empty")
	@Size(min = 3, max = 20, message = "Account ID must be 3-20 characters")
	private String fromAccount;
	
	@Column(name = "to_account", nullable = false, length = 20)
	@NotBlank(message = "Destination account cannot be empty")
	@Size(min = 3, max =20 , message = "Account ID must be 3-20 characters")		
	private String toAccount;

	@Column(name = "amount", nullable = false, precision = 19, scale = 2)
	@NotNull(message = "Amount is required")
	@Positive(message = "Amount must be positive")
	@DecimalMax(value = "50000.00", message = "Amount cannot exceed daily limit of R 50,000")
	private BigDecimal amount;
	
	@Column(name = "currency", length = 3)
	private String currency;
	
	@Column(name = "status", length = 20)
	private String status;
	
	@Column(name = "timestamp", nullable = false, updatable = false)
	private LocalDateTime timestamp;
	
	@Size(max = 200, message = "Description cannot exceed 200 characters")
	private String description;
	
	/**
	 * Transaction model with validation
	 *
	 * Validation annotations ensure data integrity
	 * before any business logic executes
	 */
	
	/**
     * JPA requires a no-arg constructor (can be protected/private)
     * Hibernate uses this to create instances when loading from database
     */
	
	// Default constructor (required for JSON deserialization)
	public Transaction() {
	}
	
	// Constructor with parameters (for creating objects)
	/**
     * Constructor for creating transactions in code
     */
	public Transaction(String fromAccount, String toAccount, BigDecimal amount) {
		this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = "ZAR";
        this.timestamp = LocalDateTime.now();
        this.status = "pending";
	}
	
	/**
     * Lifecycle callback - runs BEFORE entity is persisted to database
     * Sets timestamp automatically if not set
     */
	@PrePersist
	protected void onCreate() {
		if (timestamp == null) {
			timestamp = LocalDateTime.now();
		}
		if (currency == null) {
			currency = "ZAR";
		}
	}
	
	// Getters and Setters (required for JSON conversion)
	
	public String getTransactionId() {
		return transactionId;
	}
	
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	public String getFromAccount() {
		return fromAccount;
	}
	
	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}
	
	public String getToAccount() {
        return toAccount;
    }
	
	public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }
	
	public BigDecimal getAmount() {
        return amount;
    }
	
	public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
	
	public String getCurrency() {
        return currency;
    }
	
	public void setCurrency(String currency) {
        this.currency = currency;
    }
	
	public LocalDateTime getTimestamp() {
        return timestamp;
    }
	
	public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
	
	public String getStatus() {
        return status;
    }
	
	public void setStatus(String status) {
        this.status = status;
    }
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
     * toString for debugging
     */
	
	@Override
	public String toString() {
		return "Transaction{" +
				"transactionId='" + transactionId + '\'' + 
				", fromAccount='" + fromAccount + '\'' +
                ", toAccount='" + toAccount + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
 