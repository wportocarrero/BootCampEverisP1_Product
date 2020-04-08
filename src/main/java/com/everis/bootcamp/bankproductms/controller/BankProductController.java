package com.everis.bootcamp.bankproductms.controller;

import com.everis.bootcamp.bankproductms.dto.DatesDto;
import com.everis.bootcamp.bankproductms.dto.MessageDto;
import com.everis.bootcamp.bankproductms.model.BankProduct;
import com.everis.bootcamp.bankproductms.model.BankProductComission;
import com.everis.bootcamp.bankproductms.model.BankProductTransactionLog;
import com.everis.bootcamp.bankproductms.service.BankProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.net.URI;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Api(tags = "Bank Product API", value = "Operations for bank products")
@RestController
@RequestMapping("/bankprod")
public class BankProductController {

  @Autowired
  private BankProductService service;

  /**
   * Controlador de BankProduct ms.
   */
  @GetMapping("/test")
  public Mono<BankProduct> saludo() {
    BankProduct hola = new BankProduct();
    hola.setBankId("1");
    return Mono.justOrEmpty(hola);
  }

  @ApiOperation(value = "Service used to find a bank product by clientNumDoc")
  @GetMapping("/find/{clientNumDoc}")
  public Flux<BankProduct> findByClientNumDoc(@PathVariable("clientNumDoc") String clientNumDoc) {
    return service.findByClientNumDoc(clientNumDoc);
  }

  @ApiOperation(value = "Service used to find all bank products")
  @GetMapping("/findAll")
  public Flux<BankProduct> findAll() {
    return service.findAll();
  }

  @ApiOperation(value = "Service used to get bank product id")
  @GetMapping("/getBankId/{numAccount}")
  public Mono<String> getBankId(@PathVariable("numAccount") String numAccount) {
    return service.getBankId(numAccount);
  }

  @ApiOperation(value = "Service used to find a bank product by id")
  @GetMapping("/find/{id}")
  public Mono<BankProduct> findById(@PathVariable("id") String id) {
    return service.findById(id);
  }

  @ApiOperation(value = "Service used to return transaction log of a bank product")
  @GetMapping("/log/{clientNumDoc}")
  public Flux<BankProductTransactionLog> findLogByClientNumDoc(
      @PathVariable("clientNumDoc") String clientNumDoc) {
    return service.findLogByClientNumDoc(clientNumDoc);
  }

  @ApiOperation(value = "Service used to return all product of a client registered in certain bank")
  @GetMapping("/find/{clientNumDoc}/{bankId}")
  public Flux<BankProduct> findByClientNumDocAndBankId(
      @PathVariable("clientNumDoc") String clientNumDoc,
      @PathVariable("bankId") String bankId) {
    return service.findByNumAccountAndBankId(clientNumDoc, bankId);
  }

  /**
   * GUARDAR.
   */
  @ApiOperation(value = "Service used to save a bank product")
  @PostMapping("/save")
  public Mono<ResponseEntity<BankProduct>> create(@Valid @RequestBody BankProduct bp) {
    return service.save(bp)
        .map(b -> ResponseEntity.created(URI.create("/api/bankproduct".concat(b.getId())))
            .contentType(MediaType.APPLICATION_JSON).body(b));
  }

  /**
   * ACTUALIZAR.
   */
  @ApiOperation(value = "Service used to update a bank product")
  @PutMapping("/update/{id}")
  public Mono<ResponseEntity<BankProduct>> update(@PathVariable("id") String id,
      @RequestBody BankProduct bp) {
    return service.update(bp, id)
        .map(b -> ResponseEntity.created(URI.create("/api/bankproduct".concat(b.getId())))
            .contentType(MediaType.APPLICATION_JSON).body(b))
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  /**
   * ELIMINAR.
   */
  @ApiOperation(value = "Service used to delete a bank product")
  @DeleteMapping("/delete/{id}")
  public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
    return service.delete(id)
        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
        .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
  }

  //TRANSACCION
  @ApiOperation(value = "Service used to manage money transactions of a bank product")
  @PostMapping("/transaction/{numAccount}")
  public Mono<MessageDto> transaction(@PathVariable("numAccount") String numAccount,
      @RequestBody double money) {
    return service.depositOrRetireMoney(numAccount, money);
  }

  //PAGAR DEUDA DE CREDITO
  @ApiOperation(value = "Service used to pay credit card debt")
  @PostMapping("/payCreditDebt/{numAccount}/{creditNumber}")
  public Mono<MessageDto> payCreditDebt(@PathVariable("numAccount") String numAccount,
      @PathVariable("creditNumber") String creditNumber) {
    return service.payCreditProduct(numAccount, creditNumber);
  }

  //PAGAR A OTRA CUENTA DE BANCO
  @ApiOperation(value = "Service used to pay another bank account")
  @PostMapping("/bankProductTransaction/{numAccountOrigin}/{numAccountDestination}")
  public Mono<MessageDto> bankProductTransaction(
      @PathVariable("numAccountOrigin") String numAccountOrigin,
      @PathVariable("numAccountDestination") String numAccountDestination,
      @RequestBody double money) {
    return service.bankProductTransaction(numAccountOrigin, numAccountDestination, money);
  }

  //REPORTE DE COMISIONES
  @ApiOperation(value = "Service used to get all the comissions on a date range")
  @PostMapping("/comissionReport")
  public Flux<BankProductComission> comissionReport(@RequestBody DatesDto dates) {
    return service.comissionReport(dates);
  }

  //REPORTE DE PRODUCTOS
  @ApiOperation(value = "Service used to get all the products on a date range")
  @PostMapping("/productReport")
  public Flux<BankProduct> productReport(@RequestBody DatesDto dates) {
    return service.productReport(dates);
  }

  //COBRAR COMISION EXTERNA
  @ApiOperation(value = "Service used to pay external comission")
  @PostMapping("/chargeExtComission/{numAccount}")
  public Mono<MessageDto> chargeExtComission(@PathVariable("numAccount") String numAccount,
      @RequestBody double externalComission) {
    return service.chargeComission(numAccount, externalComission);
  }
}