package model;

import POS.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Barang {

    private int id;
    private String nama;
    private int kuantiti;
    private double harga;
    private int totalStok;

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }
    private int stok;

    public Barang() {
    }

    public Barang(int id, String nama, int kuantiti, double harga) {
        this.id = id;
        this.nama = nama;
        this.kuantiti = kuantiti;
        this.harga = harga;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public int getKuantiti() {
        return kuantiti;
    }

    public void setKuantiti(int kuantiti) {
        this.kuantiti = kuantiti;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public int getTotalStok() {
        return totalStok = stok * kuantiti;
    }

    public void setTotalStok(int totalStok) {
        this.totalStok = totalStok;
    }

    // Tambah
    public boolean insert() {
        String sql = "INSERT INTO barang (nama, kuantiti, harga) VALUES (?, ?, ?)";
        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, this.nama);
            stmt.setInt(2, this.kuantiti);
            stmt.setDouble(3, this.harga);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        this.id = keys.getInt(1);
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Perbarui
    public boolean update() {
        String sql = "UPDATE barang SET nama = ?, kuantiti = ?, harga = ? WHERE id = ?";
        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, this.nama);
            stmt.setInt(2, this.kuantiti);
            stmt.setDouble(3, this.harga);
            stmt.setInt(4, this.id);
            int rows = stmt.executeUpdate(); // executeUpdate untuk UPDATE
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Hapus
    public boolean delete() {
        String sql = "DELETE FROM barang WHERE id = ?";
        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.id);
            int rows = stmt.executeUpdate(); // executeUpdate untuk DELETE
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Mengambil seluruh data (SELECT *)
    public static List<Barang> getAll() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT * FROM barang";
        try (Connection conn = koneksi.BukaKoneksi(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) { // executeQuery untuk SELECT
            while (rs.next()) {
                Barang b = new Barang();
                b.setId(rs.getInt("id"));
                b.setNama(rs.getString("nama"));
                b.setKuantiti(rs.getInt("kuantiti"));
                b.setHarga(rs.getDouble("harga"));
                b.setStok(rs.getInt("stok"));
                b.setTotalStok(rs.getInt("total_stok"));
                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Mengambil seluruh data berdasarkan ID (SELECT WHERE)
    public static Barang getById(int id) {
        Barang b = null;
        String sql = "SELECT * FROM barang WHERE id = ?";
        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {  // pindah ke baris hasil pertama
                    b = new Barang();
                    b.setId(rs.getInt("id"));
                    b.setNama(rs.getString("nama"));
                    b.setKuantiti(rs.getInt("kuantiti"));
                    b.setHarga(rs.getDouble("harga"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return b;
    }
}
