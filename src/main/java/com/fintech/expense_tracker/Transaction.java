package com.fintech.expense_tracker;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 
 *Transaction model - represents a local transaction
 *
 *This is POJO - Plain Old Java Object, used to:
 * - Accept JSON input from client
 * - Return JSON response to client
 * - Store transaction data (next week: save to database)
*/

public class Transaction {
	
	private String transactionId;
	private String fromAccount;
	private String toAccount;
	private BigDecimal amount;
	private String currency;
	private LocalDateTime timestamp;
	private String status;
	
	// Default constructor (required for JSON deserialization)
	public Transaction() {
	}
	
	// Constructor with parameters (for creating objects)
	public Transaction(String fromAccount, String toAccount, BigDecimal amount) {
		this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = "ZAR";
        this.timestamp = LocalDateTime.now();
        this.status = "pending";
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
	
	
}
 