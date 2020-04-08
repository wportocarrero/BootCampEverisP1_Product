package com.everis.bootcamp.bankproductms.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "BANK_PRODUCT_TYPE")
@EqualsAndHashCode(callSuper = false)
public class BankProductType {

  private String numId;
  private String name;
}