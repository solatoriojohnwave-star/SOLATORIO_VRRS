package com.scc.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {

    private final ActivityLogDao logDao = new ActivityLogDao();

    public record RoomType(int id, String typeName, double ratePerHour, int capacity) {}
    public record Room(int id, String roomName, String typeName, double ratePerHour, int capacity, String status) {}

    public List<RoomType> getAllRoomTypes() {
        List<RoomType> types = new ArrayList<>();
        String sql = "SELECT id, type_name, rate_per_hour, capacity FROM room_types ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                types.add(new RoomType(rs.getInt("id"), rs.getString("type_name"), rs.getDouble("rate_per_hour"), rs.getInt("capacity")));
            }
        } catch (SQLException e) {
            System.out.println("Failed to load room types: " + e.getMessage());
        }
        return types;
    }

    public boolean addRoomType(int adminId, String typeName, double rate, int capacity) {
        String sql = "INSERT INTO room_types (type_name, rate_per_hour, capacity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, typeName);
            ps.setDouble(2, rate);
            ps.setInt(3, capacity);
            ps.executeUpdate();
            logDao.logAction(adminId, "ROOM_TYPE_CREATED", "Added room type: " + typeName + " (Rate: " + rate + ", Capacity: " + capacity + ")");
            return true;
        } catch (SQLException e) {
            System.out.println("Failed to add room type: " + e.getMessage());
            return false;
        }
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = """
            SELECT r.id, r.room_name, rt.type_name, rt.rate_per_hour, rt.capacity, r.status 
            FROM rooms r 
            JOIN room_types rt ON r.room_type_id = rt.id 
            ORDER BY r.id
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("id"),
                        rs.getString("room_name"),
                        rs.getString("type_name"),
                        rs.getDouble("rate_per_hour"),
                        rs.getInt("capacity"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Failed to load rooms: " + e.getMessage());
        }
        return rooms;
    }

    public boolean addRoom(int adminId, String roomName, int roomTypeId) {
        String sql = "INSERT INTO rooms (room_name, room_type_id, status) VALUES (?, ?, 'AVAILABLE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomName);
            ps.setInt(2, roomTypeId);
            ps.executeUpdate();
            logDao.logAction(adminId, "ROOM_CREATED", "Added videoke room: " + roomName);
            return true;
        } catch (SQLException e) {
            System.out.println("Failed to add room: " + e.getMessage());
            return false;
        }
    }

    public boolean createReservation(int userId, int roomId, int customerId, String startTime, String endTime, double totalAmount) {
        String sql = "INSERT INTO reservations (customer_id, room_id, created_by, start_time, end_time, total_amount, status) VALUES (?, ?, ?, ?, ?, ?, 'RESERVED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, roomId);
            ps.setInt(3, userId);
            ps.setString(4, startTime);
            ps.setString(5, endTime);
            ps.setDouble(6, totalAmount);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logDao.logAction(userId, "RESERVATION_CREATED", "Created reservation for Room ID " + roomId + " - Total: P" + totalAmount);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to create reservation: " + e.getMessage());
        }
        return false;
    }

    public boolean updateRoomStatus(int adminId, int roomId, String newStatus) {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.toUpperCase());
            ps.setInt(2, roomId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logDao.logAction(adminId, "ROOM_STATUS_UPDATED", "Updated Room ID " + roomId + " status to " + newStatus.toUpperCase());
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to update room status: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteRoom(int adminId, int roomId) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logDao.logAction(adminId, "ROOM_DELETED", "Deleted Room ID " + roomId);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to delete room (may have active reservations): " + e.getMessage());
        }
        return false;
    }
}