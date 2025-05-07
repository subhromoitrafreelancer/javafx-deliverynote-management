package com.aarsoma.deliverynote.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DeliveryNote {
    private Integer id;
    private String noteNumber;
    private Integer customerId;
    private Customer customer;
    private LocalDateTime issueDate;
    private String financialYear;
    private LocalDateTime createdAt;
    private List<DeliveryItem> items = new ArrayList<>();
}
