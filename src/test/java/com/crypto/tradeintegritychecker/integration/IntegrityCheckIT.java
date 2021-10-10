package com.crypto.tradeintegritychecker.integration;

import com.crypto.tradeintegritychecker.model.integrity.IntegritySummary;
import com.crypto.tradeintegritychecker.model.request.Timeframe;
import com.crypto.tradeintegritychecker.service.IntegrityService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Purpose of this test is to run the full application, using live data Tests will be run across a
 * number of key instruments
 *
 * <p>As we are using live data for this test, all tests should pass
 */
@Slf4j
@SpringBootTest
public class IntegrityCheckIT {

  @Autowired private IntegrityService integrityService;

  private static final Timeframe ONE_MINUTE = Timeframe.ONE_MINUTE;

  /**
   * There is too much trade activity on BTC_USDT instrument to get any reliable data Using a 1 min
   * candle stick - there is far more than 200 trades per minute, so it is not possible to validate
   */
  @Test
  public void runIntegrityCheckerBTC_USDT() {
    IntegritySummary summaryOutput = integrityService.evaluateDataIntegrity("BTC_USDT", ONE_MINUTE);
    assertThat(summaryOutput.getNumIntegrityBreaks()).isEqualTo(0);
    log.info(
        "Test 1.1: Total candlesticks available for Test validation {}",
        summaryOutput.getNumCandlesticksAnalyzed());
  }

  @Test
  public void runIntegrityCheckerETH_CRO() {
    IntegritySummary summaryOutput = integrityService.evaluateDataIntegrity("ETH_CRO", ONE_MINUTE);
    assertThat(summaryOutput.getNumIntegrityBreaks()).isEqualTo(0);
    assertThat(summaryOutput.getNumCandlesticksAnalyzed()).isGreaterThan(0);
    log.info(
            "Test 1.2: Total candlesticks available for Test validation {}",
            summaryOutput.getNumCandlesticksAnalyzed());
  }

  @Test
  public void runIntegrityCheckerMATIC_BTC() {
    IntegritySummary summaryOutput =
        integrityService.evaluateDataIntegrity("MATIC_BTC", ONE_MINUTE);
    assertThat(summaryOutput.getNumIntegrityBreaks()).isEqualTo(0);
    assertThat(summaryOutput.getNumCandlesticksAnalyzed()).isGreaterThan(0);
    log.info(
            "Test 1.3: Total candlesticks available for Test validation {}",
            summaryOutput.getNumCandlesticksAnalyzed());
  }

  @Test
  public void runIntegrityCheckerSHIB_USDC() {
    IntegritySummary summaryOutput =
        integrityService.evaluateDataIntegrity("SHIB_USDC", ONE_MINUTE);
    assertThat(summaryOutput.getNumIntegrityBreaks()).isEqualTo(0);
    assertThat(summaryOutput.getNumCandlesticksAnalyzed()).isGreaterThan(0);
    log.info(
            "Test 1.4: Total candlesticks available for Test validation {}",
            summaryOutput.getNumCandlesticksAnalyzed());
  }

  @Test
  public void runIntegrityCheckerVET_CRO() {
    IntegritySummary summaryOutput = integrityService.evaluateDataIntegrity("VET_CRO", ONE_MINUTE);
    assertThat(summaryOutput.getNumIntegrityBreaks()).isEqualTo(0);
    assertThat(summaryOutput.getNumCandlesticksAnalyzed()).isGreaterThan(0);
    log.info(
            "Test 1.5: Total candlesticks available for Test validation {}",
            summaryOutput.getNumCandlesticksAnalyzed());
  }

  @Test
  public void runIntegrityCheckerGRT_CRO() {
    IntegritySummary summaryOutput = integrityService.evaluateDataIntegrity("GRT_CRO", ONE_MINUTE);
    assertThat(summaryOutput.getNumIntegrityBreaks()).isEqualTo(0);
    assertThat(summaryOutput.getNumCandlesticksAnalyzed()).isGreaterThan(0);
    log.info(
            "Test 1.6: Total candlesticks available for Test validation {}",
            summaryOutput.getNumCandlesticksAnalyzed());
  }
}
