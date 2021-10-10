package com.crypto.tradeintegritychecker.model.integrity;

import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickData;
import com.crypto.tradeintegritychecker.model.response.trades.TradesData;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Builder
public class IntegrityViolation {

    private CandlestickTradeData candleStickTradeData;
    private String rule;
    private TradesData trade;
    private BigDecimal tradesVolume;
}
