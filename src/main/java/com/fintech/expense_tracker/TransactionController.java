package com.fintech.expense_tracker;

import com.fintech.expense_tracker.exceptions.*;
import jakarta.validation.Valid;
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
        response.put("description", transaction.getDescription());
        
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
    	@RequestParam(required = false) String status,
    	@RequestParam(defaultValue = "0") int page,
    	@RequestParam(defaultValue = "20") int size,
    	@RequestParam(defaultValue = "timestamp,desc") String sort) {
    	
    	// Validate pagination parameters
    	if (page < 0) page = 0;
    	if (size < 1) size = 20;
    	if (size > 100) size = 100; // Prevent abuse
    	
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
    	
    	// Sort transactions
    	String[] sortParams = sort.split(",");
    	String sortField = sortParams[0];
    	String sortDirection = sortParams.length > 1 ? sortParams[1] : "asc";
    	
    	Comparator<Transaction> comparator;
    	switch (sortField) {
    		case "amount":
    			comparator = Comparator.comparing(Transaction::getAmount);
    			break;
    		case "fromAccount":
    			comparator = Comparator.comparing(Transaction::getFromAccount);
    			break;
    		case "timestamp":
    		default:
    			comparator = Comparator.comparing(Transaction::getTimestamp);
    	}
    	
    	if ("desc".equalsIgnoreCase(sortDirection)) {
    		comparator = comparator.reversed();
    	}
    	
    	filtered.sort(comparator);
    	
    	// Pagination
    	int start = page * size;
    	int end = Math.min(start + size, filtered.size());
    		
    	List<Transaction> paginatedList;
    	if(start >= filtered.size()) {
    		paginatedList = new ArrayList<>(); // Empty page
    	} else {
    		paginatedList = filtered.subList(start, end);
    	}
    	
    	// Build response
    	Map<String, Object> response = new HashMap<>();
    	response.put("transactions", paginatedList);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalElements", filtered.size());
        response.put("totalPages", (int) Math.ceil((double) filtered.size() / size));
        response.put("isFirst", page == 0);
        response.put("isLast", end >= filtered.size());
        
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
    	
    	List<String> createdIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Transaction transaction : transactionList) {
            try {
                // Same validation as single POST
                if (transaction.getFromAccount().equals(transaction.getToAccount())) {
                    errors.add("Cannot transfer to same account: " + transaction.getFromAccount());
                    continue;
                }
                
                String txId = "TX" + String.format("%04d", transactionCounter++);
                transaction.setTransactionId(txId);
                transaction.setCurrency("ZAR");
                transaction.setTimestamp(LocalDateTime.now());
                transaction.setStatus("completed");

                transactions.add(transaction);
                createdIds.add(txId);
                
            } catch (Exception e) {
                errors.add("Failed to create transaction: " + e.getMessage());
            }
        }
         
        Map<String, Object> response = new HashMap<>();
        response.put("successCount", createdIds.size());
        response.put("failureCount", errors.size());
        response.put("createdIds", createdIds);
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

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
    	
    	String query = q.toLowerCase();
    	
    	List<Transaction> results = transactions.stream()
    	        .filter(tx -> tx.getDescription() != null 
    	                   && tx.getDescription().toLowerCase().contains(query))
    	        .collect(Collectors.toList());
    	
    	Map<String, Object> response = new HashMap<>();
        response.put("query", q);
        response.put("resultCount", results.size());
        response.put("transactions", results);

        return ResponseEntity.ok(response);
        
    }
    
}