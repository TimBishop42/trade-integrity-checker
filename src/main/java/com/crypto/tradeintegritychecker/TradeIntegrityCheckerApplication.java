package com.crypto.tradeintegritychecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TradeIntegrityCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradeIntegrityCheckerApplication.class, args);
	}

}
