package view;

import POS.koneksi;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import model.CustomerBarang;
import model.TransaksiBarang;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class PenjualanBarang extends javax.swing.JPanel {

    public Statement st;
    public ResultSet rs;
    private final Connection cn = (Connection) koneksi.BukaKoneksi();
    Calendar kalender = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String formattedDate = sdf.format(kalender.getTime());
    java.sql.Date tanggal = java.sql.Date.valueOf(formattedDate);

    public PenjualanBarang() throws SQLException {
        initComponents();
        setOpaque(true);
        dataBarang();
        dataKurir();
        dataCustomerBarang();
        dataTransaksiLengkap();
    }

    private void clear() throws SQLException {
        txHargaBarang.setText("");
        txIdCustomer.setText("");
        txIdTransaksi.setText("");
        txJumlahBarang.setText("");
        txKodeBarang.setText("");
        txNamaBarang.setText("");
        txNamaCustomerBarang.setText("");
        txTotal.setText("");
        txtCariKurir.setText("");
        txIdKurir.setText("");
        txtNamaKurirPengantar.setText("");
        cbbKendaraanKurir.setSelectedIndex(3);
        txtPlatNomorKurir.setText("");
        txtcaricustomer2.setText("");
        lblTotalHarga.setText("Rp.0");
        dataBarang();
        dataKurir();
        dataCustomerBarang();
        dataTransaksiLengkap();
    }

    public static void debugDataTransaksi() {
        String sql = "SELECT t.*, cb.nama AS nama_pelanggan, k.nama_kurir "
                + "FROM transaksi_barang t "
                + "LEFT JOIN customer_barang cb ON t.id_customer = cb.id "
                + "LEFT JOIN kurir k ON t.id_kurir = k.id_kurir "
                + "LIMIT 5";

        try (Connection conn = koneksi.BukaKoneksi(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Debug Data Transaksi:");
            while (rs.next()) {
                System.out.println(
                        "ID: " + rs.getInt("id")
                        + ", Customer: " + rs.getString("nama_pelanggan")
                        + ", Total: " + rs.getInt("total")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void dataTransaksiLengkap() {
        try {
            String[] kolom = {"ID Transaksi", "ID Customer", "Nama Pelanggan", "Nama Barang",
                "Harga Satuan", "Jumlah", "Total", "Nama Kurir", "Kendaraan", "Plat Nomor"};
            DefaultTableModel model = new DefaultTableModel(kolom, 0);
            tabelTransaksi.setModel(model);
            String sql = "SELECT DISTINCT transaksi_barang.id AS id_transaksi, transaksi_barang.id_customer, "
                    + "customer_barang.nama AS nama_pelanggan, operasional.nama_barang, "
                    + "operasional.harga_satuan AS harga, transaksi_barang.jumlah, transaksi_barang.total, "
                    + "kurir.nama_kurir, kurir.jenis_kendaraan, kurir.plat_nomor "
                    + "FROM transaksi_barang "
                    + "JOIN customer_barang ON transaksi_barang.id_customer = customer_barang.id "
                    + "JOIN operasional ON transaksi_barang.kode_barang = operasional.kode_barang "
                    + "JOIN kurir ON transaksi_barang.id_kurir = kurir.id_kurir";

            try (Statement stmt = cn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

                // Kosongkan model terlebih dahulu
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("id_transaksi"),
                        rs.getString("id_customer"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("nama_barang"),
                        rs.getInt("harga"),
                        rs.getInt("jumlah"),
                        rs.getInt("total"),
                        rs.getString("nama_kurir"),
                        rs.getString("jenis_kendaraan"),
                        rs.getString("plat_nomor")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editDataTransaksi() {
        int selectedRow = tabelTransaksi.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih baris data yang akan diedit",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idTransaksi = Integer.parseInt(tabelTransaksi.getValueAt(selectedRow, 0).toString());
            int idCustomer = Integer.parseInt(tabelTransaksi.getValueAt(selectedRow, 1).toString());
            int currentHarga = Integer.parseInt(tabelTransaksi.getValueAt(selectedRow, 4).toString()); // Harga tetap
            int currentJumlah = Integer.parseInt(tabelTransaksi.getValueAt(selectedRow, 5).toString());

            // Input untuk jumlah baru
            String newJumlahStr = JOptionPane.showInputDialog(this,
                    "Masukkan Jumlah Baru:",
                    currentJumlah);
            if (newJumlahStr == null) {
                return; // Jika user cancel
            }

            try {
                int newJumlah = Integer.parseInt(newJumlahStr);
                double newTotal = currentHarga * newJumlah;

                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("ID Transaksi", idTransaksi);
                updatedData.put("ID Customer", idCustomer);
                updatedData.put("Jumlah", newJumlah);
                updatedData.put("Total", newTotal);

                boolean success = TransaksiBarang.updateTransaksi(updatedData);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Jumlah berhasil diupdate",
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    dataTransaksiLengkap(); // Refresh tabel
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Gagal mengupdate jumlah",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Input jumlah harus berupa angka valid",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double hitungTotalHargaBarang() {
        double hargaBarang = Double.parseDouble(txHargaBarang.getText());
        int jumlahBarang = Integer.parseInt(txJumlahBarang.getText());
        if (jumlahBarang != 0) {
            double total = hargaBarang * jumlahBarang;
            txTotal.setText("" + total);
        }
        return 0;
    }

    private void dataCustomerBarang() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Nama Pelanggan");
        model.addColumn("No. Hp");
        model.addColumn("Alamat");
        tblPelangganBarang1.setModel(model);
        List<CustomerBarang> pelangganBarang = new CustomerBarang().getAll();
        for (CustomerBarang cus : pelangganBarang) {
            model.addRow(new Object[]{
                cus.getId(),
                cus.getNama(),
                cus.getNoHp(),
                cus.getAlamat()
            });
        }
    }

    public void dataBarang() throws SQLException {
        st = cn.createStatement();
//        rs = st.executeQuery("SELECT * FROM barang");
        rs = st.executeQuery("SELECT "
                + "    kode_barang, "
                + "    nama_barang, "
                + "    harga_satuan, "
                + "    SUM(jumlah) AS total_jumlah "
                + "FROM "
                + "    operasional "
                + "GROUP BY "
                + "    kode_barang, nama_barang, harga_satuan "
                + "ORDER BY "
                + "    kode_barang "
                + "LIMIT 5;");

        // Tabel 1 data operasional
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Kode Barang");
        model.addColumn("Nama Barang");
        model.addColumn("Harga Satuan");
        model.addColumn("Total Jumlah");
        tabelBarang.setModel(model);

        while (rs.next()) {
            Object[] data
                    = {
                        rs.getString("kode_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("harga_satuan"),
                        rs.getString("total_jumlah"),};
            model.addRow(data);
        }
    }

    private void dataKurir() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Nama");
        model.addColumn("No HP");
        model.addColumn("Alamat");
        model.addColumn("Kendaraan");
        model.addColumn("Plat");
        model.addColumn("Status");

        try {
            String sql = "SELECT * FROM kurir";
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_kurir"),
                    rs.getString("nama_kurir"),
                    rs.getString("no_hp"),
                    rs.getString("alamat"),
                    rs.getString("jenis_kendaraan"),
                    rs.getString("plat_nomor"),
                    rs.getString("status")
                });
            }

            tabelKurir.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void kurangiStokBarangTerjual(String kodeBarang, int jumlah) {
        String sql = "INSERT INTO operasional (kode_barang, nama_barang, jumlah, harga_satuan, total, tanggal) "
                + "SELECT ?, nama_barang, ?, harga_satuan, ? * harga_satuan, CURDATE() "
                + "FROM operasional WHERE kode_barang = ? ORDER BY tanggal DESC LIMIT 1";

        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, kodeBarang);
            stmt.setInt(2, -jumlah);
            stmt.setInt(3, jumlah);
            stmt.setString(4, kodeBarang);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal update stok: " + e.getMessage());
        }
    }

    private boolean cekStokTersedia(String kodeBarang, int jumlah) throws SQLException {
        String sql = "SELECT SUM(jumlah) as stok FROM operasional WHERE kode_barang = ?";

        try (Connection conn = koneksi.BukaKoneksi(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, kodeBarang);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int stok = rs.getInt("stok");
                    return stok >= jumlah;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblPelangganBarang1 = new javax.swing.JTable();
        cmbCariPelangganBarang = new javax.swing.JComboBox<>();
        txtcaricustomer2 = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        txIdTransaksi = new javax.swing.JTextField();
        txIdCustomer = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        txNamaCustomerBarang = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        txNamaBarang = new javax.swing.JTextField();
        jPanel27 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        tabelKurir = new javax.swing.JTable();
        cbbCariKurir = new javax.swing.JComboBox<>();
        txtCariKurir = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelBarang = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelTransaksi = new javax.swing.JTable();
        lblTotalHarga = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        txIdKurir = new javax.swing.JTextField();
        cbbKendaraanKurir = new javax.swing.JComboBox<>();
        jLabel75 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        txtPlatNomorKurir = new javax.swing.JTextField();
        btnTambah = new javax.swing.JButton();
        btnUbah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnBersihkan = new javax.swing.JButton();
        btnCetakLaporan = new javax.swing.JButton();
        jLabel42 = new javax.swing.JLabel();
        txJumlahBarang = new javax.swing.JTextField();
        jLabel43 = new javax.swing.JLabel();
        txTotal = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        txHargaBarang = new javax.swing.JTextField();
        txKodeBarang = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        txtNamaKurirPengantar = new javax.swing.JTextField();
        hitungTotal = new javax.swing.JButton();
        jLabel45 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(224, 224, 224));

        jPanel11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));

        tblPelangganBarang1.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tblPelangganBarang1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID Pelanggan", "Nama Pelanggan", "No. HP", "Alamat"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPelangganBarang1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPelangganBarang1MouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tblPelangganBarang1);
        if (tblPelangganBarang1.getColumnModel().getColumnCount() > 0) {
            tblPelangganBarang1.getColumnModel().getColumn(0).setResizable(false);
            tblPelangganBarang1.getColumnModel().getColumn(1).setResizable(false);
            tblPelangganBarang1.getColumnModel().getColumn(2).setResizable(false);
            tblPelangganBarang1.getColumnModel().getColumn(3).setResizable(false);
        }

        cmbCariPelangganBarang.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        cmbCariPelangganBarang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "id", "nama", "no_hp", " " }));

        txtcaricustomer2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtcaricustomer2KeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane5)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel11Layout.createSequentialGroup()
                        .addComponent(cmbCariPelangganBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtcaricustomer2)))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbCariPelangganBarang, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(txtcaricustomer2))
                .addGap(5, 5, 5))
        );

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("CARI PELANGGAN BARANG");

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel32.setText("No Transaksi");

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel33.setText("ID Customer");

        txIdTransaksi.setEnabled(false);

        txIdCustomer.setEnabled(false);

        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel39.setText("Nama Customer");

        jLabel41.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel41.setText("Nama Barang");

        jPanel27.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));

        tabelKurir.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tabelKurir.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Nama Kurir", "Jenis Kendaraan", "PLAT Nomor"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelKurir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelKurirMouseClicked(evt);
            }
        });
        jScrollPane10.setViewportView(tabelKurir);

        cbbCariKurir.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        cbbCariKurir.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "nama_kurir", "no_hp", "jenis_kendaraan", "plat_nomor" }));
        cbbCariKurir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbbCariKurirActionPerformed(evt);
            }
        });

        txtCariKurir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCariKurirKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCariKurirKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCariKurirKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                    .addGroup(jPanel27Layout.createSequentialGroup()
                        .addComponent(cbbCariKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtCariKurir)))
                .addContainerGap())
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbbCariKurir, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(txtCariKurir))
                .addGap(13, 13, 13))
        );

        tabelBarang.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tabelBarang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tabelBarang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelBarangMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabelBarang);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Pilih Barang");

        tabelTransaksi.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tabelTransaksi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID Transaksi", "ID Customer", "Nama Pelanggan", "Nama Barang", "Harga Barang", "Jumlah", "Total", "Nama Kurir", "Jenis Kendaraan", "PLAT Nomor"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelTransaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelTransaksiMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tabelTransaksi);
        if (tabelTransaksi.getColumnModel().getColumnCount() > 0) {
            tabelTransaksi.getColumnModel().getColumn(0).setResizable(false);
            tabelTransaksi.getColumnModel().getColumn(1).setResizable(false);
            tabelTransaksi.getColumnModel().getColumn(2).setResizable(false);
            tabelTransaksi.getColumnModel().getColumn(3).setResizable(false);
            tabelTransaksi.getColumnModel().getColumn(4).setResizable(false);
            tabelTransaksi.getColumnModel().getColumn(5).setResizable(false);
            tabelTransaksi.getColumnModel().getColumn(6).setResizable(false);
            tabelTransaksi.getColumnModel().getColumn(7).setResizable(false);
            tabelTransaksi.getColumnModel().getColumn(8).setResizable(false);
            tabelTransaksi.getColumnModel().getColumn(9).setResizable(false);
        }

        lblTotalHarga.setFont(new java.awt.Font("Tahoma", 0, 40)); // NOI18N
        lblTotalHarga.setForeground(new java.awt.Color(0, 179, 0));
        lblTotalHarga.setText("Rp. 0");

        jLabel55.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel55.setText("ID Kurir");

        txIdKurir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txIdKurirActionPerformed(evt);
            }
        });

        cbbKendaraanKurir.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Motor", "Mobil", "Pick-Up", " " }));
        cbbKendaraanKurir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbbKendaraanKurirActionPerformed(evt);
            }
        });

        jLabel75.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel75.setText("Jenis Kendaraan");

        jLabel77.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel77.setText("PLAT Nomor");

        btnTambah.setText("Tambah");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });

        btnUbah.setText("Ubah");
        btnUbah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahActionPerformed(evt);
            }
        });

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnBersihkan.setText("Bersih");
        btnBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBersihkanActionPerformed(evt);
            }
        });

        btnCetakLaporan.setText("Cetak Laporan");
        btnCetakLaporan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakLaporanActionPerformed(evt);
            }
        });

        jLabel42.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel42.setText("Jumlah");

        txJumlahBarang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txJumlahBarangMouseClicked(evt);
            }
        });

        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel43.setText("Total");

        txTotal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txTotalMouseClicked(evt);
            }
        });

        jLabel44.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel44.setText("Harga Barang");

        jLabel40.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel40.setText("ID Barang");

        jLabel56.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel56.setText("Nama Kurir");

        hitungTotal.setText("Hitung");
        hitungTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hitungTotalActionPerformed(evt);
            }
        });

        jLabel45.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel45.setText("/ Pcs");

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel38.setText("PILIH KURIR");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(31, 31, 31)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txHargaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txNamaCustomerBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txKodeBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(txIdTransaksi, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txIdCustomer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(23, 23, 23)
                                                .addComponent(hitungTotal)
                                                .addGap(31, 31, 31))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel42, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGap(5, 5, 5)))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(txTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel45))
                                            .addComponent(txJumlahBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel56, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtNamaKurirPengantar, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel55, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txIdKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel75, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbbKendaraanKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel77, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtPlatNomorKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnUbah, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnHapus, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                                    .addComponent(btnBersihkan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(lblTotalHarga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCetakLaporan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(31, 31, 31))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel37)
                            .addComponent(jLabel1))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel38)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txIdTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel33)
                            .addComponent(txIdCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txNamaCustomerBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel39))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txKodeBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel40))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel41))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txHargaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel44))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txJumlahBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel42))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel43)
                            .addComponent(hitungTotal)
                            .addComponent(jLabel45))
                        .addGap(35, 35, 35)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txIdKurir, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                            .addComponent(jLabel55, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtNamaKurirPengantar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                            .addComponent(jLabel56, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel75, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbKendaraanKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtPlatNomorKurir, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                            .addComponent(jLabel77, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnCetakLaporan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnTambah)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnUbah))
                            .addComponent(btnHapus)
                            .addComponent(btnBersihkan, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addComponent(lblTotalHarga))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE))
                .addGap(50, 50, 50))
        );

        jScrollPane2.setViewportView(jPanel1);

        jPanel10.setBackground(new java.awt.Color(0, 0, 255));
        jPanel10.setPreferredSize(new java.awt.Dimension(1500, 69));

        jPanel8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel8.setPreferredSize(new java.awt.Dimension(1500, 43));

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("PENJUALAN BARANG");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 1590, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel13)
                .addGap(5, 5, 5))
        );

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 1618, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addGap(10, 10, 10))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 1618, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 843, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 853, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblPelangganBarang1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPelangganBarang1MouseClicked
        int selectedRow = tblPelangganBarang1.getSelectedRow();

        if (selectedRow != -1) {
            txIdCustomer.setText(tblPelangganBarang1.getValueAt(selectedRow, 0).toString());
            txNamaCustomerBarang.setText(tblPelangganBarang1.getValueAt(selectedRow, 1).toString());

            txIdCustomer.setEditable(false);
        }
    }//GEN-LAST:event_tblPelangganBarang1MouseClicked

    private void txtcaricustomer2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtcaricustomer2KeyPressed
//        Logika Cari Pelanggan
    }//GEN-LAST:event_txtcaricustomer2KeyPressed

    private void tabelKurirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelKurirMouseClicked
        int selectedRow = tabelKurir.getSelectedRow();

        if (selectedRow != -1) {
            txIdKurir.setText(tabelKurir.getValueAt(selectedRow, 0).toString());
            txtNamaKurirPengantar.setText(tabelKurir.getValueAt(selectedRow, 1).toString());
            cbbKendaraanKurir.setSelectedItem(tabelKurir.getValueAt(selectedRow, 4).toString());
            txtPlatNomorKurir.setText(tabelKurir.getValueAt(selectedRow, 5).toString());
            txIdKurir.setEditable(false);
            txtNamaKurirPengantar.setEditable(false);
            cbbKendaraanKurir.setEnabled(false);
            txtPlatNomorKurir.setEditable(false);
        }
    }//GEN-LAST:event_tabelKurirMouseClicked

    private void cbbCariKurirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbCariKurirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbbCariKurirActionPerformed

    private void txtCariKurirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKurirKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCariKurirKeyPressed

    private void txtCariKurirKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKurirKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCariKurirKeyReleased

    private void txtCariKurirKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKurirKeyTyped
        try {
            String cariKurir = cbbCariKurir.getSelectedItem().toString();
            String kurir = txtCariKurir.getText();
            st = cn.createStatement();
            rs = st.executeQuery("SELECT * FROM kurir WHERE "
                    + cariKurir
                    + " LIKE '%" + kurir + "%'");

            // Tabel 1 data pelanggan
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Nama Kurir");
            model.addColumn("No. HP");
            model.addColumn("Alamat");
            model.addColumn("Jenis Kendaraan");
            model.addColumn("PLAT Nomor");
            model.addColumn("Status");
            tabelKurir.setModel(model);

            while (rs.next()) {
                // Data tabel 1
                Object[] data
                        = {
                            rs.getInt("id_kurir"),
                            rs.getString("nama_kurir"),
                            rs.getString("no_hp"),
                            rs.getString("alamat"),
                            rs.getString("jenis_kendaraan"),
                            rs.getString("plat_nomor"),
                            rs.getString("status")
                        };
                model.addRow(data);
            }
        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_txtCariKurirKeyTyped

    private void btnBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihkanActionPerformed
        try {
            clear();
        } catch (SQLException ex) {
            Logger.getLogger(PenjualanBarang.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnBersihkanActionPerformed

    private void tabelBarangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelBarangMouseClicked
        int selectedRow = tabelBarang.getSelectedRow();
        if (selectedRow != -1) {
//            JOptionPane.showMessageDialog(this, "Pilih Data Customer dan Kurir Dulu!");
            txKodeBarang.setText(tabelBarang.getValueAt(selectedRow, 0).toString());
            txNamaBarang.setText(tabelBarang.getValueAt(selectedRow, 1).toString());
            txHargaBarang.setText(tabelBarang.getValueAt(selectedRow, 2).toString());

            txKodeBarang.setEditable(false);
            txNamaBarang.setEditable(false);
            txHargaBarang.setEditable(false);
        }
    }//GEN-LAST:event_tabelBarangMouseClicked

    private void txTotalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txTotalMouseClicked

    }//GEN-LAST:event_txTotalMouseClicked

    private void txJumlahBarangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txJumlahBarangMouseClicked

    }//GEN-LAST:event_txJumlahBarangMouseClicked

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        try {
            // Validasi input kosong
            if (txIdCustomer.getText().isEmpty() || txJumlahBarang.getText().isEmpty()
                    || txHargaBarang.getText().isEmpty() || txTotal.getText().isEmpty()
                    || txKodeBarang.getText().isEmpty() || txIdKurir.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Harap lengkapi semua field!");
                return;
            }

            // Validasi format kode barang
            String kodeBarang = txKodeBarang.getText().trim();
            if (!kodeBarang.matches("[A-Za-z0-9]+")) {
                JOptionPane.showMessageDialog(this, "Kode barang hanya boleh mengandung huruf dan angka!");
                return;
            }

            // Parsing input
            int idCustomer = Integer.parseInt(txIdCustomer.getText());
            int jumlah = Integer.parseInt(txJumlahBarang.getText());
            double hargaSatuan = Double.parseDouble(txHargaBarang.getText());
            double total = Double.parseDouble(txTotal.getText());
            int idKurir = Integer.parseInt(txIdKurir.getText());

            // Validasi perhitungan total
            double expectedTotal = jumlah * hargaSatuan;
            if (Math.abs(total - expectedTotal) > 0.001) {
                JOptionPane.showMessageDialog(this,
                        "Total harus sama dengan Jumlah × Harga Satuan!\n"
                        + "Perhitungan: " + jumlah + " × " + hargaSatuan + " = " + expectedTotal);
                return;
            }

            // Cek stok tersedia (opsional)
            if (!cekStokTersedia(kodeBarang, jumlah)) {
                JOptionPane.showMessageDialog(this, "Stok tidak mencukupi!");
                return;
            }

            // Buat objek transaksi
            TransaksiBarang trx = new TransaksiBarang();
            trx.setIdCustomer(idCustomer);
            trx.setJumlah(jumlah);
            trx.setHargaSatuan((int) Math.round(hargaSatuan)); // Konversi ke int
            trx.setTanggal(new java.sql.Date(System.currentTimeMillis()));
            trx.setTotal((int) Math.round(total));
            trx.setKode_barang(kodeBarang);
            trx.setId_kurir(idKurir);

            // Simpan transaksi
            boolean sukses = TransaksiBarang.insertTransaksi(trx);

            if (sukses) {
                JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan!");
                kurangiStokBarangTerjual(kodeBarang, jumlah); // Update stok
                clear();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Format input tidak valid! Pastikan:\n"
                    + "- ID Customer, Jumlah, ID Kurir: bilangan bulat\n"
                    + "- Harga dan Total: angka (contoh: 30000 atau 30000.0)");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error database: " + e.getMessage()
                    + "\nKode error: " + e.getErrorCode());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahActionPerformed
        try {
            editDataTransaksi();
            clear();
        } catch (SQLException ex) {
            Logger.getLogger(PenjualanBarang.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnUbahActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        int selectedRow = tabelTransaksi.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih baris data yang akan dihapus",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Konfirmasi sebelum menghapus
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus transaksi ini?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.NO_OPTION) {
            try {
                clear();
            } catch (SQLException ex) {
                Logger.getLogger(PenjualanBarang.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idTransaksi = Integer.parseInt(tabelTransaksi.getValueAt(selectedRow, 0).toString());

                boolean success = TransaksiBarang.deleteTransaksi(idTransaksi);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Data transaksi berhasil dihapus",
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    clear();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Gagal menghapus data transaksi",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Terjadi kesalahan: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnHapusActionPerformed

    private void tabelTransaksiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelTransaksiMouseClicked
        int selectedRow = tabelTransaksi.getSelectedRow();

        if (selectedRow >= 0) {
            try {
                Object idTransaksi = tabelTransaksi.getValueAt(selectedRow, 0);
                Object idCustomer = tabelTransaksi.getValueAt(selectedRow, 1);
                Object namaPelanggan = tabelTransaksi.getValueAt(selectedRow, 2);
                Object namaBarang = tabelTransaksi.getValueAt(selectedRow, 3);
                Object hargaBarang = tabelTransaksi.getValueAt(selectedRow, 4);
                Object jumlah = tabelTransaksi.getValueAt(selectedRow, 5);
                Object total = tabelTransaksi.getValueAt(selectedRow, 6);
                Object namaKurir = tabelTransaksi.getValueAt(selectedRow, 7);
                Object jenisKendaraan = tabelTransaksi.getValueAt(selectedRow, 8);
                Object platNomor = tabelTransaksi.getValueAt(selectedRow, 9);

                txIdTransaksi.setText(idTransaksi.toString());
                txIdCustomer.setText(idCustomer.toString());
                txNamaCustomerBarang.setText(namaPelanggan != null ? namaPelanggan.toString() : "");
                txNamaBarang.setText(namaBarang.toString());
                txHargaBarang.setText(hargaBarang.toString());
                txJumlahBarang.setText(jumlah.toString());
                txTotal.setText(total.toString());
                txtNamaKurirPengantar.setText(namaKurir != null ? namaKurir.toString() : "");
                cbbKendaraanKurir.setSelectedItem(jenisKendaraan != null ? jenisKendaraan.toString() : "");
                txtPlatNomorKurir.setText(platNomor != null ? platNomor.toString() : "");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Gagal memuat data: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_tabelTransaksiMouseClicked

    private void btnCetakLaporanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakLaporanActionPerformed
        try {
            String reportPath = "src/laporan/LaporanPenjualanBarang.jasper";

            HashMap<String, Object> parameters = new HashMap<>();

            JasperPrint jp = JasperFillManager.fillReport(reportPath, parameters, cn);
            JasperViewer.viewReport(jp, false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal mencetak laporan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnCetakLaporanActionPerformed

    private void hitungTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hitungTotalActionPerformed

        try {
            hitungTotalHargaBarang();
            lblTotalHarga.setForeground(new Color(0, 179, 0));;
            lblTotalHarga.setText("Rp." + txTotal.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Input Jumlah Terlebih Dahulu!");
        }

    }//GEN-LAST:event_hitungTotalActionPerformed

    private void txIdKurirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txIdKurirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txIdKurirActionPerformed

    private void cbbKendaraanKurirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbKendaraanKurirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbbKendaraanKurirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBersihkan;
    private javax.swing.JButton btnCetakLaporan;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnTambah;
    private javax.swing.JButton btnUbah;
    private javax.swing.JComboBox<String> cbbCariKurir;
    private javax.swing.JComboBox<String> cbbKendaraanKurir;
    private javax.swing.JComboBox<String> cmbCariPelangganBarang;
    private javax.swing.JButton hitungTotal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel lblTotalHarga;
    private javax.swing.JTable tabelBarang;
    private javax.swing.JTable tabelKurir;
    private javax.swing.JTable tabelTransaksi;
    private javax.swing.JTable tblPelangganBarang1;
    private javax.swing.JTextField txHargaBarang;
    private javax.swing.JTextField txIdCustomer;
    private javax.swing.JTextField txIdKurir;
    private javax.swing.JTextField txIdTransaksi;
    private javax.swing.JTextField txJumlahBarang;
    private javax.swing.JTextField txKodeBarang;
    private javax.swing.JTextField txNamaBarang;
    private javax.swing.JTextField txNamaCustomerBarang;
    private javax.swing.JTextField txTotal;
    private javax.swing.JTextField txtCariKurir;
    private javax.swing.JTextField txtNamaKurirPengantar;
    private javax.swing.JTextField txtPlatNomorKurir;
    private javax.swing.JTextField txtcaricustomer2;
    // End of variables declaration//GEN-END:variables
}
