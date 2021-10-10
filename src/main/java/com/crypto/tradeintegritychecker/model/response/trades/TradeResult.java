package com.crypto.tradeintegritychecker.model.response.trades;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TradeResult {
    @JsonProperty("instrument_name")
    private String instrumentName;
    List<TradesData> data;
}
