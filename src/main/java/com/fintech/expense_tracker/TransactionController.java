package com.fintech.expense_tracker;

import com.fintech.expense_tracker.exceptions.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Transaction Controller - handles transaction operations
 * 
 * Demonstrates:
 * - @PostMapping (create transactions)
 * - @RequestBody (accept JSON input)
 * - Input validation
 * - Simulated transaction processing
 */

/**
 * Transaction Controller with validation + query parameters
 *
 * New concepts:
 * - @Valid triggers bean validation
 * - ResponseEntity controls HTTP status codes
 * - @RequestParam for query parameters
 * - @ExceptionHandler for validation errors
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    
    // Simulated in-memory storage (next week: real database)
    private List<Transaction> transactions = new ArrayList<>();
    private int transactionCounter = 1;
    
    /**
     * Create new transaction (POST)
     * 
     * URL: http://localhost:8080/api/transactions
     * Method: POST
     * Body: JSON transaction data
     * 
     * Example request body:
     * {
     *   "fromAccount": "001",
     *   "toAccount": "002",
     *   "amount": 500.00
     * }
     * 
     * @param transaction Transaction object from JSON
     * @return Created transaction with ID and status
     * 
     * @Valid triggers all annotations on Transaction class
     * ResponseEntity lets us control HTTP status code
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTransaction(@Valid @RequestBody Transaction transaction) {
    	
    	// Check for duplicate (simulated - in reality check by idempotency key)
    	String checkId = transaction.getFromAccount() + transaction.getToAccount() 
        				+ transaction.getAmount();
    	for (Transaction tx : transactions) {
    		String existingId = tx.getFromAccount() + tx.getToAccount() + tx.getAmount();
    		if (existingId.equals(checkId)) {
    			throw new DuplicateResourceException(
    					"Transaction already exists with same from/to/amount");
			}
    	}
        
    	// Business logic validation (Level 2)
    	if(transaction.getFromAccount().equals(transaction.getToAccount())) {
    		throw new InvalidOperationException(
    			   "Cannot transfer to same account");
    	}
    	
    	
        // Generate transaction ID & Set defaults
        String txId = "TX" + String.format("%04d", transactionCounter++);
        transaction.setTransactionId(txId);
  
        transaction.setCurrency("ZAR");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("completed");
        
        transactions.add(transaction);
        
        // Return 201 CREATED (not 200 OK - created new resource)
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", txId);
        response.put("fromAccount", transaction.getFromAccount());
        response.put("toAccount", transaction.getToAccount());
        response.put("amount", transaction.getAmount());
        response.put("currency", transaction.getCurrency());
        response.put("timestamp", transaction.getTimestamp());
        response.put("status", transaction.getStatus());
        response.put("message", "Transaction created successfully");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get transactions with optional filtering
     *
     * URL: GET /api/transactions
     * URL: GET /api/transactions?account=001
     * URL: GET /api/transactions?status=completed
     * URL: GET /api/transactions?account=001&status=completed
     *
     * @RequestParam extracts query parameters from URL
     * required=false means parameter is optional
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTransactions(
    	@RequestParam(required = false) String account,
    	@RequestParam(required = false) String status) {
    
    	// Start with all transactions
    	List<Transaction> filtered = new ArrayList<>(transactions);
    	
    	// This is the "magic" that lets you search by account in the URL
    	// Filter by account if provided
    	if(account != null) {
    		filtered = filtered.stream()
    				.filter(tx -> tx.getFromAccount().equals(account) || tx.getToAccount().equals(account))
    					.collect(Collectors.toList());
    	}
    	
    	// Filter by status if provided
    	if(status != null) {
    		filtered = filtered.stream()
    			.filter(tx -> tx.getStatus().equals(status))
    				.collect(Collectors.toList());
    	
    	}
    	
    	Map<String, Object> response = new HashMap<>();
        response.put("count", filtered.size());
        response.put("transactions", filtered);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get transaction by ID (GET)
     * 
     * URL: http://localhost:8080/api/transactions/TX0001
     * Method: GET
     * 
     * @param id Transaction ID
     * @return Transaction details or error
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String id) {

        return transactions.stream()
        		.filter(tx -> tx.getTransactionId().equals(id))
        		.findFirst().map(ResponseEntity::ok)
        		.orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }
    
    /**
     * PUT - Update transaction status (NEW)
     * 
     * Use case: Mark transaction as "refunded" or "disputed"
     * PUT is idempotent - same request multiple times = same result
     */
    
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransactionStatus(
            @PathVariable String id,
            @RequestParam String status) {
    	
    	 Transaction transaction = transactions.stream()
    	            .filter(tx -> tx.getTransactionId().equals(id))
    	            .findFirst()
    	            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    	 
    	 // Validate status transition
         String currentStatus = transaction.getStatus();
         if (currentStatus.equals("refunded") && !status.equals("refunded")) {
             throw new InvalidOperationException(
                 "Cannot change status of refunded transaction");
         }
         
         transaction.setStatus(status);
         
         return ResponseEntity.ok(transaction);
    }
    
    /**
     * DELETE - Cancel/delete transaction (NEW)
     * 
     * Use case: Admin cancels fraudulent transaction
     * Returns 204 No Content (success but no body)
     */
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {

        Transaction transaction = transactions.stream()
            .filter(tx -> tx.getTransactionId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        // Business rule: Cannot delete completed transactions
        if (transaction.getStatus().equals("completed")) {
            throw new InvalidOperationException(
                "Cannot delete completed transaction. Use refund instead.");
        }

        transactions.remove(transaction);

        return ResponseEntity.noContent().build();
    }

    
    
    
    
    
    
    
    
    
}