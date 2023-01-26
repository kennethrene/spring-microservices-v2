package com.in28minutes.microservices.currencyconversionservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController
public class CurrencyConversionController {
	
	private CurrencyExchangeProxy proxy;
	private WebClient.Builder webClientBuilder;

	public CurrencyConversionController(CurrencyExchangeProxy proxy, WebClient.Builder webClientBuilder) {
		this.proxy = proxy;
		this.webClientBuilder = webClientBuilder;
	}

	@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion calculateCurrencyConversion(
			@PathVariable String from,
			@PathVariable String to,
			@PathVariable BigDecimal quantity
			) {
		
		HashMap<String, String> uriVariables = new HashMap<>();
		uriVariables.put("from",from);
		uriVariables.put("to",to);
		
		ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity
		("http://localhost:8000/currency-exchange/from/{from}/to/{to}", 
				CurrencyConversion.class, uriVariables);
		
		CurrencyConversion currencyConversion = responseEntity.getBody();
		
		return new CurrencyConversion(currencyConversion.getId(), 
				from, to, quantity, 
				currencyConversion.getConversionMultiple(), 
				quantity.multiply(currencyConversion.getConversionMultiple()), 
				currencyConversion.getEnvironment()+ " " + "rest template");
	}

	@GetMapping("/currency-conversion-webclient/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion calculateCurrencyConversionWebClient(
			@PathVariable String from,
			@PathVariable String to,
			@PathVariable BigDecimal quantity
	) {
		WebClient webClient = WebClient.builder()
				.baseUrl("http://localhost:8000/currency-exchange")
				.build();

		CurrencyConversion currencyConversion = webClient.get()
				.uri((UriBuilder builder) -> builder.path("/from/{from}/to/{to}")
						.build(from, to))
				.exchangeToMono(response -> {
					if (response.statusCode().isError()) {
						return response.createException().flatMap(Mono::error);
					} else {
						return response.bodyToMono(CurrencyConversion.class);
					}
				})
				.blockOptional()
				.orElseThrow();

		return new CurrencyConversion(currencyConversion.getId(),
				from, to, quantity,
				currencyConversion.getConversionMultiple(),
				quantity.multiply(currencyConversion.getConversionMultiple()),
				currencyConversion.getEnvironment()+ " " + "webclient template");
	}

	@GetMapping("/currency-conversion-webclient-balanced/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion calculateCurrencyConversionWebClientLoadBalancer(
			@PathVariable String from,
			@PathVariable String to,
			@PathVariable BigDecimal quantity
	) {
		CurrencyConversion currencyConversion = webClientBuilder
				.baseUrl("http://currency-exchange").build().get()
				.uri((UriBuilder builder) -> builder.path("/currency-exchange/from/{from}/to/{to}")
						.build(from, to))
				.exchangeToMono(response -> {
					if (response.statusCode().isError()) {
						return response.createException().flatMap(Mono::error);
					} else {
						return response.bodyToMono(CurrencyConversion.class);
					}
				})
				.blockOptional()
				.orElseThrow();

		return new CurrencyConversion(currencyConversion.getId(),
				from, to, quantity,
				currencyConversion.getConversionMultiple(),
				quantity.multiply(currencyConversion.getConversionMultiple()),
				currencyConversion.getEnvironment()+ " " + "webclient template");
	}


	@GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion calculateCurrencyConversionFeign(
			@PathVariable String from,
			@PathVariable String to,
			@PathVariable BigDecimal quantity
			) {
				
		CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);
		
		return new CurrencyConversion(currencyConversion.getId(), 
				from, to, quantity, 
				currencyConversion.getConversionMultiple(), 
				quantity.multiply(currencyConversion.getConversionMultiple()), 
				currencyConversion.getEnvironment() + " " + "feign");
		
	}


}
