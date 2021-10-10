package com.crypto.tradeintegritychecker.model.integrity;

import com.crypto.tradeintegritychecker.model.request.Timeframe;
import com.crypto.tradeintegritychecker.model.response.candelstick.CandleStickData;
import com.crypto.tradeintegritychecker.model.response.trades.TradesData;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CandlestickTradeData {

    private String instrument;
    private Timeframe timeframe;
    private CandleStickData candlestick;
    private Long endTime;
    private List<TradesData> trades;

}
