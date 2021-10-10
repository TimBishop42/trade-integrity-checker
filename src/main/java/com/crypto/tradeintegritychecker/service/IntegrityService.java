package com.crypto.tradeintegritychecker.service;

import com.crypto.tradeintegritychecker.client.CryptoClient;
import com.crypto.tradeintegritychecker.model.integrity.CandlestickTradeData;
import com.crypto.tradeintegritychecker.model.integrity.IntegritySummary;
import com.crypto.tradeintegritychecker.model.integrity.IntegrityViolation;
import com.crypto.tradeintegritychecker.model.integrity.IntegrityViolationDetail;
import com.crypto.tradeintegritychecker.model.request.Timeframe;
import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickData;
import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickResponse;
import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickResult;
import com.crypto.tradeintegritychecker.model.response.trades.GetTradesResponse;
import com.crypto.tradeintegritychecker.model.response.trades.TradesData;
import com.crypto.tradeintegritychecker.writer.CsvFileWriter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrityService {

  private final CsvFileWriter csvFileWriter;
  private final CryptoClient cryptoClient;
  private final RuleService ruleService;

  public IntegritySummary evaluateDataIntegrity(String instrumentName, String timeframe) {
    return evaluateDataIntegrity(instrumentName, Timeframe.getTimeframeFromString(timeframe));
  }

  @SneakyThrows
  public IntegritySummary evaluateDataIntegrity(String instrumentName, Timeframe timeframe) {
    log.info(
        "Beginning data integrity evaluation on Candlestick/Trade data for Instrument: {} using timeframe: {}",
        instrumentName,
        timeframe.getTimeframeString());
    List<CandlestickTradeData> candlestickTradeData;

    Future<CandleStickResponse> candlestickFuture =
        getCandlestickDataAsync(instrumentName, timeframe);
    Future<GetTradesResponse> tradeDataFuture = getTradeDataAsync(instrumentName);

    CandleStickResponse candleStickResponseList = candlestickFuture.get();
    GetTradesResponse getTradesResponseList = tradeDataFuture.get();

    // File writer implemented to log candlestick and trade test data - no functional purpose
    csvFileWriter.writeFileToCsv(candleStickResponseList, getTradesResponseList);

    // We only want to proceed with processing if we have some candlestick data
    if (nonNull(candleStickResponseList.getResult().getData())) {
      candlestickTradeData =
          groupTradeDataIntoCandleSticks(
              candleStickResponseList.getResult(), getTradesResponseList.getResult().getData());
    } else {
      log.warn("No data returned for candlesticks, will not proceed with validations");
      return null;
    }
    return summarizedIntegrityData(
        ruleService.evaluateGroupedData(candlestickTradeData),
        candleStickResponseList,
        getTradesResponseList);
  }

  private IntegritySummary summarizedIntegrityData(
      IntegrityViolationDetail dataIntegrityBreaks,
      CandleStickResponse candleStickResponseList,
      GetTradesResponse getTradesResponseList) {
    return IntegritySummary.builder()
        .numIntegrityBreaks(dataIntegrityBreaks.getIntegrityViolations().size())
        .dataIntegrityBreaks(dataIntegrityBreaks.getIntegrityViolations())
        .numCandlesticks(candleStickResponseList.getResult().getDepth())
        .numTrades(getTradesResponseList.getResult().getData().size())
        .numCandlesticksAnalyzed(dataIntegrityBreaks.getNumCandlesticksAnalyzed())
        .build();
  }

  private List<CandlestickTradeData> groupTradeDataIntoCandleSticks(
      CandleStickResult candleStickResult, List<TradesData> tradeList) {
    List<CandlestickTradeData> candlestickTradeData = new ArrayList<>();

    candleStickResult
        .getData()
        .forEach(
            candleStick -> {
              candlestickTradeData.add(
                  CandlestickTradeData.builder()
                      .timeframe(Timeframe.getTimeframeFromString(candleStickResult.getInterval()))
                      .instrument(candleStickResult.getInstrumentName())
                      .trades(
                          getMatchingTrades(
                              candleStick, tradeList, candleStickResult.getInterval()))
                      .candlestick(candleStick)
                      .endTime(candleStick.getEndTime())
                      .build());
            });
    return candlestickTradeData;
  }

  /**
   * Method will take a single candlestick and find all trades from the list that fall within this
   * start/end time Is inclusive of start, but exclusive of end time i.e tradeTimeStamp <
   * CandleStickEnd, tradeTimeStamp >= (CandleStickEnd-time interval)
   *
   * <p>List of trades n each candlestick will be sorted
   *
   * @param candleStick
   * @param tradeList
   * @return
   */
  private List<TradesData> getMatchingTrades(
      CandleStickData candleStick, List<TradesData> tradeList, String interval) {
    List<TradesData> matchingTradeList =
        tradeList.stream()
            .filter(
                tradeData ->
                    tradeMatchesTimeframe(
                        candleStick,
                        tradeData,
                        Timeframe.getTimeframeFromString(interval).getTimeframeMillis()))
            .sorted(Comparator.comparing(TradesData::getTradeTimestamp))
            .collect(Collectors.toList());

    // Only log the matched trades if we actually have some data to work with.
    // Client only return 200 trades there is rarely more than a few candles worth
    if (matchingTradeList.size() > 0) {
      log.info(
          "Found {} matching trades for candleStick with close time {}",
          matchingTradeList.size(),
          candleStick.getEndTime());
    }
    return matchingTradeList;
  }

  private boolean tradeMatchesTimeframe(
      CandleStickData candleStick, TradesData tradeData, Long interval) {
    // The expectation is that a candlestick will include trades that were booked < the endTime of
    // the candle.
    // From looking through the test data it appears that CandleStick End Time is actually the Start
    // time of the candle.....
    return tradeData.getTradeTimestamp() < candleStick.getEndTime() + interval
        && tradeData.getTradeTimestamp() >= candleStick.getEndTime();
  }

  @Async
  private Future<CandleStickResponse> getCandlestickDataAsync(
      String instrumentName, Timeframe timeframe) {
    CandleStickResponse candlestickResponse =
        cryptoClient.getCandlestickData(instrumentName, timeframe);
    return CompletableFuture.completedFuture(candlestickResponse);
  }

  @Async
  private Future<GetTradesResponse> getTradeDataAsync(String instrumentName) {
    GetTradesResponse tradesByInstrument = cryptoClient.getTradesByInstrument(instrumentName);
    return CompletableFuture.completedFuture(tradesByInstrument);
  }

  @Async
  public CandleStickResponse getCandlestickData(String instrumentName, Timeframe timeframe) {
    return cryptoClient.getCandlestickData(instrumentName, timeframe);
  }

  @Async
  public GetTradesResponse getTradeData(String instrumentName) {
    return cryptoClient.getTradesByInstrument(instrumentName);
  }
}
