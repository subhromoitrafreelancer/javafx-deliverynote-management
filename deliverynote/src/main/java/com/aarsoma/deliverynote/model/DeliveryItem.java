package com.aarsoma.deliverynote.model;

import lombok.Data;

@Data
public class DeliveryItem {
    private Integer id;
    private Integer deliveryNoteId;
    private String itemName;
    private Integer orderedQty;
    private Integer deliveredQty;
    private Integer balanceQty;
}
