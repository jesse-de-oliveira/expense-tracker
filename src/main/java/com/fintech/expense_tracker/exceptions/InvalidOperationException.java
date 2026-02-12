package com.fintech.expense_tracker.exceptions;

/**
 * Thrown when operation violates business rules
 * Maps to HTTP 400
 */

public class InvalidOperationException extends RuntimeException {
	
	public InvalidOperationException(String message) {
		super(message);
	}
}