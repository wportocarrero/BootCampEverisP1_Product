package com.everis.bootcamp.bankproductms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication
public class BankproductmsApplication {

  public static void main(String[] args) {
    SpringApplication.run(BankproductmsApplication.class, args);
  }

}
