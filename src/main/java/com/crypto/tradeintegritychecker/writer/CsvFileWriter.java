package com.crypto.tradeintegritychecker.writer;

import com.crypto.tradeintegritychecker.model.integrity.CandlestickTradeData;
import com.crypto.tradeintegritychecker.model.integrity.IntegrityViolation;
import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickData;
import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickResponse;
import com.crypto.tradeintegritychecker.model.response.trades.GetTradesResponse;
import com.crypto.tradeintegritychecker.model.response.trades.TradesData;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@NoArgsConstructor
public class CsvFileWriter {

  private static final String CANDLESTICK_HEADER = "CandleStickEndTime,open,close,high,low,volume";
  private static final String TRADES_HEADER =
      "TradeTimestamp,tradePrice,tradeQuantity,side,tradeId";
  private static final String SUMMARY_HEADER =
      "CandleStickEndTime,open,close,high,low,volume,TradeTimestamp,tradeEpochTime,tradePrice,tradeQuantity,side,tradeId";
  private static final String SUMMARY_FILENAME = "SummaryFile";

  private static final String OUTPUT_FOLDER = "testOutput/";

  private static final String CSV_EXTENSION = ".csv";

  private static final String LINE_END = "\n";
  private static final String COMMA = ",";

  public void writeFileToCsv(
      CandleStickResponse candleStickResponseList, GetTradesResponse tradeList) {
    LocalDateTime runDateTime = LocalDateTime.now();
    String fileTime = runDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

    String candlestickFileName = "CandleSticks" + fileTime + CSV_EXTENSION;
    String tradesFileName = "Trades" + fileTime + CSV_EXTENSION;

    writeCandlestickFile(candlestickFileName, candleStickResponseList);
    writeTradeFile(tradesFileName, tradeList);
  }

  private void writeTradeFile(String tradesFileName, GetTradesResponse tradeList) {
    StringBuilder sb = new StringBuilder().append(TRADES_HEADER).append(LINE_END);

    File file = new File(tradesFileName);

    if (nonNull(tradeList.getResult().getData())) {
      tradeList.getResult().getData().stream()
          .forEach(
              tradesData -> {
                sb.append(
                        Instant.ofEpochMilli(tradesData.getTradeTimestamp())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime())
                    .append(COMMA)
                    .append(tradesData.getTradePrice())
                    .append(COMMA)
                    .append(tradesData.getTradeQuantity())
                    .append(COMMA)
                    .append(tradesData.getTradeQuantity())
                    .append(COMMA)
                    .append(tradesData.getSide().name())
                    .append(COMMA)
                    .append(tradesData.getTradeId())
                    .append(LINE_END);
              });

      try (FileWriter writer = new FileWriter(OUTPUT_FOLDER + tradesFileName)) {
        writer.write(sb.toString());
      } catch (IOException e) {
        log.error("Unable to write file {}", tradesFileName);
      }
    }
  }

  private void writeCandlestickFile(
      String candlestickFileName, CandleStickResponse candleStickResponseList) {

    StringBuilder sb = new StringBuilder().append(CANDLESTICK_HEADER).append(LINE_END);
    File file = new File(candlestickFileName);
    if (nonNull(candleStickResponseList.getResult().getData())) {
      candleStickResponseList.getResult().getData().stream()
          .forEach(
              candleStickData -> {
                sb.append(
                        Instant.ofEpochMilli(candleStickData.getEndTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime())
                    .append(COMMA)
                    .append(candleStickData.getOpen())
                    .append(COMMA)
                    .append(candleStickData.getClose())
                    .append(COMMA)
                    .append(candleStickData.getHigh())
                    .append(COMMA)
                    .append(candleStickData.getLow())
                    .append(COMMA)
                    .append(candleStickData.getVolume())
                    .append(LINE_END);
              });
      try (FileWriter writer = new FileWriter(OUTPUT_FOLDER + file)) {
        writer.write(sb.toString());
      } catch (IOException e) {
        log.error("Unable to write file {}", candlestickFileName);
      }
    }
  }

  public void writeIntegrityViolationsToFile(List<IntegrityViolation> integrityViolations) {
    LocalDateTime runDateTime = LocalDateTime.now();
    String fileTime = runDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    StringBuilder sb = new StringBuilder().append(SUMMARY_HEADER).append(LINE_END);
    File file = new File(SUMMARY_FILENAME + fileTime + CSV_EXTENSION);

    integrityViolations.stream()
        .forEach(
            integrityViolation -> {
              String candleStickCsv =
                  getCandleStickDataAsCsvString(
                      integrityViolation.getCandleStickTradeData().getCandlestick());
              sb.append(
                  getCsvTradeData(
                      integrityViolation.getCandleStickTradeData().getTrades(),
                      candleStickCsv,
                      integrityViolation));
            });
    try (FileWriter writer = new FileWriter(OUTPUT_FOLDER + file)) {
      writer.write(sb.toString());
    } catch (IOException e) {
      log.error("unable to write integrity violation summary file");
    }
  }

  private String getCsvTradeData(
      List<TradesData> trades, String candleStickCsv, IntegrityViolation integrityViolation) {
    //        TradeTimestamp,tradePrice,tradeQuantity,side,tradeId
    StringBuilder sb = new StringBuilder();
    trades.stream()
        .forEach(
            tradesData -> {
              sb.append(candleStickCsv)
                  .append(
                      Instant.ofEpochMilli(tradesData.getTradeTimestamp())
                          .atZone(ZoneId.systemDefault())
                          .toLocalDateTime())
                  .append(COMMA)
                  .append(tradesData.getTradeTimestamp())
                  .append(COMMA)
                  .append(tradesData.getTradePrice())
                  .append(COMMA)
                  .append(tradesData.getTradeQuantity())
                  .append(COMMA)
                  .append(tradesData.getSide())
                  .append(COMMA)
                  .append(tradesData.getTradeId())
                  .append(COMMA)
                  .append(integrityViolation.getRule())
                  .append(COMMA)
                  .append(integrityViolation.getTradesVolume())
                  .append(COMMA)
                  .append(integrityViolation.getTrade())
                  .append(LINE_END);
            });
    return sb.toString();
  }

  private String getCandleStickDataAsCsvString(CandleStickData candleStickData) {
    return new StringBuilder()
        .append(
            Instant.ofEpochMilli(candleStickData.getEndTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime())
        .append(COMMA)
        .append(candleStickData.getOpen())
        .append(COMMA)
        .append(candleStickData.getClose())
        .append(COMMA)
        .append(candleStickData.getHigh())
        .append(COMMA)
        .append(candleStickData.getLow())
        .append(COMMA)
        .append(candleStickData.getVolume())
        .append(COMMA)
        .toString();
  }
}
