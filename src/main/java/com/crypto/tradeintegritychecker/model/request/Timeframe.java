package com.crypto.tradeintegritychecker.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
public enum Timeframe {
    ONE_MINUTE("1m", 60000L),
    FIVE_MINUTES("5m", 300000L),
    FIFTEEN_MINUTES("15m", 900000L),
    THIRTY_MINUTES("30m", 1800000L),
    ONE_HOUR("1h", 3600000L),
    FOUR_HOURS("4h", 14400000L),
    SIX_HOURS("6h", 21600000L),
    TWELVE_HOURS("12h", 43200000L),
    ONE_DAY("1D", 86400000L),
    ONE_WEEK("7D", 604800000L),
    TWO_WEEKS("14D", 1209600000L),
    ONE_MONTH("1M", 2629800000L);

    @Getter
    private final String timeframeString;

    @Getter
    private final Long timeframeMillis;

    //Find the matching TimeFrame enum, given a 1m/5m etc string input
    //Will always have a string match present, so no isPresent check is needed
    public static Timeframe getTimeframeFromString(String timeframeString) {
        return Arrays.stream(Timeframe.values())
                .filter(timeframe -> timeframe.getTimeframeString().equals(timeframeString))
                .findFirst().get();
    }
}
