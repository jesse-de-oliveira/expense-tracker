package com.fintech.expense_tracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * TransactionRepository - Data access layer for Transaction entity
 * 
 * Extends JpaRepository<Transaction, String>:
 * - Transaction: Entity type
 * - String: Primary key type (transactionId is String)
 * 
 * Spring Data JPA automatically implements this interface at runtime.
 * No need to write implementation class!
 * 
 * Provides built-in methods:
 * - save(), findById(), findAll(), deleteById(), count(), etc.
 * 
 * Custom methods are generated from method names automatically.
 */

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
	 /**
     * Find all transactions involving a specific account
     * (either as sender or receiver)
     * 
     * Generated SQL:
     * SELECT * FROM transactions 
     * WHERE from_account = ? OR to_account = ?
     * 
     * @param fromAccount Account to search in from_account field
     * @param toAccount Account to search in to_account field
     * @return List of matching transactions
     */
	List<Transaction> findByFromAccountOrToAccount(String fromAccount, String toAccount);
	
	/**
     * Find transactions by status
     * 
     * Generated SQL:
     * SELECT * FROM transactions WHERE status = ?
     * 
     * @param status Transaction status (completed, pending, refunded, failed)
     * @return List of transactions with given status
     */
    List<Transaction> findByStatus(String status);
    
    /**
     * Find transactions from a specific account
     * 
     * Generated SQL:
     * SELECT * FROM transactions WHERE from_account = ?
     * 
     * @param fromAccount Source account
     * @return List of transactions sent from this account
     */
    List<Transaction> findByFromAccount(String fromAccount);
    
    /**
     * Find transactions to a specific account
     * 
     * Generated SQL:
     * SELECT * FROM transactions WHERE to_account = ?
     * 
     * @param toAccount Destination account
     * @return List of transactions sent to this account
     */
    List<Transaction> findByToAccount(String toAccount);
    
    /**
     * Find transactions with amount greater than specified value
     * 
     * Generated SQL:
     * SELECT * FROM transactions WHERE amount > ?
     * 
     * Use case: Find large transactions for fraud detection
     * 
     * @param amount Minimum amount
     * @return List of transactions above threshold
     */
    List<Transaction> findByAmountGreaterThan(BigDecimal amount);
    
    /**
     * Find transactions by account AND status
     * (from_account or to_account matches, and status matches)
     * 
     * Generated SQL:
     * SELECT * FROM transactions 
     * WHERE (from_account = ? OR to_account = ?) AND status = ?
     * 
     * @param fromAccount Account to check in from_account
     * @param toAccount Account to check in to_account
     * @param status Transaction status
     * @return List of matching transactions
     */
    List<Transaction> findByFromAccountOrToAccountAndStatus(
        String fromAccount, 
        String toAccount, 
        String status
    );
    
    /**
     * Count transactions for a specific account
     * 
     * Generated SQL:
     * SELECT COUNT(*) FROM transactions 
     * WHERE from_account = ? OR to_account = ?
     * 
     * @param fromAccount Account to check
     * @param toAccount Account to check
     * @return Number of transactions
     */
    long countByFromAccountOrToAccount(String fromAccount, String toAccount);
    
    // Add this method to your interface
    List<Transaction> findByDescriptionContainingIgnoreCase(String description);
}
