package com.crypto.tradeintegritychecker.integration;


import com.crypto.tradeintegritychecker.client.CryptoClient;
import com.crypto.tradeintegritychecker.model.integrity.IntegritySummary;
import com.crypto.tradeintegritychecker.model.request.Timeframe;
import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickResponse;
import com.crypto.tradeintegritychecker.model.response.trades.GetTradesResponse;
import com.crypto.tradeintegritychecker.service.IntegrityService;
import com.crypto.tradeintegritychecker.service.RuleService;
import com.crypto.tradeintegritychecker.util.JsonParser;
import com.crypto.tradeintegritychecker.writer.CsvFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

/**
 * Purpose of this test is to feed in mocked API data, which has been intentionally modified to contain errors
 * This will demonstrate the triggering of the Integrity Rules
 */

@Slf4j
@ExtendWith(MockitoExtension.class)
public class IntegrityCheckMockedAPIDataIT {

    @Mock
    private CryptoClient cryptoClientMock;

    @InjectMocks
    private IntegrityService integrityService;

    @Spy
    private CsvFileWriter csvFileWriter = new CsvFileWriter();

    @Spy
    private RuleService ruleService = new RuleService(csvFileWriter);

    private CandleStickResponse candleStickResponse;
    private GetTradesResponse getTradesResponse;

    @BeforeEach
    public void setup() {
        JsonParser parser = new JsonParser();
        candleStickResponse = parser.parseCandlestickFromFile();
        getTradesResponse = parser.parseTradesData();
        when(cryptoClientMock.getCandlestickData("ETH_CRO", Timeframe.ONE_MINUTE)).thenReturn(candleStickResponse);
        when(cryptoClientMock.getTradesByInstrument("ETH_CRO")).thenReturn(getTradesResponse);
    }

    /**
     * One candle in the stubbed JSON file has had all of its attributes modified.
     * Thus the analysis should print a break for every rule (5 in total)
     */
    @Test
    public void runIntegrityCheckChangedOpenPrice() {
        IntegritySummary result = integrityService.evaluateDataIntegrity("ETH_CRO", Timeframe.ONE_MINUTE);
        assertThat(result.getNumIntegrityBreaks()).isEqualTo(5);
        result.getDataIntegrityBreaks().stream()
                .forEach(integrityBreak -> log.info(integrityBreak.getRule()));
    }



}
