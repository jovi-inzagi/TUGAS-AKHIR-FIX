/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POS;

/**
 *
 * @author Jovii
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class Transaksi extends javax.swing.JFrame {

    String Tanggal;
    public Statement st;
    public ResultSet rs;
    Connection cn = POS.koneksi.BukaKoneksi();

    public Transaksi() {
        initComponents();
    }

    public Transaksi(int id_level) {
        initComponents();
        aturFontTextField();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Bersih();
        aturMenu(id_level);
        TampilDataPenjualan();
        CariDataPenjualan();
        TampilDataCustomer();
        CariDataCustomer();
    }

    public void TampilkanDataLaporanBulanan() {
        try
        {
            int bulan = cmbBulan.getSelectedIndex() + 1;
            String tahun = cmbTahun.getSelectedItem().toString();
            String jenisCustomer = cmbJenisCustomer.getSelectedItem().toString();
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
            model.setColumnIdentifiers(new Object[]
            {
                "No.", "ID", "Tanggal", "Customer", "Status", "Galon", "Total"
            });

            int totalGalon = 0;
            int totalPendapatan = 0;
            int totalTransaksi = 0;
            int no = 1;

            while (rs.next())
            {
                int galon = rs.getInt("jumlah");
                int total = rs.getInt("total");

                model.addRow(new Object[]
                {
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

            tableLaporanBulanan.setModel(model);

            // Atur Font setelah setModel
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16);
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18);

            tableLaporanBulanan.setFont(fontIsi);
            tableLaporanBulanan.setRowHeight(24);
            tableLaporanBulanan.getTableHeader().setFont(fontHeader);

            // Format angka
            DecimalFormat formatter = new DecimalFormat("#,###");

            // Tampilkan ringkasan ke JLabel
            lblTotalGalonTerjual.setText(totalGalon + " Galon");
//            lblTotalPendapatan.setText("<html><span style='color:green; font-weight:bold; font-size:20pt;'>Rp. " + formatter.format(totalPendapatan) + "</span></html>");
            lblTotalGalonTerjual.setText("Rp. " + formatter.format(totalPendapatan));
            lblTotalTransaksi.setText(totalTransaksi + " Transaksi");

        } catch (SQLException e)
        {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data laporan: " + e.getMessage());
        }
    }

    public Map<String, Object> getLaporanBulananParameters() {
        // Daftar nama bulan untuk referensi judul laporan
        String[] namaBulan =
        {
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

    public void aturMenu(int id_level) {
        switch (id_level)
        {
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

    public void aturFontTextField() {
        Font fontBesar = new Font("Tahoma", Font.PLAIN, 20);
        Font fontBiggest = new Font("Tahoma", Font.PLAIN, 40);

        // Form input customer
        txtidcustomer.setFont(fontBesar);
        txtnamacustomer.setFont(fontBesar);
        txtalamat.setFont(fontBesar);
        txttelephone.setFont(fontBesar);
        txtjeniscustomer.setFont(fontBesar);

        // Form input penjualan
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
        txtcaricustomer2.setFont(fontBesar);

        // JComboBox
        cmbcaricustomer.setFont(fontBesar);
        cmbcaricustomer2.setFont(fontBesar);
        cmbjeniscustomer.setFont(fontBesar);
        cmbjeniscustomer2.setFont(fontBesar);
        cmbstatus.setFont(fontBesar);

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

    }

    public void Bersih() {

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
        DefaultTableModel modelBulanan = (DefaultTableModel) tableLaporanBulanan.getModel();
        modelBulanan.setRowCount(0); // Hapus semua baris
        lblTotalGalonTerjual.setText("0");
        lblTotalPendapatan.setText("Rp. 0");
        lblTotalTransaksi.setText("0");

    }

    public void TampilDataPenjualan() {
        try
        {
            st = cn.createStatement();
            rs = st.executeQuery("SELECT * FROM transaksi");

            // Tabel 1 data operasional
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID Transaksi");
            model.addColumn("Nama Customer");
            model.addColumn("ID Customer");
            model.addColumn("Jenis Customer");
            model.addColumn("Harga Satuan");
            model.addColumn("Jumlah");
            model.addColumn("Total");
            model.addColumn("Tanggal");
            model.addColumn("Status");
            tblpenjualan.setModel(model);

            while (rs.next())
            {
                // Data tabel 1 tanpa nomor urut
                Object[] data =
                {
                    rs.getInt("id_transaksi"),
                    rs.getString("nama_customer"),
                    rs.getInt("id_customer"),
                    rs.getString("jenis_customer"),
                    rs.getInt("harga"),
                    rs.getInt("jumlah"),
                    rs.getInt("total"),
                    rs.getString("tanggal"),
                    rs.getString("status")
                };
                model.addRow(data);
            }

            // Untuk atur font dan jarak antar baris
            Font fontIsi = new Font("Segoe UI", Font.PLAIN, 16); // Font isi tabel
            Font fontHeader = new Font("Segoe UI", Font.BOLD, 18); // Font header kolom
            tblpenjualan.setFont(fontIsi);
            tblpenjualan.setRowHeight(28); // Atur tinggi baris (bisa disesuaikan)
            tblpenjualan.getTableHeader().setFont(fontHeader);

        } catch (SQLException e)
        {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void CariDataPenjualan() {
        try
        {
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

            while (rs.next())
            {
                // Data tabel 1
                Object[] data =
                {
                    rs.getInt("id_customer"),
                    rs.getString("nama_customer"),
                    rs.getString("jenis_customer"),
                    rs.getString("alamat"),
                    rs.getString("telephone")
                };
                model.addRow(data);
            }
        } catch (SQLException e)
        {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void TampilDataCustomer() {
        try
        {
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
            while (rs.next())
            {
                Object[] data =
                {
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

        } catch (SQLException e)
        {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void CariDataCustomer() {
        try
        {
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
            while (rs.next())
            {
                // Data tabel 1
                Object[] data =
                {
                    no1++,
                    rs.getInt("id_customer"),
                    rs.getString("nama_customer"),
                    rs.getString("jenis_customer"),
                    rs.getString("alamat"),
                    rs.getString("telephone")
                };
                model.addRow(data);
            }
        } catch (SQLException e)
        {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        jLabel23 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        txtnamacustomer = new javax.swing.JTextField();
        txtidcustomer = new javax.swing.JTextField();
        txtalamat = new javax.swing.JTextField();
        txttelephone = new javax.swing.JTextField();
        cmbjeniscustomer = new javax.swing.JComboBox<>();
        btnhapuscustomer = new javax.swing.JButton();
        btnbatalcustomer = new javax.swing.JButton();
        btntambahcustomer = new javax.swing.JButton();
        txtjeniscustomer = new javax.swing.JTextField();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblcustomer = new javax.swing.JTable();
        jLabel28 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        txtcaricustomer = new javax.swing.JTextField();
        cmbcaricustomer = new javax.swing.JComboBox<>();
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
        LAPORAN_Penjualan = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jPanel20 = new javax.swing.JPanel();
        jLabel50 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        cmbBulan = new javax.swing.JComboBox<>();
        cmbJudulBulan = new javax.swing.JComboBox<>();
        jLabel43 = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
        jLabel41 = new javax.swing.JLabel();
        cmbTahun = new javax.swing.JComboBox<>();
        cmbJenisCustomer = new javax.swing.JComboBox<>();
        jLabel45 = new javax.swing.JLabel();
        btnTampilkan = new javax.swing.JButton();
        btnBersihBulanan = new javax.swing.JButton();
        btnCetak = new javax.swing.JButton();
        jPanel21 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        lblTotalTransaksi = new javax.swing.JLabel();
        lblTotalGalonTerjual = new javax.swing.JLabel();
        lblTotalPendapatan = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableLaporanBulanan = new javax.swing.JTable();
        btnKembaliPenjualan = new javax.swing.JButton();
        MENU = new javax.swing.JPanel();
        menu = new javax.swing.JPanel();
        customer = new javax.swing.JButton();
        keluar = new javax.swing.JButton();
        penjualan = new javax.swing.JButton();
        operasional = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        jLabel44 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        FRAME.setBackground(new java.awt.Color(51, 51, 51));
        FRAME.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 255), 2));
        FRAME.setPreferredSize(new java.awt.Dimension(1920, 1080));

        HEADER.setBackground(new java.awt.Color(0, 0, 255));

        jLabel1.setBackground(new java.awt.Color(204, 204, 204));
        jLabel1.setFont(new java.awt.Font("Rockwell", 3, 36)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("POINT OF SALES PENJUALAN DEPOT AIR MINUM");
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

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("TAMBAH PELANGGAN");

        jPanel9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel24.setText("ID Customer");

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel25.setText("Nama Customer");

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel26.setText("Alamat");

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel30.setText("Telephone");

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel27.setText("Jenis Pelanggan");

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
                            .addComponent(jLabel25)
                            .addComponent(jLabel26)
                            .addComponent(jLabel30)
                            .addComponent(jLabel24))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txttelephone)
                            .addComponent(txtalamat)
                            .addComponent(txtnamacustomer)
                            .addComponent(txtidcustomer)))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addGap(18, 18, 18)
                        .addComponent(cmbjeniscustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtjeniscustomer))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnhapuscustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnbatalcustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btntambahcustomer)))
                .addGap(20, 20, 20))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(txtidcustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
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
                    .addComponent(jLabel27)
                    .addComponent(cmbjeniscustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtjeniscustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btntambahcustomer)
                    .addComponent(btnbatalcustomer)
                    .addComponent(btnhapuscustomer))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

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

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1058, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 1, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 758, Short.MAX_VALUE)
        );

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("CARI PELANGGAN");

        jPanel13.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtcaricustomer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtcaricustomerKeyPressed(evt);
            }
        });

        cmbcaricustomer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "id_customer", "nama_customer", "jenis_customer" }));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbcaricustomer, 0, 152, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(txtcaricustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtcaricustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbcaricustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61)
                        .addGroup(CUSTOMERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel28, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(CUSTOMERLayout.createSequentialGroup()
                                .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(15, 15, 15))
                            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        CUSTOMERLayout.setVerticalGroup(
            CUSTOMERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CUSTOMERLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(CUSTOMERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(CUSTOMERLayout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
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
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
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
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cmbcaricustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtcaricustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addComponent(jScrollPane1)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
        );

        btnLaporan.setText("LAPORAN DATA");
        btnLaporan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLaporanActionPerformed(evt);
            }
        });

        lblTotalHarga.setFont(new java.awt.Font("Tahoma", 1, 40)); // NOI18N
        lblTotalHarga.setText("Rp. 0");

        btnCetakBarcode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnCetakBarcode.setText("Cetak Barcode");
        btnCetakBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakBarcodeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PENJUALANLayout = new javax.swing.GroupLayout(PENJUALAN);
        PENJUALAN.setLayout(PENJUALANLayout);
        PENJUALANLayout.setHorizontalGroup(
            PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 1614, Short.MAX_VALUE)
            .addGroup(PENJUALANLayout.createSequentialGroup()
                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 381, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnLaporan, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel39)
                                    .addComponent(jLabel33)
                                    .addComponent(jLabel32))
                                .addGap(18, 18, 18)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtnamacustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtidcustomer2, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtidtransaksi, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel34)
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addComponent(cmbjeniscustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtjeniscustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(27, 27, 27)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel40)
                                    .addComponent(txthargasatuan2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel42)
                                    .addComponent(txtjumlahpenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel38)
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addComponent(cmbstatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtstatus, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(PENJUALANLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addComponent(lblTotalHarga)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnbatal2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btntambahpenjualan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnCetakBarcode)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnhapus2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnBayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDataTerpilih, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnbayarcetak, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE))))
                .addContainerGap())
        );
        PENJUALANLayout.setVerticalGroup(
            PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PENJUALANLayout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(btnLaporan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addComponent(jLabel38)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(cmbstatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtstatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                                .addComponent(jLabel42)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtjumlahpenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                                .addComponent(jLabel40)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txthargasatuan2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                                .addComponent(jLabel34)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(cmbjeniscustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtjeniscustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(PENJUALANLayout.createSequentialGroup()
                            .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel32)
                                .addComponent(txtidtransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel33)
                                .addComponent(txtidcustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel39)
                                .addComponent(txtnamacustomer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PENJUALANLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addComponent(btnBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDataTerpilih, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PENJUALANLayout.createSequentialGroup()
                                .addComponent(btntambahpenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(btnbatal2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(PENJUALANLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnhapus2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnCetakBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(PENJUALANLayout.createSequentialGroup()
                                        .addGap(7, 7, 7)
                                        .addComponent(btnbayarcetak, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PENJUALANLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotalHarga)
                        .addGap(69, 69, 69))))
        );

        LAPORAN_Penjualan.setBackground(new java.awt.Color(255, 255, 255));
        LAPORAN_Penjualan.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jPanel7.setBackground(new java.awt.Color(0, 153, 255));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("Laporan Bulanan Penjualan Air Galon");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel21)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel20.setBackground(new java.awt.Color(243, 243, 243));

        jLabel50.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel50.setText("Judul Bulan :");

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel22.setText("Bulan :");

        cmbBulan.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbBulan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember" }));

        cmbJudulBulan.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbJudulBulan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember", " " }));

        jLabel43.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel43.setText("Tahun :");

        cmbStatus.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Lunas", "Belum Lunas" }));
        cmbStatus.setPreferredSize(new java.awt.Dimension(100, 25));

        jLabel41.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel41.setText("Status :");

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

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jLabel50)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbJudulBulan, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addGap(24, 24, 24)
                        .addComponent(cmbBulan, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel20Layout.createSequentialGroup()
                            .addComponent(jLabel41)
                            .addGap(18, 18, 18)
                            .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel20Layout.createSequentialGroup()
                            .addComponent(jLabel43)
                            .addGap(18, 18, 18)
                            .addComponent(cmbTahun, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jLabel45)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbJenisCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnCetak, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBersihBulanan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnTampilkan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)))
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
                    .addComponent(jLabel22)
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
                            .addComponent(jLabel41)
                            .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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

        lblTotalGalonTerjual.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalGalonTerjual.setText("0");

        lblTotalPendapatan.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTotalPendapatan.setText("Rp. 0");

        jLabel47.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel47.setText("Total Transaksi :");

        jLabel48.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel48.setText("Total Galon Terjual :");

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
                            .addComponent(lblTotalGalonTerjual)
                            .addComponent(lblTotalPendapatan))))
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
                    .addComponent(lblTotalGalonTerjual)
                    .addComponent(jLabel48))
                .addGap(18, 18, 18)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalPendapatan)
                    .addComponent(jLabel49))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tableLaporanBulanan.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane6.setViewportView(tableLaporanBulanan);

        btnKembaliPenjualan.setText("Kembali");
        btnKembaliPenjualan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliPenjualanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout LAPORAN_PenjualanLayout = new javax.swing.GroupLayout(LAPORAN_Penjualan);
        LAPORAN_Penjualan.setLayout(LAPORAN_PenjualanLayout);
        LAPORAN_PenjualanLayout.setHorizontalGroup(
            LAPORAN_PenjualanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(LAPORAN_PenjualanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 1067, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(LAPORAN_PenjualanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(LAPORAN_PenjualanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnKembaliPenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        LAPORAN_PenjualanLayout.setVerticalGroup(
            LAPORAN_PenjualanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LAPORAN_PenjualanLayout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(LAPORAN_PenjualanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LAPORAN_PenjualanLayout.createSequentialGroup()
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnKembaliPenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout ISILayout = new javax.swing.GroupLayout(ISI);
        ISI.setLayout(ISILayout);
        ISILayout.setHorizontalGroup(
            ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PENJUALAN, javax.swing.GroupLayout.DEFAULT_SIZE, 1618, Short.MAX_VALUE)
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(LAPORAN_Penjualan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(CUSTOMER, javax.swing.GroupLayout.DEFAULT_SIZE, 1618, Short.MAX_VALUE))
        );
        ISILayout.setVerticalGroup(
            ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PENJUALAN, javax.swing.GroupLayout.DEFAULT_SIZE, 861, Short.MAX_VALUE)
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(LAPORAN_Penjualan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(CUSTOMER, javax.swing.GroupLayout.DEFAULT_SIZE, 861, Short.MAX_VALUE))
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

        keluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/closed.png"))); // NOI18N
        keluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keluarActionPerformed(evt);
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

        jPanel22.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel44.setBackground(new java.awt.Color(204, 204, 204));
        jLabel44.setFont(new java.awt.Font("Rockwell", 3, 30)); // NOI18N
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel44.setText("WATERMEN");
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
            .addGroup(menuLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(menuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customer, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keluar, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(penjualan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(operasional, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, menuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        menuLayout.setVerticalGroup(
            menuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 186, Short.MAX_VALUE)
                .addComponent(customer, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(penjualan, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(operasional, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(keluar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(168, 168, 168))
        );

        javax.swing.GroupLayout MENULayout = new javax.swing.GroupLayout(MENU);
        MENU.setLayout(MENULayout);
        MENULayout.setHorizontalGroup(
            MENULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(menu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
        );
        MENULayout.setVerticalGroup(
            MENULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(menu, javax.swing.GroupLayout.DEFAULT_SIZE, 861, Short.MAX_VALUE)
        );

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("@ 2025 | POS WATERMEN | By: Jovi Inzagi");

        javax.swing.GroupLayout FRAMELayout = new javax.swing.GroupLayout(FRAME);
        FRAME.setLayout(FRAMELayout);
        FRAMELayout.setHorizontalGroup(
            FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(HEADER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FRAMELayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(FRAMELayout.createSequentialGroup()
                        .addComponent(MENU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(ISI, javax.swing.GroupLayout.PREFERRED_SIZE, 1622, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 12, Short.MAX_VALUE)))
                .addContainerGap())
        );
        FRAMELayout.setVerticalGroup(
            FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FRAMELayout.createSequentialGroup()
                .addComponent(HEADER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ISI, javax.swing.GroupLayout.DEFAULT_SIZE, 865, Short.MAX_VALUE)
                    .addComponent(MENU, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(FRAME, javax.swing.GroupLayout.PREFERRED_SIZE, 1913, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 28, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FRAME, javax.swing.GroupLayout.PREFERRED_SIZE, 990, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void operasionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_operasionalActionPerformed
        // TODO add your handling code here:
        //        ISI.removeAll();
        //        ISI.repaint();
        //        ISI.revalidate();
        //
        //        //menambahkan panel
        //        ISI.add(OPERASIONAL);
        //        ISI.repaint();
        //        ISI.revalidate();
    }//GEN-LAST:event_operasionalActionPerformed

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

    private void keluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keluarActionPerformed
        // TODO add your handling code here:
        Login l = new Login();
        l.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_keluarActionPerformed

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

    private void btnTampilkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilkanActionPerformed
        TampilkanDataLaporanBulanan();
    }//GEN-LAST:event_btnTampilkanActionPerformed

    private void btnBersihBulananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihBulananActionPerformed
        // TODO add your handling code here:
        Bersih();
    }//GEN-LAST:event_btnBersihBulananActionPerformed

    private void btnCetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakActionPerformed
        // TODO add your handling code here:
        String judulBulan = cmbJudulBulan.getSelectedItem().toString();
        String bulan = cmbBulan.getSelectedItem().toString();

        if (!judulBulan.equalsIgnoreCase(bulan))
        {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(this,
                    "Judul Bulan dan Bulan harus sama!\nSilakan sesuaikan terlebih dahulu.",
                    "Validasi Bulan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try
        {
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

        } catch (JRException e)
        {
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

    private void btnLaporanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLaporanActionPerformed
        // TODO add your handling code here:
        ISI.removeAll();
        ISI.repaint();
        ISI.revalidate();

        //menambahkan panel
        ISI.add(LAPORAN_Penjualan);
        ISI.repaint();
        ISI.revalidate();
    }//GEN-LAST:event_btnLaporanActionPerformed

    private void tblpenjualanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblpenjualanMouseClicked
        int selectedRow = tblpenjualan.getSelectedRow();

        if (selectedRow != -1)
        {
            txtidtransaksi.setText(tblpenjualan.getValueAt(selectedRow, 0).toString());
            txtnamacustomer2.setText(tblpenjualan.getValueAt(selectedRow, 1).toString());
            txtidcustomer2.setText(tblpenjualan.getValueAt(selectedRow, 2).toString());
            txtjeniscustomer2.setText(tblpenjualan.getValueAt(selectedRow, 3).toString());
            txthargasatuan2.setText(tblpenjualan.getValueAt(selectedRow, 4).toString());
            txtjumlahpenjualan.setText(tblpenjualan.getValueAt(selectedRow, 5).toString());
            txtstatus.setText(tblpenjualan.getValueAt(selectedRow, 8).toString());

            // Format total ke desimal (misalnya Rp. 10.000)
            try
            {
                int totalInt = Integer.parseInt(tblpenjualan.getValueAt(selectedRow, 6).toString());

                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setGroupingSeparator(',');
                DecimalFormat formatter = new DecimalFormat("#,###", symbols);
                String totalFormatted = formatter.format(totalInt);

//                lblTotalHarga.setText("<html><span style='color:green; font-weight:bold;'>Rp. " + totalFormatted + "</span></html>");
                lblTotalHarga.setForeground(Color.GREEN);
                lblTotalHarga.setFont(new Font("Tahoma", Font.BOLD, 40));
                lblTotalHarga.setText("" + totalFormatted);
            } catch (NumberFormatException e)
            {
                lblTotalHarga.setText("Rp. 0"); // Fallback jika parsing gagal
            }

            txtidcustomer2.setEditable(false);
            btntambahpenjualan.setText("UBAH");
        }
    }//GEN-LAST:event_tblpenjualanMouseClicked

    private void btnDataTerpilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDataTerpilihActionPerformed
        try
        {
            int selectedRow = tblpenjualan.getSelectedRow();
            if (selectedRow == -1)
            {
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

            if (jp.getPages().isEmpty())
            {
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk ditampilkan");
            } else
            {
                JasperViewer.viewReport(jp, false);
            }
        } catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(null, "ID Transaksi harus berupa angka: " + e.getMessage());
        } catch (JRException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }//GEN-LAST:event_btnDataTerpilihActionPerformed

    private void btnBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBayarActionPerformed

        String orderId = txtidtransaksi.getText();
        String customerName = txtnamacustomer2.getText();
        String customerEmail = "admin@gmail.com";
        String customerPhone = "08123456789";

        String totalText = lblTotalHarga.getText().replace("Rp.", "").replace(",", "")
                .replace(".", "").trim();
        int totalAmount = Integer.parseInt(totalText);

        try
        {
            if (orderId == null || orderId.isEmpty())
            {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(this, "No Transaksi tidak boleh kosong", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean paymentInitiated = MidtransPayment.processPayment(
                    orderId, totalAmount, customerName, customerEmail, customerPhone);

            if (paymentInitiated)
            {
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

        } catch (HeadlessException | NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }//GEN-LAST:event_btnBayarActionPerformed

    private void btnbatal2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbatal2ActionPerformed
        Bersih();
    }//GEN-LAST:event_btnbatal2ActionPerformed

    private void txtcaricustomer2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtcaricustomer2KeyPressed
        CariDataPenjualan();
    }//GEN-LAST:event_txtcaricustomer2KeyPressed

    private void tblcaricustomer2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblcaricustomer2MouseClicked
        int selectedRow = tblcaricustomer2.getSelectedRow(); // Ambil baris yang diklik

        if (selectedRow != -1)
        { // Pastikan baris valid
            txtidcustomer2.setText(tblcaricustomer2.getValueAt(selectedRow, 0).toString());
            txtnamacustomer2.setText(tblcaricustomer2.getValueAt(selectedRow, 1).toString());
            txtjeniscustomer2.setText(tblcaricustomer2.getValueAt(selectedRow, 2).toString());

            txtidcustomer2.setEditable(false);
        }
    }//GEN-LAST:event_tblcaricustomer2MouseClicked

    private void btnbayarcetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbayarcetakActionPerformed
        int selectedRow = tblpenjualan.getSelectedRow();
        try
        {
            Connection cn = koneksi.BukaKoneksi();
            File namafile = new File("src/laporan/laporan_transaksi.jasper");
            JasperPrint jp = JasperFillManager.fillReport(namafile.getPath(), null, cn);
            JasperViewer.viewReport(jp, false);
        } catch (JRException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }//GEN-LAST:event_btnbayarcetakActionPerformed

    private void btntambahpenjualanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btntambahpenjualanActionPerformed
        try
        {
            st = cn.createStatement();

            // Validasi input
            if (txtnamacustomer2.getText().trim().equals("")
                    || txtidcustomer2.getText().trim().equals("")
                    || txtjeniscustomer2.getText().trim().equals("")
                    || txthargasatuan2.getText().trim().equals("")
                    || txtjumlahpenjualan.getText().trim().equals("")
                    || txtstatus.getText().trim().equals(""))
            {

                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(null, "Data tidak boleh kosong", "Validasi Data",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Validasi data customer sesuai database
            String queryValidasiCustomer = "SELECT nama_customer, jenis_customer FROM customer WHERE id_customer = ?";
            try (PreparedStatement pstCek = cn.prepareStatement(queryValidasiCustomer))
            {
                pstCek.setString(1, txtidcustomer2.getText().trim());
                ResultSet rsCek = pstCek.executeQuery();

                if (rsCek.next())
                {
                    String namaCustomerAsli = rsCek.getString("nama_customer");
                    String jenisCustomerAsli = rsCek.getString("jenis_customer");

                    if (!namaCustomerAsli.equalsIgnoreCase(txtnamacustomer2.getText().trim()))
                    {
                        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                        JOptionPane.showMessageDialog(null,
                                "Nama customer tidak sesuai dengan ID customer.\nHarusnya: " + namaCustomerAsli,
                                "Validasi Nama Customer", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!jenisCustomerAsli.equalsIgnoreCase(txtjeniscustomer2.getText().trim()))
                    {
                        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                        JOptionPane.showMessageDialog(null,
                                "Jenis customer tidak sesuai dengan ID customer.\nHarusnya: " + jenisCustomerAsli,
                                "Validasi Jenis Customer", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else
                {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "ID Customer tidak ditemukan di database!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String jenisCustomer = txtjeniscustomer2.getText().trim();
            int hargaSatuan = 0;

            switch (jenisCustomer)
            {
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
            if (btntambahpenjualan.getText().equals("TAMBAH"))
            {
                // 1. Cari ID transaksi yang bisa digunakan kembali
                String queryCheckID = "SELECT MIN(t1.id_transaksi + 1) AS next_id "
                        + "FROM transaksi t1 "
                        + "LEFT JOIN transaksi t2 ON t1.id_transaksi + 1 = t2.id_transaksi "
                        + "WHERE t2.id_transaksi IS NULL";
                ResultSet rs = st.executeQuery(queryCheckID);

                int newID = 1; // Default ID jika tabel kosong

                if (rs.next() && rs.getInt("next_id") > 0)
                {
                    newID = rs.getInt("next_id"); // Gunakan kembali ID yang hilang
                }

                // 2. Tambahkan data dengan ID yang ditemukan
                String sql = "INSERT INTO transaksi (id_transaksi, nama_customer, id_customer, jenis_customer, harga, jumlah, total, tanggal, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pst = cn.prepareStatement(sql))
                {
                    pst.setInt(1, newID);
                    pst.setString(2, txtnamacustomer2.getText());
                    pst.setString(3, txtidcustomer2.getText());
                    pst.setString(4, txtjeniscustomer2.getText());
                    pst.setInt(5, hargaSatuan);
                    pst.setInt(6, jumlahBeli);
                    pst.setInt(7, totalBayar);
                    pst.setDate(8, new java.sql.Date(new java.util.Date().getTime()));
                    pst.setString(9, txtstatus.getText());

                    pst.executeUpdate();
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Data berhasil disimpan dengan ID: " + newID);
                    Bersih();
                    TampilDataPenjualan();
                }
            } else
            {
                // Aksi ubah data
                String update = "UPDATE transaksi SET jenis_customer = ?, harga = ?, jumlah = ?, total = ?, status = ? WHERE id_transaksi = ?";
                try (PreparedStatement pstmt = cn.prepareStatement(update))
                {
                    pstmt.setString(1, txtjeniscustomer2.getText());
                    pstmt.setInt(2, hargaSatuan);
                    pstmt.setInt(3, jumlahBeli);
                    pstmt.setInt(4, totalBayar);
                    pstmt.setString(5, txtstatus.getText());
                    pstmt.setInt(6, Integer.parseInt(txtidtransaksi.getText())); // ✅ Perbaikan jumlah parameter

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Data berhasil diperbarui");
                    Bersih();
                    TampilDataPenjualan();
                }
            }
        } catch (SQLException e)
        {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e)
        {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Masukkan harga dan jumlah sebagai angka!", "Validasi Data", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btntambahpenjualanActionPerformed

    private void btnhapus2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnhapus2ActionPerformed
        try
        {
            // Validasi input
            if (txtidtransaksi.getText().trim().equals(""))
            {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus transaksi ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION)
            {
                String sql = "DELETE FROM transaksi WHERE id_transaksi = ?";
                try (PreparedStatement pst = cn.prepareStatement(sql))
                {
                    pst.setInt(1, Integer.parseInt(txtidtransaksi.getText()));

                    int rowsDeleted = pst.executeUpdate();
                    if (rowsDeleted > 0)
                    {
                        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                        JOptionPane.showMessageDialog(null, "Data berhasil dihapus");
                        Bersih();
                        TampilDataPenjualan();
                    } else
                    {
                        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                        JOptionPane.showMessageDialog(null, "Data tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e)
        {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e)
        {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "ID transaksi harus berupa angka!", "Validasi Data", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnhapus2ActionPerformed

    private void cmbstatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbstatusActionPerformed

        switch (cmbstatus.getSelectedIndex())
        {
            case 0:
                txtstatus.setText("Belum Lunas");
                break;
            default:
                txtstatus.setText("Lunas");
                break;
        }
    }//GEN-LAST:event_cmbstatusActionPerformed

    private void cmbjeniscustomer2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbjeniscustomer2ActionPerformed

        switch (cmbjeniscustomer2.getSelectedIndex())
        {
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

    private void cmbjeniscustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbjeniscustomerActionPerformed
        switch (cmbjeniscustomer.getSelectedIndex())
        {
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
        if (txtidcustomer.getText().trim().equals(""))
        {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int jawab = JOptionPane.showConfirmDialog(null,
                "Data ini akan dihapus, lanjutkan?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (jawab == JOptionPane.YES_OPTION)
        {
            try
            {
                String sql = "DELETE FROM customer WHERE id_customer = ?";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(txtidcustomer.getText())); // Konversi ke integer

                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0)
                {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    Bersih();
                    TampilDataCustomer();
                } else
                {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(null, "Data gagal dihapus. ID tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e)
            {
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
        try
        {
            st = cn.createStatement();

            // Validasi input tidak boleh kosong
            if (txtnamacustomer.getText().trim().equals("")
                    || txtjeniscustomer.getText().trim().equals("")
                    || txtalamat.getText().trim().equals("")
                    || txttelephone.getText().trim().equals(""))
            {

                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                JOptionPane.showMessageDialog(null, "Data tidak boleh kosong!", "Validasi Data",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (btntambahcustomer.getText().equals("TAMBAH"))
            {
                // Cari ID customer yang bisa digunakan kembali
                // 1. Cari ID transaksi yang bisa digunakan kembali
                String queryCheckID = "SELECT MIN(t1.id_customer + 1) AS next_id "
                        + "FROM customer t1 "
                        + "LEFT JOIN customer t2 ON t1.id_customer + 1 = t2.id_customer "
                        + "WHERE t2.id_customer IS NULL";
                ResultSet rs = st.executeQuery(queryCheckID);

                int newID = 1; // Default ID jika tabel kosong

                if (rs.next() && rs.getInt("next_id") > 0)
                {
                    newID = rs.getInt("next_id"); // Gunakan kembali ID yang hilang
                }

                // Insert data customer
                String sql = "INSERT INTO customer (id_customer, nama_customer, jenis_customer, alamat, telephone) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pst = cn.prepareStatement(sql))
                {
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

            } else
            {
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

        } catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btntambahcustomerActionPerformed

    private void tblcustomerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblcustomerMouseClicked
        int selectedRow = tblcustomer.getSelectedRow(); // Ambil baris yang diklik

        if (selectedRow != -1)
        { // Pastikan baris valid
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

    private void btnCetakBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakBarcodeActionPerformed

    }//GEN-LAST:event_btnCetakBarcodeActionPerformed

    private void updatePaymentStatus(String orderId, String status) {
        try
        {
            Connection conn = koneksi.BukaKoneksi();
            if (conn == null)
            {
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
        } catch (SQLException e)
        {
            System.out.println("Error updating payment status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkPaymentStatus(String orderId) {
        new Thread(() ->
        {
            try
            {
                boolean isPaid = false;
                int attempts = 0;

                while (!isPaid && attempts < 60)
                {
                    Thread.sleep(5000);
                    attempts++;

                    String status = MidtransStatusChecker.checkStatus(orderId);
                    System.out.println("Checking payment status attempt " + attempts + ": " + status);

                    if ("settlement".equals(status) || "capture".equals(status))
                    {
                        updatePaymentStatus(orderId, "Lunas");
                        isPaid = true;

                        SwingUtilities.invokeLater(() ->
                        {
                            JOptionPane.showMessageDialog(this,
                                    "Pembayaran berhasil!",
                                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
                            Bersih();
                        });
                    } else if ("deny".equals(status) || "cancel".equals(status) || "expire".equals(status))
                    {
                        updatePaymentStatus(orderId, "Belum Lunas");
                        isPaid = true;

                        SwingUtilities.invokeLater(() ->
                        {
                            JOptionPane.showMessageDialog(this,
                                    "Pembayaran gagal atau dibatalkan.",
                                    "Gagal", JOptionPane.WARNING_MESSAGE);
                        });
                    }
                }

                if (!isPaid)
                {
                    SwingUtilities.invokeLater(() ->
                    {
                        JOptionPane.showMessageDialog(this,
                                "Waktu pembayaran habis. Silakan coba lagi.",
                                "Timeout", JOptionPane.WARNING_MESSAGE);
                    });
                }

            } catch (InterruptedException e)
            {
                e.printStackTrace();
                SwingUtilities.invokeLater(() ->
                {
                    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
                    UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
                    JOptionPane.showMessageDialog(this,
                            "Error saat memeriksa status pembayaran: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Transaksi().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CUSTOMER;
    private javax.swing.JPanel FRAME;
    private javax.swing.JPanel HEADER;
    private javax.swing.JPanel ISI;
    private javax.swing.JPanel LAPORAN_Penjualan;
    private javax.swing.JPanel MENU;
    private javax.swing.JPanel PENJUALAN;
    private javax.swing.JButton btnBayar;
    private javax.swing.JButton btnBersihBulanan;
    private javax.swing.JButton btnCetak;
    private javax.swing.JButton btnCetakBarcode;
    private javax.swing.JButton btnDataTerpilih;
    private javax.swing.JButton btnKembaliPenjualan;
    private javax.swing.JButton btnLaporan;
    private javax.swing.JButton btnTampilkan;
    private javax.swing.JButton btnbatal2;
    private javax.swing.JButton btnbatalcustomer;
    private javax.swing.JButton btnbayarcetak;
    private javax.swing.JButton btnhapus2;
    private javax.swing.JButton btnhapuscustomer;
    private javax.swing.JButton btntambahcustomer;
    private javax.swing.JButton btntambahpenjualan;
    private javax.swing.JComboBox<String> cmbBulan;
    private javax.swing.JComboBox<String> cmbJenisCustomer;
    private javax.swing.JComboBox<String> cmbJudulBulan;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JComboBox<String> cmbTahun;
    private javax.swing.JComboBox<String> cmbcaricustomer;
    private javax.swing.JComboBox<String> cmbcaricustomer2;
    private javax.swing.JComboBox<String> cmbjeniscustomer;
    private javax.swing.JComboBox<String> cmbjeniscustomer2;
    private javax.swing.JComboBox<String> cmbstatus;
    private javax.swing.JButton customer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
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
    private javax.swing.JLabel jLabel50;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JButton keluar;
    private javax.swing.JLabel lblTotalGalonTerjual;
    private javax.swing.JLabel lblTotalHarga;
    private javax.swing.JLabel lblTotalPendapatan;
    private javax.swing.JLabel lblTotalTransaksi;
    private javax.swing.JPanel menu;
    private javax.swing.JButton operasional;
    private javax.swing.JButton penjualan;
    private javax.swing.JTable tableLaporanBulanan;
    private javax.swing.JTable tblcaricustomer2;
    private javax.swing.JTable tblcustomer;
    private javax.swing.JTable tblpenjualan;
    private javax.swing.JTextField txtalamat;
    private javax.swing.JTextField txtcaricustomer;
    private javax.swing.JTextField txtcaricustomer2;
    private javax.swing.JTextField txthargasatuan2;
    private javax.swing.JTextField txtidcustomer;
    private javax.swing.JTextField txtidcustomer2;
    private javax.swing.JTextField txtidtransaksi;
    private javax.swing.JTextField txtjeniscustomer;
    private javax.swing.JTextField txtjeniscustomer2;
    private javax.swing.JTextField txtjumlahpenjualan;
    private javax.swing.JTextField txtnamacustomer;
    private javax.swing.JTextField txtnamacustomer2;
    private javax.swing.JTextField txtstatus;
    private javax.swing.JTextField txttelephone;
    // End of variables declaration//GEN-END:variables
}
