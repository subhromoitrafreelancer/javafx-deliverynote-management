package com.aarsoma.deliverynote.service;

import com.aarsoma.deliverynote.model.DeliveryNote;
import com.aarsoma.deliverynote.repository.DeliveryNoteRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DeliveryNoteService {

    private final DeliveryNoteRepository deliveryNoteRepository = new DeliveryNoteRepository();

    public DeliveryNote saveDeliveryNote(DeliveryNote deliveryNote) throws SQLException {
        return deliveryNoteRepository.save(deliveryNote);
    }

    public List<DeliveryNote> getAllDeliveryNotes() throws SQLException {
        return deliveryNoteRepository.findAll();
    }

    public Optional<DeliveryNote> getDeliveryNoteById(int id) throws SQLException {
        return deliveryNoteRepository.findById(id);
    }

    public List<DeliveryNote> getDeliveryNotesByCustomer(int customerId) throws SQLException {
        return deliveryNoteRepository.findByCustomerId(customerId);
    }

    public List<DeliveryNote> getDeliveryNotesByDate(LocalDate date) throws SQLException {
        return deliveryNoteRepository.findByDate(date);
    }

    public List<DeliveryNote> getDeliveryNotesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return deliveryNoteRepository.findByDateRange(startDate, endDate);
    }

    public String generateNextDeliveryNoteNumber() throws SQLException {
        return deliveryNoteRepository.generateNextDeliveryNoteNumber();
    }
}
