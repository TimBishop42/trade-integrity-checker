package com.crypto.tradeintegritychecker.controller;

import com.crypto.tradeintegritychecker.client.CryptoClient;
import com.crypto.tradeintegritychecker.model.integrity.IntegritySummary;
import com.crypto.tradeintegritychecker.model.request.Timeframe;
import com.crypto.tradeintegritychecker.service.IntegrityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Controller class created to facilitate manual testing */
@RequiredArgsConstructor
@RestController
public class TradeIntegrityController {

  private final IntegrityService integrityService;
  private final CryptoClient cryptoClient;

  @GetMapping("/run-checker/{instrumentName}/{interval}")
  public ResponseEntity<IntegritySummary> runTradeIntegrityChecker(
      @PathVariable String instrumentName,
      @PathVariable String interval) {
    return ResponseEntity.ok(integrityService.evaluateDataIntegrity(instrumentName, interval));
  }

  @GetMapping("/run-checker")
  public ResponseEntity<String> runTradeIntegrityChecker() {
    return ResponseEntity.ok("All Good");
  }

  @GetMapping("/getTrades")
  public ResponseEntity getTrades(){
    return ResponseEntity.ok(cryptoClient.getTrades());
  }

  @GetMapping("/getCandlesticks")
  public ResponseEntity getCandlesticks(){
    return ResponseEntity.ok(
        integrityService.getCandlestickData("BTC_USDT", Timeframe.getTimeframeFromString("15m")));
  }
}
