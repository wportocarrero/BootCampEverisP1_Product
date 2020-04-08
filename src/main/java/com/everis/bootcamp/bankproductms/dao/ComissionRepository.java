package com.everis.bootcamp.bankproductms.dao;

import com.everis.bootcamp.bankproductms.model.Comission;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ComissionRepository extends ReactiveMongoRepository<Comission, String> {

  public Mono<Comission> findFirstByOrderByDateCreatedDesc();
}