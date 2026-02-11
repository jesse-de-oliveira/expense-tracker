package com.fintech.expense_tracker;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Transaction Controller - handles transaction operations
 * 
 * Demonstrates:
 * - @PostMapping (create transactions)
 * - @RequestBody (accept JSON input)
 * - Input validation
 * - Simulated transaction processing
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
     */
    @PostMapping
    public Map<String, Object> createTransaction(@RequestBody Transaction transaction) {
        
        // Generate transaction ID
        String txId = "TX" + String.format("%04d", transactionCounter++);
        transaction.setTransactionId(txId);
        
        // Set defaults
        transaction.setCurrency("ZAR");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("completed");
        
        // Store transaction (in-memory for now)
        transactions.add(transaction);
        
        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", txId);
        response.put("fromAccount", transaction.getFromAccount());
        response.put("toAccount", transaction.getToAccount());
        response.put("amount", transaction.getAmount());
        response.put("currency", transaction.getCurrency());
        response.put("timestamp", transaction.getTimestamp());
        response.put("status", transaction.getStatus());
        response.put("message", "Transaction created successfully");
        
        return response;
    }
    
    /**
     * Get all transactions (GET)
     * 
     * URL: http://localhost:8080/api/transactions
     * Method: GET
     * 
     * @return List of all transactions
     */
    @GetMapping
    public Map<String, Object> getAllTransactions() {
        Map<String, Object> response = new HashMap<>();
        response.put("count", transactions.size());
        response.put("transactions", transactions);
        return response;
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
    public Map<String, Object> getTransactionById(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        // Find transaction by ID
        for (Transaction tx : transactions) {
            if (tx.getTransactionId().equals(id)) {
                response.put("transaction", tx);
                response.put("status", "found");
                return response;
            }
        }
        
        // Not found
        response.put("transactionId", id);
        response.put("error", "Transaction not found");
        response.put("status", "not_found");
        return response;
    }
}