package model;

import POS.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransaksiBarang {

    private int id;
    private int idCustomer;
    private int jumlah;
    private int hargaSatuan;
    private Date tanggal;
    private double total;
    private String kode_barang;

    public String getKode_barang() {
        return kode_barang;
    }

    public void setKode_barang(String kode_barang) {
        this.kode_barang = kode_barang;
    }

    public int getId_kurir() {
        return id_kurir;
    }

    public void setId_kurir(int id_kurir) {
        this.id_kurir = id_kurir;
    }
    private int id_kurir;

    public TransaksiBarang() {
    }

    public TransaksiBarang(int id, int idCustomer, int jumlah, int hargaSatuan, Date tanggal, int total) {
        this.id = id;
        this.idCustomer = idCustomer;
        this.jumlah = jumlah;
        this.hargaSatuan = hargaSatuan;
        this.tanggal = tanggal;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(int idCustomer) {
        this.idCustomer = idCustomer;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public int getHargaSatuan() {
        return hargaSatuan;
    }

    public void setHargaSatuan(int hargaSatuan) {
        this.hargaSatuan = hargaSatuan;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public static boolean insertTransaksi(TransaksiBarang trx) {
        String insertSQL = "INSERT INTO transaksi_barang "
                + "(id_customer, jumlah, harga_satuan, tanggal, total, kode_barang, id_kurir) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = koneksi.BukaKoneksi();
            conn.setAutoCommit(false);

            // Debug: Print data yang akan diinsert
            System.out.println("=== DEBUG INSERT TRANSAKSI ===");
            System.out.println("ID Customer: " + trx.getIdCustomer());
            System.out.println("Jumlah: " + trx.getJumlah());
            System.out.println("Harga Satuan: " + trx.getHargaSatuan());
            System.out.println("Tanggal: " + trx.getTanggal());
            System.out.println("Total: " + trx.getTotal());
            System.out.println("Kode Barang: " + trx.getKode_barang());
            System.out.println("ID Kurir: " + trx.getId_kurir());
            System.out.println("=============================");

            // INSERT transaksi_barang
            PreparedStatement stmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, trx.getIdCustomer());
            stmt.setInt(2, trx.getJumlah());
            stmt.setInt(3, trx.getHargaSatuan());
            stmt.setDate(4, trx.getTanggal());
            stmt.setDouble(5, trx.getTotal());
            stmt.setString(6, trx.getKode_barang());
            stmt.setInt(7, trx.getId_kurir());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Dapatkan generated ID
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        trx.setId(rs.getInt(1));
                        System.out.println("Transaksi berhasil disimpan dengan ID: " + rs.getInt(1));
                    }
                }

                conn.commit();
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error saat insert transaksi:");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();

            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rollback successful");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    public static List<TransaksiBarang> getAll() {
        List<TransaksiBarang> list = new ArrayList<>();
        String sql = "SELECT * FROM transaksi_barang";

        try (Connection conn = koneksi.BukaKoneksi(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TransaksiBarang t = new TransaksiBarang();
                t.setId(rs.getInt("id"));
                t.setIdCustomer(rs.getInt("id_customer"));
                t.setJumlah(rs.getInt("jumlah"));
                t.setHargaSatuan(rs.getInt("harga_satuan"));
                t.setTanggal(rs.getDate("tanggal"));
                t.setTotal(rs.getInt("total"));
                t.setKode_barang(rs.getString("kode_barang"));
                t.setId_kurir(rs.getInt("id_kurir"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static TransaksiBarang getById(int id) {
        TransaksiBarang t = null;
        String sql = "SELECT * FROM transaksi_barang WHERE id = ?";

        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    t = new TransaksiBarang();
                    t.setId(rs.getInt("id"));
                    t.setIdCustomer(rs.getInt("id_customer"));
                    t.setJumlah(rs.getInt("jumlah"));
                    t.setHargaSatuan(rs.getInt("harga_satuan"));
                    t.setTanggal(rs.getDate("tanggal"));
                    t.setTotal(rs.getInt("total"));
                    t.setKode_barang(rs.getString("kode_barang"));
                    t.setId_kurir(rs.getInt("id_kurir"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static List<Map<String, Object>> getLaporanTransaksi() {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT tb.id, tb.id_customer, c.nama AS nama_pelanggan, "
                + "o.harga_satuan, tb.jumlah, tb.total, tb.tanggal, "
                + "o.nama_barang, "
                + "k.nama_kurir, k.jenis_kendaraan, k.plat_nomor, k.status AS status_kurir "
                + "FROM transaksi_barang tb "
                + "LEFT JOIN customer c ON tb.id_customer = c.id "
                + "LEFT JOIN operasional o ON tb.kode_barang = o.kode_barang "
                + "LEFT JOIN kurir k ON tb.id_kurir = k.id_kurir "
                + "ORDER BY tb.tanggal DESC";

        System.out.println("Executing query: " + sql);

        try (Connection conn = koneksi.BukaKoneksi(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("ID Transaksi", rs.getInt("id"));
                row.put("ID Customer", rs.getInt("id_customer"));
                row.put("Nama Pelanggan", rs.getString("nama_pelanggan"));
                row.put("Harga Satuan", rs.getInt("harga_satuan"));
                row.put("Jumlah", rs.getInt("jumlah"));
                row.put("Total", rs.getInt("total"));
                row.put("Tanggal", rs.getDate("tanggal"));
                row.put("Nama Barang", rs.getString("nama_barang"));
                row.put("Nama Kurir", rs.getString("nama_kurir"));
                row.put("Jenis Kendaraan", rs.getString("jenis_kendaraan"));
                row.put("Plat Nomor", rs.getString("plat_nomor"));
                row.put("Status Kurir", rs.getString("status_kurir"));

                list.add(row);
                System.out.println("Row " + rowCount + ": " + row);
            }

            System.out.println("Total rows fetched: " + rowCount);
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public static boolean updateTransaksi(Map<String, Object> data) {
        String sql = "UPDATE transaksi_barang SET "
                + "id_customer = ?, "
                + "jumlah = ?, "
                + "total = ? "
                + "WHERE id = ?";

        Connection conn = null;

        try {
            conn = koneksi.BukaKoneksi();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, (int) data.get("ID Customer"));
            stmt.setInt(2, (int) data.get("Jumlah"));
            stmt.setDouble(3, (double) data.get("Total"));
            stmt.setInt(4, (int) data.get("ID Transaksi"));

            int affectedRows = stmt.executeUpdate();
            conn.commit();

            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean deleteTransaksi(int idTransaksi) {
        String sql = "DELETE FROM transaksi_barang WHERE id = ?";
        Connection conn = null;

        try {
            conn = koneksi.BukaKoneksi();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idTransaksi);

            int affectedRows = stmt.executeUpdate();
            conn.commit();

            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
