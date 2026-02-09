package com.fintech.expense_tracker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** 
 * First Spring Boot REST Controller
 * 
 *  Demonstrates:
 *   - @Rest Controller annotation
 *   - Simple GET endpoint
 *   - JSON response
 * */

@RestController
public class HelloController {
	/**
     * Simple hello endpoint
     * 
     * URL: http://localhost:8080/hello
     * Method: GET
     * Response: Plain text "Hello World from Spring Boot!"
     */
	
	@GetMapping("/hello")
	public String hello() {
		return "Hello World from Spring Boot!";
	}
	
	/**
	 * Get account balance by ID
	 * 
	 * URL: http://localhost:8080/balance/001
	 * Method: GET
	 * Response: JSON with account info
	 * 
	 * @param accountId Account number from URL
	 * @return Map converted to JSON automatically
	 */
	
	@GetMapping("/balance/{accountId}")
	public Map<String, Object> getBalance(@PathVariable String accountId) {
		//Simulated data(next week: from database)
		Map<String, String> accounts = new HashMap<>();
		accounts.put("001", "5000.00");
		accounts.put("002", "3000.00");
		accounts.put("003", "7500.00");
		
		//Create response
		Map<String, Object> response = new HashMap<>();
		
		if (accounts.containsKey(accountId)) {
			response.put("accountId", accountId);
			response.put("balance", accounts.get(accountId));
			response.put("currency", "ZAR");
			response.put("status", "success");
		} else {
			response.put("accountId", accountId);
			response.put("error", "Account not found!");
			response.put("status", "failed");
		}
		
		return response;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
