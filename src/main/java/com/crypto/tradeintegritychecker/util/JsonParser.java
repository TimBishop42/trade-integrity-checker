package com.crypto.tradeintegritychecker.util;

import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickResponse;
import com.crypto.tradeintegritychecker.model.response.trades.GetTradesResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;

/** Class will be used in testing only */
public class JsonParser {
  ObjectMapper objectMapper = new ObjectMapper();

  public CandleStickResponse parseCandlestickFromFile() {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {

      ClassLoader classLoader = getClass().getClassLoader();
      File file = new File(classLoader.getResource("candlesticks.json").getFile());

      CandleStickResponse candlestick = objectMapper.readValue(file, CandleStickResponse.class);
      return candlestick;

    } catch (FileNotFoundException fe) {
      fe.printStackTrace();
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public GetTradesResponse parseTradesData() {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {

      ClassLoader classLoader = getClass().getClassLoader();
      File jsonFile = new File(classLoader.getResource("trades.json").getFile());

      GetTradesResponse trades = objectMapper.readValue(jsonFile, GetTradesResponse.class);
      return trades;

    } catch (FileNotFoundException fe) {
      fe.printStackTrace();
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
