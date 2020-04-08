package com.everis.bootcamp.bankproductms.service;

import com.everis.bootcamp.bankproductms.model.BankProductType;
import reactor.core.publisher.Mono;

public interface BankProductTypeService {

  public Mono<BankProductType> findByNumId(String numId);
}
