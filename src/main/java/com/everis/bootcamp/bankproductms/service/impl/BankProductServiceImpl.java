package com.everis.bootcamp.bankproductms.service.impl;

import static java.lang.StrictMath.abs;

import com.everis.bootcamp.bankproductms.dao.BankProductComissionRepository;
import com.everis.bootcamp.bankproductms.dao.BankProductRepository;
import com.everis.bootcamp.bankproductms.dao.BankProductTransactionLogRepository;
import com.everis.bootcamp.bankproductms.dao.ComissionRepository;
import com.everis.bootcamp.bankproductms.dto.BankMaxTransDto;
import com.everis.bootcamp.bankproductms.dto.DatesDto;
import com.everis.bootcamp.bankproductms.dto.MessageDto;
import com.everis.bootcamp.bankproductms.model.BankProduct;
import com.everis.bootcamp.bankproductms.model.BankProductComission;
import com.everis.bootcamp.bankproductms.model.BankProductTransactionLog;
import com.everis.bootcamp.bankproductms.service.BankProductService;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BankProductServiceImpl implements BankProductService {

  private static final Logger logger = LoggerFactory.getLogger(BankProductServiceImpl.class);

  @Autowired
  private BankProductRepository bankRepo;

  @Autowired
  private BankProductTransactionLogRepository logRepo;

  @Autowired
  private ComissionRepository comissionRepo;

  @Autowired
  private BankProductComissionRepository bankProdComissionRepo;


  @Override
  public Mono<BankProduct> findByNumAccount(String name) {
    return bankRepo.findByNumAccount(name);
  }

  @Override
  public Mono<BankProduct> findById(String id) {
    return bankRepo.findById(id);
  }

  @Override
  public Flux<BankProduct> findByClientNumDoc(String numDoc) {
    return bankRepo.findAllByClientNumDoc(numDoc);
  }

  @Override
  public Flux<BankProductTransactionLog> findLogByClientNumDoc(String numDoc) {

    return logRepo.findAllByClientNumDoc(numDoc);
  }

  @Override
  public Flux<BankProduct> findAll() {
    return bankRepo.findAll();
  }

  @Override
  public Mono<BankProduct> update(BankProduct bp, String id) {
    try {
      return bankRepo.findById(id)
          .flatMap(dbBankProd -> {

            //CreateDate
            if (bp.getCreateDate() != null) {
              dbBankProd.setCreateDate(bp.getCreateDate());
            }

            //ModifyDate
            dbBankProd.setModifyDate(new Date());

            //idProdType
            if (bp.getIdProdType() != null) {
              dbBankProd.setIdProdType(bp.getIdProdType());
            }

            //total
            if (bp.getTotal() != 0) {
              dbBankProd.setTotal(bp.getTotal());
            }

            //clientNumDoc
            if (bp.getClientNumDoc() != null) {
              dbBankProd.setClientNumDoc(bp.getClientNumDoc());
            }

            //numAccount
            if (bp.getNumAccount() != null) {
              dbBankProd.setNumAccount(bp.getNumAccount());
            }

            //bankName
            if (bp.getBankId() != null) {
              dbBankProd.setBankId(bp.getBankId());
            }

            //holders
            if (bp.getHolders() != null) {
              //verificar que lista interna no sea nula
              if (dbBankProd.getHolders() == null) {
                dbBankProd.setHolders(new HashSet<>());
              }
              //combinar lista con lista interna y borrar duplicados
              Set<String> holders = dbBankProd.getHolders();
              holders.addAll(bp.getHolders());
            }

            //authorized
            if (bp.getAuthorized() != null) {
              //verificar que lista interna no sea nula
              if (dbBankProd.getAuthorized() == null) {
                dbBankProd.setAuthorized(new HashSet<>());
              }
              //combinar lista con lista interna y borrar duplicados
              Set<String> authorized = dbBankProd.getAuthorized();
              authorized.addAll(bp.getAuthorized());
            }

            return bankRepo.save(dbBankProd);

          }).switchIfEmpty(Mono.error(new Exception("cuenta bancaria no encontrada")));
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  @Override
  public Mono<Void> delete(String id) {
    try {
      return bankRepo.findById(id).flatMap(cl -> {
        return bankRepo.delete(cl);
      });
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  private Mono<Boolean> validClient(String clientNumDoc) {
    Flux<BankProduct> productosCliente = bankRepo.findAllByClientNumDoc(clientNumDoc);
    Mono<Long> cantAccounts = productosCliente.count();
    Mono<Boolean> ret = cantAccounts.map(c -> {
      if (c < 1) {
        return true;
      } else {
        return false;
      }
    });

    return ret;
  }

  private Mono<String> getClientTypeFromApi(String numDoc) {
    String url = "http://localhost:8001/client/getClientType/" + numDoc;
    return WebClient.create()
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class);
  }


  private Mono<Boolean> getExistBank(String numId) {
    String url = "http://localhost:8002/bank/exist/" + numId;
    return WebClient.create()
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(Boolean.class);
  }


  private Mono<BankMaxTransDto> getBankComission(String numId) {
    String url = "http://localhost:8002/bank/bankComission/" + numId;
    return WebClient.create()
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(BankMaxTransDto.class);
  }

  private Mono<BankProduct> saveNewClientTypes(String ct, String idProdType, BankProduct bp) {
    //verificar monto minimo y monto fin
    if (bp.getTotal() <= 0) {
      return Mono.error(new Exception("Ingresar monto minimo de creacion"));
    } else if (bp.getMinFin() <= 0) {
      return Mono.error(new Exception("Ingresar monto minimo de fin de mes"));
    }

    if (ct.equals("3")) { //personal VIP
      if (idProdType.equals("1") || idProdType.equals("2") || idProdType.equals("3")) {
        return bankRepo.save(bp);
      }
      return Mono.error(new Exception("Tipo de cuenta no soportado"));

    } else if (ct.equals("4")) { //PYME
      if (idProdType.equals("2")) {
        return bankRepo.save(bp);
      }
      return Mono.error(new Exception("Tipo de cuenta no soportado"));

    } else { // Corporativo
      if (idProdType.equals("2")) {
        return bankRepo.save(bp);
      }
      return Mono.error(new Exception("Tipo de cuenta no soportado"));
    }
  }


  private Mono<BankProduct> saveCliPerEmp(String ct, String idProdType, BankProduct bp) {
    if (bp.getMinFin() != 0) {
      return Mono.error(new Exception(
          "Un cliente personal o empresarial no puede tener monto minimo de fin de mes"));
    }

    if (ct.equals("1")) { //personal

      if (idProdType.equals("1") || idProdType.equals("2") || idProdType.equals("3")) {
        //si lo es, buscar el numero de cuentas con numDoc del cliente

        //buscar todos los productos que coincidan con numDoc y idProdType 1,2 o 3
        List<String> ids = Arrays.asList("1", "2", "3");
        Flux<BankProduct> productosCliente = bankRepo
            .findByClientNumDocAndIdProdTypeIn(bp.getClientNumDoc(), ids);
        Mono<Long> cantAccounts = productosCliente.count();
        return cantAccounts.flatMap(c -> {
          logger.info("cantidad de cuentas = " + c.toString());
          if (c >= 1) {
            return Mono.error(new Exception(
                "Un cliente personal solo puede tener un máximo de una cuenta de ahorro, "
                    + "una cuenta corriente o cuentas a plazo fijo."));
          }
          return bankRepo.save(bp);
        });
      }
      return Mono.error(new Exception("Tipo de cuenta no soportado"));

    } else { //empresarial
      //ver si el tipo de cuenta es ahorro o plazo fijo
      if (idProdType.equals("1") || idProdType.equals("2")) {
        //si es una de esas, no dejar guardar
        return Mono.error(new Exception(
            "Un cliente empresarial no puede tener cuenta de ahorro ni de plazo fijo."));

      } else if (idProdType.equals("3")) {
        //si es cuenta corriente guardar nomas
        return bankRepo.save(bp);
      }
      return Mono.error(new Exception("Tipo de cuenta no soportado"));
    }
  }

  @Override
  public Mono<BankProduct> save(BankProduct bp) {
    try {
      Mono<Boolean> existeBanco = getExistBank(bp.getBankId());

      Mono<Boolean> debts = validateClientDebts(bp.getClientNumDoc());

      return existeBanco.flatMap(existe -> {
        return debts.flatMap(validDebts -> {
          if (existe && validDebts) {
            if (bp.getCreateDate() == null) {
              bp.setCreateDate(new Date());
            } else {
              bp.setCreateDate(bp.getCreateDate());
            }
            if (bp.getMaxTransactions() <= 0) {
              return Mono
                  .error(new Exception("Ingresar un número de transacciones máximas válido"));
            }
            bp.setCurrentTransNumber(0);
            if (bp.getTotal() < 0) {
              return Mono.error(new Exception("Ingresar un saldo valido"));
            }
            bp.setLastTransactionDate(new Date());
            //Añadir numDocCliente a Holders
            Set<String> holders = new HashSet<>();
            holders.add(bp.getClientNumDoc());
            bp.setHolders(holders);
            //crear authorized
            bp.setAuthorized(new HashSet<>());

            String idProdType = bp.getIdProdType();
            //traer al tipo de cliente de la api clientes
            Mono<String> clientType = getClientTypeFromApi(bp.getClientNumDoc());

            return clientType.flatMap(ct -> {
              logger.info("client type -> " + ct);
              if (!ct.equals("-1")) {
                //si al final existe, buscar el tipo de cliente
                if (ct.equals("1") || ct.equals("2")) {
                  return saveCliPerEmp(ct, idProdType, bp);
                } else if (ct.equals("3") || ct.equals("4") || ct.equals("5")) {
                  return saveNewClientTypes(ct, idProdType, bp);
                }

                return Mono.error(new Exception("Tipo de cliente no soportado"));


              } else {
                return Mono.error(new Exception("Cliente no registrado"));
              }
            });
          } else {
            return Mono.error(new Exception("El banco del producto no existe"));
          }

        });
      });

    } catch (Exception e) {
      return Mono.error(e);
    }

  }

  private Boolean diferentMonth(BankProduct bp) {
    Calendar today = Calendar.getInstance();
    today.setTime(new Date());
    Calendar lastTrans = Calendar.getInstance();
    today.setTime(bp.getLastTransactionDate());
    return today.get(Calendar.MONTH) != lastTrans.get(Calendar.MONTH);
  }

  @Override
  public Mono<MessageDto> depositOrRetireMoney(String numAccount, double money) {
    try {
      return bankRepo.findByNumAccount(numAccount)
          .flatMap(dbBankProd -> {

            Mono<Boolean> existeBanco = getExistBank(dbBankProd.getBankId());

            return existeBanco.flatMap(existe -> {
              if (existe) {
                //resetear numero de transaccion actual si cambio el mes
                if (diferentMonth(dbBankProd)) {
                  dbBankProd.setCurrentTransNumber(0);
                }
                //verificar si se debe pagar comision
                //cambiar la forma en que se busca la comision
                Mono<BankMaxTransDto> maxTransMono = getBankComission(dbBankProd.getBankId());

                return maxTransMono.flatMap(maxTrans -> {
                  HashMap<String, Integer> productMaxTrans = maxTrans.getProductMaxTrans();
                  Integer mt = productMaxTrans.get(dbBankProd.getIdProdType());

                  logger.info(productMaxTrans.toString());
                  logger.info(dbBankProd.getIdProdType());
                  if (mt != null) {
                    return comissionRepo.findFirstByOrderByDateCreatedDesc().flatMap(com -> {

                      double comission = abs(money * com.getComission());
                      double amount = money;

                      boolean comissionAplicable = dbBankProd.getCurrentTransNumber() > mt;

                      if (comissionAplicable) {
                        //aplicar comision
                        amount = money - comission;
                      }

                      double currentMoney = dbBankProd.getTotal();

                      if (currentMoney + amount > dbBankProd.getMinFin()) {
                        dbBankProd.setTotal(currentMoney + amount);
                        dbBankProd.setCurrentTransNumber(dbBankProd.getCurrentTransNumber() + 1);
                        dbBankProd.setLastTransactionDate(new Date());
                        //guardar log de comisiones
                        if (comissionAplicable) {
                          BankProductComission bpc = new BankProductComission(
                              dbBankProd.getNumAccount(),
                              money, comission, new Date());
                          bankProdComissionRepo.save(bpc).subscribe();
                        }
                      } else {
                        return Mono.error(
                            new Exception("Monto de retiro supera el monto minimo de la cuenta"));
                      }

                      //guardar log
                      BankProductTransactionLog transactionLog = new BankProductTransactionLog(
                          dbBankProd.getClientNumDoc(),
                          dbBankProd.getNumAccount(), dbBankProd.getTotal() - money, money,
                          new Date());
                      logRepo.save(transactionLog).subscribe();

                      bankRepo.save(dbBankProd).subscribe();

                      return Mono.just(new MessageDto("1", "Transaccion realizada correctamente"));

                    });

                  } else {
                    return Mono.error(new Exception(
                        "No se encontro maximo numero de transacciones en el banco "
                            + "para el tipo de producto bancario"));
                  }
                });
              } else {
                return Mono.error(new Exception("Banco no existe"));
              }

            });

          }).switchIfEmpty(Mono.error(new Exception("cuenta no encontrada")));
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  @Override
  public Mono<MessageDto> bankProductTransaction(String numAccountOrigin,
      String numAccountDestination, double money) {
    try {
      if (money >= 0) {
        return bankRepo.findByNumAccount(numAccountOrigin).flatMap(bpOrigin -> {
          if (bpOrigin.getTotal() - money >= bpOrigin.getMinFin()) {

            return bankRepo.findByNumAccount(numAccountDestination).flatMap(bpDestination -> {
              Mono<Boolean> bankOriginExist = getExistBank(bpOrigin.getBankId());
              Mono<Boolean> bankDestinationExist = getExistBank(bpDestination.getBankId());

              return bankOriginExist.flatMap(originExist -> {
                return bankDestinationExist.flatMap(destinationExist -> {
                  if (originExist) {
                    if (destinationExist) {
                      bpOrigin.setTotal(bpOrigin.getTotal() - money);
                      bpDestination.setTotal(bpDestination.getTotal() + money);
                      bankRepo.save(bpOrigin).subscribe();
                      bankRepo.save(bpDestination).subscribe();
                      return Mono
                          .justOrEmpty(new MessageDto("1", "Transaccion realizada correctamente"));

                    } else {
                      return Mono.error(new Exception("banco de destino no existe"));
                    }
                  } else {
                    return Mono.error(new Exception("banco de origen no existe"));
                  }
                });
              });
            }).switchIfEmpty(Mono.error(new Exception("Cuenta destino no existe")));
          } else {
            return Mono.error(new Exception("el monto supera el limite de la cuenta de origen"));
          }
        }).switchIfEmpty(Mono.error(new Exception("Cuenta origen no existe")));
      } else {
        return Mono.error(new Exception("El monto no puede ser menor a 0"));
      }
    } catch (Exception e) {
      return Mono.error(e);
    }
  }


  private Mono<Double> getCreditDebt(String creditNumber) {
    String url = "http://localhost:8020/creditprod/getDebt/" + creditNumber;
    return WebClient.create()
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(Double.class);
  }


  private Mono<String> payCreditDebt(String creditNumber) {
    String url = "http://localhost:8020/creditprod/payDebt/" + creditNumber;
    return WebClient.create()
        .post()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class);
  }

  private Mono<Boolean> validateClientDebts(String clientNumDoc) {
    String url = "http://localhost:8020/creditprod/validDebt/" + clientNumDoc;
    return WebClient.create()
        .post()
        .uri(url)
        .retrieve()
        .bodyToMono(Boolean.class);
  }

  @Override
  public Mono<MessageDto> payCreditProduct(String numAccount, String creditNumber) {
    try {
      //traer monto a pagar del producto de credito de microservicio de productos de credito
      return bankRepo.findByNumAccount(numAccount).flatMap(dbBankProd -> {
        Mono<Boolean> existeBanco = getExistBank(dbBankProd.getBankId());
        return existeBanco.flatMap(existe -> {

          if (existe) {
            Mono<Double> creditDebt = getCreditDebt(creditNumber);
            return creditDebt.flatMap(debt -> {
              logger.info("debt=" + debt);
              if (dbBankProd.getTotal() - debt >= dbBankProd.getMinFin()) {
                dbBankProd.setTotal(dbBankProd.getTotal() - debt);
                //enviar pago a tarjeta de credito
                Mono<String> creditMsResponse = payCreditDebt(creditNumber);
                return creditMsResponse.flatMap(response -> {
                  if (!response.equals("-1")) {
                    bankRepo.save(dbBankProd).subscribe();
                    return Mono.justOrEmpty(new MessageDto("1", response));
                  } else {
                    return Mono.error(new Exception("El banco del producto de credito no existe"));
                  }
                });
              } else {
                return Mono.error(
                    new Exception("Monto a pagar supera el monto mínimo de la cuenta bancaria"));
              }
            });

          } else {
            return Mono.error(new Exception("Banco de cuenta bancaria no existe"));
          }

        });
      }).switchIfEmpty(Mono.error(new Exception("cuenta bancaria no encontrada")));
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  @Override
  public Flux<BankProductComission> comissionReport(DatesDto dates) {
    return bankProdComissionRepo
        .findAllByComissionDateBetween(dates.getStartDate(), dates.getEndDate());
  }

  @Override
  public Flux<BankProduct> findByNumAccountAndBankId(String numAccount, String bankId) {
    return bankRepo.findByClientNumDocAndBankId(numAccount, bankId);
  }

  @Override
  public Flux<BankProduct> productReport(DatesDto dates) {
    return bankRepo.findAllByModifyDateBetween(dates.getStartDate(), dates.getEndDate());
  }

  @Override
  public Mono<String> getBankId(String numAccount) {
    return bankRepo.findByNumAccount(numAccount).map(bp -> {
      return bp.getBankId();
    });
  }

  @Override
  public Mono<MessageDto> chargeComission(String numAccount, double comission) {
    return bankRepo.findByNumAccount(numAccount).flatMap(bp -> {
      if (bp.getTotal() - comission >= bp.getMinFin()) {
        bp.setTotal(bp.getTotal() - comission);
        bankRepo.save(bp).subscribe();
        return Mono.justOrEmpty(new MessageDto("1", "Comision cobrada correctamente"));
      } else {
        double pendiente = bp.getMinFin() - bp.getTotal() + comission;
        bp.setTotal(bp.getMinFin());
        bankRepo.save(bp).subscribe();
        return Mono
            .justOrEmpty(new MessageDto("1", "Saldo insuficiente, usted debe: " + pendiente));
      }
    });
  }
}
