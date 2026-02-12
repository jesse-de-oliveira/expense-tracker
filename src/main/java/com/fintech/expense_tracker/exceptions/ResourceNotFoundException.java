package com.fintech.expense_tracker.exceptions;

/**
 * Thrown when requested resource doesn't exist
 * Maps to HTTP 404
 */

public class ResourceNotFoundException extends RuntimeException {
	public ResourceNotFoundException(String message) {
		super(message);
	}
	
	public ResourceNotFoundException(String resource, String id) {
		super(resource + "Not found with id: " + id);
	}
}