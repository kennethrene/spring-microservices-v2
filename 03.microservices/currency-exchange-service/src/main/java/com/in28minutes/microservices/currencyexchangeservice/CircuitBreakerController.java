package com.in28minutes.microservices.currencyexchangeservice;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CircuitBreakerController {
	
	private Logger logger = 
				LoggerFactory.getLogger(CircuitBreakerController.class);
	
	@GetMapping("/sample-api")
	//@Retry(name = "sample-api", fallbackMethod = "hardcodedResponse")
	//@Retry(name = "default")
	//@Retry(name = "sample-api")
	@CircuitBreaker(name = "default", fallbackMethod = "hardcodedResponse")
	//@CircuitBreaker(name = "default")
	public String sampleApi() {
		logger.info("Sample api call received");
		ResponseEntity<String> forEntity = new RestTemplate().getForEntity("http://localhost:8080/some-dummy-url",
					String.class);
		return forEntity.getBody();
//		return "sample-api";
	}

	@GetMapping("/sample-api-rate-limiter")
	@RateLimiter(name="default")
	public String sampleApiRateLimiter() {
		logger.info("Sample api call received with rate limiter");
		return "sample-api-rate-limiter";
	}

	@GetMapping("/sample-api-bulk-head")
	@Bulkhead(name="sample-api")
	public String sampleApiBulkHead() {
		logger.info("Sample api call received with bulk head");
		return "sample-api-bulk-head";
	}
	
	public String hardcodedResponse(Exception ex) {
		return "fallback-response";
	}
}
