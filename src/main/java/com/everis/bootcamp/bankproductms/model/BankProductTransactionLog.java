package com.everis.bootcamp.bankproductms.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "BANK_PRODUCT_TRANS_LOG")
@EqualsAndHashCode(callSuper = false)
public class BankProductTransactionLog {

  @Id
  private String id;
  private String clientNumDoc;
  private String numAccount;
  private double total;
  private double transaction;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date date;

  /**
   * BankProductTransactionLog.
   */
  public BankProductTransactionLog(String clientNumDoc, String numAccount, double total,
      double transaction, Date date) {
    this.clientNumDoc = clientNumDoc;
    this.numAccount = numAccount;
    this.total = total;
    this.transaction = transaction;
    this.date = date;
  }

  ;
}