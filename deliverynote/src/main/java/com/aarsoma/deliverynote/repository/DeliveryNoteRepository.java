package com.aarsoma.deliverynote.repository;

import com.aarsoma.deliverynote.config.DBConfig;
import com.aarsoma.deliverynote.model.Customer;
import com.aarsoma.deliverynote.model.DeliveryItem;
import com.aarsoma.deliverynote.model.DeliveryNote;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryNoteRepository {

    private final CustomerRepository customerRepository = new CustomerRepository();
    private final DeliveryItemRepository itemRepository = new DeliveryItemRepository();

    public DeliveryNote save(DeliveryNote deliveryNote) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (deliveryNote.getId() == null) {
                    // Insert new delivery note
                    String sql = "INSERT INTO delivery_notes (note_number, customer_id, issue_date, financial_year) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, deliveryNote.getNoteNumber());
                        pstmt.setInt(2, deliveryNote.getCustomerId());
                        pstmt.setTimestamp(3, Timestamp.valueOf(deliveryNote.getIssueDate()));
                        pstmt.setString(4, deliveryNote.getFinancialYear());

                        pstmt.executeUpdate();

                        ResultSet generatedKeys = pstmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            deliveryNote.setId(generatedKeys.getInt(1));
                        }
                    }

                    // Save delivery items
                    if (deliveryNote.getItems() != null && !deliveryNote.getItems().isEmpty()) {
                        for (DeliveryItem item : deliveryNote.getItems()) {
                            item.setDeliveryNoteId(deliveryNote.getId());
                            itemRepository.save(item, conn);
                        }
                    }
                }

                conn.commit();
                return deliveryNote;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<DeliveryNote> findAll() throws SQLException {
        List<DeliveryNote> deliveryNotes = new ArrayList<>();
        String sql = "SELECT * FROM delivery_notes ORDER BY issue_date DESC";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                DeliveryNote note = mapResultSetToDeliveryNote(rs);

                // Load customer - we need to create a separate connection in customerRepository
                Optional<Customer> customer = customerRepository.findById(note.getCustomerId());
                customer.ifPresent(note::setCustomer);

                // Load items - separate connection in itemRepository
                List<DeliveryItem> items = itemRepository.findByDeliveryNoteId(note.getId());
                note.setItems(items);

                deliveryNotes.add(note);
            }
        }

        return deliveryNotes;
    }

    public Optional<DeliveryNote> findById(int id) throws SQLException {
        String sql = "SELECT * FROM delivery_notes WHERE id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    DeliveryNote note = mapResultSetToDeliveryNote(rs);

                    // Load customer
                    Optional<Customer> customer = customerRepository.findById(note.getCustomerId());
                    customer.ifPresent(note::setCustomer);

                    // Load items
                    List<DeliveryItem> items = itemRepository.findByDeliveryNoteId(note.getId());
                    note.setItems(items);

                    return Optional.of(note);
                }
            }
        }

        return Optional.empty();
    }

    public List<DeliveryNote> findByCustomerId(int customerId) throws SQLException {
        List<DeliveryNote> deliveryNotes = new ArrayList<>();
        String sql = "SELECT * FROM delivery_notes WHERE customer_id = ? ORDER BY issue_date DESC";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DeliveryNote note = mapResultSetToDeliveryNote(rs);

                    // Load customer
                    Optional<Customer> customer = customerRepository.findById(note.getCustomerId());
                    customer.ifPresent(note::setCustomer);

                    // Load items
                    List<DeliveryItem> items = itemRepository.findByDeliveryNoteId(note.getId());
                    note.setItems(items);

                    deliveryNotes.add(note);
                }
            }
        }

        return deliveryNotes;
    }

    public List<DeliveryNote> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<DeliveryNote> deliveryNotes = new ArrayList<>();
        String sql = "SELECT * FROM delivery_notes WHERE CAST(issue_date AS DATE) BETWEEN ? AND ? ORDER BY issue_date DESC";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DeliveryNote note = mapResultSetToDeliveryNote(rs);

                    // Load customer
                    Optional<Customer> customer = customerRepository.findById(note.getCustomerId());
                    customer.ifPresent(note::setCustomer);

                    // Load items
                    List<DeliveryItem> items = itemRepository.findByDeliveryNoteId(note.getId());
                    note.setItems(items);

                    deliveryNotes.add(note);
                }
            }
        }

        return deliveryNotes;
    }

    public List<DeliveryNote> findByDate(LocalDate date) throws SQLException {
        return findByDateRange(date, date);
    }

    public String generateNextDeliveryNoteNumber() throws SQLException {
        // Get the current year for the prefix
        int currentYear = LocalDate.now().getYear();
        String prefix = "DN" + currentYear + "-";

        // Find the last number used
        String sql = "SELECT MAX(note_number) FROM delivery_notes WHERE note_number LIKE ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, prefix + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) {
                    String lastNumber = rs.getString(1);
                    // Extract the numeric part
                    String numericPart = lastNumber.substring(prefix.length());
                    // Increment
                    int nextNumeric = Integer.parseInt(numericPart) + 1;
                    // Format with leading zeros (4 digits)
                    return prefix + String.format("%04d", nextNumeric);
                } else {
                    // First note of the year
                    return prefix + "0001";
                }
            }
        }
    }

    private DeliveryNote mapResultSetToDeliveryNote(ResultSet rs) throws SQLException {
        DeliveryNote note = new DeliveryNote();
        note.setId(rs.getInt("id"));
        note.setNoteNumber(rs.getString("note_number"));
        note.setCustomerId(rs.getInt("customer_id"));
        note.setIssueDate(rs.getTimestamp("issue_date").toLocalDateTime());
        note.setFinancialYear(rs.getString("financial_year"));
        note.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return note;
    }
}
