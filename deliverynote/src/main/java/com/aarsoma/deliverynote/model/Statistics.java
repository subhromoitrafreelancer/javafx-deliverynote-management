package com.aarsoma.deliverynote.model;

import lombok.Data;

@Data
public class Statistics {
    private long totalDeliveryNotes;
    private long financialYearDeliveryNotes;
    private long monthlyDeliveryNotes;
    private long weeklyDeliveryNotes;
    private long dailyDeliveryNotes;
}
