package POS;

import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
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

public class Operasional extends javax.swing.JFrame {

    String Tanggal;
    public Statement st;
    public ResultSet rs;
    Connection cn = POS.koneksi.BukaKoneksi();

    public Operasional() {

    }

    public Operasional(int id_level) {
        initComponents();
        aturFontTextField(); // baru setelah itu atur font
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Bersih();
        aturMenu(id_level);
        TampilDataOperasional();
        CariDataBarang();
    }

    public void Operasional(int id_level) {
        btnTampilkan1.addActionListener(e -> TampilkanDataLaporanOperasional());
    }

    public void TampilkanDataLaporanOperasional() {
        try
        {
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
            model.setColumnIdentifiers(new Object[]
            {
                "No.", "ID", "Tanggal", "Nama Barang", "Jumlah", "Total"
            });

            int totalBeli = 0;
            int totalPengeluaran = 0;
            int totalTransaksi = 0;
            int no = 1;

            while (rs.next())
            {
                int beli = rs.getInt("jumlah");
                int total = rs.getInt("total");
                Date tanggal = rs.getDate("tanggal"); // Ambil tanggal sebagai Date

                model.addRow(new Object[]
                {
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

        } catch (SQLException e)
        {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data laporan: " + e.getMessage());
        }
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
        Font fontOperasional = new Font("Tahoma", Font.PLAIN, 18); // Font khusus form operasional

        txtcaribarang.setFont(fontBesar);
        cmbkodebarang.setFont(fontBesar);
        cmbnamabarang.setFont(fontBesar);
        cmbcaribarang.setFont(fontBesar);

        // Form input operasional (pakai font ukuran 18)
        txtkodebarang.setFont(fontOperasional);
        txtnamabarang.setFont(fontOperasional);
        txttotalbarang.setFont(fontOperasional);
        txtidbarang.setFont(fontOperasional);
        txtjumlahbarang.setFont(fontOperasional);
        txthargasatuan.setFont(fontOperasional);

        btnKembaliPenjualan1.setFont(fontBesar);
        btnBersihOperasional.setFont(fontBesar);
        btnTampilkan1.setFont(fontBesar);
        btnLaporan1.setFont(fontBesar);
        cmbJenisBarang.setFont(fontBesar);
        cmbTahun1.setFont(fontBesar);
        cmbBulan1.setFont(fontBesar);

    }

    public void Bersih() {

        //bersih operasional
        txtidbarang.setText("");
        txtkodebarang.setText("");
        txtnamabarang.setText("");
        txtjumlahbarang.setText("");
        txthargasatuan.setText("");
        txttotalbarang.setText("");
        btntambahoperasional.setText("TAMBAH");
        txtidbarang.setEditable(true);

        TampilkanDataLaporanOperasional();
        DefaultTableModel modelOperasional = (DefaultTableModel) tableLaporanOperasional.getModel();
        modelOperasional.setRowCount(0); // Hapus semua baris
        lblTotalTransaksiBarang.setText("0");
        lblTotalBeliBarang.setText("0");
        lblTotalPengeluaran.setText("Rp. 0");

    }

    public void TampilDataOperasional() {
        try
        {
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

            int no1 = 1;
            while (rs.next())
            {
                // Data tabel 1 tanpa nomor urut
                Object[] data
                        =
                        {
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

        } catch (SQLException e)
        {
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Ukuran bisa disesuaikan
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void CariDataBarang() {
        try
        {
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

            while (rs.next())
            {
                // Data tabel 1 tanpa nomor urut
                Object[] data
                        =
                        {
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
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        btntambahoperasional = new javax.swing.JButton();
        btnbatal1 = new javax.swing.JButton();
        btnhapus1 = new javax.swing.JButton();
        btnLaporan1 = new javax.swing.JButton();
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
        jLabel1.setText("POINT OF SALES PENJUALAN TOKO BARANG");
        jLabel1.setToolTipText("");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1863, Short.MAX_VALUE)
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

        OPERASIONAL.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        OPERASIONAL.setPreferredSize(new java.awt.Dimension(1670, 964));

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
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(44, Short.MAX_VALUE))
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

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/tutup galon.png"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/tisu.png"))); // NOI18N

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/segel.png"))); // NOI18N

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/galon.png"))); // NOI18N

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/sedimen.png"))); // NOI18N

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
                                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(OPERASIONALLayout.createSequentialGroup()
                                        .addGap(38, 38, 38)
                                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addGroup(OPERASIONALLayout.createSequentialGroup()
                                        .addGap(34, 34, 34)
                                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
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
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 65, Short.MAX_VALUE))
                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnbatal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnhapus1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btntambahoperasional, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnLaporan1, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
                                .addComponent(jLabel4))
                            .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(OPERASIONALLayout.createSequentialGroup()
                                    .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(7, 7, 7)
                                    .addComponent(jLabel2))
                                .addGroup(OPERASIONALLayout.createSequentialGroup()
                                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel7))
                                .addGroup(OPERASIONALLayout.createSequentialGroup()
                                    .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(7, 7, 7)
                                    .addComponent(jLabel6))
                                .addGroup(OPERASIONALLayout.createSequentialGroup()
                                    .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addGroup(OPERASIONALLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(OPERASIONALLayout.createSequentialGroup()
                                .addComponent(btntambahoperasional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(btnbatal1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(btnhapus1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLaporan1, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addGap(18, 18, 18)
                .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        cmbJenisBarang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tutup Galon", "Tisu Galon", "Segel Galon", "Galon", "Sedimen" }));

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
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
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout ISILayout = new javax.swing.GroupLayout(ISI);
        ISI.setLayout(ISILayout);
        ISILayout.setHorizontalGroup(
            ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(OPERASIONAL, javax.swing.GroupLayout.DEFAULT_SIZE, 1618, Short.MAX_VALUE)
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(LAPORAN_OPERASIONAL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ISILayout.setVerticalGroup(
            ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(OPERASIONAL, javax.swing.GroupLayout.DEFAULT_SIZE, 869, Short.MAX_VALUE)
            .addGroup(ISILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(LAPORAN_OPERASIONAL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            .addComponent(menu, javax.swing.GroupLayout.DEFAULT_SIZE, 869, Short.MAX_VALUE)
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ISI, javax.swing.GroupLayout.PREFERRED_SIZE, 1622, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        FRAMELayout.setVerticalGroup(
            FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FRAMELayout.createSequentialGroup()
                .addComponent(HEADER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(FRAMELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ISI, javax.swing.GroupLayout.PREFERRED_SIZE, 873, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MENU, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FRAME, javax.swing.GroupLayout.DEFAULT_SIZE, 1915, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FRAME, javax.swing.GroupLayout.PREFERRED_SIZE, 990, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void customerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerActionPerformed
        // TODO add your handling code here:
//        ISI.removeAll();
//        ISI.repaint();
//        ISI.revalidate();
//
//        //menambahkan panel
//        ISI.add(CUSTOMER);
//        ISI.repaint();
//        ISI.revalidate();
    }//GEN-LAST:event_customerActionPerformed

    private void keluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keluarActionPerformed
        // TODO add your handling code here:
        Login l = new Login();
        l.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_keluarActionPerformed

    private void penjualanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_penjualanActionPerformed
        //        // TODO add your handling code here:
//        ISI.removeAll();
//        ISI.repaint();
//        ISI.revalidate();
//
//        //menambahkan panel
//        ISI.add(PENJUALAN);
//        ISI.repaint();
//        ISI.revalidate();
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

    private void tbloperasionalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbloperasionalMouseClicked
        int selectedRow = tbloperasional.getSelectedRow();

        if (selectedRow != -1)
        {
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
        switch (cmbnamabarang.getSelectedIndex())
        {
            case 0:
                txtnamabarang.setText("Tutup Galon (1000pcs)");
                txthargasatuan.setText("100000");
                break;
            case 1:
                txtnamabarang.setText("Tisu Galon (100pcs)");
                txthargasatuan.setText("7500");
                break;
            case 2:
                txtnamabarang.setText("Segel Galon (200pcs)");
                txthargasatuan.setText("12000");
                break;
            case 3:
                txtnamabarang.setText("Galon (1pcs)");
                txthargasatuan.setText("30000");
                break;
            case 4:
                txtnamabarang.setText("Sedimen (1pcs)");
                txthargasatuan.setText("15000");
                break;
            default:
                txtnamabarang.setText("");
                txthargasatuan.setText("");
                break;
        }
    }//GEN-LAST:event_cmbnamabarangActionPerformed

    private void cmbkodebarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbkodebarangActionPerformed
        switch (cmbkodebarang.getSelectedIndex())
        {
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
        try
        {
            st = cn.createStatement();

            // Validasi input
            if (txtkodebarang.getText().trim().equals("")
                    || txtnamabarang.getText().trim().equals("")
                    || txtjumlahbarang.getText().trim().equals("")
                    || txthargasatuan.getText().trim().equals(""))
            {

                JOptionPane.showMessageDialog(null, "Data tidak boleh kosong", "Validasi Data",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Mengambil nama barang dari input (tanpa mengubah huruf besar/kecil)
            String namaBarang = txtnamabarang.getText().trim();
            int hargaSatuan = 0;

            // Menentukan harga satuan berdasarkan nama barang
            switch (namaBarang)
            {
                case "Tutup Galon (1000pcs)":
                    hargaSatuan = 100000;
                    break;
                case "Tisu Galon (100pcs)":
                    hargaSatuan = 7500;
                    break;
                case "Segel Galon (200pcs)":
                    hargaSatuan = 12000;
                    break;
                case "Galon (1pcs)":
                    hargaSatuan = 30000;
                    break;
                case "Sedimen (1pcs)":
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
            if (btntambahoperasional.getText().equals("TAMBAH"))
            {
                // Periksa apakah ID transaksi sudah ada
                String queryCheckID = "SELECT MIN(t1.id_barang + 1) AS next_id_barang "
                        + "FROM operasional t1 "
                        + "LEFT JOIN operasional t2 ON t1.id_barang + 1 = t2.id_barang "
                        + "WHERE t2.id_barang IS NULL";
                ResultSet rs = st.executeQuery(queryCheckID);

                int newID = 1; // Default ID jika tabel kosong

                if (rs.next() && rs.getInt("next_id_barang") > 0)
                { // ✅ Perbaikan di sini
                    newID = rs.getInt("next_id_barang"); // Gunakan kembali ID yang hilang
                }

                // Query untuk menambahkan data
                String sql = "INSERT INTO operasional (id_barang, kode_barang, nama_barang, jumlah, harga_satuan, total, tanggal) VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = cn.prepareStatement(sql))
                {
                    pstmt.setInt(1, newID);  // ID yang baru dibuat
                    pstmt.setString(2, txtkodebarang.getText());
                    pstmt.setString(3, txtnamabarang.getText());
                    pstmt.setInt(4, jumlahBeli);
                    pstmt.setInt(5, hargaSatuan);
                    pstmt.setInt(6, totalBayar);
                    pstmt.setDate(7, new java.sql.Date(new java.util.Date().getTime()));

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Data berhasil disimpan dengan ID: " + newID);
                    Bersih();
                    TampilDataOperasional();
                }
            } else
            {
                // Aksi ubah data
                String update = "UPDATE operasional SET kode_barang = ?, nama_barang = ?, jumlah = ?, harga_satuan = ?, total = ? WHERE id_barang = ?";
                try (PreparedStatement pstmt = cn.prepareStatement(update))
                {
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
        } catch (SQLException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(null, "Masukkan jumlah beli dan harga satuan sebagai angka", "Validasi Data", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btntambahoperasionalActionPerformed

    private void btnbatal1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbatal1ActionPerformed
        Bersih();
    }//GEN-LAST:event_btnbatal1ActionPerformed

    private void btnhapus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnhapus1ActionPerformed
        try
        {
            // Pastikan pengguna memilih baris yang akan dihapus
            int selectedRow = tbloperasional.getSelectedRow();
            if (selectedRow == -1)
            {
                JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Ambil ID Barang dari tabel berdasarkan baris yang dipilih
            String idBarang = tbloperasional.getValueAt(selectedRow, 0).toString();

            // Konfirmasi penghapusan
            int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION)
            {
                String sql = "DELETE FROM operasional WHERE id_barang = ?";

                try (PreparedStatement pstmt = cn.prepareStatement(sql))
                {
                    pstmt.setString(1, idBarang);
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    Bersih();
                    TampilDataOperasional(); // Refresh tampilan tabel
                }
            }
        } catch (SQLException e)
        {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnhapus1ActionPerformed

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

    private void btnTampilkan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilkan1ActionPerformed
        // TODO add your handling code here:
        TampilkanDataLaporanOperasional();
    }//GEN-LAST:event_btnTampilkan1ActionPerformed

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

    private void btnBersihOperasionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihOperasionalActionPerformed
        // TODO add your handling code here:
        Bersih();
    }//GEN-LAST:event_btnBersihOperasionalActionPerformed

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
            java.util.logging.Logger.getLogger(Operasional.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(Operasional.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(Operasional.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(Operasional.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Operasional().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FRAME;
    private javax.swing.JPanel HEADER;
    private javax.swing.JPanel ISI;
    private javax.swing.JPanel LAPORAN_OPERASIONAL;
    private javax.swing.JPanel MENU;
    private javax.swing.JPanel OPERASIONAL;
    private javax.swing.JButton btnBersihOperasional;
    private javax.swing.JButton btnKembaliPenjualan1;
    private javax.swing.JButton btnLaporan1;
    private javax.swing.JButton btnTampilkan1;
    private javax.swing.JButton btnbatal1;
    private javax.swing.JButton btnhapus1;
    private javax.swing.JButton btntambahoperasional;
    private javax.swing.JComboBox<String> cmbBulan1;
    private javax.swing.JComboBox<String> cmbJenisBarang;
    private javax.swing.JComboBox<String> cmbTahun1;
    private javax.swing.JComboBox<String> cmbcaribarang;
    private javax.swing.JComboBox<String> cmbkodebarang;
    private javax.swing.JComboBox<String> cmbnamabarang;
    private javax.swing.JButton customer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JButton keluar;
    private javax.swing.JLabel lblTotalBeliBarang;
    private javax.swing.JLabel lblTotalPengeluaran;
    private javax.swing.JLabel lblTotalTransaksiBarang;
    private javax.swing.JPanel menu;
    private javax.swing.JButton operasional;
    private javax.swing.JButton penjualan;
    private javax.swing.JTable tableLaporanOperasional;
    private javax.swing.JTable tbloperasional;
    private javax.swing.JTextField txtcaribarang;
    private javax.swing.JTextField txthargasatuan;
    private javax.swing.JTextField txtidbarang;
    private javax.swing.JTextField txtjumlahbarang;
    private javax.swing.JTextField txtkodebarang;
    private javax.swing.JTextField txtnamabarang;
    private javax.swing.JTextField txttotalbarang;
    // End of variables declaration//GEN-END:variables
}
