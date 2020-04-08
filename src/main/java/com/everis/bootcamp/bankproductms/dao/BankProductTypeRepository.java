package com.everis.bootcamp.bankproductms.dao;


import com.everis.bootcamp.bankproductms.model.BankProductType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface BankProductTypeRepository extends
    ReactiveMongoRepository<BankProductType, String> {

  public Mono<BankProductType> findByNumId(String numId);
}