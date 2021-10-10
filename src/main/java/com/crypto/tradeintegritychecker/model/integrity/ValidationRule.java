package com.crypto.tradeintegritychecker.model.integrity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ValidationRule {

    OPEN("Price of the open trade was not equal to the open price of the candle"),
    CLOSE("Price of the close trade was not equal to the close price of the candle"),
    HIGH("Highest Price of all trades was not equal to the high price of the candle"),
    LOW("Lowest Price of all trades was not equal to the lowest price of the candle"),
    VOLUME("Total volume of trades was not equal to the volume shown in the candle"),
    NO_TRADE_OPEN_CLOSE("Open price was not equal to CLose price, on a candlestick where there was no trading activity"),
    NO_TRADE_VOLUME("Volume reported on a candlestick where there was no trades logged");

    @Getter
    private final String ruleBreak;
}
