package com.crypto.tradeintegritychecker.model.response.candelstick;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CandleStickResult {

    @JsonProperty("instrument_name")
    private String instrumentName;
    private int depth;
    private String interval;
    @JsonProperty("data")
    private List<CandleStickData> data;

}
