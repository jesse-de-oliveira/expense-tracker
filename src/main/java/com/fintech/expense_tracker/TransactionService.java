package com.fintech.expense_tracker;

import com.fintech.expense_tracker.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * TransactionService - Business logic layer for transactions
 * 
 * Responsibilities:
 * - Business validation (domain rules)
 * - Transaction orchestration
 * - ID generation
 * - Status management
 * 
 * Does NOT handle:
 * - HTTP requests/responses (Controller's job)
 * - Database queries (Repository's job)
 */

@Service
@Transactional // All methods run in database transaction
public class TransactionService {
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	
	 /**
     * Create new transaction with full business validation
     * 
     * Business rules:
     * 1. From and to accounts must be different
     * 2. Amount must be positive (already validated by @Valid)
     * 3. Amount cannot exceed daily limit (already validated by @Valid)
     * 4. Generate unique transaction ID
     * 5. Set defaults (currency, timestamp, status)
     * 
     * @param transaction Transaction data from controller
     * @return Saved transaction with generated ID
     * @throws InvalidOperationException if business rules violated
     */
	
	public Transaction createTransaction(Transaction transaction) {
        // Business rule: Cannot transfer to same account
        validateDifferentAccounts(transaction);
        
        // Generate transaction ID (business logic)
        String txId = generateTransactionId();
        transaction.setTransactionId(txId);
        
        // Set defaults
        if (transaction.getCurrency() == null) {
            transaction.setCurrency("ZAR");
        }
        if (transaction.getTimestamp() == null) {
            transaction.setTimestamp(LocalDateTime.now());
        }
        if (transaction.getStatus() == null) {
            transaction.setStatus("completed");
        }
        
        // Save to database (delegate to repository)
        return transactionRepository.save(transaction);   
	}
	
	/**
     * Get all transactions
     * 
     * @return List of all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    /**
     * Get transactions by account (sender or receiver)
     * 
     * @param accountId Account to filter by
     * @return List of transactions involving this account
     */
    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactionRepository.findByFromAccountOrToAccount(accountId, accountId);
    }

  
    /**
     * Get transactions by status
     * 
     * @param status Transaction status
     * @return List of transactions with this status
     */
    public List<Transaction> getTransactionsByStatus(String status) {
        return transactionRepository.findByStatus(status);
    }
    
    /**
     * Get transactions by account AND status
     * 
     * @param accountId Account to filter by
     * @param status Status to filter by
     * @return List of matching transactions
     */
    public List<Transaction> getTransactionsByAccountAndStatus(String accountId, String status) {
        return transactionRepository.findByFromAccountOrToAccountAndStatus(accountId, accountId, status);
    }
    
    /**
     * Get transaction by ID
     * 
     * @param id Transaction ID
     * @return Transaction
     * @throws ResourceNotFoundException if not found
     */
    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id)
        		.orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }
    
    /**
     * Update transaction status with business rule validation
     * 
     * Business rule: Cannot change status of refunded transaction
     * 
     * @param id Transaction ID
     * @param newStatus New status
     * @return Updated transaction
     * @throws ResourceNotFoundException if transaction not found
     * @throws InvalidOperationException if status change violates rules
     */
    public Transaction updateTransactionStatus(String id, String newStatus) {
        Transaction transaction = getTransactionById(id);
        
        // Business rule: Refunded transactions are final
        if (transaction.getStatus().equals("refunded") && !newStatus.equals("refunded")) {
        	throw new InvalidOperationException(
        			"Cannot change status of refunded transaction");
        }
        
        transaction.setStatus(newStatus);
        return transactionRepository.save(transaction);
    }
    
    /**
     * Delete transaction with business rule validation
     * 
     * Business rule: Cannot delete completed transactions (use refund instead)
     * 
     * @param id Transaction ID
     * @throws ResourceNotFoundException if transaction not found
     * @throws InvalidOperationException if deletion violates rules
     */
    public void deleteTransaction(String id) {
        Transaction transaction = getTransactionById(id);
        
        // Business rule: Completed transactions cannot be deleted
        if (transaction.getStatus().equals("completed")) {
        	throw new InvalidOperationException(
        			"Cannot delete completed transaction. Use refund instead.");
        }
        
        transactionRepository.delete(transaction);
    }
    
    /**
     * Get total number of transactions
     * 
     * @return Count
     */
    public long getTotalTransactionCount() {
        return transactionRepository.count();
    }

    /**
     * Get number of transactions for specific account
     * 
     * @param accountId Account ID
     * @return Count of transactions involving this account
     */
    public long getTransactionCountByAccount(String accountId) {
        return transactionRepository.countByFromAccountOrToAccount(accountId, accountId);
    }

    /**
     * Get large transactions (above threshold)
     * Use case: Fraud detection, risk analysis
     * 
     * @param threshold Minimum amount
     * @return List of transactions above threshold
     */
    public List<Transaction> getLargeTransactions(BigDecimal threshold) {
        return transactionRepository.findByAmountGreaterThan(threshold);
    }
    
    
    /**
     * Process a batch of transactions
     * Moves the loop and logic out of the Controller
     */
    public Map<String, Object> createBatch(List<Transaction> transactionList) {
        List<Transaction> validTransactions = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Transaction transaction : transactionList) {
            try {
                // 1. Business Validation (using your private helper)
                validateDifferentAccounts(transaction);

                // 2. ID and Default Generation
                transaction.setTransactionId(generateTransactionId());
                transaction.setCurrency("ZAR");
                transaction.setTimestamp(LocalDateTime.now());
                transaction.setStatus("completed");

                validTransactions.add(transaction);
            } catch (Exception e) {
                errors.add("Failed to process transaction: " + e.getMessage());
            }
        }

        // 3. The Repository call happens here
        List<Transaction> saved = transactionRepository.saveAll(validTransactions);
        
        List<String> createdIds = saved.stream()
                .map(Transaction::getTransactionId)
                .collect(Collectors.toList());

        // 4. Build the report for the Controller
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", createdIds.size());
        result.put("failureCount", errors.size());
        result.put("createdIds", createdIds);
        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }
        
        return result;
    }
    
    /**
     * Search transactions by description
     * Logic: Delegates the case-insensitive search to the repository
     */
    public List<Transaction> searchTransactions(String query) {
        return transactionRepository.findByDescriptionContainingIgnoreCase(query);
    }
    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate that from and to accounts are different
     * Business rule enforcement
    */
    private void validateDifferentAccounts(Transaction transaction) {
        if (transaction.getFromAccount().equals(transaction.getToAccount())) {
            throw new InvalidOperationException(
                "Cannot transfer to same account");
        }
    }

    /**
     * Generate unique transaction ID
     * Format: TX0001, TX0002, TX0003...
     * 
     * In production: Use UUID or database sequence
     */
    private String generateTransactionId() {
    	List<Transaction> allTransactions = transactionRepository.findAll();
    	
    	if(allTransactions.isEmpty()) { return "TX0001"; }
    	
    	// Find highest transaction number
    	int maxNumber = allTransactions.stream()
    			.map(Transaction::getTransactionId)
    			.filter(id -> id != null && id.startsWith("TX"))
    			.map(id -> id.substring(2))
    			.filter(numStr -> numStr.matches("\\d+"))
    			.mapToInt(Integer::parseInt)
    			.max()
    			.orElse(0);
    	
    	// Increment and format
    	int nextNumber = maxNumber + 1;
    	return "TX" + String.format("%04d", nextNumber);	
    }

}


