package com.example.cashdocumentsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(
        name = "CashOperationsDetails",
        description = "Schema to hold Cash Operations, Cash Reporting and Cash Documents information."
)
public class CashOperationsDetails {

    @Schema(
            description = "Cash balances in EUR and BGN for all cashiers"
    )
    private List<CashBalanceResponse> cashBalanceResponses;



    private String date;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal endOfDayBalance;
    private Map<String, BigDecimal> currencyBreakdown;



    @Schema(
            description = "All files metadata"
    )
    private List<FileDto> filesDtos;
}
