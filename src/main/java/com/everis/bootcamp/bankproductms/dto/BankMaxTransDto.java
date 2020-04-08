package com.everis.bootcamp.bankproductms.dto;

import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankMaxTransDto {

  private HashMap<String, Integer> productMaxTrans;
}