package com.example.learningSpringBootApp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearningSpringBootAppApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(LearningSpringBootAppApplication.class, args);
	}

	private final PaymentService paymentService;

	public LearningSpringBootAppApplication(PaymentService payment) {
		this.paymentService = payment;
	}

	@Override
	public void run(String... args) throws Exception {
		String payment = paymentService.pay();
		System.out.println("Payment done! "+payment);
	}
}
