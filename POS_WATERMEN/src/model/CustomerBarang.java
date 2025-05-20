package model;

import POS.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerBarang {

    private int id;
    private String nama;
    private String noHp;
    private String alamat;

    public CustomerBarang() {
    }

    public CustomerBarang(int id, String nama, String noHp, String alamat) {
        this.id = id;
        this.nama = nama;
        this.noHp = noHp;
        this.alamat = alamat;
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

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    // Tambah
    public boolean insert() {
        String sql = "INSERT INTO customer_barang (nama, no_hp, alamat) VALUES (?, ?, ?)";
        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, this.nama);
            stmt.setString(2, this.noHp);
            stmt.setString(3, this.alamat);
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
        String sql = "UPDATE customer_barang SET nama = ?, no_hp = ?, alamat = ? WHERE id = ?";
        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, this.nama);
            stmt.setString(2, this.noHp);
            stmt.setString(3, this.alamat);
            stmt.setInt(4, this.id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Hapus
    public boolean delete() {
        String sql = "DELETE FROM customer_barang WHERE id = ?";
        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Mengambil seluruh data customer barang
    public static List<CustomerBarang> getAll() {
        List<CustomerBarang> list = new ArrayList<>();
        String sql = "SELECT * FROM customer_barang";
        try (Connection conn = koneksi.BukaKoneksi(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                CustomerBarang c = new CustomerBarang();
                c.setId(rs.getInt("id"));
                c.setNama(rs.getString("nama"));
                c.setNoHp(rs.getString("no_hp"));
                c.setAlamat(rs.getString("alamat"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Mengambil Data Customer Barang Berdasarkan ID
    public static CustomerBarang getById(int id) {
        CustomerBarang c = null;
        String sql = "SELECT * FROM customer_barang WHERE id = ?";
        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    c = new CustomerBarang();
                    c.setId(rs.getInt("id"));
                    c.setNama(rs.getString("nama"));
                    c.setNoHp(rs.getString("no_hp"));
                    c.setAlamat(rs.getString("alamat"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c;
    }
}
