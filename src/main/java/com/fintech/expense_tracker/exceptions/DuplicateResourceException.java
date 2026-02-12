package com.fintech.expense_tracker.exceptions;

/**
 * Thrown when trying to create resource that already exists
 * Maps to HTTP 409
 */

public class DuplicateResourceException extends RuntimeException {
	
	public DuplicateResourceException(String message) {
		super(message);
	}
}