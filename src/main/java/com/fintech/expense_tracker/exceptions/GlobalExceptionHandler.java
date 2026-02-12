package com.fintech.expense_tracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for entire application
 * 
 * @RestControllerAdvice applies to ALL controllers
 * Catches exceptions and returns proper HTTP responses
 * Prevents stack traces from leaking to clients
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
     * Handle validation errors (from @Valid)
     * Returns 400 Bad Request
     */
	
	@ExceptionHandler(MethodArgumentNotValidException.class) 
		public ResponseEntity<Map<String, Object>> handleValidationErrors(
				MethodArgumentNotValidException ex) {
			List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
			
			Map<String, Object> response = new HashMap<>();
			response.put("timestamp", LocalDateTime.now());
	        response.put("status", 400);
	        response.put("error", "Validation Failed");
	        response.put("messages", errors);
	        
	        return ResponseEntity.badRequest().body(response);
	}
	/**
     * Handle resource not found
     * Returns 404 Not Found
     */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {
		
		Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", 404);
        response.put("error", "Resource Not Found");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
	
	/**
     * Handle duplicate resource
     * Returns 409 Conflict
     */
	 @ExceptionHandler(DuplicateResourceException.class)
	    public ResponseEntity<Map<String, Object>> handleDuplicate(
	            DuplicateResourceException ex) {
		 
		 Map<String, Object> response = new HashMap<>();
	            response.put("timestamp", LocalDateTime.now());
	            response.put("status", 409);
	            response.put("error", "Duplicate Resource");
	            response.put("message", ex.getMessage());
		 
	     return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	 }
	 
	 /**
	  * Handle invalid business operations
	  * Returns 400 Bad Request
	  */
	 
	 @ExceptionHandler(InvalidOperationException.class)
	 public ResponseEntity<Map<String, Object>> handleInvalidOperation(
			 InvalidOperationException ex) {
		 
		 Map<String, Object> response = new HashMap<>();
	        response.put("timestamp", LocalDateTime.now());
	        response.put("status", 400);
	        response.put("error", "Invalid Operation");
	        response.put("message", ex.getMessage());
	        
	     return ResponseEntity.badRequest().body(response);
	 }
	 
	 /**
	 * Catch-all for unexpected errors
	 * Returns 500 Internal Server Error
	 * 
	 * IMPORTANT: In production, log full stack trace but
	 * only return generic message to client (security)
	 */
	 @ExceptionHandler(Exception.class)
	 public ResponseEntity<Map<String, Object>> handleGenericError (
			 Exception ex) {
		 
		// TODO: Log full stack trace to file/monitoring system
	    System.err.println("Unexpected error: " + ex.getMessage());
	    ex.printStackTrace();
	        
	    Map<String, Object> response = new HashMap<>();
	    response.put("timestamp", LocalDateTime.now());
	    response.put("status", 500);
	    response.put("error", "Internal Server Error");
	    response.put("message", "An unexpected error occurred. Please try again later.");
	    
	    // NEVER include: ex.getMessage() or stack trace in production!
	    
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	 }
	 
	 
	        
}