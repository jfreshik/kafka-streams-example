package com.chelab.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaStreamWordCountApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamWordCountApplication.class, args);
	}

}
