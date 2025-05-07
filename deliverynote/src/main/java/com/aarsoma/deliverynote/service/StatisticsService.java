package com.aarsoma.deliverynote.service;

import com.aarsoma.deliverynote.config.DBConfig;
import com.aarsoma.deliverynote.model.FinancialYear;
import com.aarsoma.deliverynote.model.Statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public class StatisticsService {

    public Statistics getDeliveryNoteStatistics() throws SQLException {
        Statistics statistics = new Statistics();

        // Get total delivery notes
        statistics.setTotalDeliveryNotes(getTotalDeliveryNotes());

        // Get financial year delivery notes
        statistics.setFinancialYearDeliveryNotes(getFinancialYearDeliveryNotes());

        // Get monthly delivery notes
        statistics.setMonthlyDeliveryNotes(getMonthlyDeliveryNotes());

        // Get weekly delivery notes
        statistics.setWeeklyDeliveryNotes(getWeeklyDeliveryNotes());

        // Get daily delivery notes
        statistics.setDailyDeliveryNotes(getDailyDeliveryNotes());

        return statistics;
    }

    private long getTotalDeliveryNotes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM delivery_notes";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        }

        return 0;
    }

    private long getFinancialYearDeliveryNotes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM delivery_notes WHERE CAST(issue_date AS DATE) BETWEEN ? AND ?";

        String fyStartDate = FinancialYear.getFinancialYearStartDate().toString();
        String fyEndDate = FinancialYear.getFinancialYearEndDate().toString();

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fyStartDate);
            pstmt.setString(2, fyEndDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return 0;
    }

    private long getMonthlyDeliveryNotes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM delivery_notes WHERE CAST(issue_date AS DATE) BETWEEN ? AND ?";

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstDayOfMonth.toString());
            pstmt.setString(2, lastDayOfMonth.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return 0;
    }

    private long getWeeklyDeliveryNotes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM delivery_notes WHERE CAST(issue_date AS DATE) BETWEEN ? AND ?";

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastDayOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstDayOfWeek.toString());
            pstmt.setString(2, lastDayOfWeek.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return 0;
    }

    private long getDailyDeliveryNotes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM delivery_notes WHERE CAST(issue_date AS DATE) = ?";

        LocalDate today = LocalDate.now();

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, today.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return 0;
    }
}
