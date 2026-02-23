package com.fintech.expense_tracker;

import com.fintech.expense_tracker.exceptions.*;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    
	// CHANGED: Inject repository instead of in-memory list
    @Autowired
    private TransactionService transactionService;
    
    
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
        
    	Transaction created = transactionService.createTransaction(transaction);
        
        // Return 201 CREATED 
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", created.getTransactionId());
        response.put("fromAccount", created.getFromAccount());
        response.put("toAccount", created.getToAccount());
        response.put("amount", created.getAmount());
        response.put("currency", created.getCurrency());
        response.put("timestamp", created.getTimestamp());
        response.put("status", created.getStatus());
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
     * 
     * 
     *  @param account Filter by account (optional)
	 *  @param status Filter by status (optional)
	 *  @param page Page number (default 0)
	 *  @param size Page size (default 20, max 100)
	 *  @param sort Sort field and direction (default timestamp,desc)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTransactions(
    	@RequestParam(required = false) String account,
    	@RequestParam(required = false) String status) {
    	
    	List<Transaction> transactions;
    	
    	// CHANGED: Use repository methods instead of stream filtering
    	if (account != null && status != null) {
    		// Filter by both account and status	
    		transactions = transactionService.getTransactionsByAccountAndStatus(account, status);
    	} else if (account != null) {
    		// Filter by account only
    		transactions = transactionService.getTransactionsByAccount(account);
    	} else if (status != null) {
    		// Filter by status only
    		transactions = transactionService.getTransactionsByStatus(status);
    	} else {
    		// No filter - get all
    		transactions = transactionService.getAllTransactions();
    	}
    	
    	// Build response
    	Map<String, Object> response = new HashMap<>();
    	response.put("count", transactions.size());
    	response.put("transactions", transactions);
        
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

    	Transaction transaction = transactionService.getTransactionById(id);
    	return ResponseEntity.ok(transaction);
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
    	
        Transaction updated = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(updated);
        
    }
    
    /**
     * DELETE - Cancel/delete transaction (NEW)
     * 
     * Use case: Admin cancels fraudulent transaction
     * Returns 204 No Content (success but no body)
     */
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {

    	transactionService.deleteTransaction(id);
    	return ResponseEntity.noContent().build();
    }

    /**
     * GET - Transaction statistics
     * 
     * URL: GET /api/transactions/stats
     * 
     * Returns:
     * - Total count
     * - Total amount transferred
     * - Average transaction amount
     * - Breakdown by status
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
    	
    	// CHANGED: Get all from database
        List<Transaction> transactions = transactionService.getAllTransactions();
    	
    	if (transactions.isEmpty()) {
    		Map<String, Object> response = new HashMap<>();
    		response.put("totalTransactions", 0);
            response.put("totalAmount", BigDecimal.ZERO);
            response.put("averageAmount", BigDecimal.ZERO);
            response.put("statusBreakdown", new HashMap<>());
            return ResponseEntity.ok(response);
    	}
    	
    	// Total count
    	int totalCount = transactions.size();
    	
    	// Total amount
        BigDecimal totalAmount = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Average amount
        BigDecimal averageAmount = totalAmount.divide(
            new BigDecimal(totalCount), 
            2, 
            RoundingMode.HALF_UP
        );
        
        // Status breakdown
        Map<String, Long> statusBreakdown = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getStatus,
                Collectors.counting()
            ));
        
        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("totalTransactions", totalCount);
        response.put("totalAmount", totalAmount);
        response.put("averageAmount", averageAmount);
        response.put("currency", "ZAR");
        response.put("statusBreakdown", statusBreakdown);

        return ResponseEntity.ok(response);

   
    }
    
    /**
     * POST - Create multiple transactions (batch)
     *  
     * URL: POST /api/transactions/batch
     * Body: Array of transactions
     * 
     * Use case: Import transactions from CSV
     */ 
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createBatch(
            @RequestBody List<@Valid Transaction> transactionList) {
        
        // The Controller just calls the Service manager
        Map<String, Object> response = transactionService.createBatch(transactionList);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

                
    /**
     * GET - Search transactions by description
     * 
     * URL: GET /api/transactions/search?q=invoice
     * 
     * Searches in description field (case-insensitive)
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTransactions(
            @RequestParam String q) {
    
    	if (q == null || q.trim().isEmpty()) {
            throw new InvalidOperationException("Search query cannot be empty");
        }
    	
    	List<Transaction> results = transactionService.searchTransactions(q);
    	
    	Map<String, Object> response = new HashMap<>();
        response.put("query", q);
        response.put("resultCount", results.size());
        response.put("transactions", results);

        return ResponseEntity.ok(response);
        
    }
    
    /**
     * GET - Find large transactions (fraud detection)
     */
    @GetMapping("/large")
    public ResponseEntity<List<Transaction>> getLargeTransactions(
            @RequestParam(defaultValue = "1000") BigDecimal threshold) {
        
        List<Transaction> largeTransactions = transactionService.getLargeTransactions(threshold);
        return ResponseEntity.ok(largeTransactions);	
    }
    
}