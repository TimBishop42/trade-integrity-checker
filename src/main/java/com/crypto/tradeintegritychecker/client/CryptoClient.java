package com.crypto.tradeintegritychecker.client;

import com.crypto.tradeintegritychecker.model.request.Timeframe;
import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickResponse;
import com.crypto.tradeintegritychecker.model.response.trades.GetTradesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Service
public class CryptoClient {

    private static final String BASE_URL = "https://api.crypto.com/v2/public";
    private static final String GET_CANDLESTICKS = "/get-candlestick";
    private static final String GET_TRADES = "/get-trades";


    private final WebClient cryptoClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .build();

    public CandleStickResponse getCandlestickData(String instrumentName, Timeframe timeFrame) {
        log.info("Querying crypto.com candlestick endpoint for Instrument: {} and Timeframe: {}", instrumentName, timeFrame.getTimeframeString());
        return cryptoClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(GET_CANDLESTICKS)
                        .queryParam("instrument_name", instrumentName)
                        .queryParam("timeframe", timeFrame.getTimeframeString())
                        .build(instrumentName, timeFrame.getTimeframeString()))
                .retrieve()
                .bodyToMono(CandleStickResponse.class).block();
    }

    public GetTradesResponse getTradesByInstrument(String instrumentName) {
        log.info("Querying crypto.com getTrades endpoint for Instrument: {}", instrumentName);
        return cryptoClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(GET_TRADES)
                        .queryParam("instrument_name", instrumentName)
                        .build())
                .retrieve()
                .bodyToMono(GetTradesResponse.class).block();
    }

    @Async
    public GetTradesResponse getTrades() {
        log.info("Querying crypto.com getTrades endpoint for all instrument types");
        return cryptoClient.get()
                .uri(GET_TRADES)
                .retrieve()
                .bodyToMono(GetTradesResponse.class).block();
    }

}
