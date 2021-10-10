package com.crypto.tradeintegritychecker.service;

import com.crypto.tradeintegritychecker.model.integrity.CandlestickTradeData;
import com.crypto.tradeintegritychecker.model.integrity.IntegrityViolation;
import com.crypto.tradeintegritychecker.model.integrity.IntegrityViolationDetail;
import com.crypto.tradeintegritychecker.model.integrity.ValidationRule;
import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickData;
import com.crypto.tradeintegritychecker.model.response.trades.TradesData;
import com.crypto.tradeintegritychecker.writer.CsvFileWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleService {

  private final CsvFileWriter csvFileWriter;

  public IntegrityViolationDetail evaluateGroupedData(
      List<CandlestickTradeData> candlestickTradeData) {
    List<IntegrityViolation> integrityViolations = new ArrayList<>();

    // First filter out any candlesticks that do not have any trade data present
    // Then we must skip the first candlestick in the sorted list, as it will never have a complete
    // set of trades
    List<CandlestickTradeData> trimmedList =
        removeFirstAndLastElementsFromSortedList(candlestickTradeData);

    trimmedList.stream().forEach(candlestick -> runRuleChecks(candlestick, integrityViolations));

    // File writing for data gathering purposes only
    csvFileWriter.writeIntegrityViolationsToFile(integrityViolations);
    return IntegrityViolationDetail.builder()
        .integrityViolations(integrityViolations)
        .numCandlesticksAnalyzed(trimmedList.size())
        .build();
  }

  /**
   * Due to the differences in data sets returned for GetTrades and GetCandlesticks, it can never be
   * guaranteed that the first and last candle stick elements will have a full set of trade data.
   * Thus to avoid erroneous Rule Breaks, we must remove these elements from validation
   *
   * @param candlestickTradeData
   */
  private List<CandlestickTradeData> removeFirstAndLastElementsFromSortedList(
      List<CandlestickTradeData> candlestickTradeData) {
    List<CandlestickTradeData> sortedAndTrimmedList =
        candlestickTradeData.stream()
            .filter(data -> data.getTrades().size() != 0)
            .sorted(Comparator.comparing(CandlestickTradeData::getEndTime))
            .skip(1L)
            .collect(Collectors.toList());

    if (sortedAndTrimmedList.size() > 0) {
      sortedAndTrimmedList.remove(sortedAndTrimmedList.size() - 1);
    }
    log.info(
        "After removing first and last elements of the candlestick list, we have {} candlestick/trade data sets to validate",
        sortedAndTrimmedList.size());

    return sortedAndTrimmedList;
  }

  /**
   * Method will run all of the rules against a candlestick Open Price - The oldest trade/lowest
   * timestamp Long should = Open attribute on candle Close Price - The newest trade/highest
   * timestamp Long should = Close attribute on candle Low Price - The lowest trade price should =
   * Low attribute on candle High Price - The highest trade price should = High attribute on candle
   * Volume - The sum of all trades should = the Volume attribute on the candle
   *
   * @param candlestick
   * @param integrityViolations
   * @return
   */
  private void runRuleChecks(
      CandlestickTradeData candlestick, List<IntegrityViolation> integrityViolations) {
    // We only want to perform validation on candlesticks when there is trade data present
    validateOpenPrice(candlestick, integrityViolations);
    validateClosePrice(candlestick, integrityViolations);
    validateHighPrice(candlestick, integrityViolations);
    validateLowPrice(candlestick, integrityViolations);
    validateVolume(candlestick, integrityViolations);
  }

  /**
   * Method is required to account for the common scenario where we only have a small portion of the
   * trades for a candlestick This is guarnteed to occur for the "first" candlestick in our list
   * which has matched trades. Thus we must always exclude the first matched candlestick in the list
   *
   * @param candlestick
   */
  private void candlestickHasCompleteTradeSet(CandlestickTradeData candlestick) {}

  private void validateVolume(
      CandlestickTradeData candlestickSummary, List<IntegrityViolation> integrityViolations) {
    // If we have at least one trade and the sum of all trade volume does not equal the listed
    // volume on the candlestick, we have a break

    // Volume on Candlestick is round to 5 DP, where trade quantity is much higher precision
    BigDecimal tradeVolume =
        candlestickSummary.getTrades().stream()
            .map(TradesData::getTradeQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(5, RoundingMode.HALF_UP);

    BigDecimal candleStickVolume =
        candlestickSummary.getCandlestick().getVolume().setScale(5, RoundingMode.HALF_UP);

    if (!tradeVolume.equals(candleStickVolume)) {
      log.warn(
          "Candlestick integrity violation found, total volume of trades did not equal volume listed on candlestick. Candlestick Volume {}, tradeVolume {}",
          candleStickVolume,
          tradeVolume);
      integrityViolations.add(
          IntegrityViolation.builder()
              .candleStickTradeData(candlestickSummary)
              .rule(ValidationRule.VOLUME.getRuleBreak())
              .tradesVolume(tradeVolume)
              .build());
    }
  }

  private void validateLowPrice(
      CandlestickTradeData candlestickSummary, List<IntegrityViolation> integrityViolations) {
    // If we have at least one trade in this interval and Candlestick Low is not equal to Minimum
    // Trade Price, we have a break
    TradesData lowestTrade =
        Collections.min(
            candlestickSummary.getTrades(), Comparator.comparing(TradesData::getTradePrice));
    // Big decimal compareTo return -1 if calling object is less that parameter, 0 if equal, 1 if
    // greater
    // Using this method will account for any scaling differences
    if (lowestTrade.getTradePrice().compareTo(candlestickSummary.getCandlestick().getLow()) != 0) {
      log.warn(
          "Candlestick integrity violation found, Lowest price of a trade did not equal Low price listed on candlestick. Candlestick {}, trade {}",
          candlestickSummary,
          lowestTrade);
      integrityViolations.add(
          IntegrityViolation.builder()
              .candleStickTradeData(candlestickSummary)
              .rule(ValidationRule.LOW.getRuleBreak())
              .trade(lowestTrade)
              .build());
    }
  }

  private void validateHighPrice(
      CandlestickTradeData candlestickSummary, List<IntegrityViolation> integrityViolations) {
    // If we have at least one trade in this interval and Candlestick High is not equal to Max Trade
    // Price, we have a break
    TradesData highestTrade =
        Collections.max(
            candlestickSummary.getTrades(), Comparator.comparing(TradesData::getTradePrice));
    // Big decimal compareTo return -1 if calling object is less that parameter, 0 if equal, 1 if
    // greater
    // Using this method will account for any scaling differences
    if (highestTrade.getTradePrice().compareTo(candlestickSummary.getCandlestick().getHigh())
        != 0) {
      log.warn(
          "Candlestick integrity violation found, Highest price of a trade did not equal High price listed on candlestick. Candlestick {}, trade {}",
          candlestickSummary,
          highestTrade);
      integrityViolations.add(
          IntegrityViolation.builder()
              .candleStickTradeData(candlestickSummary)
              .rule(ValidationRule.HIGH.getRuleBreak())
              .trade(highestTrade)
              .build());
    }
  }

  private void validateClosePrice(
      CandlestickTradeData candlestickSummary, List<IntegrityViolation> integrityViolations) {
    // If we have at least one trade in this interval and Candlestick Close is not equal to Trade
    // Price, we have a break
    TradesData closeTrade =
        candlestickSummary.getTrades().get(candlestickSummary.getTrades().size() - 1);
    // Big decimal compareTo return -1 if calling object is less that parameter, 0 if equal, 1 if
    // greater
    // Using this method will account for any scaling differences
    if (handleDuplicateTimestampCloseTrades(
        candlestickSummary.getTrades(), candlestickSummary.getCandlestick()))
      if (closeTrade.getTradePrice().compareTo(candlestickSummary.getCandlestick().getClose())
          != 0) {
        log.warn(
            "Candlestick integrity violation found, newest trade did not equal Close price listed on candlestick {}, trade: {}",
            candlestickSummary,
            closeTrade);
        integrityViolations.add(
            IntegrityViolation.builder()
                .candleStickTradeData(candlestickSummary)
                .rule(ValidationRule.CLOSE.getRuleBreak())
                .trade(closeTrade)
                .build());
      }
  }

  // The list of trades is already sorted by Timestamp in the IntegrityService class.
  // Simply selecting the trade in first positions will suffice

  /**
   * The data running for the open rule is tricky - as we cannot guarantee that we will have all of
   * the trades for the first candle in our sequence. The Trades API only returns 200 trades - so we
   * a guaranteed to have a candle without a full set of trades
   *
   * @param candlestickSummary
   * @param integrityViolations
   */
  private void validateOpenPrice(
      CandlestickTradeData candlestickSummary, List<IntegrityViolation> integrityViolations) {
    // If we have at least one trade in this interval and Candlestick Open is not equal to Trade
    // Price, we have a break

    if (handleDuplicateTimestampOpenTrades(
        candlestickSummary.getTrades(), candlestickSummary.getCandlestick())) {
      log.warn(
          "Candlestick integrity violation found, oldest trade did not equal Open price listed on candlestick {}, trade: {}",
          candlestickSummary,
          candlestickSummary.getTrades().get(0).getTradePrice());
      integrityViolations.add(
          IntegrityViolation.builder()
              .candleStickTradeData(candlestickSummary)
              .rule(ValidationRule.OPEN.getRuleBreak())
              .trade(candlestickSummary.getTrades().get(0))
              .build());
    }
  }

  /**
   * Found some instances of a trade being booked at an identical timestamp as a second trade. As we
   * cannot detirmine the logic of how the candlestick picks which trade should count as OPEN or
   * CLOSE, We will check the first 2 and last two trades in a set. We will only run this check in
   * instances where trade timestamps are equal
   */
  private boolean handleDuplicateTimestampOpenTrades(
      List<TradesData> tradeData, CandleStickData candleStickData) {
    TradesData openTrade = tradeData.get(0);
    // If there is only one trade, do the standard OPEN check
    if (!(tradeData.size() > 1)) {
      return openTrade.getTradePrice().compareTo(candleStickData.getOpen()) != 0;
    }

    // If we have 2 trades booked at the same time for OPEN, check if either is equal to the
    // Candlestick OPEN
    else if (openTrade.getTradeTimestamp().equals(tradeData.get(1).getTradeTimestamp())) {
      List<TradesData> equalTimeStampList =
              getSortedTradesGivenTimestamp(tradeData, openTrade.getTradeTimestamp());
      log.info(
          "Found a candlestick that has two or more trades booked at the same time for Open Price. Will check price of both. Candlestick: {}, numTrades: {}",
          candleStickData, equalTimeStampList.size());
      return equalTimeStampList
              .get(equalTimeStampList.size() - 1)
              .getTradePrice()
              .compareTo(candleStickData.getOpen()) != 0;
    }
    // Otherwise we have multiple trades, but they are booked sequentially
    else {
      return openTrade.getTradePrice().compareTo(candleStickData.getOpen()) != 0;
    }
  }

  private boolean handleDuplicateTimestampCloseTrades(
      List<TradesData> trades, CandleStickData candlestick) {
    TradesData closeTrade = trades.get(trades.size() - 1);
    // If there is only one trade, do the standard CLOSE check
    if (!(trades.size() > 1)) {
      return closeTrade.getTradePrice().compareTo(candlestick.getClose()) != 0;
    }

    // If we have multiple trades booked at the same time for OPEN, check if either is equal to the
    // Candlestick OPEN
    else if (closeTrade
        .getTradeTimestamp()
        .equals(trades.get(trades.size() - 2).getTradeTimestamp())) {
      List<TradesData> equalTimeStampList =
          getSortedTradesGivenTimestamp(trades, closeTrade.getTradeTimestamp());
      log.info(
          "Found a candlestick that has two or more trades booked at the same time for Open Price. Will select the max price for open. Candlestick: {}, numtrades {}",
          candlestick,
          equalTimeStampList.size());
      return equalTimeStampList
              .get(equalTimeStampList.size() - 1)
              .getTradePrice()
              .compareTo(candlestick.getClose()) != 0;
    }
    // Otherwise we have multiple trades, but they are booked sequentially
    else {
      return closeTrade.getTradePrice().compareTo(candlestick.getClose()) != 0;
    }
  }

  private List<TradesData> getSortedTradesGivenTimestamp(List<TradesData> trades, Long timestamp) {
    return trades.stream()
        .filter(trade -> trade.getTradeTimestamp().equals(timestamp))
        .sorted(Comparator.comparing(TradesData::getTradePrice))
        .collect(Collectors.toList());
  }
}
