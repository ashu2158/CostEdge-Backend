package com.costedge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication

@RestController
public class CostEdgeApplication {


	public static void main(String[] args) {
		SpringApplication.run(CostEdgeApplication.class, args);
	}

	@GetMapping("/root")
	public String apiRoot() {
		return "Hello world MCS";
	}
}
