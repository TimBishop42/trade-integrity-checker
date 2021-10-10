package com.crypto.tradeintegritychecker.model.response.candelstick;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CandleStickData {

    @JsonProperty("t")
    private Long endTime;

    @JsonProperty("o")
    private BigDecimal open;

    @JsonProperty("h")
    private BigDecimal high;

    @JsonProperty("l")
    private BigDecimal low;

    @JsonProperty("c")
    private BigDecimal close;

    @JsonProperty("v")
    private BigDecimal volume;
}
