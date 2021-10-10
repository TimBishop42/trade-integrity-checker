package com.crypto.tradeintegritychecker.model.integrity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Pojo to hold data for the results of the trade/candlestick integrity analysis
 * Primary use case is for returning data to the user when calling the API
 */
@Data
@Builder
public class IntegritySummary {

    private int numIntegrityBreaks;
    private int numTrades;
    private int numCandlesticks;
    private int numCandlesticksAnalyzed;
    private List<IntegrityViolation> dataIntegrityBreaks;

    public boolean getIntegrityBreakStatus() {
        if(dataIntegrityBreaks.size() > 0) {
            return true;
        }
        else {
            return false;
        }
    }
}
