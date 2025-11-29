package com.example.LearnAssist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearnAssistApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearnAssistApplication.class, args);

	}
//	@Bean
//	CommandLineRunner commandLineRunner(ChatClient.Builder builder) {
//		return args -> {
//			var client = builder.build();
//			var response = client.prompt("Tell me an interesting fact about Islam")
//					.call()
//					.content();
//
//			System.out.println(response);
//		};
//	}
}
