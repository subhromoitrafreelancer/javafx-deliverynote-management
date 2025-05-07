package com.aarsoma.deliverynote.repository;

import com.aarsoma.deliverynote.config.DBConfig;
import com.aarsoma.deliverynote.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository {

    public List<Customer> findAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer customer = mapResultSetToCustomer(rs);
                customers.add(customer);
            }
        }

        return customers;
    }

    public Optional<Customer> findById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Customer customer = mapResultSetToCustomer(rs);
                return Optional.of(customer);
            }
        }

        return Optional.empty();
    }

    public Customer save(Customer customer) throws SQLException {
        // Use try-with-resources to ensure connection is closed even if exception occurs
        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Customer savedCustomer;

                if (customer.getId() == null) {
                    // Insert new customer
                    String sql = "INSERT INTO customers (name, address, contact_person, phone, email) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, customer.getName());
                        pstmt.setString(2, customer.getAddress());
                        pstmt.setString(3, customer.getContactPerson());
                        pstmt.setString(4, customer.getPhone());
                        pstmt.setString(5, customer.getEmail());

                        pstmt.executeUpdate();

                        ResultSet generatedKeys = pstmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            customer.setId(generatedKeys.getInt(1));
                        }
                    }
                } else {
                    // Update existing customer
                    String sql = "UPDATE customers SET name = ?, address = ?, contact_person = ?, phone = ?, email = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, customer.getName());
                        pstmt.setString(2, customer.getAddress());
                        pstmt.setString(3, customer.getContactPerson());
                        pstmt.setString(4, customer.getPhone());
                        pstmt.setString(5, customer.getEmail());
                        pstmt.setInt(6, customer.getId());

                        pstmt.executeUpdate();
                    }
                }

                conn.commit();

                // Get fresh data with updated timestamps
                Optional<Customer> refreshed = findById(customer.getId());
                return refreshed.orElse(customer);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                // Reset auto-commit mode
                if(!conn.isClosed()) conn.setAutoCommit(true);
            }
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Check if customer is referenced in delivery notes
                if (isReferencedInDeliveryNotes(id)) {
                    throw new SQLException("Cannot delete customer as it is referenced in one or more delivery notes");
                }

                // Delete customer
                String sql = "DELETE FROM customers WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Deleting customer failed, no rows affected.");
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                // Reset auto-commit mode
                if(!conn.isClosed()) conn.setAutoCommit(true);
            }
        }
    }

    public boolean isReferencedInDeliveryNotes(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM delivery_notes WHERE customer_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        }

        return false;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setName(rs.getString("name"));
        customer.setAddress(rs.getString("address"));
        customer.setContactPerson(rs.getString("contact_person"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            customer.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            customer.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return customer;
    }
}
