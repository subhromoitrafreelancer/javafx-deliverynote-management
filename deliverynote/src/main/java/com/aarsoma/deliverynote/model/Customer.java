package com.aarsoma.deliverynote.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Customer {
    private Integer id;
    private String name;
    private String address;
    private String contactPerson;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return name;
    }
}
