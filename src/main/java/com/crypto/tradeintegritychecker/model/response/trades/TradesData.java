package com.crypto.tradeintegritychecker.model.response.trades;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradesData {

    private Long dataTime;

    @JsonProperty("p")
    private BigDecimal tradePrice;

    @JsonProperty("q")
    private BigDecimal tradeQuantity;

    //BUY/SELL
    @JsonProperty("s")
    private Side side;

    @JsonProperty("d")
    private Long tradeId;

    @JsonProperty("t")
    private Long tradeTimestamp;
}
