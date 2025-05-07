package com.aarsoma.deliverynote.repository;

import com.aarsoma.deliverynote.config.DBConfig;
import com.aarsoma.deliverynote.model.DeliveryItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryItemRepository {

    public DeliveryItem save(DeliveryItem item, Connection conn) throws SQLException {
        String sql = "INSERT INTO delivery_items (delivery_note_id, item_name, ordered_qty, delivered_qty, balance_qty) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, item.getDeliveryNoteId());
            pstmt.setString(2, item.getItemName());
            pstmt.setInt(3, item.getOrderedQty());
            pstmt.setInt(4, item.getDeliveredQty());
            pstmt.setInt(5, item.getBalanceQty());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                }
            }
        }

        return item;
    }

    public List<DeliveryItem> findByDeliveryNoteId(int deliveryNoteId) throws SQLException {
        List<DeliveryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM delivery_items WHERE delivery_note_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, deliveryNoteId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DeliveryItem item = mapResultSetToDeliveryItem(rs);
                    items.add(item);
                }
            }
        }

        return items;
    }

    private DeliveryItem mapResultSetToDeliveryItem(ResultSet rs) throws SQLException {
        DeliveryItem item = new DeliveryItem();
        item.setId(rs.getInt("id"));
        item.setDeliveryNoteId(rs.getInt("delivery_note_id"));
        item.setItemName(rs.getString("item_name"));
        item.setOrderedQty(rs.getInt("ordered_qty"));
        item.setDeliveredQty(rs.getInt("delivered_qty"));
        item.setBalanceQty(rs.getInt("balance_qty"));
        return item;
    }
}
