package POS;

import Notification.Notification;
import QR.QRISPrinter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import model.Barang;
import model.CustomerBarang;
import view.PendapatanPenjualanBarang;
import view.PenjualanBarang;

public class Customer extends javax.swing.JFrame {

    String Tanggal;
    public Statement st;
    public ResultSet rs;
    Connection cn = POS.koneksi.BukaKoneksi();

    public Customer() {
        initComponents();
        String[] bulan
                = {
                    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                };

        cmbJudulBulan.setModel(new DefaultComboBoxModel<>(bulan));
        cmbBulan.setModel(new DefaultComboBoxModel<>(bulan));
    }

    public Customer(int id_level) {
        initComponents();
        aturFontTextField(); // baru setelah itu atur font
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Bersih();
        aturMenu(id_level);
        TampilDataCustomer();
        CariDataCustomer();
        TampilDataPenjualan();
        CariDataPenjualan();
        TampilDataOperasional();
        CariDataBarang();
        tampilDataKurir();
        cekStokBarangTisu();
        cekStokGalon1pcs();
        cekStokSedimenPcs();
        cekStokSegelGalon200pcs();
        cekStokTutupGalon1000pcs();
        dataCustomerBarang();
    }

    private void resetTable() {
        tampilDataKurir();
        cekStokBarangTisu();
        cekStokGalon1pcs();
        cekStokSedimenPcs();
        cekStokSegelGalon200pcs();
        cekStokTutupGalon1000pcs();
    }

    public void Customer(int id_level) {
        btnTampilkan.addActionListener(e -> TampilkanDataLaporanBulanan());
        btnTampilkan1.addActionListener(e -> TampilkanDataLaporanOperasional());
    }
    
    public void TampilkanDataLaporanBulanan() {
        try {
            int bulan = cmbBulan.getSelectedIndex() + 1;
            String tahun = cmbTahun.getSelectedItem().toString();
            String jenisCustomer = cbbJenisBarangPenjualan.getSelectedItem().toString();
            String status = cmbStatus.getSelectedItem().toString();

            String sql = "SELECT id_transaksi, tanggal, nama_customer, status, jumlah, total "
                    + "FROM transaksi WHERE MONTH(tanggal) = ? AND YEAR(tanggal) = ? "
                    + "AND jenis_customer = ? AND status = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, bulan);
            pst.setString(2, tahun);
            pst.setString(3, jenisCustomer);
            pst.setString(4, status);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.setColumnIdentifiers(new Object[]{
                "No.", "ID", "Tanggal", "Customer", "Status", "Galon", "Total"
            });

            int totalGalon = 0;
            int totalPendapatan = 0;
            int totalTransaksi = 0;
            int no = 1;

            while (rs.next()) {
                int galon = rs.getInt("jumlah");
                int total = rs.getInt("total");

                model.addRow(new Object[]{
                    no++,
                    rs.getString("id_transaksi"),
                    rs.getString("tanggal"),
                    rs.getString("nama_customer"),
                    rs.getString("status"),
                    galon,
                    total
                });

                totalGalon += galon;
                totalPendapatan += total;
                totalTransaksi++;
            }

            tableLaporanBulananBarang.setModel(model);

            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16);
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18);
            tableLaporanBulananBarang.setFont(fontIsi);
            tableLaporanBulananBarang.setRowHeight(24);
            tableLaporanBulananBarang.getTableHeader().setFont(fontHeader);
            DecimalFormat formatter = new DecimalFormat("#,###");
            lblTotalBarangTerjual.setText(totalGalon + " Galon");
            lblTotalPendapatanN.setFont(new Font("Tahoma", Font.BOLD, 20));
            lblTotalPendapatanN.setForeground(new Color(10, 240, 10));
            lblTotalPendapatanN.setText("Rp. " + formatter.format(totalPendapatan));
            lblTotalTransaksi.setText(totalTransaksi + " Transaksi");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data laporan: " + e.getMessage());
        }
    }

    public void TampilkanDataLaporanOperasional() {
        try {
            int bulan = cmbBulan1.getSelectedIndex() + 1;
            String tahun = cmbTahun1.getSelectedItem().toString();
            String jenisBarang = cmbJenisBarang.getSelectedItem().toString();

            String sql = "SELECT id_barang, tanggal, nama_barang, jumlah, total "
                    + "FROM operasional "
                    + "WHERE MONTH(tanggal) = ? AND YEAR(tanggal) = ? "
                    + "AND LOWER(nama_barang) LIKE ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, bulan);
            pst.setString(2, tahun);
            pst.setString(3, "%" + jenisBarang.toLowerCase() + "%");

            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.setColumnIdentifiers(new Object[]{
                "No.", "ID", "Tanggal", "Nama Barang", "Jumlah", "Total"
            });

            int totalBeli = 0;
            int totalPengeluaran = 0;
            int totalTransaksi = 0;
            int no = 1;

            while (rs.next()) {
                int beli = rs.getInt("jumlah");
                int total = rs.getInt("total");
                Date tanggal = rs.getDate("tanggal"); // Ambil tanggal sebagai Date

                model.addRow(new Object[]{
                    no++,
                    rs.getString("id_barang"),
                    tanggal,
                    rs.getString("nama_barang"),
                    beli,
                    total
                });

                totalBeli += beli;
                totalPengeluaran += total;
                totalTransaksi++;
            }

            tableLaporanOperasional.setModel(model);

            // Atur Font setelah setModel
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16);
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18);

            tableLaporanOperasional.setFont(fontIsi);
            tableLaporanOperasional.setRowHeight(24);
            tableLaporanOperasional.getTableHeader().setFont(fontHeader);

            // Format angka
            DecimalFormat formatter = new DecimalFormat("#,###");

            lblTotalBeliBarang.setText(totalBeli + " Jumlah");
            lblTotalPengeluaran.setText("<html><span style='color:green; font-weight:bold; font-size:20pt;'>Rp. " + formatter.format(totalPengeluaran) + "</span></html>");
            lblTotalTransaksiBarang.setText(totalTransaksi + " Transaksi");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data laporan: " + e.getMessage());
        }
    }

    public void aturMenu(int id_level) {
        switch (id_level) {
            case 1: // Administrator
                customer.setVisible(true);
                penjualan.setVisible(true);
                operasional.setVisible(true);
                break;
            case 2: // Kasir
                customer.setVisible(true);
                penjualan.setVisible(true);
                operasional.setVisible(false);
                break;
            case 3: // Logistik
                customer.setVisible(false);
                penjualan.setVisible(false);
                operasional.setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Hak akses tidak dikenali.");
                dispose();
                break;
        }
    }

    private void aturFontTextField() {
        Font fontBesar = new Font("Tahoma", Font.PLAIN, 18);
        Font fontBiggest = new Font("Tahoma", Font.PLAIN, 40);
        
        // Form input customer air galon
        txtidcustomer.setFont(fontBesar);
        txtnamacustomer.setFont(fontBesar);
        txtalamat.setFont(fontBesar);
        txttelephone.setFont(fontBesar);
        txtjeniscustomer.setFont(fontBesar);
        
        //Form input customer barang
        txtidcustomer1.setFont(fontBesar);
        txtnamacustomer1.setFont(fontBesar);
        txtalamat1.setFont(fontBesar);
        txttelephone1.setFont(fontBesar);
        
        // Form input penjualan
        txtNamaKurirPengantar.setFont(fontBesar);
        txtPlatNomorKurir.setFont(fontBesar);
        txtidtransaksi.setFont(fontBesar);
        txtidcustomer2.setFont(fontBesar);
        txtnamacustomer2.setFont(fontBesar);
        txtjeniscustomer2.setFont(fontBesar);
        txthargasatuan2.setFont(fontBesar);
        txtjumlahpenjualan.setFont(fontBesar);
        txtstatus.setFont(fontBesar);
        lblTotalHarga.setFont(fontBiggest);

        // Form pencarian
        txtcaricustomer.setFont(fontBesar);
        txtcaricustomer1.setFont(fontBesar);
        txtcaricustomer2.setFont(fontBesar);
        txtcaribarang.setFont(fontBesar);

        // JComboBox
        cbbKendaraanKurir.setFont(fontBesar);
        cmbcaricustomer.setFont(fontBesar);
        cmbcaricustomer1.setFont(fontBesar);
        cmbcaricustomer2.setFont(fontBesar);
        cmbjeniscustomer.setFont(fontBesar);
        cmbjeniscustomer2.setFont(fontBesar);
        cmbstatus.setFont(fontBesar);
        cmbkodebarang.setFont(fontBesar);
        cmbnamabarang.setFont(fontBesar);
        cmbcaribarang.setFont(fontBesar);

        // Form input operasional (pakai font ukuran 18)
        txtkodebarang.setFont(fontBesar);
        txtnamabarang.setFont(fontBesar);
        txttotalbarang.setFont(fontBesar);
        txtidbarang.setFont(fontBesar);
        txtjumlahbarang.setFont(fontBesar);
        txthargasatuan.setFont(fontBesar);

        //laporan bulanan
        cmbBulan.setFont(fontBesar);
        cmbTahun.setFont(fontBesar);
        cmbJenisCustomer.setFont(fontBesar);
        cmbJudulBulan.setFont(fontBesar);
        cmbStatus.setFont(fontBesar);
        btnTampilkan.setFont(fontBesar);
        btnCetak.setFont(fontBesar);
        btnKembaliPenjualan.setFont(fontBesar);
        btnBersihBulanan.setFont(fontBesar);
        btnLaporan.setFont(fontBesar);

        btnKembaliPenjualan1.setFont(fontBesar);
        btnBersihOperasional.setFont(fontBesar);
        btnTampilkan1.setFont(fontBesar);
        btnLaporan1.setFont(fontBesar);
        cmbJenisBarang.setFont(fontBesar);
        cmbTahun1.setFont(fontBesar);
        cmbBulan1.setFont(fontBesar);

    }

    private void Bersih() {

        //bersih customer
        txtidcustomer.setText("");
        txtnamacustomer.setText("");
        txtjeniscustomer.setText("");
        txtalamat.setText("");
        txttelephone.setText("");
        btntambahcustomer.setText("TAMBAH");
        txtidcustomer.setEditable(true);
        
        //bersih customer barang
        txtidcustomer1.setText("");
        txtnamacustomer1.setText("");
        txtalamat1.setText("");
        txttelephone1.setText("");
        btntambahcustomer1.setText("TAMBAH");
        
        //bersih operasional
        txtidbarang.setText("");
        txtkodebarang.setText("");
        txtnamabarang.setText("");
        txtjumlahbarang.setText("");
        txthargasatuan.setText("");
        txttotalbarang.setText("");
        btntambahoperasional.setText("TAMBAH");
        txtidbarang.setEditable(true);

        //bersih penjualan
        TampilDataPenjualan();
        txtidtransaksi.setText("");
        txtidcustomer2.setText("");
        txtnamacustomer2.setText("");
        txtjeniscustomer2.setText("");
        txthargasatuan2.setText("");
        txtjumlahpenjualan.setText("");
        lblTotalHarga.setText("Rp. 0");
        txtstatus.setText("");
        btntambahpenjualan.setText("TAMBAH");
        txtidtransaksi.setEditable(true);

        //bersih laporan
        TampilkanDataLaporanBulanan();
        DefaultTableModel modelBulanan = (DefaultTableModel) tableLaporanBulananBarang.getModel();
        modelBulanan.setRowCount(0); // Hapus semua baris
        lblTotalBarangTerjual.setText("0");
        lblTotalPendapatanN.setText("Rp. 0");
        lblTotalTransaksi.setText("0");

        TampilkanDataLaporanOperasional();
        DefaultTableModel modelOperasional = (DefaultTableModel) tableLaporanOperasional.getModel();
        modelOperasional.setRowCount(0); // Hapus semua baris
        lblTotalTransaksiBarang.setText("0");
        lblTotalBeliBarang.setText("0");
        lblTotalPengeluaran.setText("Rp. 0");

        TampilDataCustomer();
        CariDataCustomer();
        TampilDataPenjualan();
        CariDataPenjualan();
        TampilDataOperasional();
        CariDataBarang();
        tampilDataKurir();

        txtNamaKurirPengantar.setText("");
        cbbKendaraanKurir.setSelectedIndex(0);
        txtPlatNomorKurir.setText("");
    }

    public Map<String, Object> getLaporanBulananParameters() {
        // Daftar nama bulan untuk referensi judul laporan
        String[] namaBulan
                = {
                    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                };

        // Ambil index bulan yang dipilih (index mulai dari 0, maka +1)
        int bulanIndex = cmbBulan.getSelectedIndex() + 1;

        // Ambil nilai-nilai dari combo box
        String tahun = (String) cmbTahun.getSelectedItem();
        String jenisCustomer = (String) cmbJenisCustomer.getSelectedItem();
        String judulBulan = (String) cmbJudulBulan.getSelectedItem(); // ini untuk keperluan tampilan
        String status = (String) cmbStatus.getSelectedItem();

        // Buat map parameter
        Map<String, Object> params = new HashMap<>();
        params.put("bulan", bulanIndex); // Integer sesuai query
        params.put("tahun", tahun);      // Tetap String, cocok dengan class parameter jrxml
        params.put("jenis_customer", jenisCustomer);
        params.put("laporan_bulanan", judulBulan + " " + tahun); // untuk ditampilkan di header
        params.put("status", status);

        return params;
    }
    
     public void dataCustomerBarang() {
        try {
            st = cn.createStatement();
            rs = st.executeQuery("SELECT * FROM customer_barang");

            // Tabel 1 data pelanggan
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("No.");
            model.addColumn("ID");
            model.addColumn("NAMA");
            model.addColumn("TELEPHONE");
            model.addColumn("ALAMAT");
            tblPelangganBarang.setModel(model);

            int no = 1;
            while (rs.next()) {
                Object[] data
                        = {
                            no++,
                            rs.getInt("id"),
                            rs.getString("nama"),
                            rs.getString("no_hp"),
                            rs.getString("alamat"),
                        };
                model.addRow(data);
            }

            // Untuk atur font dan jarak antar baris
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16); // Font isi tabel
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18); // Font header kolom
            tblPelangganBarang.setFont(fontIsi);
            tblPelangganBarang.setRowHeight(28); // Atur tinggi baris (bisa disesuaikan)
            tblPelangganBarang.getTableHeader().setFont(fontHeader);

        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
     
    public void TampilDataCustomer() {
        try {
            st = cn.createStatement();
            rs = st.executeQuery("SELECT * FROM customer");

            // Tabel 1 data pelanggan
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("No.");
            model.addColumn("ID Customer");
            model.addColumn("Nama Customer");
            model.addColumn("Jenis Customer");
            model.addColumn("Alamat");
            model.addColumn("Telephone");
            tblcustomer.setModel(model);

            int no = 1;
            while (rs.next()) {
                Object[] data
                        = {
                            no++,
                            rs.getInt("id_customer"),
                            rs.getString("nama_customer"),
                            rs.getString("jenis_customer"),
                            rs.getString("alamat"),
                            rs.getString("telephone")
                        };
                model.addRow(data);
            }

            // Untuk atur font dan jarak antar baris
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16); // Font isi tabel
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18); // Font header kolom
            tblcustomer.setFont(fontIsi);
            tblcustomer.setRowHeight(28); // Atur tinggi baris (bisa disesuaikan)
            tblcustomer.getTableHeader().setFont(fontHeader);

        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void CariDataCustomer() {
        try {
            //fungsi cari 1
            st = cn.createStatement();
            rs = st.executeQuery("SELECT * FROM customer WHERE "
                    + cmbcaricustomer.getSelectedItem().toString()
                    + " LIKE '%" + txtcaricustomer.getText() + "%'");

            // Tabel 1 data pelanggan
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("No.");
            model.addColumn("ID Customer");
            model.addColumn("Nama Customer");
            model.addColumn("Jenis Customer");
            model.addColumn("Alamat");
            model.addColumn("Telephone");
            tblcustomer.setModel(model);

            int no1 = 1;
            while (rs.next()) {
                // Data tabel 1
                Object[] data
                        = {
                            no1++,
                            rs.getInt("id_customer"),
                            rs.getString("nama_customer"),
                            rs.getString("jenis_customer"),
                            rs.getString("alamat"),
                            rs.getString("telephone")
                        };
                model.addRow(data);
            }
        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void TampilDataPenjualan() {
        try {
            st = cn.createStatement();
            rs = st.executeQuery("SELECT * FROM transaksi");

            // Tabel 1 data operasional
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Nama Customer");
            model.addColumn("ID Customer");
            model.addColumn("Jenis Customer");
            model.addColumn("Harga Satuan");
            model.addColumn("Jumlah");
            model.addColumn("Total");
            model.addColumn("Tanggal");
            model.addColumn("Status");
            model.addColumn("Nama Kurir");
            model.addColumn("Jenis Kendaraan");
            model.addColumn("PLAT Nomor");
            tblpenjualan.setModel(model);
            tabelPenjualanforPendapatan.setModel(model);

            while (rs.next()) {
                // Data tabel 1 tanpa nomor urut
                Object[] data
                        = {
                            rs.getInt("id_transaksi"),
                            rs.getString("nama_customer"),
                            rs.getInt("id_customer"),
                            rs.getString("jenis_customer"),
                            rs.getInt("harga"),
                            rs.getInt("jumlah"),
                            rs.getInt("total"),
                            rs.getString("tanggal"),
                            rs.getString("status"),
                            rs.getString("nama_kurir"),
                            rs.getString("jenis_kendaraan"),
                            rs.getString("plat_nomor")
                        };
                model.addRow(data);
            }

            // Untuk atur font dan jarak antar baris
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16); // Font isi tabel
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18); // Font header kolom
            tblpenjualan.setFont(fontIsi);
            tblpenjualan.setRowHeight(28); // Atur tinggi baris (bisa disesuaikan)
            tblpenjualan.getTableHeader().setFont(fontHeader);
            tabelPenjualanforPendapatan.setFont(fontIsi);
            tabelPenjualanforPendapatan.setRowHeight(28); // Atur tinggi baris (bisa disesuaikan)
            tabelPenjualanforPendapatan.getTableHeader().setFont(fontHeader);

        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void CariDataPenjualan() {
        try {
            //fungsi cari 1
            st = cn.createStatement();
            rs = st.executeQuery("SELECT * FROM customer WHERE "
                    + cmbcaricustomer2.getSelectedItem().toString()
                    + " LIKE '%" + txtcaricustomer2.getText() + "%'");

            // Tabel 1 data pelanggan
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID Customer");
            model.addColumn("Nama Customer");
            model.addColumn("Jenis Customer");
            model.addColumn("Alamat");
            model.addColumn("Telephone");
            tblcaricustomer2.setModel(model);

            while (rs.next()) {
                // Data tabel 1
                Object[] data
                        = {
                            rs.getInt("id_customer"),
                            rs.getString("nama_customer"),
                            rs.getString("jenis_customer"),
                            rs.getString("alamat"),
                            rs.getString("telephone")
                        };
                model.addRow(data);
            }
        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void TampilDataOperasional() {
        try {
            st = cn.createStatement();
            rs = st.executeQuery("SELECT * FROM operasional");

            // Tabel 1 data operasional
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("No.");
            model.addColumn("ID Barang");
            model.addColumn("Kode Barang");
            model.addColumn("Nama Barang");
            model.addColumn("Jumlah");
            model.addColumn("Harga Satuan");
            model.addColumn("Total");
            model.addColumn("Tanggal");
            tbloperasional.setModel(model);
            tabelOperasionalforPendapatan.setModel(model);

            int no1 = 1;
            while (rs.next()) {
                // Data tabel 1 tanpa nomor urut
                Object[] data
                        = {
                            no1++,
                            rs.getString("id_barang"),
                            rs.getString("kode_barang"),
                            rs.getString("nama_barang"),
                            rs.getInt("jumlah"),
                            rs.getInt("harga_satuan"),
                            rs.getInt("total"),
                            rs.getString("tanggal")
                        };
                model.addRow(data);
            }

            // Untuk atur font dan jarak antar baris
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16); // Font isi tabel
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18); // Font header kolom
            tbloperasional.setFont(fontIsi);
            tbloperasional.setRowHeight(28); // Atur tinggi baris (bisa disesuaikan)
            tbloperasional.getTableHeader().setFont(fontHeader);

            tabelOperasionalforPendapatan.setFont(fontIsi);
            tabelOperasionalforPendapatan.setRowHeight(28); // Atur tinggi baris (bisa disesuaikan)
            tabelOperasionalforPendapatan.getTableHeader().setFont(fontHeader);

        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void CariDataBarang() {
        try {
            //fungsi cari 1
            st = cn.createStatement();
            rs = st.executeQuery("SELECT * FROM operasional WHERE "
                    + cmbcaribarang.getSelectedItem().toString()
                    + " LIKE '%" + txtcaribarang.getText() + "%'");

            // Tabel 1 data operasional
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID Barang");
            model.addColumn("Kode Barang");
            model.addColumn("Nama Barang");
            model.addColumn("Jumlah");
            model.addColumn("Harga Satuan");
            model.addColumn("Total");
            model.addColumn("Tanggal");
            tbloperasional.setModel(model);

            while (rs.next()) {
                // Data tabel 1 tanpa nomor urut
                Object[] data
                        = {
                            rs.getString("id_barang"),
                            rs.getString("kode_barang"),
                            rs.getString("nama_barang"),
                            rs.getInt("jumlah"),
                            rs.getInt("harga_satuan"),
                            rs.getInt("total"),
                            rs.getString("tanggal")
                        };
                model.addRow(data);
            }
        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cekStokBarangTisu() {
        try {
            String sql = "SELECT SUM(jumlah) AS total_barang "
                    + "FROM operasional "
                    + "WHERE nama_barang = 'Tisu Galon';";
            st = cn.createStatement();
            rs = st.executeQuery(sql);
//            ImageIcon originalIcon = new ImageIcon("src/picture/tisu.png");

            if (rs.next()) {
                int totalBarang = rs.getInt("total_barang");
                if (totalBarang < 5) {
                    btnCekTisuGalon.setBackground(Color.red);
                    Notification notiPeringatan = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Tisu Galon Kurang dari 5pcs, Tersisa " + totalBarang + "pcs");
                    notiPeringatan.showNotification();
////                    ImageIcon icon = recolorImage(originalIcon, Color.RED);
//                    labelTisuGalon.setIcon(icon);
                } else if (totalBarang < 10) // Total Barang 1 = 100pcs
                {
                    btnCekTisuGalon.setBackground(Color.yellow);
//                    ImageIcon icon = recolorImage(originalIcon, Color.yellow);
//                    labelTisuGalon.setIcon(icon);
                    Notification notiPeringatan = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Tisu Galon Kurang dari 10pcs, Tersisa " + totalBarang + "pcs");
                    notiPeringatan.showNotification();
                } else {
                    btnCekTisuGalon.setBackground(Color.green);
                    Notification notiInfo = new Notification(this, Notification.Type.INFO, Notification.Location.TOP_CENTER, "Stok Tisu Galon aman, Total Stok " + (totalBarang) + " pcs");
                    notiInfo.showNotification();
                }
            } else {
                System.out.println("Data tidak ditemukan.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cekStokGalon1pcs() {
        try {
            String sql = "SELECT SUM(jumlah) AS total_barang "
                    + "FROM operasional "
                    + "WHERE nama_barang = 'Galon'";
            st = cn.createStatement();
            rs = st.executeQuery(sql);
//            ImageIcon originalIcon = new ImageIcon("src/picture/galon.png");
            if (rs.next()) {
                int totalBarang = rs.getInt("total_barang");
                if (totalBarang < 5) {
                    btnCekGalon.setBackground(Color.red);
                    Notification noti = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Galon Kurang Dari 5pcs, Total Stok " + totalBarang + "pcs");
                    noti.showNotification();
//                    ImageIcon icon = recolorImage(originalIcon, Color.RED);
//                    labelGalon.setIcon(icon);
                } else if (totalBarang < 10) {
                    btnCekGalon.setBackground(Color.yellow);
//                    ImageIcon icon = recolorImage(originalIcon, Color.YELLOW);
//                    labelGalon.setIcon(icon);
                    Notification noti = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Galonn Kurang Dari 10pcs, Total Stok " + totalBarang + "pcs");
                    noti.showNotification();
                    System.out.println(totalBarang);
                } else {
                    btnCekGalon.setBackground(Color.green);
                    Notification noti = new Notification(this, Notification.Type.INFO, Notification.Location.TOP_CENTER, "Stok Galon Aman, Total Stok " + totalBarang + " pcs");
                    noti.showNotification();
                    System.out.println(totalBarang);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cekStokTutupGalon1000pcs() {
        try {
            String sql = "SELECT SUM(jumlah) AS total_barang "
                    + "FROM operasional "
                    + "WHERE nama_barang = 'Tutup Galon'";
            st = cn.createStatement();
            rs = st.executeQuery(sql);

//            ImageIcon originalIcon = new ImageIcon("src/picture/tutup_galon.png");
            if (rs.next()) {
                int totalBarang = rs.getInt("total_barang");
                if (totalBarang < 5) {
                    btnCekTutupGalon.setBackground(Color.red);
                    Notification noti = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Tutup Galon Kurang Dari 5pcs, Total Stok " + totalBarang + "pcs");
                    noti.showNotification();
//                    ImageIcon icon = recolorImage(originalIcon, Color.RED);
//                    labelTutupGalon.setIcon(icon);

                } else if (totalBarang < 10) // Total Barang 1 = 1000pcs
                {
                    btnCekTutupGalon.setBackground(Color.yellow);
//                    ImageIcon icon = recolorImage(originalIcon, Color.yellow);
//                    labelTutupGalon.setIcon(icon);
                    Notification noti = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Tutup Galon Kurang Dari 10pcs, Total Stok " + totalBarang + "pcs");
                    noti.showNotification();
                } else {
                    btnCekTutupGalon.setBackground(Color.green);
                    Notification noti = new Notification(this, Notification.Type.INFO, Notification.Location.TOP_CENTER, "Stok Tutup Galon Aman, Total Stok " + totalBarang + " pcs");
                    noti.showNotification();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cekStokSegelGalon200pcs() {
        try {
            String sql = "SELECT SUM(jumlah) AS total_barang "
                    + "FROM operasional "
                    + "WHERE nama_barang = 'Segel Galon'";
            st = cn.createStatement();
            rs = st.executeQuery(sql);

//            ImageIcon originalIcon = new ImageIcon("src/picture/segel.png");
            if (rs.next()) {
                int totalBarang = rs.getInt("total_barang");
                if (totalBarang < 5) {
                    btnCekSegelGalon.setBackground(Color.red);
                    Notification noti = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Segel Galon Kurang dari 5pcs, Total Stok " + totalBarang + "pcs");
                    noti.showNotification();
//                    ImageIcon redIcon = recolorImage(originalIcon, Color.RED);
//                    labelSegelGalon.setIcon(redIcon);
                } else if (totalBarang < 10) {
                    btnCekSegelGalon.setBackground(Color.yellow);
//                    ImageIcon redIcon = recolorImage(originalIcon, Color.RED);
//                    labelSegelGalon.setIcon(redIcon);
                    Notification noti = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Segel Galon Kurang dari 10pcs, Total Stok " + totalBarang + "pcs");
                    noti.showNotification();
                } else {
                    btnCekSegelGalon.setBackground(Color.green);
                    Notification noti = new Notification(this, Notification.Type.INFO, Notification.Location.TOP_CENTER, "Stok Segel Galon Aman, Total Stok " + totalBarang + " pcs");
                    noti.showNotification();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cekStokSedimenPcs() {
        try {
            String sql = "SELECT SUM(jumlah) AS total_barang "
                    + "FROM operasional "
                    + "WHERE nama_barang = 'Sedimen'";
            st = cn.createStatement();
            rs = st.executeQuery(sql);
//            ImageIcon originalIcon = new ImageIcon("src/picture/sedimen.png");

            if (rs.next()) {
                int totalBarang = rs.getInt("total_barang");
                if (totalBarang < 5) {
                    btnCekSedimen.setBackground(Color.red);
                    Notification noti = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Sedimen Kurang Dari 5pcs, Total Stok " + totalBarang + "pcs");
                    noti.showNotification();
//                    ImageIcon redIcon = recolorImage(originalIcon, Color.RED);
//                    labelSedimen.setIcon(redIcon);
                } else if (totalBarang < 10) {
                    btnCekSedimen.setBackground(Color.yellow);
//                    ImageIcon redIcon = recolorImage(originalIcon, Color.yellow);
//                    labelSedimen.setIcon(redIcon);
                    Notification noti = new Notification(this, Notification.Type.WARNING, Notification.Location.TOP_CENTER, "Stok Sedimen Kurang Dari 10pcs, Total Stok " + totalBarang + "pcs");
                    noti.showNotification();
                } else {
                    btnCekSedimen.setBackground(Color.green);
                    Notification noti = new Notification(this, Notification.Type.INFO, Notification.Location.TOP_CENTER, "Stok Sedimen Aman, Total stok " + totalBarang + " pcs");
                    noti.showNotification();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        FRAME = new javax.swing.JPanel();
        HEADER = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        ISI = new javax.swing.JPanel();
        CUSTOMER = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        txtnamacustomer = new javax.swing.JTextField();
        txtidcustomer = new javax.swing.JTextField();
        txtalamat = new javax.swing.JTextField();
        txttelephone = new javax.swing.JTextField();
        cmbjeniscustomer = new javax.swing.JComboBox<>();
        btnhapuscustomer = new javax.swing.JButton();
        btnbatalcustomer = new javax.swing.JButton();
        btntambahcustomer = new javax.swing.JButton();
        txtjeniscustomer = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblcustomer = new javax.swing.JTable();
        jScrollPane13 = new javax.swing.JScrollPane();
        tblPelangganBarang = new javax.swing.JTable();
        jLabel85 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        txtcaricustomer = new javax.swing.JTextField();
        cmbcaricustomer = new javax.swing.JComboBox<>();
        jLabel41 = new javax.swing.JLabel();
        jPanel28 = new javax.swing.JPanel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        txtnamacustomer1 = new javax.swing.JTextField();
        txtidcustomer1 = new javax.swing.JTextField();
        txtalamat1 = new javax.swing.JTextField();
        txttelephone1 = new javax.swing.JTextField();
        btnhapuscustomer1 = new javax.swing.JButton();
        btnbatalcustomer1 = new javax.swing.JButton();
        btntambahcustomer1 = new javax.swing.JButton();
        jLabel84 = new javax.swing.JLabel();
        jPanel32 = new javax.swing.JPanel();
        txtcaricustomer1 = new javax.swing.JTextField();
        cmbcaricustomer1 = new javax.swing.JComboBox<>();
        PENJUALAN = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        cmbjeniscustomer2 = new javax.swing.JComboBox<>();
        txthargasatuan2 = new javax.swing.JTextField();
        txtjumlahpenjualan = new javax.swing.JTextField();
        cmbstatus = new javax.swing.JComboBox<>();
        txtnamacustomer2 = new javax.swing.JTextField();
        txtidcustomer2 = new javax.swing.JTextField();
        btnhapus2 = new javax.swing.JButton();
        btntambahpenjualan = new javax.swing.JButton();
        btnbayarcetak = new javax.swing.JButton();
        txtstatus = new javax.swing.JTextField();
        txtjeniscustomer2 = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblcaricustomer2 = new javax.swing.JTable();
        cmbcaricustomer2 = new javax.swing.JComboBox<>();
        txtcaricustomer2 = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        btnbatal2 = new javax.swing.JButton();
        txtidtransaksi = new javax.swing.JTextField();
        btnBayar = new javax.swing.JButton();
        btnDataTerpilih = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblpenjualan = new javax.swing.JTable();
        btnLaporan = new javax.swing.JButton();
        lblTotalHarga = new javax.swing.JLabel();
        btnCetakBarcode = new javax.swing.JButton();
        jPanel27 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        tblCariKurir = new javax.swing.JTable();
        cbbCariKurir = new javax.swing.JComboBox<>();
        txtCariKurir = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        txtNamaKurirPengantar = new javax.swing.JTextField();
        jLabel75 = new javax.swing.JLabel();
        cbbKendaraanKurir = new javax.swing.JComboBox<>();
        jLabel77 = new javax.swing.JLabel();
        txtPlatNomorKurir = new javax.swing.JTextField();
        OPERASIONAL = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jPanel19 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbloperasional = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        txtkodebarang = new javax.swing.JTextField();
        txtjumlahbarang = new javax.swing.JTextField();
        txthargasatuan = new javax.swing.JTextField();
        cmbnamabarang = new javax.swing.JComboBox<>();
        txtnamabarang = new javax.swing.JTextField();
        txttotalbarang = new javax.swing.JTextField();
        cmbkodebarang = new javax.swing.JComboBox<>();
        jLabel29 = new javax.swing.JLabel();
        txtidbarang = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        txtcaribarang = new javax.swing.JTextField();
        cmbcaribarang = new javax.swing.JComboBox<>();
        jLabel22 = new javax.swing.JLabel();
        labelTutupGalon = new javax.swing.JLabel();
        labelTisuGalon = new javax.swing.JLabel();
        labelSegelGalon = new javax.swing.JLabel();
        labelGalon = new javax.swing.JLabel();
        labelSedimen = new javax.swing.JLabel();
        btntambahoperasional = new javax.swing.JButton();
        btnbatal1 = new javax.swing.JButton();
        btnhapus1 = new javax.swing.JButton();
        btnLaporan1 = new javax.swing.JButton();
        btnCekTisuGalon = new javax.swing.JButton();
        btnCekGalon = new javax.swing.JButton();
        btnCekTutupGalon = new javax.swing.JButton();
        btnCekSegelGalon = new javax.swing.JButton();
        btnCekSedimen = new javax.swing.JButton();
        LAPORAN_PENJUALAN = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jPanel20 = new javax.swing.JPanel();
        jLabel50 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        cmbBulan = new javax.swing.JComboBox<>();
        cmbJudulBulan = new javax.swing.JComboBox<>();
        jLabel43 = new javax.swing.JLabel();
        cmbTahun = new javax.swing.JComboBox<>();
        cmbJenisCustomer = new javax.swing.JComboBox<>();
        jLabel45 = new javax.swing.JLabel();
        btnTampilkan = new javax.swing.JButton();
        btnBersihBulanan = new javax.swing.JButton();
        btnCetak = new javax.swing.JButton();
        jLabel70 = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
        cbbJenisBarangPenjualan = new javax.swing.JComboBox<>();
        jLabel78 = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        lblTotalTransaksi = new javax.swing.JLabel();
        lblTotalBarangTerjual = new javax.swing.JLabel();
        lblTotalPendapatanN = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableLaporanBulananBarang = new javax.swing.JTable();
        btnKembaliPenjualan = new javax.swing.JButton();
        LAPORAN_OPERASIONAL = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jLabel51 = new javax.swing.JLabel();
        jPanel24 = new javax.swing.JPanel();
        jLabel53 = new javax.swing.JLabel();
        cmbBulan1 = new javax.swing.JComboBox<>();
        jLabel54 = new javax.swing.JLabel();
        cmbTahun1 = new javax.swing.JComboBox<>();
        cmbJenisBarang = new javax.swing.JComboBox<>();
        jLabel56 = new javax.swing.JLabel();
        btnTampilkan1 = new javax.swing.JButton();
        btnKembaliPenjualan1 = new javax.swing.JButton();
        btnBersihOperasional = new javax.swing.JButton();
        jPanel25 = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        lblTotalTransaksiBarang = new javax.swing.JLabel();
        lblTotalBeliBarang = new javax.swing.JLabel();
        lblTotalPengeluaran = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tableLaporanOperasional = new javax.swing.JTable();
        KURIR = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        jLabel52 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabelKurir = new javax.swing.JTable();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        txtIdKurir = new javax.swing.JTextField();
        txtNoHP = new javax.swing.JTextField();
        txtPlatNomot = new javax.swing.JTextField();
        jScrollPane8 = new javax.swing.JScrollPane();
        txtAlamatKurir = new javax.swing.JTextArea();
        cbbJenisKendaraan = new javax.swing.JComboBox<>();
        cbbStatusKurir = new javax.swing.JComboBox<>();
        btnTambahKurir = new javax.swing.JButton();
        btnUbahKurir = new javax.swing.JButton();
        btnUbahKurir1 = new javax.swing.JButton();
        btnUbahKurir2 = new javax.swing.JButton();
        txtPencarian = new javax.swing.JTextField();
        jLabel76 = new javax.swing.JLabel();
        txtNamaKurir = new javax.swing.JTextField();
        PENDAPATANIsiUlangGalon = new javax.swing.JPanel();
        jPanel29 = new javax.swing.JPanel();
        jLabel67 = new javax.swing.JLabel();
        jPanel30 = new javax.swing.JPanel();
        jLabel68 = new javax.swing.JLabel();
        cmbBulanPendapatan = new javax.swing.JComboBox<>();
        jLabel69 = new javax.swing.JLabel();
        cmbTahunPendapatan = new javax.swing.JComboBox<>();
        btnTampilPenjualanOpearasional1 = new javax.swing.JButton();
        bersihPendapatan = new javax.swing.JButton();
        btnTampilPenjualanOpearasional2 = new javax.swing.JButton();
        jPanel31 = new javax.swing.JPanel();
        jLabel71 = new javax.swing.JLabel();
        labelOperasionalPendapatan = new javax.swing.JLabel();
        labelPenjualanPendapatan = new javax.swing.JLabel();
        labelPendapatan = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        btnCetakPendapatanBulanan = new javax.swing.JButton();
        btnCetakPendapatanBulanan1 = new javax.swing.JButton();
        jScrollPane9 = new javax.swing.JScrollPane();
        tabelPenjualanforPendapatan = new javax.swing.JTable();
        jScrollPane11 = new javax.swing.JScrollPane();
        tabelOperasionalforPendapatan = new javax.swing.JTable();
        jScrollPane12 = new javax.swing.JScrollPane();
        tabelPendapatan = new javax.swing.JTable();
        MENU = new javax.swing.JPanel();
        menu = new javax.swing.JPanel();
        customer = new javax.swing.JButton();
        penjualan = new javax.swing.JButton();
        operasional = new javax.swing.JButton();
        kurir = new javax.swing.JButton();
        pendapatanIsiUlangGalon = new javax.swing.JButton();
        PenjualanBarang = new javax.swing.JButton();
        keluar = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        jLabel44 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        FRAME.setBackground(new java.awt.Color(51, 51, 51));
        FRAME.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 255), 2));
        FRAME.setPreferredSize(new java.awt.Dimension(1920, 1080));

        HEADER.setBackground(new java.awt.Color(0, 0, 255));

        jLabel1.setBackground(new java.awt.Color(204, 204, 204));
        jLabel1.setFont(new java.awt.Font("Rockwell", 3, 36)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("POINT OF SALES DEPOT WATERMEN");
        jLabel1.setToolTipText("");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout HEADERLayout = new javax.swing.GroupLayout(HEADER);
        HEADER.setLayout(HEADERLayout);
        HEADERLayout.setHorizontalGroup(
            HEADERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, HEADERLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        HEADERLayout.setVerticalGroup(
            HEADERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HEADERLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        ISI.setBackground(new java.awt.Color(0, 0, 255));
        ISI.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        ISI.setPreferredSize(new java.awt.Dimension(1, 955));
        ISI.setRequestFocusEnabled(false);
        ISI.setVerifyInputWhenFocusTarget(false);

        CUSTOMER.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        CUSTOMER.setPreferredSize(new java.awt.Dimension(1670, 964));

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("CUSTOMER");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel3)
                .addGap(5, 5, 5))
        );

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("TAMBAH PELANGGAN");

        jPanel9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel23.setText("ID");

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel24.setText("Nama");

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel26.setText("Alamat");

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel30.setText("Telephone");

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel25.setText("Pelanggan");

        txtidcustomer.setEnabled(false);

        cmbjeniscustomer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pribadi", "Warung", "Agen" }));
        cmbjeniscustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbjeniscustomerActionPerformed(evt);
            }
        });

        btnhapuscustomer.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnhapuscustomer.setText("HAPUS");
        btnhapuscustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnhapuscustomerActionPerformed(evt);
            }
        });

        btnbatalcustomer.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnbatalcustomer.setText("BATAL");
        btnbatalcustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbatalcustomerActionPerformed(evt);
            }
        });

        btntambahcustomer.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btntambahcustomer.setText("TAMBAH");
        btntambahcustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btntambahcustomerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel24)
                            .addComponent(jLabel26)
                            .addComponent(jLabel30)
                            .addComponent(jLabel23))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txttelephone)
                            .addComponent(txtalamat)
                            .addComponent(txtnamacustomer)
                            .addComponent(txtidcustomer)))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(cmbjeniscustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txtjeniscustomer, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnhapuscustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnbatalcustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(btntambahcustomer)))))
                .addGap(20, 20, 20))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(txtidcustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(txtnamacustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(txtalamat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(txttelephone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(cmbjeniscustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtjeniscustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btntambahcustomer)
                    .addComponent(btnbatalcustomer)
                    .addComponent(btnhapuscustomer))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        tblcustomer.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID Customer", "Nama Customer", "Jenis Customer", "Alamat", "Telephone"
            }
        ));
        tblcustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblcustomerMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblcustomer);

        tblPelangganBarang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "NAMA", "TELEPHONE", "TELEPHONE"
            }
        ));
        tblPelangganBarang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPelangganBarangMouseClicked(evt);
            }
        });
        jScrollPane13.setViewportView(tblPelangganBarang);

        jLabel85.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel85.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel85.setText("Customer Galon");

        jLabel86.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel86.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel86.setText("Customer Barang");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 1064, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLabel85, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel86, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel85)
                .addGap(10, 10, 10)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel86)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("CARI PELANGGAN");

        jPanel7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtcaricustomer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtcaricustomerKeyPressed(evt);
            }
        });

        cmbcaricustomer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "id_customer", "nama_customer", "jenis_customer" }));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbcaricustomer, 0, 196, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(txtcaricustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtcaricustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbcaricustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel41.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel41.setText("TAMBAH PELANGGAN BARANG");

        jPanel28.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel79.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel79.setText("ID");

        jLabel80.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel80.setText("Nama");

        jLabel81.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel81.setText("Alamat");

        jLabel82.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel82.setText("Telephone");

        txtidcustomer1.setEnabled(false);

        btnhapuscustomer1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnhapuscustomer1.setText("HAPUS");
        btnhapuscustomer1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnhapuscustomer1ActionPerformed(evt);
            }
        });

        btnbatalcustomer1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnbatalcustomer1.setText("BATAL");
        btnbatalcustomer1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbatalcustomer1ActionPerformed(evt);
            }
        });

        btntambahcustomer1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btntambahcustomer1.setText("TAMBAH");
        btntambahcustomer1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btntambahcustomer1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel80)
                            .addComponent(jLabel81)
                            .addComponent(jLabel82)
                            .addComponent(jLabel79))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txttelephone1)
                            .addComponent(txtalamat1)
                            .addComponent(txtnamacustomer1)
                            .addComponent(txtidcustomer1)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel28Layout.createSequentialGroup()
                        .addGap(0, 191, Short.MAX_VALUE)
                        .addComponent(btnhapuscustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnbatalcustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btntambahcustomer1)))
                .addGap(20, 20, 20))
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel79)
                    .addComponent(txtidcustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel80)
                    .addComponent(txtnamacustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel81)
                    .addComponent(txtalamat1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel82)
                    .addComponent(txttelephone1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btntambahcustomer1)
                    .addComponent(btnbatalcustomer1)
                    .addComponent(btnhapuscustomer1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel84.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel84.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel84.setText("CARI PELANGGAN");

        jPanel32.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtcaricustomer1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtcaricustomer1KeyPressed(evt);
            }
        });

        cmbcaricustomer1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "id_customer", "nama_customer", "jenis_customer" }));

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32.setLayout(jPanel32Layout);
        jPanel32Layout.setHorizontalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbcaricustomer1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(txtcaricustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel32Layout.setVerticalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtcaricustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbcaricustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout CUSTOMERLayout = new javax.swing.GroupLayout(CUSTOMER);
        CUSTOMER.setLayout(CUSTOMERLayout);
        CUSTOMERLayout.setHorizontalGroup(
            CUSTOMERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CUSTOMERLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CUSTOMERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(CUSTOMERLayout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(CUSTOMERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel84, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        CUSTOMERLayout.setVerticalGroup(
            CUSTOMERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CUSTOMERLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(CUSTOMERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(CUSTOMERLayout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel41)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel84)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 736, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PENJUALAN.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        PENJUALAN.setPreferredSize(new java.awt.Dimension(1670, 964));

        jPanel10.setBackground(new java.awt.Color(0, 0, 255));
        jPanel10.setPreferredSize(new java.awt.Dimension(1500, 69));

        jPanel8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel8.setPreferredSize(new java.awt.Dimension(1500, 43));

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("PENJUALAN");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 1614, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel32.setText("No Transaksi");

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel33.setText("ID Customer");

        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel39.setText("Nama Customer");

        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel34.setText("Jenis Customer");

        jLabel40.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel40.setText("Harga");

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel38.setText("Status");

        jLabel42.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel42.setText("Jumlah ");

        cmbjeniscustomer2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pribadi", "Warung", "Agen" }));
        cmbjeniscustomer2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbjeniscustomer2ActionPerformed(evt);
            }
        });

        txthargasatuan2.setEnabled(false);

        cmbstatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Belum Lunas", "Lunas", " " }));
        cmbstatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbstatusActionPerformed(evt);
            }
        });

        txtidcustomer2.setEnabled(false);

        btnhapus2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnhapus2.setText("HAPUS");
        btnhapus2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnhapus2ActionPerformed(evt);
            }
        });

        btntambahpenjualan.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btntambahpenjualan.setText("SIMPAN");
        btntambahpenjualan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btntambahpenjualanActionPerformed(evt);
            }
        });

        btnbayarcetak.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnbayarcetak.setText("Cetak Laporan Harian");
        btnbayarcetak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbayarcetakActionPerformed(evt);
            }
        });

        txtstatus.setEnabled(false);

        txtjeniscustomer2.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        txtjeniscustomer2.setDisabledTextColor(new java.awt.Color(255, 0, 0));
        txtjeniscustomer2.setEnabled(false);

        jPanel11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));

        tblcaricustomer2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID Customer", "Nama Customer", "Jenis Customer", "Alamat", "Telephone"
            }
        ));
        tblcaricustomer2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblcaricustomer2MouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tblcaricustomer2);

        cmbcaricustomer2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        cmbcaricustomer2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "id_customer", "nama_customer", "jenis_customer", " " }));

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
                .addComponent(cmbcaricustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtcaricustomer2)
                .addContainerGap())
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbcaricustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(txtcaricustomer2)
                        .addGap(3, 3, 3)))
                .addContainerGap())
        );

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("CARI PELANGGAN");

        btnbatal2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnbatal2.setText("BATAL");
        btnbatal2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbatal2ActionPerformed(evt);
            }
        });

        txtidtransaksi.setEnabled(false);

        btnBayar.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnBayar.setText("BAYAR ONLINE");
        btnBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBayarActionPerformed(evt);
            }
        });

        btnDataTerpilih.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        btnDataTerpilih.setText("CETAK BILL");
        btnDataTerpilih.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDataTerpilihActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        tblpenjualan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No Transaksi", "Id Pelanggan", "Nama Pelanggan", "Jenis Pelanggan", "Jumlah", "Harga", "Total", "Tanggal", "Status"
            }
        ));
        tblpenjualan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblpenjualanMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblpenjualan);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        btnLaporan.setText("LAPORAN DATA");
        btnLaporan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLaporanActionPerformed(evt);
            }
        });

        lblTotalHarga.setFont(new java.awt.Font("Tahoma", 0, 40)); // NOI18N
        lblTotalHarga.setForeground(new java.awt.Color(0, 179, 0));
        lblTotalHarga.setText("Rp. 0");

        btnCetakBarcode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnCetakBarcode.setText("Cetak Barcode");
        btnCetakBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakBarcodeActionPerformed(evt);
            }
        });

        jPanel27.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));

        tblCariKurir.setModel(new javax.swing.table.DefaultTableModel(
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
        tblCariKurir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCariKurirMouseClicked(evt);
            }
        });
        jScrollPane10.setViewportView(tblCariKurir);
        if (tblCariKurir.getColumnModel().getColumnCount() > 0) {
            tblCariKurir.getColumnModel().getColumn(0).setMaxWidth(25);
        }

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
                    .addGroup(jPanel27Layout.createSequentialGroup()
                        .addComponent(cbbCariKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtCariKurir))
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbbCariKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCariKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel55.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel55.setText("Nama Kurir");

        jLabel75.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel75.setText("Jenis Kendaraan");

        cbbKendaraanKurir.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Motor", "Mobil", "Pick-Up", " " }));
        cbbKendaraanKurir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbbKendaraanKurirActionPerformed(evt);
            }
        });

        jLabel77.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel77.setText("PLAT Nomor");

        javax.swing.GroupLayout PENJUALANLayout = new javax.swing.GroupLayout(PENJUALAN);
        PENJUALAN.setLayout(PENJUALANLayout);
        PENJUALANLayout.setHorizontalGroup(
            PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 1614, Short.MAX_VALUE)
            .addGroup(PENJUALANLayout.createSequentialGroup()
                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PENJUALANLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblTotalHarga)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                                .addComponent(btnhapus2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addComponent(jLabel77, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtPlatNomorKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addComponent(jLabel55, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtNamaKurirPengantar, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addComponent(jLabel75, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(cbbKendaraanKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(121, 121, 121)))
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addComponent(btnbatal2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btntambahpenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(btnBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnCetakBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnLaporan, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnDataTerpilih, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnbayarcetak, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(PENJUALANLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                                .addGap(20, 20, 20)
                                                .addComponent(txtidcustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(txtnamacustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(txtidtransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(jLabel38)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                                        .addComponent(cmbstatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtstatus, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel34)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                                            .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jLabel40, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(PENJUALANLayout.createSequentialGroup()
                                                    .addComponent(cmbjeniscustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(txtjeniscustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addGap(29, 29, 29)))
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel42)
                                            .addComponent(txtjumlahpenjualan, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                            .addComponent(txthargasatuan2))
                                        .addGap(118, 118, 118)))
                                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        PENJUALANLayout.setVerticalGroup(
            PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PENJUALANLayout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PENJUALANLayout.createSequentialGroup()
                        .addComponent(jLabel37)
                        .addGap(3, 3, 3)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PENJUALANLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addComponent(jLabel34)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(cmbjeniscustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtjeniscustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel42)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtjumlahpenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addGap(5, 5, 5)
                                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel32)
                                            .addComponent(txtidtransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, Short.MAX_VALUE)
                                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel33)
                                            .addComponent(txtidcustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(txtnamacustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel39))))
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel38)
                                        .addComponent(jLabel40))
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addGap(29, 29, 29)
                                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(cmbstatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtstatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txthargasatuan2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                .addGap(15, 15, 15)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PENJUALANLayout.createSequentialGroup()
                        .addGap(0, 7, Short.MAX_VALUE)
                        .addComponent(lblTotalHarga)
                        .addGap(122, 122, 122))
                    .addGroup(PENJUALANLayout.createSequentialGroup()
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btntambahpenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnbatal2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnhapus2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                                        .addComponent(btnBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(5, 5, 5)))
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addComponent(btnLaporan, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnCetakBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addComponent(btnDataTerpilih, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnbayarcetak, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtNamaKurirPengantar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel55))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel75)
                                    .addComponent(cbbKendaraanKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtPlatNomorKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel77))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        OPERASIONAL.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        OPERASIONAL.setPreferredSize(new java.awt.Dimension(1670, 964));
        OPERASIONAL.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                OPERASIONALComponentShown(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel5.setText("OPERASIONAL");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel5)
                .addGap(5, 5, 5))
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel10.setText("TUTUP GALON");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel15.setText("TISU GALON");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel15)
                .addGap(18, 18, 18))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jPanel17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel16.setText("SEGEL GALON");

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jPanel18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel17.setText("GALON");

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel17)
                .addGap(37, 37, 37))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jPanel19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel11.setText("SEDIMEN");

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel11)
                .addGap(29, 29, 29))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        tbloperasional.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "No", "ID Barang", "Kode Barang", "Nama Barang", "Jumlah", "Harga Satuan", "Total", "Tanggal"
            }
        ));
        tbloperasional.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbloperasionalMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tbloperasional);

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("TABEL OPERASIONAL");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel27.setText("Kode Barang");

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel28.setText("Nama Barang");

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel31.setText("Jumlah");

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel35.setText("Harga Satuan");

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel36.setText("Total");

        cmbnamabarang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tutup Galon", "Tisu Galon", "Segel Galon", "Galon", "Sedimen" }));
        cmbnamabarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbnamabarangActionPerformed(evt);
            }
        });

        txttotalbarang.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        txttotalbarang.setDisabledTextColor(new java.awt.Color(0, 153, 51));
        txttotalbarang.setEnabled(false);

        cmbkodebarang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tutup Galon", "Tisu Galon", "Segel Galon", "Galon", "Sedimen" }));
        cmbkodebarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbkodebarangActionPerformed(evt);
            }
        });

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel29.setText("ID Barang");

        txtidbarang.setEnabled(false);

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("INPUT BARANG");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel28)
                            .addComponent(jLabel31)
                            .addComponent(jLabel35)
                            .addComponent(jLabel27)
                            .addComponent(jLabel36)
                            .addComponent(jLabel29))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txttotalbarang)
                            .addComponent(txthargasatuan)
                            .addComponent(txtjumlahbarang)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(cmbnamabarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cmbkodebarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtkodebarang, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                                    .addComponent(txtnamabarang)))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(txtidbarang, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addGap(20, 20, 20))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(txtidbarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(txtkodebarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbkodebarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(cmbnamabarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtnamabarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(txtjumlahbarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(txthargasatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(txttotalbarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13))
        );

        jPanel15.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtcaribarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtcaribarangKeyPressed(evt);
            }
        });

        cmbcaribarang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "kode_barang", "nama_barang", "tanggal", " ", " " }));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("CARI BARANG");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(cmbcaribarang, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtcaribarang, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel22)
                .addGap(18, 18, 18)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtcaribarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbcaribarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        labelTutupGalon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/tutup_galon.png"))); // NOI18N

        labelTisuGalon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/tisu.png"))); // NOI18N

        labelSegelGalon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/segel.png"))); // NOI18N

        labelGalon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/galon.png"))); // NOI18N

        labelSedimen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/sedimen.png"))); // NOI18N

        btntambahoperasional.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btntambahoperasional.setText("TAMBAH");
        btntambahoperasional.setMaximumSize(new java.awt.Dimension(100, 40));
        btntambahoperasional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btntambahoperasionalActionPerformed(evt);
            }
        });

        btnbatal1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnbatal1.setText("BATAL");
        btnbatal1.setMaximumSize(new java.awt.Dimension(100, 40));
        btnbatal1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbatal1ActionPerformed(evt);
            }
        });

        btnhapus1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnhapus1.setText("HAPUS");
        btnhapus1.setMaximumSize(new java.awt.Dimension(100, 40));
        btnhapus1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnhapus1ActionPerformed(evt);
            }
        });

        btnLaporan1.setText("LAPORAN DATA");
        btnLaporan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLaporan1ActionPerformed(evt);
            }
        });

        btnCekTisuGalon.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnCekTisuGalon.setText("Cek Tisu Galon");
        btnCekTisuGalon.setMaximumSize(new java.awt.Dimension(100, 40));
        btnCekTisuGalon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekTisuGalonActionPerformed(evt);
            }
        });

        btnCekGalon.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnCekGalon.setText("Cek Galon");
        btnCekGalon.setMaximumSize(new java.awt.Dimension(100, 40));
        btnCekGalon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekGalonActionPerformed(evt);
            }
        });

        btnCekTutupGalon.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnCekTutupGalon.setText("Cek Tutup Galon");
        btnCekTutupGalon.setMaximumSize(new java.awt.Dimension(100, 40));
        btnCekTutupGalon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekTutupGalonActionPerformed(evt);
            }
        });

        btnCekSegelGalon.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnCekSegelGalon.setText("Cek Segel Galon");
        btnCekSegelGalon.setMaximumSize(new java.awt.Dimension(100, 40));
        btnCekSegelGalon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekSegelGalonActionPerformed(evt);
            }
        });

        btnCekSedimen.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnCekSedimen.setText("Cek Sedimen");
        btnCekSedimen.setMaximumSize(new java.awt.Dimension(100, 40));
        btnCekSedimen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekSedimenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout OPERASIONALLayout = new javax.swing.GroupLayout(OPERASIONAL);
        OPERASIONAL.setLayout(OPERASIONALLayout);
        OPERASIONALLayout.setHorizontalGroup(
            OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OPERASIONALLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(OPERASIONALLayout.createSequentialGroup()
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnbatal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnhapus1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btntambahoperasional, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(OPERASIONALLayout.createSequentialGroup()
                                        .addGap(38, 38, 38)
                                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(labelTutupGalon)
                                    .addComponent(btnCekTutupGalon, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(labelTisuGalon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(OPERASIONALLayout.createSequentialGroup()
                                        .addGap(34, 34, 34)
                                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(btnCekTisuGalon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(OPERASIONALLayout.createSequentialGroup()
                                        .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(labelGalon)
                                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                                .addGap(35, 35, 35)
                                                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(1, 1, 1)
                                        .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                                .addGap(40, 40, 40)
                                                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(73, 73, 73)
                                                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                                .addComponent(labelSegelGalon)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(labelSedimen, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(OPERASIONALLayout.createSequentialGroup()
                                        .addComponent(btnCekGalon, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnCekSegelGalon, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btnCekSedimen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, OPERASIONALLayout.createSequentialGroup()
                                .addGap(825, 825, 825)
                                .addComponent(btnLaporan1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 53, Short.MAX_VALUE))
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        OPERASIONALLayout.setVerticalGroup(
            OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OPERASIONALLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(OPERASIONALLayout.createSequentialGroup()
                        .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(labelTutupGalon))
                            .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(OPERASIONALLayout.createSequentialGroup()
                                    .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(7, 7, 7)
                                    .addComponent(labelTisuGalon))
                                .addGroup(OPERASIONALLayout.createSequentialGroup()
                                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(labelGalon))
                                .addGroup(OPERASIONALLayout.createSequentialGroup()
                                    .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(7, 7, 7)
                                    .addComponent(labelSegelGalon))
                                .addGroup(OPERASIONALLayout.createSequentialGroup()
                                    .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelSedimen, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(OPERASIONALLayout.createSequentialGroup()
                                    .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnCekTisuGalon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnCekGalon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnCekSegelGalon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(16, 16, 16))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, OPERASIONALLayout.createSequentialGroup()
                                    .addComponent(btnCekTutupGalon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)))
                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                .addComponent(btnCekSedimen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                .addComponent(btntambahoperasional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(btnbatal1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnhapus1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnLaporan1)))
                            .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        LAPORAN_PENJUALAN.setBackground(new java.awt.Color(255, 255, 255));
        LAPORAN_PENJUALAN.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jPanel4.setBackground(new java.awt.Color(0, 153, 255));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setText("Laporan Bulanan Penjualan Barang");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel18)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel20.setBackground(new java.awt.Color(243, 243, 243));

        jLabel50.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel50.setText("Judul Bulan :");

        jLabel19.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel19.setText("Bulan :");

        cmbBulan.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbBulan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember" }));

        cmbJudulBulan.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbJudulBulan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember", " " }));

        jLabel43.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel43.setText("Tahun :");

        cmbTahun.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbTahun.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));

        cmbJenisCustomer.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbJenisCustomer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pribadi", "Warung", "Agen" }));

        jLabel45.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel45.setText("Jenis Customer :");

        btnTampilkan.setText("Tampilkan");
        btnTampilkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTampilkanActionPerformed(evt);
            }
        });

        btnBersihBulanan.setText("Bersih");
        btnBersihBulanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBersihBulananActionPerformed(evt);
            }
        });

        btnCetak.setText("Cetak");
        btnCetak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakActionPerformed(evt);
            }
        });

        jLabel70.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel70.setText("Status");

        cmbStatus.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Belum Lunas", "Lunas" }));

        cbbJenisBarangPenjualan.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cbbJenisBarangPenjualan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua Barang", "Tutup Galon", "Tisu Galon", "Segel Galon", "Galon", "Sedimen" }));

        jLabel78.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel78.setText("Jenis Barang :");

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addComponent(jLabel50)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbJudulBulan, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addGap(24, 24, 24)
                                .addComponent(cmbBulan, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addComponent(jLabel43)
                                .addGap(18, 18, 18)
                                .addComponent(cmbTahun, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel45)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbJenisCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel20Layout.createSequentialGroup()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addComponent(jLabel70)
                                .addGap(18, 18, 18)
                                .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addComponent(jLabel78)
                                .addGap(18, 18, 18)
                                .addComponent(cbbJenisBarangPenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnCetak, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnBersihBulanan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnTampilkan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel50)
                    .addComponent(cmbJudulBulan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel45)
                    .addComponent(cmbJenisCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(cmbBulan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTampilkan))
                .addGap(18, 18, 18)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel43)
                            .addComponent(cmbTahun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel70)
                            .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel78)
                            .addComponent(cbbJenisBarangPenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(btnBersihBulanan)
                        .addGap(18, 18, 18)
                        .addComponent(btnCetak)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel46.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel46.setText("Ringkasan Laporan");

        lblTotalTransaksi.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalTransaksi.setText("0");

        lblTotalBarangTerjual.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalBarangTerjual.setText("0");

        lblTotalPendapatanN.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalPendapatanN.setText("Rp. 0");

        jLabel47.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel47.setText("Total Transaksi :");

        jLabel48.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel48.setText("Total Barang Terjual :");

        jLabel49.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel49.setText("Total Pendapatan :");

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel46)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel47)
                            .addComponent(jLabel48)
                            .addComponent(jLabel49))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTotalTransaksi)
                            .addComponent(lblTotalBarangTerjual)
                            .addComponent(lblTotalPendapatanN))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel46)
                .addGap(18, 18, 18)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalTransaksi)
                    .addComponent(jLabel47))
                .addGap(18, 18, 18)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalBarangTerjual)
                    .addComponent(jLabel48))
                .addGap(18, 18, 18)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalPendapatanN)
                    .addComponent(jLabel49))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tableLaporanBulananBarang.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane6.setViewportView(tableLaporanBulananBarang);

        btnKembaliPenjualan.setText("Kembali");
        btnKembaliPenjualan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliPenjualanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout LAPORAN_PENJUALANLayout = new javax.swing.GroupLayout(LAPORAN_PENJUALAN);
        LAPORAN_PENJUALAN.setLayout(LAPORAN_PENJUALANLayout);
        LAPORAN_PENJUALANLayout.setHorizontalGroup(
            LAPORAN_PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(LAPORAN_PENJUALANLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 1067, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(LAPORAN_PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(LAPORAN_PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnKembaliPenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(51, Short.MAX_VALUE))
        );
        LAPORAN_PENJUALANLayout.setVerticalGroup(
            LAPORAN_PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LAPORAN_PENJUALANLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(LAPORAN_PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LAPORAN_PENJUALANLayout.createSequentialGroup()
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnKembaliPenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 284, Short.MAX_VALUE))
                    .addComponent(jScrollPane6))
                .addContainerGap())
        );

        LAPORAN_OPERASIONAL.setBackground(new java.awt.Color(255, 255, 255));
        LAPORAN_OPERASIONAL.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jPanel23.setBackground(new java.awt.Color(255, 153, 0));

        jLabel51.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setText("Laporan Bulanan OPERASIONAL");

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel51)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel24.setBackground(new java.awt.Color(243, 243, 243));

        jLabel53.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel53.setText("Bulan :");

        cmbBulan1.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbBulan1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember" }));

        jLabel54.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel54.setText("Tahun :");

        cmbTahun1.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbTahun1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));

        cmbJenisBarang.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbJenisBarang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tutup Galon (1000pcs)", "Tisu Galon (100pcs)", "Segel Galon (100pcs)", "Galon (1pcs)", "Sedimen (1pcs)" }));

        jLabel56.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel56.setText("Jenis Barang :");

        btnTampilkan1.setText("Tampilkan");
        btnTampilkan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTampilkan1ActionPerformed(evt);
            }
        });

        btnKembaliPenjualan1.setText("Kembali");
        btnKembaliPenjualan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliPenjualan1ActionPerformed(evt);
            }
        });

        btnBersihOperasional.setText("Bersih");
        btnBersihOperasional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBersihOperasionalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(jLabel53)
                        .addGap(24, 24, 24)
                        .addComponent(cmbBulan1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(jLabel54)
                        .addGap(18, 18, 18)
                        .addComponent(cmbTahun1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(jLabel56)
                        .addGap(18, 18, 18)
                        .addComponent(cmbJenisBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnTampilkan1, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(btnBersihOperasional, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnKembaliPenjualan1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel53)
                            .addComponent(cmbBulan1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel54)
                            .addComponent(cmbTahun1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(btnTampilkan1)
                        .addGap(18, 18, 18)
                        .addComponent(btnBersihOperasional)))
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel56)
                            .addComponent(cmbJenisBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(btnKembaliPenjualan1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel57.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel57.setText("Ringkasan Laporan");

        lblTotalTransaksiBarang.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalTransaksiBarang.setText("0");

        lblTotalBeliBarang.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalBeliBarang.setText("0");

        lblTotalPengeluaran.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalPengeluaran.setText("Rp. 0");

        jLabel58.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel58.setText("Total Transaksi :");

        jLabel59.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel59.setText("Total Beli Barang :");

        jLabel60.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel60.setText("Total Pengeluaran :");

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel57)
                    .addGroup(jPanel25Layout.createSequentialGroup()
                        .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel58)
                            .addComponent(jLabel59)
                            .addComponent(jLabel60))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTotalTransaksiBarang)
                            .addComponent(lblTotalBeliBarang)
                            .addComponent(lblTotalPengeluaran))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel57)
                .addGap(18, 18, 18)
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalTransaksiBarang)
                    .addComponent(jLabel58))
                .addGap(18, 18, 18)
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalBeliBarang)
                    .addComponent(jLabel59))
                .addGap(18, 18, 18)
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalPengeluaran)
                    .addComponent(jLabel60))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tableLaporanOperasional.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane7.setViewportView(tableLaporanOperasional);

        javax.swing.GroupLayout LAPORAN_OPERASIONALLayout = new javax.swing.GroupLayout(LAPORAN_OPERASIONAL);
        LAPORAN_OPERASIONAL.setLayout(LAPORAN_OPERASIONALLayout);
        LAPORAN_OPERASIONALLayout.setHorizontalGroup(
            LAPORAN_OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(LAPORAN_OPERASIONALLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 1088, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(LAPORAN_OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        LAPORAN_OPERASIONALLayout.setVerticalGroup(
            LAPORAN_OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LAPORAN_OPERASIONALLayout.createSequentialGroup()
                .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(LAPORAN_OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LAPORAN_OPERASIONALLayout.createSequentialGroup()
                        .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE))
                .addContainerGap())
        );

        KURIR.setBackground(new java.awt.Color(255, 255, 255));
        KURIR.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jPanel26.setBackground(new java.awt.Color(204, 204, 255));

        jLabel52.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(51, 51, 51));
        jLabel52.setText("Data Kurir");

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel52)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel52, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabelKurir.setModel(new javax.swing.table.DefaultTableModel(
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
        tabelKurir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelKurirMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tabelKurir);

        jLabel61.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel61.setForeground(new java.awt.Color(51, 51, 51));
        jLabel61.setText("ID :");

        jLabel62.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel62.setForeground(new java.awt.Color(51, 51, 51));
        jLabel62.setText("No. HP :");

        jLabel63.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel63.setForeground(new java.awt.Color(51, 51, 51));
        jLabel63.setText("Alamat :");

        jLabel64.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel64.setForeground(new java.awt.Color(51, 51, 51));
        jLabel64.setText("Jenis Kendaraan :");

        jLabel65.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel65.setForeground(new java.awt.Color(51, 51, 51));
        jLabel65.setText("PLAT Nomor :");

        jLabel66.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel66.setForeground(new java.awt.Color(51, 51, 51));
        jLabel66.setText("Status :");

        txtAlamatKurir.setColumns(20);
        txtAlamatKurir.setRows(5);
        jScrollPane8.setViewportView(txtAlamatKurir);

        cbbJenisKendaraan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Kendaraan", "Motor\t", "Mobil", "Pick-Up" }));
        cbbJenisKendaraan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbbJenisKendaraanActionPerformed(evt);
            }
        });

        cbbStatusKurir.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Status", "aktif", "nonaktif" }));

        btnTambahKurir.setText("Tambah");
        btnTambahKurir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahKurirActionPerformed(evt);
            }
        });

        btnUbahKurir.setText("Ubah");
        btnUbahKurir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahKurirActionPerformed(evt);
            }
        });

        btnUbahKurir1.setText("Delete");
        btnUbahKurir1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahKurir1ActionPerformed(evt);
            }
        });

        btnUbahKurir2.setText("Bersih");
        btnUbahKurir2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahKurir2ActionPerformed(evt);
            }
        });

        jLabel76.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel76.setForeground(new java.awt.Color(51, 51, 51));
        jLabel76.setText("Nama :");

        javax.swing.GroupLayout KURIRLayout = new javax.swing.GroupLayout(KURIR);
        KURIR.setLayout(KURIRLayout);
        KURIRLayout.setHorizontalGroup(
            KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(KURIRLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(KURIRLayout.createSequentialGroup()
                        .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel63, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel62, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                            .addComponent(jLabel61, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel76, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane8)
                            .addComponent(txtNoHP)
                            .addComponent(txtIdKurir)
                            .addComponent(txtNamaKurir))
                        .addGap(80, 80, 80)
                        .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel65, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel64, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtPlatNomot)
                            .addComponent(cbbStatusKurir, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbbJenisKendaraan, 0, 186, Short.MAX_VALUE))
                        .addGap(80, 80, 80)
                        .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUbahKurir1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnTambahKurir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnUbahKurir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(btnUbahKurir2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(188, 188, 188))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, KURIRLayout.createSequentialGroup()
                        .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4)
                            .addGroup(KURIRLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(txtPencarian, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(50, 50, 50))))
        );
        KURIRLayout.setVerticalGroup(
            KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(KURIRLayout.createSequentialGroup()
                .addComponent(jPanel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(KURIRLayout.createSequentialGroup()
                        .addComponent(btnTambahKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUbahKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUbahKurir1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUbahKurir2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(KURIRLayout.createSequentialGroup()
                        .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel61)
                            .addComponent(txtIdKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(KURIRLayout.createSequentialGroup()
                                .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel76)
                                    .addComponent(txtNamaKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel62)
                                    .addComponent(txtNoHP, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, KURIRLayout.createSequentialGroup()
                                        .addComponent(jLabel63)
                                        .addGap(36, 36, 36))))
                            .addGroup(KURIRLayout.createSequentialGroup()
                                .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel65)
                                    .addComponent(txtPlatNomot, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(KURIRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel66)
                                    .addComponent(cbbStatusKurir, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(cbbJenisKendaraan, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel64))
                .addGap(30, 30, 30)
                .addComponent(txtPencarian, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                .addContainerGap())
        );

        PENDAPATANIsiUlangGalon.setBackground(new java.awt.Color(255, 255, 255));
        PENDAPATANIsiUlangGalon.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jPanel29.setBackground(new java.awt.Color(204, 204, 255));

        jLabel67.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel67.setForeground(new java.awt.Color(51, 51, 51));
        jLabel67.setText("Laporan Pendapatan Isi Ulang Galon");

        javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel67, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel67, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel30.setBackground(new java.awt.Color(243, 243, 243));

        jLabel68.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel68.setText("Bulan :");

        cmbBulanPendapatan.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbBulanPendapatan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember" }));

        jLabel69.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel69.setText("Tahun :");

        cmbTahunPendapatan.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbTahunPendapatan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Tahun", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));
        cmbTahunPendapatan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTahunPendapatanActionPerformed(evt);
            }
        });

        btnTampilPenjualanOpearasional1.setText("Tampilkan Bulanan");
        btnTampilPenjualanOpearasional1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTampilPenjualanOpearasional1ActionPerformed(evt);
            }
        });

        bersihPendapatan.setText("Bersih");
        bersihPendapatan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bersihPendapatanActionPerformed(evt);
            }
        });

        btnTampilPenjualanOpearasional2.setText("Tampilkan Semua");
        btnTampilPenjualanOpearasional2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTampilPenjualanOpearasional2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel30Layout.createSequentialGroup()
                        .addComponent(jLabel68)
                        .addGap(24, 24, 24)
                        .addComponent(cmbBulanPendapatan, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel30Layout.createSequentialGroup()
                        .addComponent(jLabel69)
                        .addGap(18, 18, 18)
                        .addComponent(cmbTahunPendapatan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(27, 27, 27)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnTampilPenjualanOpearasional2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnTampilPenjualanOpearasional1, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(bersihPendapatan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(42, 42, 42))
        );
        jPanel30Layout.setVerticalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel68)
                    .addComponent(cmbBulanPendapatan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bersihPendapatan))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel69)
                    .addComponent(cmbTahunPendapatan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTampilPenjualanOpearasional1))
                .addGap(18, 18, 18)
                .addComponent(btnTampilPenjualanOpearasional2)
                .addContainerGap())
        );

        jLabel71.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel71.setText("Ringkasan Laporan");

        labelOperasionalPendapatan.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        labelOperasionalPendapatan.setText("Rp. 0");

        labelPenjualanPendapatan.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        labelPenjualanPendapatan.setText("Rp. 0");

        labelPendapatan.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        labelPendapatan.setText("Rp. 0");

        jLabel72.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel72.setText("Total Operasional :");

        jLabel73.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel73.setText("Total Penjualan   :");

        jLabel74.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel74.setText("Total Pendapatan :");

        btnCetakPendapatanBulanan.setText("Cetak Pendapatan Bulanan");
        btnCetakPendapatanBulanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakPendapatanBulananActionPerformed(evt);
            }
        });

        btnCetakPendapatanBulanan1.setText("Cetak Seluruh Pendapatan");
        btnCetakPendapatanBulanan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakPendapatanBulanan1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel31Layout.createSequentialGroup()
                        .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel72, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel74, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel73, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(labelPenjualanPendapatan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                                .addComponent(labelPendapatan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(labelOperasionalPendapatan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCetakPendapatanBulanan, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCetakPendapatanBulanan1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(24, Short.MAX_VALUE))
                    .addGroup(jPanel31Layout.createSequentialGroup()
                        .addComponent(jLabel71, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel71)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelOperasionalPendapatan)
                    .addComponent(jLabel72))
                .addGap(18, 18, 18)
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPenjualanPendapatan)
                    .addComponent(jLabel73)
                    .addComponent(btnCetakPendapatanBulanan))
                .addGap(18, 18, 18)
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPendapatan)
                    .addComponent(jLabel74)
                    .addComponent(btnCetakPendapatanBulanan1))
                .addGap(13, 13, 13))
        );

        tabelPenjualanforPendapatan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane9.setViewportView(tabelPenjualanforPendapatan);

        tabelOperasionalforPendapatan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane11.setViewportView(tabelOperasionalforPendapatan);

        tabelPendapatan.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane12.setViewportView(tabelPendapatan);

        javax.swing.GroupLayout PENDAPATANIsiUlangGalonLayout = new javax.swing.GroupLayout(PENDAPATANIsiUlangGalon);
        PENDAPATANIsiUlangGalon.setLayout(PENDAPATANIsiUlangGalonLayout);
        PENDAPATANIsiUlangGalonLayout.setHorizontalGroup(
            PENDAPATANIsiUlangGalonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(PENDAPATANIsiUlangGalonLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(PENDAPATANIsiUlangGalonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 969, Short.MAX_VALUE)
                    .addComponent(jScrollPane11))
                .addGap(18, 18, 18)
                .addGroup(PENDAPATANIsiUlangGalonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(PENDAPATANIsiUlangGalonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel31, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.Alignment.TRAILING)))
                .addGap(20, 20, 20))
        );
        PENDAPATANIsiUlangGalonLayout.setVerticalGroup(
            PENDAPATANIsiUlangGalonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PENDAPATANIsiUlangGalonLayout.createSequentialGroup()
                .addComponent(jPanel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(PENDAPATANIsiUlangGalonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PENDAPATANIsiUlangGalonLayout.createSequentialGroup()
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(PENDAPATANIsiUlangGalonLayout.createSequentialGroup()
                        .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)))
                .addGap(50, 50, 50))
        );

        javax.swing.GroupLayout ISILayout = new javax.swing.GroupLayout(ISI);
        ISI.setLayout(ISILayout);
        ISILayout.setHorizontalGroup(
            ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1618, Short.MAX_VALUE)
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ISILayout.createSequentialGroup()
                    .addComponent(CUSTOMER, javax.swing.GroupLayout.PREFERRED_SIZE, 1618, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(PENJUALAN, javax.swing.GroupLayout.DEFAULT_SIZE, 1618, Short.MAX_VALUE))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(OPERASIONAL, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1618, Short.MAX_VALUE))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(LAPORAN_PENJUALAN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(LAPORAN_OPERASIONAL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(KURIR, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(PENDAPATANIsiUlangGalon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ISILayout.setVerticalGroup(
            ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ISILayout.createSequentialGroup()
                    .addComponent(CUSTOMER, javax.swing.GroupLayout.PREFERRED_SIZE, 848, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 160, Short.MAX_VALUE)))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ISILayout.createSequentialGroup()
                    .addComponent(PENJUALAN, javax.swing.GroupLayout.PREFERRED_SIZE, 848, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 137, Short.MAX_VALUE)))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ISILayout.createSequentialGroup()
                    .addComponent(OPERASIONAL, javax.swing.GroupLayout.PREFERRED_SIZE, 846, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 162, Short.MAX_VALUE)))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ISILayout.createSequentialGroup()
                    .addComponent(LAPORAN_PENJUALAN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 163, Short.MAX_VALUE)))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ISILayout.createSequentialGroup()
                    .addComponent(LAPORAN_OPERASIONAL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 163, Short.MAX_VALUE)))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ISILayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(KURIR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(140, 140, 140)))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ISILayout.createSequentialGroup()
                    .addComponent(PENDAPATANIsiUlangGalon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 161, Short.MAX_VALUE)))
        );

        MENU.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        menu.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        menu.setPreferredSize(new java.awt.Dimension(250, 0));

        customer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/customer.png"))); // NOI18N
        customer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerActionPerformed(evt);
            }
        });

        penjualan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/penjualan.png"))); // NOI18N
        penjualan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                penjualanActionPerformed(evt);
            }
        });

        operasional.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/operasional.png"))); // NOI18N
        operasional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                operasionalActionPerformed(evt);
            }
        });

        kurir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/KURIR (1).png"))); // NOI18N
        kurir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kurirActionPerformed(evt);
            }
        });

        pendapatanIsiUlangGalon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/pendapatan (1).png"))); // NOI18N
        pendapatanIsiUlangGalon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pendapatanIsiUlangGalonActionPerformed(evt);
            }
        });

        PenjualanBarang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Penjualan Barang.png"))); // NOI18N
        PenjualanBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PenjualanBarangActionPerformed(evt);
            }
        });

        keluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/closed.png"))); // NOI18N
        keluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keluarActionPerformed(evt);
            }
        });

        jPanel22.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel44.setBackground(new java.awt.Color(204, 204, 204));
        jLabel44.setFont(new java.awt.Font("Rockwell", 3, 24)); // NOI18N
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel44.setText("MENU ADMIN");
        jLabel44.setToolTipText("");

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel44, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel44, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout menuLayout = new javax.swing.GroupLayout(menu);
        menu.setLayout(menuLayout);
        menuLayout.setHorizontalGroup(
            menuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, menuLayout.createSequentialGroup()
                .addGroup(menuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(menuLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(keluar, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(menuLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, menuLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(menuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(customer, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(penjualan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(operasional, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(kurir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pendapatanIsiUlangGalon, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(PenjualanBarang, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        menuLayout.setVerticalGroup(
            menuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(customer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(penjualan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PenjualanBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(operasional, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pendapatanIsiUlangGalon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(kurir)
                .addGap(49, 49, 49)
                .addComponent(keluar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(44, 44, 44))
        );

        javax.swing.GroupLayout MENULayout = new javax.swing.GroupLayout(MENU);
        MENU.setLayout(MENULayout);
        MENULayout.setHorizontalGroup(
            MENULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(menu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
        );
        MENULayout.setVerticalGroup(
            MENULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(menu, javax.swing.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE)
        );

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("@ 2025 | POS WATERMEN | By: Jovi Inzagi");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("@ 2025 | POS WATERMEN | By: Jovi Inzagi");

        javax.swing.GroupLayout FRAMELayout = new javax.swing.GroupLayout(FRAME);
        FRAME.setLayout(FRAMELayout);
        FRAMELayout.setHorizontalGroup(
            FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(HEADER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(FRAMELayout.createSequentialGroup()
                .addGroup(FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FRAMELayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(FRAMELayout.createSequentialGroup()
                                .addComponent(MENU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ISI, javax.swing.GroupLayout.PREFERRED_SIZE, 1622, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        FRAMELayout.setVerticalGroup(
            FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FRAMELayout.createSequentialGroup()
                .addComponent(HEADER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(MENU, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ISI, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(497, 497, 497)
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FRAME, javax.swing.GroupLayout.PREFERRED_SIZE, 1889, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FRAME, javax.swing.GroupLayout.PREFERRED_SIZE, 983, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbjeniscustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbjeniscustomerActionPerformed
        switch (cmbjeniscustomer.getSelectedIndex()) {
            case 0:
                txtjeniscustomer.setText("Pribadi");
                break;
            case 1:
                txtjeniscustomer.setText("Warung");
                break;
            default:
                txtjeniscustomer.setText("Agen");
                break;
        }
    }//GEN-LAST:event_cmbjeniscustomerActionPerformed

    private void btnhapuscustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnhapuscustomerActionPerformed
        if (txtidcustomer.getText().trim().equals("")) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int jawab = JOptionPane.showConfirmDialog(null,
                "Data ini akan dihapus, lanjutkan?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (jawab == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM customer WHERE id_customer = ?";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtidcustomer.getText())); // Konversi ke integer

                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    Bersih();
                    TampilDataCustomer();
                } else {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Data gagal dihapus. ID tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnhapuscustomerActionPerformed

    private void btnbatalcustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbatalcustomerActionPerformed
        Bersih();
    }//GEN-LAST:event_btnbatalcustomerActionPerformed

    private void btntambahcustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btntambahcustomerActionPerformed
        try {
            st = cn.createStatement();

            // Validasi input tidak boleh kosong
            if (txtnamacustomer.getText().trim().equals("")
                    || txtjeniscustomer.getText().trim().equals("")
                    || txtalamat.getText().trim().equals("")
                    || txttelephone.getText().trim().equals("")) {

                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(null, "Data tidak boleh kosong!", "Validasi Data",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (btntambahcustomer.getText().equals("TAMBAH")) {
                // Cari ID customer yang bisa digunakan kembali
                // 1. Cari ID transaksi yang bisa digunakan kembali
                String queryCheckID = "SELECT MIN(t1.id_customer + 1) AS next_id "
                        + "FROM customer t1 "
                        + "LEFT JOIN customer t2 ON t1.id_customer + 1 = t2.id_customer "
                        + "WHERE t2.id_customer IS NULL";
                ResultSet rs = st.executeQuery(queryCheckID);

                int newID = 1; // Default ID jika tabel kosong

                if (rs.next() && rs.getInt("next_id") > 0) {
                    newID = rs.getInt("next_id"); // Gunakan kembali ID yang hilang
                }

                // Insert data customer
                String sql = "INSERT INTO customer (id_customer, nama_customer, jenis_customer, alamat, telephone) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pst = cn.prepareStatement(sql)) {
                    pst.setInt(1, newID);
                    pst.setString(2, txtnamacustomer.getText().trim());
                    pst.setString(3, txtjeniscustomer.getText().trim());
                    pst.setString(4, txtalamat.getText().trim());
                    pst.setString(5, txttelephone.getText().trim());

                    pst.executeUpdate();
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Data berhasil disimpan dengan ID: " + newID);
                    Bersih();
                    TampilDataCustomer();
                }

            } else {
                // Proses update
                String update = "UPDATE customer SET nama_customer = ?, jenis_customer = ?, alamat = ?, telephone = ? WHERE id_customer = ?";
                PreparedStatement pst = cn.prepareStatement(update);
                pst.setString(1, txtnamacustomer.getText().trim());
                pst.setString(2, txtjeniscustomer.getText().trim());
                pst.setString(3, txtalamat.getText().trim());
                pst.setString(4, txttelephone.getText().trim());
                pst.setInt(5, Integer.parseInt(txtidcustomer.getText().trim()));

                pst.executeUpdate();
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(null, "Data berhasil diperbarui!");
                Bersih();
                TampilDataCustomer();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btntambahcustomerActionPerformed

    private void tblcustomerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblcustomerMouseClicked
        int selectedRow = tblcustomer.getSelectedRow(); // Ambil baris yang diklik

        if (selectedRow != -1) { // Pastikan baris valid
            txtidcustomer.setText(tblcustomer.getValueAt(selectedRow, 1).toString());
            txtnamacustomer.setText(tblcustomer.getValueAt(selectedRow, 2).toString());
            txtjeniscustomer.setText(tblcustomer.getValueAt(selectedRow, 3).toString());
            txtalamat.setText(tblcustomer.getValueAt(selectedRow, 4).toString());
            txttelephone.setText(tblcustomer.getValueAt(selectedRow, 5).toString());

            txtidcustomer.setEditable(false);
            btntambahcustomer.setText("UBAH");
        }
    }//GEN-LAST:event_tblcustomerMouseClicked

    private void txtcaricustomerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtcaricustomerKeyPressed
        CariDataCustomer();
    }//GEN-LAST:event_txtcaricustomerKeyPressed

    private void customerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerActionPerformed
        // TODO add your handling code here:
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();

        //menambahkan panel
        ISI.add(CUSTOMER);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_customerActionPerformed

    private void keluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keluarActionPerformed
        // TODO add your handling code here:
        Login l = new Login();
        l.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_keluarActionPerformed

    private void penjualanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_penjualanActionPerformed
//        // TODO add your handling code here:
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();

        //menambahkan panel
        ISI.add(PENJUALAN);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_penjualanActionPerformed

    private void operasionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_operasionalActionPerformed
        // TODO add your handling code here:
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();

        //menambahkan panel
        ISI.add(OPERASIONAL);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_operasionalActionPerformed

    private void cmbjeniscustomer2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbjeniscustomer2ActionPerformed

        switch (cmbjeniscustomer2.getSelectedIndex()) {
            case 0:
                txtjeniscustomer2.setText("Pribadi");
                txthargasatuan2.setText("7000");
                break;
            case 1:
                txtjeniscustomer2.setText("Warung");
                txthargasatuan2.setText("6000");
                break;
            default:
                txtjeniscustomer2.setText("Agen");
                txthargasatuan2.setText("6000");
                break;
        }
    }//GEN-LAST:event_cmbjeniscustomer2ActionPerformed

    private void cmbstatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbstatusActionPerformed

        switch (cmbstatus.getSelectedIndex()) {
            case 0:
                txtstatus.setText("Belum Lunas");
                break;
            default:
                txtstatus.setText("Lunas");
                break;
        }
    }//GEN-LAST:event_cmbstatusActionPerformed

    private void btnhapus2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnhapus2ActionPerformed
        try {
            // Validasi input
            if (txtidtransaksi.getText().trim().equals("")) {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus transaksi ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM transaksi WHERE id_transaksi = ?";
                try (PreparedStatement pst = cn.prepareStatement(sql)) {
                    pst.setInt(1, Integer.parseInt(txtidtransaksi.getText()));

                    int rowsDeleted = pst.executeUpdate();
                    if (rowsDeleted > 0) {
                        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                        JOptionPane.showMessageDialog(null, "Data berhasil dihapus");
                        Bersih();
                        TampilDataPenjualan();
                    } else {
                        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                        JOptionPane.showMessageDialog(null, "Data tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "ID transaksi harus berupa angka!", "Validasi Data", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnhapus2ActionPerformed

    private void btntambahpenjualanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btntambahpenjualanActionPerformed
        try {
            st = cn.createStatement();

            // Validasi input
            if (txtnamacustomer2.getText().trim().equals("")
                    || txtidcustomer2.getText().trim().equals("")
                    || txtjeniscustomer2.getText().trim().equals("")
                    || txthargasatuan2.getText().trim().equals("")
                    || txtjumlahpenjualan.getText().trim().equals("")
                    || txtstatus.getText().trim().equals("")
                    || txtNamaKurirPengantar.getText().trim().equals("")
                    || txtPlatNomorKurir.getText().trim().equals("")) {

                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(null, "Data tidak boleh kosong", "Validasi Data",
                        JOptionPane.INFORMATION_MESSAGE);

                return;
            }

            // Validasi data customer sesuai database
            String queryValidasiCustomer = "SELECT nama_customer, jenis_customer FROM customer WHERE id_customer = ?";
            try (PreparedStatement pstCek = cn.prepareStatement(queryValidasiCustomer)) {
                pstCek.setString(1, txtidcustomer2.getText().trim());
                ResultSet rsCek = pstCek.executeQuery();

                if (rsCek.next()) {
                    String namaCustomerAsli = rsCek.getString("nama_customer");
                    String jenisCustomerAsli = rsCek.getString("jenis_customer");

                    if (!namaCustomerAsli.equalsIgnoreCase(txtnamacustomer2.getText().trim())) {
                        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                        JOptionPane.showMessageDialog(null,
                                "Nama customer tidak sesuai dengan ID customer.\nHarusnya: " + namaCustomerAsli,
                                "Validasi Nama Customer", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!jenisCustomerAsli.equalsIgnoreCase(txtjeniscustomer2.getText().trim())) {
                        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                        JOptionPane.showMessageDialog(null,
                                "Jenis customer tidak sesuai dengan ID customer.\nHarusnya: " + jenisCustomerAsli,
                                "Validasi Jenis Customer", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "ID Customer tidak ditemukan di database!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String jenisCustomer = txtjeniscustomer2.getText().trim();
            int hargaSatuan = 0;

            switch (jenisCustomer) {
                case "Pribadi":
                    hargaSatuan = 7000;
                    break;
                case "Warung":
                case "Agen":
                    hargaSatuan = 6000;
                    break;
                default:
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Jenis customer tidak valid!", "Validasi Data",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
            }

            int jumlahBeli = Integer.parseInt(txtjumlahpenjualan.getText());
            int totalBayar = hargaSatuan * jumlahBeli;

            // Format total ke format desimal (Rp. 10.000)
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            DecimalFormat formatter = new DecimalFormat("#,###", symbols);
            String totalFormatted = formatter.format(totalBayar);

//            lblTotalHarga.setText("<html><span style='color:green; font-weight:bold; font-size:20pt;'>Rp. " + totalFormatted + "</span></html>");
            lblTotalHarga.setText("Rp. " + totalFormatted);

            // Aksi simpan data
            if (btntambahpenjualan.getText().equals("TAMBAH")) {
                // 1. Cari ID transaksi yang bisa digunakan kembali
                String queryCheckID = "SELECT MIN(t1.id_transaksi + 1) AS next_id "
                        + "FROM transaksi t1 "
                        + "LEFT JOIN transaksi t2 ON t1.id_transaksi + 1 = t2.id_transaksi "
                        + "WHERE t2.id_transaksi IS NULL";
                ResultSet rs = st.executeQuery(queryCheckID);

                int newID = 1; // Default ID jika tabel kosong

                if (rs.next() && rs.getInt("next_id") > 0) {
                    newID = rs.getInt("next_id"); // Gunakan kembali ID yang hilang
                }

                // 2. Tambahkan data dengan ID yang ditemukan
                String sql = "INSERT INTO transaksi (id_transaksi, nama_customer, id_customer, jenis_customer, harga, jumlah, total, tanggal, status, nama_kurir, jenis_kendaraan, plat_nomor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?)";
                try (PreparedStatement pst = cn.prepareStatement(sql)) {
                    pst.setInt(1, newID);
                    pst.setString(2, txtnamacustomer2.getText());
                    pst.setString(3, txtidcustomer2.getText());
                    pst.setString(4, txtjeniscustomer2.getText());
                    pst.setInt(5, hargaSatuan);
                    pst.setInt(6, jumlahBeli);
                    pst.setInt(7, totalBayar);
                    pst.setDate(8, new java.sql.Date(new java.util.Date().getTime()));
                    pst.setString(9, txtstatus.getText());
                    pst.setString(10, txtNamaKurir.getText());
                    pst.setString(11, cbbJenisKendaraan.getSelectedItem().toString());
                    pst.setString(12, txtPlatNomorKurir.getText());

                    pst.executeUpdate();
                    int jumlahTransaksi = Integer.parseInt(txtjumlahpenjualan.getText());
                    System.out.println("Jumlah transaksi: " + jumlahTransaksi);
                    kurangiStokGalon(jumlahTransaksi);
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Data berhasil disimpan dengan ID: " + newID);
                    JOptionPane.showMessageDialog(this, "Stok Barang berkurang " + jumlahTransaksi + " untuk pembelian Isi Ulang Air Galon.");
                    resetTable();
                    Bersih();
                    TampilDataPenjualan();
                }
            } else {
                // Aksi ubah data
                String update = "UPDATE transaksi SET jenis_customer = ?, harga = ?, jumlah = ?, total = ?, status = ?, nama_kurir=?, jenis_kendaraan=?, plat_nomor=? WHERE id_transaksi = ?";
                try (PreparedStatement pstmt = cn.prepareStatement(update)) {
                    pstmt.setString(1, txtjeniscustomer2.getText());
                    pstmt.setInt(2, hargaSatuan);
                    pstmt.setInt(3, jumlahBeli);
                    pstmt.setInt(4, totalBayar);
                    pstmt.setString(5, txtstatus.getText());
                    pstmt.setString(6, txtNamaKurirPengantar.getText());
                    pstmt.setString(7, cbbKendaraanKurir.getSelectedItem().toString());
                    pstmt.setString(8, txtPlatNomorKurir.getText());
                    pstmt.setInt(9, Integer.parseInt(txtidtransaksi.getText())); // ✅ Perbaikan jumlah parameter

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Data berhasil diperbarui");
                    Bersih();
                    TampilDataPenjualan();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Masukkan harga dan jumlah sebagai angka!", "Validasi Data", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btntambahpenjualanActionPerformed

    private void btnbayarcetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbayarcetakActionPerformed
        int selectedRow = tblpenjualan.getSelectedRow();
        try {
            Connection cn = koneksi.BukaKoneksi();
            File namafile = new File("src/laporan/laporan_transaksi.jasper");
            JasperPrint jp = JasperFillManager.fillReport(namafile.getPath(), null, cn);
            JasperViewer.viewReport(jp, false);
        } catch (JRException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }//GEN-LAST:event_btnbayarcetakActionPerformed

    private void tblcaricustomer2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblcaricustomer2MouseClicked
        int selectedRow = tblcaricustomer2.getSelectedRow(); // Ambil baris yang diklik

        if (selectedRow != -1) { // Pastikan baris valid
            txtidcustomer2.setText(tblcaricustomer2.getValueAt(selectedRow, 0).toString());
            txtnamacustomer2.setText(tblcaricustomer2.getValueAt(selectedRow, 1).toString());
            txtjeniscustomer2.setText(tblcaricustomer2.getValueAt(selectedRow, 2).toString());

            txtidcustomer2.setEditable(false);
        }
    }//GEN-LAST:event_tblcaricustomer2MouseClicked

    private void txtcaricustomer2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtcaricustomer2KeyPressed
        CariDataPenjualan();
    }//GEN-LAST:event_txtcaricustomer2KeyPressed

    private void btnbatal2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbatal2ActionPerformed
        Bersih();
    }//GEN-LAST:event_btnbatal2ActionPerformed

    private void btnBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBayarActionPerformed

        try {
            String orderId = txtidtransaksi.getText();
            if (orderId == null || orderId.isEmpty()) {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(this, "No Transaksi tidak boleh kosong", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String customerName = txtnamacustomer2.getText();
            String customerEmail = "admin@gmail.com";
            String customerPhone = "08123456789";

            String totalText = lblTotalHarga.getText().replace("Rp.", "").replace(",", "")
                    .replace(".", "").trim();
            int totalAmount = Integer.parseInt(totalText);

            boolean paymentInitiated = MidtransPayment.processPayment(
                    orderId, totalAmount, customerName, customerEmail, customerPhone);

            if (paymentInitiated) {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                updatePaymentStatus(orderId, "Pending");
                JOptionPane.showMessageDialog(this,
                        "Pembayaran sedang diproses di browser.\n"
                        + "Untuk simulasi pembayaran QRIS:\n"
                        + "1. Pilih metode pembayaran QRIS di Snap\n"
                        + "2. Salin kode QR yang muncul\n"
                        + "3. Buka simulator QRIS di: https://simulator.sandbox.midtrans.com/v2/qris/index \n"
                        + "4. Paste kode QR dan klik 'Scan QR'",
                        "Petunjuk Simulasi", JOptionPane.INFORMATION_MESSAGE);

                checkPaymentStatus(orderId);
            }
        } catch (HeadlessException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnBayarActionPerformed

    private void btnDataTerpilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDataTerpilihActionPerformed
        try {
            int selectedRow = tblpenjualan.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Silakan pilih data terlebih dahulu");
                return;
            }

            String idTransaksiStr = tblpenjualan.getValueAt(selectedRow, 0).toString();

            Integer idTransaksi = Integer.valueOf(idTransaksiStr);

            Connection cn = koneksi.BukaKoneksi();
            File namafile = new File("src/laporan/SelectedData.jasper");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id_transaksi", idTransaksi);

            JasperPrint jp = JasperFillManager.fillReport(namafile.getPath(), parameters, cn);

            if (jp.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk ditampilkan");
            } else {
                JasperViewer.viewReport(jp, false);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID Transaksi harus berupa angka: " + e.getMessage());
        } catch (JRException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }//GEN-LAST:event_btnDataTerpilihActionPerformed

    private void tblpenjualanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblpenjualanMouseClicked
        int selectedRow = tblpenjualan.getSelectedRow();

        if (selectedRow != -1) {
            txtidtransaksi.setText(tblpenjualan.getValueAt(selectedRow, 0).toString());
            txtnamacustomer2.setText(tblpenjualan.getValueAt(selectedRow, 1).toString());
            txtidcustomer2.setText(tblpenjualan.getValueAt(selectedRow, 2).toString());
            txtjeniscustomer2.setText(tblpenjualan.getValueAt(selectedRow, 3).toString());
            txthargasatuan2.setText(tblpenjualan.getValueAt(selectedRow, 4).toString());
            txtjumlahpenjualan.setText(tblpenjualan.getValueAt(selectedRow, 5).toString());
            txtstatus.setText(tblpenjualan.getValueAt(selectedRow, 8).toString());

            // Format total ke desimal (misalnya Rp. 10.000)
            try {
                int totalInt = Integer.parseInt(tblpenjualan.getValueAt(selectedRow, 6).toString());

                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setGroupingSeparator(',');
                DecimalFormat formatter = new DecimalFormat("#,###", symbols);
                String totalFormatted = formatter.format(totalInt);

//                lblTotalHarga.setText("<html><span style='color:green; font-weight:bold;'>Rp. " + totalFormatted + "</span></html>");
                lblTotalHarga.setForeground(new Color(0, 179, 0));
                lblTotalHarga.setFont(new Font("Tahoma", Font.BOLD, 40));
                lblTotalHarga.setText("Rp. " + totalFormatted);
            } catch (NumberFormatException e) {
                lblTotalHarga.setText("Rp. 0"); // Fallback jika parsing gagal
            }

            txtidcustomer2.setEditable(false);
            btntambahpenjualan.setText("UBAH");
        }
    }//GEN-LAST:event_tblpenjualanMouseClicked

    private void tbloperasionalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbloperasionalMouseClicked
        int selectedRow = tbloperasional.getSelectedRow();

        if (selectedRow != -1) {
            txtidbarang.setText(tbloperasional.getValueAt(selectedRow, 0).toString()); // ID Barang
            txtkodebarang.setText(tbloperasional.getValueAt(selectedRow, 1).toString()); // Kode Barang
            txtnamabarang.setText(tbloperasional.getValueAt(selectedRow, 2).toString()); // Nama Barang
            txtjumlahbarang.setText(tbloperasional.getValueAt(selectedRow, 3).toString()); // Jumlah
            txthargasatuan.setText(tbloperasional.getValueAt(selectedRow, 4).toString()); // Harga Satuan
            txttotalbarang.setText(tbloperasional.getValueAt(selectedRow, 5).toString()); // Total

            btntambahoperasional.setText("UBAH");
        }
    }//GEN-LAST:event_tbloperasionalMouseClicked

    private void cmbnamabarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbnamabarangActionPerformed
        switch (cmbnamabarang.getSelectedIndex()) {
            case 0:
                txtnamabarang.setText("Tutup Galon");
                txthargasatuan.setText("100");
                break;
            case 1:
                txtnamabarang.setText("Tisu Galon");
                txthargasatuan.setText("75");
                break;
            case 2:
                txtnamabarang.setText("Segel Galon");
                txthargasatuan.setText("60");
                break;
            case 3:
                txtnamabarang.setText("Galon");
                txthargasatuan.setText("30000");
                break;
            case 4:
                txtnamabarang.setText("Sedimen");
                txthargasatuan.setText("15000");
                break;
            default:
                txtnamabarang.setText("");
                txthargasatuan.setText("");
                break;
        }
    }//GEN-LAST:event_cmbnamabarangActionPerformed

    private void cmbkodebarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbkodebarangActionPerformed
        switch (cmbkodebarang.getSelectedIndex()) {
            case 0:
                txtkodebarang.setText("A1");
                break;
            case 1:
                txtkodebarang.setText("B2");
                break;
            case 2:
                txtkodebarang.setText("C3");
                break;
            case 3:
                txtkodebarang.setText("D4");
                break;
            default:
                txtkodebarang.setText("E5");
                break;
        }
    }//GEN-LAST:event_cmbkodebarangActionPerformed

    private void txtcaribarangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtcaribarangKeyPressed
        CariDataBarang();
    }//GEN-LAST:event_txtcaribarangKeyPressed

    private void btntambahoperasionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btntambahoperasionalActionPerformed
        try {
            st = cn.createStatement();

            // Validasi input
            if (txtkodebarang.getText().trim().equals("")
                    || txtnamabarang.getText().trim().equals("")
                    || txtjumlahbarang.getText().trim().equals("")
                    || txthargasatuan.getText().trim().equals("")) {

                JOptionPane.showMessageDialog(null, "Data tidak boleh kosong", "Validasi Data",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Mengambil nama barang dari input (tanpa mengubah huruf besar/kecil)
            String namaBarang = txtnamabarang.getText().trim();
            int hargaSatuan = 0;

            // Menentukan harga satuan berdasarkan nama barang
            switch (namaBarang) {
                case "Tutup Galon":
                    hargaSatuan = 100;
                    break;
                case "Tisu Galon":
                    hargaSatuan = 75;
                    break;
                case "Segel Galon":
                    hargaSatuan = 60;
                    break;
                case "Galon":
                    hargaSatuan = 30000;
                    break;
                case "Sedimen":
                    hargaSatuan = 15000;
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Nama barang tidak valid!", "Validasi Data",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
            }

            int jumlahBeli = Integer.parseInt(txtjumlahbarang.getText());
            int totalBayar = hargaSatuan * jumlahBeli;

            // Aksi simpan data
            if (btntambahoperasional.getText().equals("TAMBAH")) {
                // Periksa apakah ID transaksi sudah ada
                String queryCheckID = "SELECT MIN(t1.id_barang + 1) AS next_id_barang "
                        + "FROM operasional t1 "
                        + "LEFT JOIN operasional t2 ON t1.id_barang + 1 = t2.id_barang "
                        + "WHERE t2.id_barang IS NULL";
                ResultSet rs = st.executeQuery(queryCheckID);

                int newID = 1; // Default ID jika tabel kosong

                if (rs.next() && rs.getInt("next_id_barang") > 0) { // ✅ Perbaikan di sini
                    newID = rs.getInt("next_id_barang"); // Gunakan kembali ID yang hilang
                }

                // Query untuk menambahkan data
                String sql = "INSERT INTO operasional (id_barang, kode_barang, nama_barang, jumlah, harga_satuan, total, tanggal) VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = cn.prepareStatement(sql)) {
                    pstmt.setInt(1, newID);  // ID yang baru dibuat
                    pstmt.setString(2, txtkodebarang.getText());
                    pstmt.setString(3, txtnamabarang.getText());
                    pstmt.setInt(4, jumlahBeli);
                    pstmt.setInt(5, hargaSatuan);
                    pstmt.setInt(6, totalBayar);
                    pstmt.setDate(7, new java.sql.Date(new java.util.Date().getTime()));

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Data berhasil disimpan dengan ID: " + newID);
                    resetTable();
                    Bersih();
                    TampilDataOperasional();
                }
            } else {
                // Aksi ubah data
                String update = "UPDATE operasional SET kode_barang = ?, nama_barang = ?, jumlah = ?, harga_satuan = ?, total = ? WHERE id_barang = ?";
                try (PreparedStatement pstmt = cn.prepareStatement(update)) {
                    pstmt.setString(1, txtkodebarang.getText());
                    pstmt.setString(2, txtnamabarang.getText());
                    pstmt.setInt(3, jumlahBeli);
                    pstmt.setInt(4, hargaSatuan);
                    pstmt.setInt(5, totalBayar);
                    pstmt.setString(6, txtidbarang.getText());

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Data berhasil diperbarui");
                    Bersih();
                    TampilDataOperasional();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Masukkan jumlah beli dan harga satuan sebagai angka", "Validasi Data", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btntambahoperasionalActionPerformed

    private void btnbatal1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbatal1ActionPerformed
        Bersih();
    }//GEN-LAST:event_btnbatal1ActionPerformed

    private void btnhapus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnhapus1ActionPerformed
        try {
            // Pastikan pengguna memilih baris yang akan dihapus
            int selectedRow = tbloperasional.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Ambil ID Barang dari tabel berdasarkan baris yang dipilih
            String idBarang = tbloperasional.getValueAt(selectedRow, 0).toString();

            // Konfirmasi penghapusan
            int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM operasional WHERE id_barang = ?";

                try (PreparedStatement pstmt = cn.prepareStatement(sql)) {
                    pstmt.setString(1, idBarang);
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    Bersih();
                    TampilDataOperasional(); // Refresh tampilan tabel
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnhapus1ActionPerformed

    private void btnLaporanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLaporanActionPerformed
        // TODO add your handling code here:
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();

        //menambahkan panel
        ISI.add(LAPORAN_PENJUALAN);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_btnLaporanActionPerformed

    private void btnTampilkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilkanActionPerformed
        try {
            // Validasi input
            if (cmbBulan.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Silakan pilih bulan terlebih dahulu.", "Validasi Input", JOptionPane.WARNING_MESSAGE);
                cmbBulan.requestFocus();
                return;
            }

            if (cmbTahun.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Silakan pilih tahun terlebih dahulu.", "Validasi Input", JOptionPane.WARNING_MESSAGE);
                cmbTahun.requestFocus();
                return;
            }

            // Tampilkan cursor loading
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Pastikan combobox untuk nama barang sudah terisi
            isiComboBoxNamaBarang();

            // Tampilkan data laporan dengan method yang sudah diperbaiki
            TampilkanDataLaporanBulananBaru();

            // Jika tidak ada data yang ditampilkan pada tabel, beri tahu pengguna
            if (tableLaporanBulananBarang.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Tidak ada data untuk bulan " + cmbBulan.getSelectedItem().toString()
                        + " tahun " + cmbTahun.getSelectedItem().toString()
                        + " dengan filter yang dipilih.",
                        "Informasi", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            // Tangani error
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan saat menampilkan laporan: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Untuk debugging
        } finally {
            // Kembalikan cursor ke normal
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_btnTampilkanActionPerformed

    private void btnCetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakActionPerformed
        // TODO add your handling code here:
        String judulBulan = cmbJudulBulan.getSelectedItem().toString();
        String bulan = cmbBulan.getSelectedItem().toString();

        if (!judulBulan.equalsIgnoreCase(bulan)) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(this,
                    "Judul Bulan dan Bulan harus sama!\nSilakan sesuaikan terlebih dahulu.",
                    "Validasi Bulan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Lokasi file laporan
            String reportPath = "src/laporan/laporan_penjualan_bulanan.jasper";

            // Koneksi dan parameter
            Connection conn = koneksi.BukaKoneksi();
            Map<String, Object> parameters = getLaporanBulananParameters();

            // Tambahkan nama user login ke parameter laporan
            parameters.put("printed_by", Session.namaUserLogin); // <-- PENTING

            // Cetak laporan
            JasperPrint print = JasperFillManager.fillReport(reportPath, parameters, conn);
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Laporan Penjualan Bulanan");
            viewer.setVisible(true);

        } catch (JRException e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak laporan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnCetakActionPerformed

    private void btnKembaliPenjualanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliPenjualanActionPerformed
        // TODO add your handling code here:
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();

        //menambahkan panel
        ISI.add(PENJUALAN);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_btnKembaliPenjualanActionPerformed

    private void btnLaporan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLaporan1ActionPerformed
        // TODO add your handling code here:
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();

        //menambahkan panel
        ISI.add(LAPORAN_OPERASIONAL);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_btnLaporan1ActionPerformed

    private void btnBersihBulananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihBulananActionPerformed
        // TODO add your handling code here:
        Bersih();
    }//GEN-LAST:event_btnBersihBulananActionPerformed

    private void btnCetakBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakBarcodeActionPerformed
        try {
            // Get data from form fields
            final String orderId = txtidtransaksi.getText().trim();
            final String customerName = txtnamacustomer2.getText().trim();
            final String customerEmail = "admin@gmail.com"; // Consider making this configurable
            final String customerPhone = "08123456789";    // Consider making this configurable

            // Parse total price - removing formatting characters
            String totalText = lblTotalHarga.getText()
                    .replace("Rp.", "")
                    .replace(",", "")
                    .replace(".", "")
                    .trim();

            // Validate inputs
            if (customerName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Nama pelanggan tidak boleh kosong.",
                        "Validasi Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Parse and validate amount
            final int amount;
            try {
                amount = Integer.parseInt(totalText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Total harga harus lebih dari 0.",
                            "Validasi Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Format total harga tidak valid!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Handle empty Order ID - Create final variables for SwingWorker
            final String finalOrderId;
            if (orderId.isEmpty()) {
                finalOrderId = "ORDER-" + UUID.randomUUID().toString().substring(0, 8);
                txtidtransaksi.setText(finalOrderId); // Update field with generated Order ID
            } else {
                finalOrderId = orderId;
            }

            // Show processing message
            JOptionPane pane = new JOptionPane("Sedang memproses QRIS...",
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.DEFAULT_OPTION,
                    null, new Object[]{}, null);
            final JDialog processingDialog = pane.createDialog("Harap Tunggu");

            // Use SwingWorker to handle processing in background
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // Process the transaction and print QR code
                    return QRISPrinter.processSelectedTransaction(finalOrderId, amount,
                            customerName, customerEmail, customerPhone);
                }

                @Override
                protected void done() {
                    processingDialog.dispose();
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(null,
                                    "QRIS berhasil dibuat dan dicetak!",
                                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

                            Bersih();
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        JOptionPane.showMessageDialog(null,
                                "Error saat memproses: " + e.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                        System.out.println("Errornya ini ya iyan : " + e);
                    }
                }
            };

            // Start processing in background
            worker.execute();
            processingDialog.setModal(true);
            processingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            processingDialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error tidak terduga: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCetakBarcodeActionPerformed

    private void btnCekTisuGalonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekTisuGalonActionPerformed
        cekStokBarangTisu();
    }//GEN-LAST:event_btnCekTisuGalonActionPerformed

    private void btnCekGalonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekGalonActionPerformed
        cekStokGalon1pcs();
    }//GEN-LAST:event_btnCekGalonActionPerformed

    private void btnCekTutupGalonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekTutupGalonActionPerformed
        cekStokTutupGalon1000pcs();
    }//GEN-LAST:event_btnCekTutupGalonActionPerformed

    private void btnCekSegelGalonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekSegelGalonActionPerformed
        cekStokSegelGalon200pcs();
    }//GEN-LAST:event_btnCekSegelGalonActionPerformed

    private void btnCekSedimenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekSedimenActionPerformed
        cekStokSedimenPcs();
    }//GEN-LAST:event_btnCekSedimenActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void kurirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kurirActionPerformed
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();
        ISI.add(KURIR);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_kurirActionPerformed

    private void pendapatanIsiUlangGalonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pendapatanIsiUlangGalonActionPerformed
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();
        ISI.add(PENDAPATANIsiUlangGalon);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_pendapatanIsiUlangGalonActionPerformed

    private void btnBersihOperasionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihOperasionalActionPerformed
        // TODO add your handling code here:
        Bersih();
    }//GEN-LAST:event_btnBersihOperasionalActionPerformed

    private void btnKembaliPenjualan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliPenjualan1ActionPerformed
        // TODO add your handling code here:
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();

        //menambahkan panel
        ISI.add(OPERASIONAL);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_btnKembaliPenjualan1ActionPerformed

    private void btnTampilkan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilkan1ActionPerformed
        // TODO add your handling code here:
        TampilkanDataLaporanOperasional();
    }//GEN-LAST:event_btnTampilkan1ActionPerformed

    private void btnTampilPenjualanOpearasional1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilPenjualanOpearasional1ActionPerformed
        dataPendapatanBulanan();
    }//GEN-LAST:event_btnTampilPenjualanOpearasional1ActionPerformed

    private void bersihPendapatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bersihPendapatanActionPerformed
        cmbBulanPendapatan.setSelectedIndex(0);
        cmbTahunPendapatan.setSelectedIndex(0);
        DefaultTableModel model = (DefaultTableModel) tabelPendapatan.getModel();
        model.setRowCount(0);
    }//GEN-LAST:event_bersihPendapatanActionPerformed

    private void btnCetakPendapatanBulananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakPendapatanBulananActionPerformed
        try {
            String namaBulan = cmbBulanPendapatan.getSelectedItem().toString();
            String tahun = cmbTahunPendapatan.getSelectedItem().toString();
            String bulanAngka = getBulanAngka(namaBulan);
            String periode = tahun + "-" + bulanAngka;

            String reportPath = "src/laporan/Laporan_Pendapatan_Bulanan.jasper";

            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("periode", periode);

            JasperPrint jp = JasperFillManager.fillReport(reportPath, parameters, cn);
            JasperViewer.viewReport(jp, false); // false -> tidak menutup aplikasi utama

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal mencetak laporan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnCetakPendapatanBulananActionPerformed

    private void cmbTahunPendapatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTahunPendapatanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbTahunPendapatanActionPerformed

    private void btnTampilPenjualanOpearasional2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilPenjualanOpearasional2ActionPerformed
        cmbBulanPendapatan.setSelectedIndex(0);
        cmbTahunPendapatan.setSelectedIndex(0);
        dataSeluruhPendapatanBualanan();
    }//GEN-LAST:event_btnTampilPenjualanOpearasional2ActionPerformed

    private void btnCetakPendapatanBulanan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakPendapatanBulanan1ActionPerformed
        try {
            String reportPath = "src/laporan/Laporan_Pendapatan.jasper";
            Map<String, Object> parameters = getLaporanBulananParameters();
            parameters.put("printed_by", Session.namaUserLogin); // <-- PENTING
            JasperPrint print = JasperFillManager.fillReport(reportPath, parameters, cn);
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Laporan Penjualan Bulanan");
            viewer.setVisible(true);

        } catch (JRException e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak laporan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnCetakPendapatanBulanan1ActionPerformed

    private void cbbJenisKendaraanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbJenisKendaraanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbbJenisKendaraanActionPerformed

    private void btnTambahKurirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahKurirActionPerformed
        try {
            String sql = "INSERT INTO kurir (nama_kurir, no_hp, alamat, jenis_kendaraan, plat_nomor, status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, txtIdKurir.getText());
            ps.setString(2, txtNoHP.getText());
            ps.setString(3, txtAlamatKurir.getText());
            ps.setString(4, cbbJenisKendaraan.getSelectedItem().toString());
            ps.setString(5, txtPlatNomot.getText());
            ps.setString(6, cbbStatusKurir.getSelectedItem().toString());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Data kurir berhasil ditambahkan");
            tampilDataKurir(); // refresh tabel
        } catch (HeadlessException | SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnTambahKurirActionPerformed

    private void btnUbahKurirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahKurirActionPerformed
        try {
            String sql = "UPDATE kurir SET nama_kurir=?, no_hp=?, alamat=?, jenis_kendaraan=?, plat_nomor=?, status=? WHERE id_kurir=?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, txtIdKurir.getText());
            ps.setString(2, txtNoHP.getText());
            ps.setString(3, txtAlamatKurir.getText());
            ps.setString(4, cbbJenisKendaraan.getSelectedItem().toString());
            ps.setString(5, txtPlatNomot.getText());
            ps.setString(6, cbbStatusKurir.getSelectedItem().toString());
            ps.setInt(7, Integer.parseInt(txtIdKurir.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Data kurir berhasil diperbarui");
            tampilDataKurir();
        } catch (HeadlessException | NumberFormatException | SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnUbahKurirActionPerformed

    private void btnUbahKurir1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahKurir1ActionPerformed
        try {
            String sql = "DELETE FROM kurir WHERE id_kurir=?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(txtIdKurir.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Data kurir berhasil dihapus");
            tampilDataKurir();
        } catch (HeadlessException | NumberFormatException | SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnUbahKurir1ActionPerformed

    private void tabelKurirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelKurirMouseClicked
        int baris = tabelKurir.getSelectedRow();

        String idKurir = tabelKurir.getValueAt(baris, 0).toString();
        String namaKurir = tabelKurir.getValueAt(baris, 1).toString();
        String noHp = tabelKurir.getValueAt(baris, 2).toString();
        String alamat = tabelKurir.getValueAt(baris, 3).toString();
        String jenisKendaraan = tabelKurir.getValueAt(baris, 4).toString();
        String plat = tabelKurir.getValueAt(baris, 5).toString();
        String status = tabelKurir.getValueAt(baris, 6).toString();

        txtIdKurir.setText(idKurir);
        txtNamaKurir.setText(namaKurir);
        txtNoHP.setText(noHp);
        txtAlamatKurir.setText(alamat);
        txtPlatNomot.setText(plat);
        cbbJenisKendaraan.setSelectedItem(jenisKendaraan);
        cbbStatusKurir.setSelectedItem(status);
    }//GEN-LAST:event_tabelKurirMouseClicked

    private void btnUbahKurir2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahKurir2ActionPerformed
        bersihkanFormKurir();
        tampilDataKurir();
    }//GEN-LAST:event_btnUbahKurir2ActionPerformed

    private void tblCariKurirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCariKurirMouseClicked
        int selectedRow = tblCariKurir.getSelectedRow();

        if (selectedRow != -1) {
            txtNamaKurirPengantar.setText(tblCariKurir.getValueAt(selectedRow, 1).toString());
            cbbKendaraanKurir.setSelectedItem(tblCariKurir.getValueAt(selectedRow, 4).toString());
            txtPlatNomorKurir.setText(tblCariKurir.getValueAt(selectedRow, 5).toString());

            txtidcustomer2.setEditable(false);
        }

    }//GEN-LAST:event_tblCariKurirMouseClicked

    private void txtCariKurirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKurirKeyPressed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtCariKurirKeyPressed

    private void cbbCariKurirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbCariKurirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbbCariKurirActionPerformed

    private void txtCariKurirKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKurirKeyTyped
        // Login Cari Kurir Berdasarkan Combo Box
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
            tblCariKurir.setModel(model);

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
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_txtCariKurirKeyTyped

    private void txtCariKurirKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKurirKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCariKurirKeyReleased

    private void cbbKendaraanKurirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbKendaraanKurirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbbKendaraanKurirActionPerformed

    private void PenjualanBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PenjualanBarangActionPerformed
        ISI.removeAll();
        PenjualanBarang penjualan = null;
        try {
            penjualan = new PenjualanBarang();
        } catch (SQLException ex) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
        }
        ISI.setLayout(new java.awt.BorderLayout());
        ISI.add(penjualan, BorderLayout.CENTER);
        ISI.revalidate();
        ISI.repaint();
    }//GEN-LAST:event_PenjualanBarangActionPerformed

    private void tblPelangganBarangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPelangganBarangMouseClicked
        // TODO add your handling code here:
        int selectedRow = tblPelangganBarang.getSelectedRow(); // Ambil baris yang diklik

        if (selectedRow != -1) { // Pastikan baris valid
            txtidcustomer1.setText(tblPelangganBarang.getValueAt(selectedRow, 1).toString());
            txtnamacustomer1.setText(tblPelangganBarang.getValueAt(selectedRow, 2).toString());
            txttelephone1.setText(tblPelangganBarang.getValueAt(selectedRow, 3).toString());
            txtalamat1.setText(tblPelangganBarang.getValueAt(selectedRow, 4).toString());
            
            txtidcustomer1.setEditable(false);
            btntambahcustomer1.setText("UBAH");
        }
    }//GEN-LAST:event_tblPelangganBarangMouseClicked

    private void btnhapuscustomer1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnhapuscustomer1ActionPerformed
        // TODO add your handling code here:
        if (txtidcustomer1.getText().trim().equals("")) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int jawab = JOptionPane.showConfirmDialog(null,
                "Data ini akan dihapus, lanjutkan?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (jawab == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM customer_barang WHERE id = ?";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtidcustomer1.getText())); // Konversi ke integer

                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    Bersih();
                    dataCustomerBarang();
                } else {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Data gagal dihapus. ID tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnhapuscustomer1ActionPerformed

    private void btnbatalcustomer1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbatalcustomer1ActionPerformed
        // TODO add your handling code here:
        Bersih();
    }//GEN-LAST:event_btnbatalcustomer1ActionPerformed

    private void btntambahcustomer1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btntambahcustomer1ActionPerformed
        // TODO add your handling code here:
        try {
        st = cn.createStatement();

        // Validasi input tidak boleh kosong
        if (txtnamacustomer1.getText().trim().equals("")
                || txtalamat1.getText().trim().equals("")
                || txttelephone1.getText().trim().equals("")) {

            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Data tidak boleh kosong!", "Validasi Data",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (btntambahcustomer1.getText().equals("TAMBAH")) {
            // Cari ID customer yang bisa digunakan kembali
            String queryCheckID = "SELECT MIN(t1.id + 1) AS next_id "
                    + "FROM customer_barang t1 "
                    + "LEFT JOIN customer_barang t2 ON t1.id + 1 = t2.id "
                    + "WHERE t2.id IS NULL";
            ResultSet rs = st.executeQuery(queryCheckID);

            int newID = 1; // Default ID jika tabel kosong
            if (rs.next() && rs.getInt("next_id") > 0) {
                newID = rs.getInt("next_id");
            }

            // Perbaikan: Gunakan nama kolom sesuai dengan tabel (no_hp)
            String sql = "INSERT INTO customer_barang (id, nama, no_hp, alamat) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = cn.prepareStatement(sql)) {
                pst.setInt(1, newID);
                pst.setString(2, txtnamacustomer1.getText().trim());
                pst.setString(3, txttelephone1.getText().trim()); // No HP
                pst.setString(4, txtalamat1.getText().trim());

                pst.executeUpdate();
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(null, "Data berhasil disimpan dengan ID: " + newID);
                Bersih();
                dataCustomerBarang();
            }

        } else {
            // Perbaikan: Gunakan nama kolom sesuai dengan tabel (no_hp)
            String update = "UPDATE customer_barang SET nama = ?, alamat = ?, no_hp = ? WHERE id = ?";
            PreparedStatement pst = cn.prepareStatement(update);
            pst.setString(1, txtnamacustomer1.getText().trim());
            pst.setString(2, txtalamat1.getText().trim());
            pst.setString(3, txttelephone1.getText().trim()); // No HP
            pst.setInt(4, Integer.parseInt(txtidcustomer1.getText().trim()));

            pst.executeUpdate();
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Data berhasil diperbarui!");
            Bersih();
            dataCustomerBarang();
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
    }//GEN-LAST:event_btntambahcustomer1ActionPerformed

    private void txtcaricustomer1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtcaricustomer1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtcaricustomer1KeyPressed

    private void OPERASIONALComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_OPERASIONALComponentShown
        resetTable();
    }//GEN-LAST:event_OPERASIONALComponentShown

    private void updatePaymentStatus(String orderId, String status) {
        try {
            Connection conn = koneksi.BukaKoneksi();
            if (conn == null) {
                System.out.println("Koneksi database gagal");
                return;
            }

            String sql = "UPDATE transaksi SET status = ? WHERE id_transaksi = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, status);
            pst.setString(2, orderId);
            int result = pst.executeUpdate();

            System.out.println("Update status: " + result + " row(s) affected for ID " + orderId);

            pst.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error updating payment status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkPaymentStatus(String orderId) {
        new Thread(()
                -> {
            try {
                boolean isPaid = false;
                int attempts = 0;

                while (!isPaid && attempts < 60) {
                    Thread.sleep(5000);
                    attempts++;

                    String status = MidtransStatusChecker.checkStatus(orderId);
                    System.out.println("Checking payment status attempt " + attempts + ": " + status);

                    if ("settlement".equals(status) || "capture".equals(status)) {
                        updatePaymentStatus(orderId, "Lunas");
                        isPaid = true;

                        SwingUtilities.invokeLater(()
                                -> {
                            JOptionPane.showMessageDialog(this,
                                    "Pembayaran berhasil!",
                                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
                            Bersih();
                        });
                    } else if ("deny".equals(status) || "cancel".equals(status) || "expire".equals(status)) {
                        updatePaymentStatus(orderId, "Belum Lunas");
                        isPaid = true;

                        SwingUtilities.invokeLater(()
                                -> {
                            JOptionPane.showMessageDialog(this,
                                    "Pembayaran gagal atau dibatalkan.",
                                    "Gagal", JOptionPane.WARNING_MESSAGE);
                        });
                    }
                }

                if (!isPaid) {
                    SwingUtilities.invokeLater(()
                            -> {
                        JOptionPane.showMessageDialog(this,
                                "Waktu pembayaran habis. Silakan coba lagi.",
                                "Timeout", JOptionPane.WARNING_MESSAGE);
                    });
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(()
                        -> {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(this,
                            "Error saat memeriksa status pembayaran: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    public String getBulanAngka(String namaBulan) {
        switch (namaBulan) {
            case "Januari":
                return "01";
            case "Februari":
                return "02";
            case "Maret":
                return "03";
            case "April":
                return "04";
            case "Mei":
                return "05";
            case "Juni":
                return "06";
            case "Juli":
                return "07";
            case "Agustus":
                return "08";
            case "September":
                return "09";
            case "Oktober":
                return "10";
            case "November":
                return "11";
            case "Desember":
                return "12";
            default:
                return "01";
        }
    }

    private void dataPendapatanBulanan() {
        try {
            String namaBulan = cmbBulanPendapatan.getSelectedItem().toString();
            String tahun = cmbTahunPendapatan.getSelectedItem().toString();
            String bulanAngka = getBulanAngka(namaBulan);

            String periode = tahun + "-" + bulanAngka; // Format: "2020-01"

            String query
                    = "SELECT "
                    + "    DATE_FORMAT(tanggal, '%Y-%m') AS bulan, "
                    + "    SUM(CASE WHEN sumber = 'pemasukan' THEN total ELSE 0 END) AS total_pemasukan, "
                    + "    SUM(CASE WHEN sumber = 'pengeluaran' THEN total ELSE 0 END) AS total_pengeluaran, "
                    + "    SUM(CASE WHEN sumber = 'pemasukan' THEN total ELSE 0 END) - "
                    + "    SUM(CASE WHEN sumber = 'pengeluaran' THEN total ELSE 0 END) AS pendapatan_bersih "
                    + "FROM ( "
                    + "    SELECT tanggal, total, 'pemasukan' AS sumber FROM transaksi "
                    + "    UNION ALL "
                    + "    SELECT tanggal, total, 'pengeluaran' AS sumber FROM operasional "
                    + ") AS gabungan "
                    + "WHERE DATE_FORMAT(tanggal, '%Y-%m') = ? "
                    + "GROUP BY bulan "
                    + "ORDER BY bulan DESC";

            // Gunakan PreparedStatement untuk keamanan
            PreparedStatement ps = cn.prepareStatement(query);
            ps.setString(1, periode);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Bulan");
            model.addColumn("Total Operasional");
            model.addColumn("Total Penjualan");
            model.addColumn("Pendapatan");

            tabelPendapatan.setModel(model);

            while (rs.next()) {
                String bulan = rs.getString("bulan");
                String pengeluaran = String.format("Rp. %,d", rs.getInt("total_pengeluaran"));
                String pemasukan = String.format("Rp. %,d", rs.getInt("total_pemasukan"));
                String pendapatanBulanan = String.format("Rp. %,d", rs.getInt("pendapatan_bersih"));
                Object[] data
                        = {
                            bulan,
                            pemasukan,
                            pengeluaran,
                            pendapatanBulanan
                        };
                model.addRow(data);
                labelOperasionalPendapatan.setText(pengeluaran);
                labelPenjualanPendapatan.setText(pemasukan);
                labelPendapatan.setText(pendapatanBulanan);
            }

            // Styling tabel pendapatan
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16);
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18);
            tabelPendapatan.setFont(fontIsi);
            tabelPendapatan.setRowHeight(28);
            tabelPendapatan.getTableHeader().setFont(fontHeader);

        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void dataSeluruhPendapatanBualanan() {
        try {
            String query
                    = "SELECT "
                    + "    DATE_FORMAT(tanggal, '%Y-%m') AS bulan, "
                    + "    SUM(CASE WHEN sumber = 'pemasukan' THEN total ELSE 0 END) AS total_pemasukan, "
                    + "    SUM(CASE WHEN sumber = 'pengeluaran' THEN total ELSE 0 END) AS total_pengeluaran, "
                    + "    SUM(CASE WHEN sumber = 'pemasukan' THEN total ELSE 0 END) - "
                    + "    SUM(CASE WHEN sumber = 'pengeluaran' THEN total ELSE 0 END) AS pendapatan_bersih "
                    + "FROM ( "
                    + "    SELECT tanggal, total, 'pemasukan' AS sumber FROM transaksi "
                    + "    UNION ALL "
                    + "    SELECT tanggal, total, 'pengeluaran' AS sumber FROM operasional "
                    + ") AS gabungan "
                    + "GROUP BY bulan "
                    + "ORDER BY bulan DESC";

            PreparedStatement ps = cn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Bulan");
            model.addColumn("Total Penjualan");
            model.addColumn("Total Operasional");
            model.addColumn("Pendapatan");

            tabelPendapatan.setModel(model);

            while (rs.next()) {
                Object[] data
                        = {
                            rs.getString("bulan"),
                            String.format("Rp. %,d", rs.getInt("total_pemasukan")),
                            String.format("Rp. %,d", rs.getInt("total_pengeluaran")),
                            String.format("Rp. %,d", rs.getInt("pendapatan_bersih"))
                        };
                model.addRow(data);
            }

            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16);
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18);
            tabelPendapatan.setFont(fontIsi);
            tabelPendapatan.setRowHeight(28);
            tabelPendapatan.getTableHeader().setFont(fontHeader);

        } catch (SQLException e) {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void tampilDataKurir() {
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
            tblCariKurir.setModel(model);
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16); // Font isi tabel
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18); // Font header kolom
            tabelKurir.setFont(fontIsi);
            tabelKurir.getTableHeader().setFont(fontHeader);;
            tabelKurir.setRowHeight(28);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void bersihkanFormKurir() {
        txtIdKurir.setText("");
        txtNamaKurir.setText("");
        txtNoHP.setText("");
        txtAlamatKurir.setText("");
        txtPlatNomot.setText("");
        cbbJenisKendaraan.setSelectedIndex(0);
        cbbStatusKurir.setSelectedIndex(0);
    }

    public void isiComboBoxNamaBarang() {
        try {
            String sql = "SELECT DISTINCT nama_barang FROM operasional ORDER BY nama_barang";
            PreparedStatement pst = cn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            // Reset combo box
            cbbJenisBarangPenjualan.removeAllItems();

            // Tambahkan opsi "Semua Barang" di posisi pertama
            cbbJenisBarangPenjualan.addItem("Semua Barang");

            // Tambahkan nama barang ke combo box
            while (rs.next()) {
                cbbJenisBarangPenjualan.addItem(rs.getString("nama_barang"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal mengisi combo box nama barang: " + e.getMessage());
        }
    }

    public void TampilkanDataLaporanBulananBaru() {
        try {
            // Ambil nilai dari komponen form yang ada
            int bulan = cmbBulan.getSelectedIndex() + 1;
            String tahun = cmbTahun.getSelectedItem().toString();
            String jenisBarang = cbbJenisBarangPenjualan.getSelectedItem().toString();
            String status = cmbStatus.getSelectedItem().toString();
            String jenisCustomer = cmbJenisCustomer.getSelectedItem().toString();

            // Bangun query berdasarkan parameter yang dipilih
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT ")
                    .append("o.nama_barang, ")
                    .append("SUM(o.jumlah) as total_jumlah, ")
                    .append("SUM(o.total) as total_pendapatan, ")
                    .append("DATE_FORMAT(o.tanggal, '%Y-%m') as bulan_tahun ") // Mengubah tanggal menjadi format bulan-tahun
                    .append("FROM operasional o ")
                    .append("JOIN transaksi t ON o.id_barang = t.id_transaksi ") // Sesuaikan dengan relasi tabel Anda
                    .append("WHERE MONTH(o.tanggal) = ? AND YEAR(o.tanggal) = ? ");

            // Tambahkan filter tambahan sesuai pilihan
            if (!jenisBarang.equals("Semua Barang")) { // Disesuaikan dengan nilai yang sebenarnya di combo box
                queryBuilder.append("AND o.nama_barang = ? ");
            }

            if (!status.equals("Semua")) {
                queryBuilder.append("AND t.status = ? ");
            }

            if (!jenisCustomer.equals("Semua")) {
                queryBuilder.append("AND t.jenis_customer = ? ");
            }

            // Tambahkan grouping dan ordering
            queryBuilder.append("GROUP BY o.nama_barang, bulan_tahun ")
                    .append("ORDER BY total_jumlah DESC");

            String sqlBarang = queryBuilder.toString();
            PreparedStatement pstBarang = cn.prepareStatement(sqlBarang);

            // Set parameter query
            int paramIndex = 1;
            pstBarang.setInt(paramIndex++, bulan);
            pstBarang.setString(paramIndex++, tahun);

            // Set parameter tambahan jika ada filter
            if (!jenisBarang.equals("Semua Barang")) { // Disesuaikan dengan nilai yang sebenarnya di combo box
                pstBarang.setString(paramIndex++, jenisBarang);
            }

            if (!status.equals("Semua")) {
                pstBarang.setString(paramIndex++, status);
            }

            if (!jenisCustomer.equals("Semua")) {
                pstBarang.setString(paramIndex++, jenisCustomer);
            }

            ResultSet rsBarang = pstBarang.executeQuery();

            // Buat model untuk tabel laporan
            DefaultTableModel modelBarang = new DefaultTableModel();
            modelBarang.setColumnIdentifiers(new Object[]{
                "No.", "Nama Barang", "Jumlah Terjual", "Total Penjualan", "Bulan"
            });

            int no = 1;
            int totalBarangTerjual = 0;
            int totalPendapatanBarang = 0;
            int totalTransaksi = 0;

            while (rsBarang.next()) {
                int jumlahTerjual = rsBarang.getInt("total_jumlah");
                int pendapatan = rsBarang.getInt("total_pendapatan");

                modelBarang.addRow(new Object[]{
                    no++,
                    rsBarang.getString("nama_barang"),
                    jumlahTerjual,
                    "Rp. " + new DecimalFormat("#,###").format(pendapatan),
                    getNamaBulan(bulan) + " " + tahun
                });

                totalBarangTerjual += jumlahTerjual;
                totalPendapatanBarang += pendapatan;
                totalTransaksi++;
            }

            // Set model ke tabel laporan
            tableLaporanBulananBarang.setModel(modelBarang);

            // Format tabel
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16);
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18);
            tableLaporanBulananBarang.setFont(fontIsi);
            tableLaporanBulananBarang.setRowHeight(24);
            tableLaporanBulananBarang.getTableHeader().setFont(fontHeader);

            // Set nilai di ringkasan laporan
            DecimalFormat formatter = new DecimalFormat("#,###");
            lblTotalTransaksi.setText(totalTransaksi + " Transaksi");
            lblTotalBarangTerjual.setText(totalBarangTerjual + " Galon");
            lblTotalPendapatanN.setText("Rp. " + formatter.format(totalPendapatanBarang)); // Memperbaiki nama label

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data laporan: " + e.getMessage());
        }
    }

// Metode helper untuk nama bulan
    private String getNamaBulan(int bulan) {
        String[] namaBulan
                = {
                    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                };
        return namaBulan[bulan - 1];
    }

    public static void kurangiStokGalon(int jumlahTransaksi) {
        String[] kodeBarangs = {"A1", "B2", "D4", "C3"};

        try (Connection conn = koneksi.BukaKoneksi()) {
            for (String kodeBarang : kodeBarangs) {
                String sqlTotalStok = "SELECT nama_barang, SUM(jumlah) as total_stok "
                        + "FROM operasional WHERE kode_barang = ? "
                        + "GROUP BY nama_barang";

                String namaBarang = "";
                int totalStok = 0;

                try (PreparedStatement pstmtTotal = conn.prepareStatement(sqlTotalStok)) {
                    pstmtTotal.setString(1, kodeBarang);
                    ResultSet rs = pstmtTotal.executeQuery();

                    if (rs.next()) {
                        namaBarang = rs.getString("nama_barang");
                        totalStok = rs.getInt("total_stok");
                    } else {
                        System.err.println("Barang dengan kode " + kodeBarang + " tidak ditemukan");
                        continue;
                    }
                }
                if (totalStok < jumlahTransaksi) {
                    JOptionPane.showMessageDialog(null,
                            "Stok untuk barang \"" + namaBarang + "\" tidak mencukupi!\n"
                            + "Stok tersedia: " + totalStok + "\n"
                            + "Dibutuhkan: " + jumlahTransaksi,
                            "Peringatan Stok Habis",
                            JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                String sqlHarga = "SELECT harga_satuan FROM operasional "
                        + "WHERE kode_barang = ? "
                        + "ORDER BY tanggal DESC LIMIT 1";
                int hargaSatuan = 0;

                try (PreparedStatement pstmtHarga = conn.prepareStatement(sqlHarga)) {
                    pstmtHarga.setString(1, kodeBarang);
                    ResultSet rs = pstmtHarga.executeQuery();
                    if (rs.next()) {
                        hargaSatuan = rs.getInt("harga_satuan");
                    }
                }
                String sqlInsert = "INSERT INTO operasional (kode_barang, nama_barang, jumlah, "
                        + "harga_satuan, total, tanggal) "
                        + "VALUES (?, ?, ?, ?, ?, CURDATE())";

                try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                    pstmtInsert.setString(1, kodeBarang);
                    pstmtInsert.setString(2, namaBarang);
                    pstmtInsert.setInt(3, -jumlahTransaksi);
                    pstmtInsert.setInt(4, hargaSatuan);
                    pstmtInsert.setInt(5, jumlahTransaksi * hargaSatuan);
                    pstmtInsert.executeUpdate();

                    System.out.println("Stok " + namaBarang + " berkurang dari "
                            + totalStok + " menjadi " + (totalStok - jumlahTransaksi));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal memperbarui stok: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static ImageIcon recolorImage(ImageIcon icon, Color newColor) {
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Gambar icon ke BufferedImage
        g.drawImage(icon.getImage(), 0, 0, null);

        // Ubah warna
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = image.getRGB(x, y);
                Color col = new Color(rgba, true);
                if (col.getAlpha() != 0) { // ubah hanya pixel yang tidak transparan
                    Color newCol = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), col.getAlpha());
                    image.setRGB(x, y, newCol.getRGB());
                }
            }
        }

        g.dispose();
        return new ImageIcon(image);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CUSTOMER;
    private javax.swing.JPanel FRAME;
    private javax.swing.JPanel HEADER;
    private javax.swing.JPanel ISI;
    private javax.swing.JPanel KURIR;
    private javax.swing.JPanel LAPORAN_OPERASIONAL;
    private javax.swing.JPanel LAPORAN_PENJUALAN;
    private javax.swing.JPanel MENU;
    private javax.swing.JPanel OPERASIONAL;
    private javax.swing.JPanel PENDAPATANIsiUlangGalon;
    private javax.swing.JPanel PENJUALAN;
    private javax.swing.JButton PenjualanBarang;
    private javax.swing.JButton bersihPendapatan;
    private javax.swing.JButton btnBayar;
    private javax.swing.JButton btnBersihBulanan;
    private javax.swing.JButton btnBersihOperasional;
    private javax.swing.JButton btnCekGalon;
    private javax.swing.JButton btnCekSedimen;
    private javax.swing.JButton btnCekSegelGalon;
    private javax.swing.JButton btnCekTisuGalon;
    private javax.swing.JButton btnCekTutupGalon;
    private javax.swing.JButton btnCetak;
    private javax.swing.JButton btnCetakBarcode;
    private javax.swing.JButton btnCetakPendapatanBulanan;
    private javax.swing.JButton btnCetakPendapatanBulanan1;
    private javax.swing.JButton btnDataTerpilih;
    private javax.swing.JButton btnKembaliPenjualan;
    private javax.swing.JButton btnKembaliPenjualan1;
    private javax.swing.JButton btnLaporan;
    private javax.swing.JButton btnLaporan1;
    private javax.swing.JButton btnTambahKurir;
    private javax.swing.JButton btnTampilPenjualanOpearasional1;
    private javax.swing.JButton btnTampilPenjualanOpearasional2;
    private javax.swing.JButton btnTampilkan;
    private javax.swing.JButton btnTampilkan1;
    private javax.swing.JButton btnUbahKurir;
    private javax.swing.JButton btnUbahKurir1;
    private javax.swing.JButton btnUbahKurir2;
    private javax.swing.JButton btnbatal1;
    private javax.swing.JButton btnbatal2;
    private javax.swing.JButton btnbatalcustomer;
    private javax.swing.JButton btnbatalcustomer1;
    private javax.swing.JButton btnbayarcetak;
    private javax.swing.JButton btnhapus1;
    private javax.swing.JButton btnhapus2;
    private javax.swing.JButton btnhapuscustomer;
    private javax.swing.JButton btnhapuscustomer1;
    private javax.swing.JButton btntambahcustomer;
    private javax.swing.JButton btntambahcustomer1;
    private javax.swing.JButton btntambahoperasional;
    private javax.swing.JButton btntambahpenjualan;
    private javax.swing.JComboBox<String> cbbCariKurir;
    private javax.swing.JComboBox<String> cbbJenisBarangPenjualan;
    private javax.swing.JComboBox<String> cbbJenisKendaraan;
    private javax.swing.JComboBox<String> cbbKendaraanKurir;
    private javax.swing.JComboBox<String> cbbStatusKurir;
    private javax.swing.JComboBox<String> cmbBulan;
    private javax.swing.JComboBox<String> cmbBulan1;
    private javax.swing.JComboBox<String> cmbBulanPendapatan;
    private javax.swing.JComboBox<String> cmbJenisBarang;
    private javax.swing.JComboBox<String> cmbJenisCustomer;
    private javax.swing.JComboBox<String> cmbJudulBulan;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JComboBox<String> cmbTahun;
    private javax.swing.JComboBox<String> cmbTahun1;
    private javax.swing.JComboBox<String> cmbTahunPendapatan;
    private javax.swing.JComboBox<String> cmbcaribarang;
    private javax.swing.JComboBox<String> cmbcaricustomer;
    private javax.swing.JComboBox<String> cmbcaricustomer1;
    private javax.swing.JComboBox<String> cmbcaricustomer2;
    private javax.swing.JComboBox<String> cmbjeniscustomer;
    private javax.swing.JComboBox<String> cmbjeniscustomer2;
    private javax.swing.JComboBox<String> cmbkodebarang;
    private javax.swing.JComboBox<String> cmbnamabarang;
    private javax.swing.JComboBox<String> cmbstatus;
    private javax.swing.JButton customer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JButton keluar;
    private javax.swing.JButton kurir;
    private javax.swing.JLabel labelGalon;
    private javax.swing.JLabel labelOperasionalPendapatan;
    private javax.swing.JLabel labelPendapatan;
    private javax.swing.JLabel labelPenjualanPendapatan;
    private javax.swing.JLabel labelSedimen;
    private javax.swing.JLabel labelSegelGalon;
    private javax.swing.JLabel labelTisuGalon;
    private javax.swing.JLabel labelTutupGalon;
    private javax.swing.JLabel lblTotalBarangTerjual;
    private javax.swing.JLabel lblTotalBeliBarang;
    private javax.swing.JLabel lblTotalHarga;
    private javax.swing.JLabel lblTotalPendapatanN;
    private javax.swing.JLabel lblTotalPengeluaran;
    private javax.swing.JLabel lblTotalTransaksi;
    private javax.swing.JLabel lblTotalTransaksiBarang;
    private javax.swing.JPanel menu;
    private javax.swing.JButton operasional;
    private javax.swing.JButton pendapatanIsiUlangGalon;
    private javax.swing.JButton penjualan;
    private javax.swing.JTable tabelKurir;
    private javax.swing.JTable tabelOperasionalforPendapatan;
    private javax.swing.JTable tabelPendapatan;
    private javax.swing.JTable tabelPenjualanforPendapatan;
    private javax.swing.JTable tableLaporanBulananBarang;
    private javax.swing.JTable tableLaporanOperasional;
    private javax.swing.JTable tblCariKurir;
    private javax.swing.JTable tblPelangganBarang;
    private javax.swing.JTable tblcaricustomer2;
    private javax.swing.JTable tblcustomer;
    private javax.swing.JTable tbloperasional;
    private javax.swing.JTable tblpenjualan;
    private javax.swing.JTextArea txtAlamatKurir;
    private javax.swing.JTextField txtCariKurir;
    private javax.swing.JTextField txtIdKurir;
    private javax.swing.JTextField txtNamaKurir;
    private javax.swing.JTextField txtNamaKurirPengantar;
    private javax.swing.JTextField txtNoHP;
    private javax.swing.JTextField txtPencarian;
    private javax.swing.JTextField txtPlatNomorKurir;
    private javax.swing.JTextField txtPlatNomot;
    private javax.swing.JTextField txtalamat;
    private javax.swing.JTextField txtalamat1;
    private javax.swing.JTextField txtcaribarang;
    private javax.swing.JTextField txtcaricustomer;
    private javax.swing.JTextField txtcaricustomer1;
    private javax.swing.JTextField txtcaricustomer2;
    private javax.swing.JTextField txthargasatuan;
    private javax.swing.JTextField txthargasatuan2;
    private javax.swing.JTextField txtidbarang;
    private javax.swing.JTextField txtidcustomer;
    private javax.swing.JTextField txtidcustomer1;
    private javax.swing.JTextField txtidcustomer2;
    private javax.swing.JTextField txtidtransaksi;
    private javax.swing.JTextField txtjeniscustomer;
    private javax.swing.JTextField txtjeniscustomer2;
    private javax.swing.JTextField txtjumlahbarang;
    private javax.swing.JTextField txtjumlahpenjualan;
    private javax.swing.JTextField txtkodebarang;
    private javax.swing.JTextField txtnamabarang;
    private javax.swing.JTextField txtnamacustomer;
    private javax.swing.JTextField txtnamacustomer1;
    private javax.swing.JTextField txtnamacustomer2;
    private javax.swing.JTextField txtstatus;
    private javax.swing.JTextField txttelephone;
    private javax.swing.JTextField txttelephone1;
    private javax.swing.JTextField txttotalbarang;
    // End of variables declaration//GEN-END:variables
}
