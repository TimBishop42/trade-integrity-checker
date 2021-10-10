package com.crypto.tradeintegritychecker.model.response.trades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetTradesResponse {

    private TradeResult result;
}
