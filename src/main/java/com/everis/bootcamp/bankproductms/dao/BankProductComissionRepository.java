package com.everis.bootcamp.bankproductms.dao;

import com.everis.bootcamp.bankproductms.model.BankProductComission;
import java.util.Date;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface BankProductComissionRepository extends
    ReactiveMongoRepository<BankProductComission, String> {

  Flux<BankProductComission> findByComissionDateBeforeAndComissionDateAfter(Date startDate,
      Date endDate);

  Flux<BankProductComission> findAllByComissionDateBetween(Date startDate, Date endDate);
}