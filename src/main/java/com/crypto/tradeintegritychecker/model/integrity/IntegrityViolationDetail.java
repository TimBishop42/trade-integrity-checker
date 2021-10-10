package com.crypto.tradeintegritychecker.model.integrity;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class IntegrityViolationDetail {

    private List<IntegrityViolation> integrityViolations;
    private int numCandlesticksAnalyzed;
}
