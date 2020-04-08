package com.everis.bootcamp.bankproductms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "BANK_PRODUCT_COMISSION")
@EqualsAndHashCode(callSuper = false)
public class BankProductComission {

  private String numAccount;
  private double transactionMoney;
  private double comission;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date comissionDate;
}