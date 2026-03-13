package com.son.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SonApplication {

	public static void main(String[] args) {

		SpringApplication.run(SonApplication.class, args);
		System.out.println("Application started successfully.");
		System.out.println("Access the application at: http://localhost:8080/");
	}

}
