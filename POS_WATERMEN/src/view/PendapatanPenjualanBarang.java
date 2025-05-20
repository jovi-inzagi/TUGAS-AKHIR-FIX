package view;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class PendapatanPenjualanBarang extends javax.swing.JPanel {

    public PendapatanPenjualanBarang() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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

        setBackground(new java.awt.Color(153, 153, 153));

        PENDAPATANIsiUlangGalon.setBackground(new java.awt.Color(255, 255, 255));
        PENDAPATANIsiUlangGalon.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jPanel29.setBackground(new java.awt.Color(204, 204, 255));

        jLabel67.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel67.setForeground(new java.awt.Color(51, 51, 51));
        jLabel67.setText("Laporan Pendapatan Penjualan Barang");

        javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel67, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(833, Short.MAX_VALUE))
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel67, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
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
                    .addComponent(btnTampilPenjualanOpearasional1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                        .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnCetakPendapatanBulanan, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                            .addComponent(btnCetakPendapatanBulanan1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addGroup(PENDAPATANIsiUlangGalonLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(PENDAPATANIsiUlangGalonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane9)
                    .addComponent(jScrollPane11))
                .addGap(18, 18, 18)
                .addGroup(PENDAPATANIsiUlangGalonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel30, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel31, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
            .addGroup(PENDAPATANIsiUlangGalonLayout.createSequentialGroup()
                .addComponent(jPanel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
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
                        .addComponent(jPanel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)))
                .addGap(50, 50, 50))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1273, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(PENDAPATANIsiUlangGalon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 773, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(PENDAPATANIsiUlangGalon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbTahunPendapatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTahunPendapatanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbTahunPendapatanActionPerformed

    private void btnTampilPenjualanOpearasional1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilPenjualanOpearasional1ActionPerformed
//        dataPendapatanBulanan();
    }//GEN-LAST:event_btnTampilPenjualanOpearasional1ActionPerformed

    private void bersihPendapatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bersihPendapatanActionPerformed
        cmbBulanPendapatan.setSelectedIndex(0);
        cmbTahunPendapatan.setSelectedIndex(0);
        DefaultTableModel model = (DefaultTableModel) tabelPendapatan.getModel();
        model.setRowCount(0);
    }//GEN-LAST:event_bersihPendapatanActionPerformed

    private void btnTampilPenjualanOpearasional2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilPenjualanOpearasional2ActionPerformed
        cmbBulanPendapatan.setSelectedIndex(0);
        cmbTahunPendapatan.setSelectedIndex(0);
//        dataSeluruhPendapatanBualanan();
    }//GEN-LAST:event_btnTampilPenjualanOpearasional2ActionPerformed

    private void btnCetakPendapatanBulananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakPendapatanBulananActionPerformed
        try {
            String namaBulan = cmbBulanPendapatan.getSelectedItem().toString();
            String tahun = cmbTahunPendapatan.getSelectedItem().toString();
//            String bulanAngka = getBulanAngka(namaBulan);
//            String periode = tahun + "-" + bulanAngka;

            String reportPath = "src/laporan/Laporan_Pendapatan_Bulanan.jasper";

            HashMap<String, Object> parameters = new HashMap<>();
//            parameters.put("periode", periode);

//            JasperPrint jp = JasperFillManager.fillReport(reportPath, parameters, cn);
//            JasperViewer.viewReport(jp, false); // false -> tidak menutup aplikasi utama
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal mencetak laporan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnCetakPendapatanBulananActionPerformed

    private void btnCetakPendapatanBulanan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakPendapatanBulanan1ActionPerformed
        String reportPath = "src/laporan/Laporan_Pendapatan.jasper";
        //            Map<String, Object> parameters = getLaporanBulananParameters();
//            parameters.put("printed_by", Session.namaUserLogin); // <-- PENTING
//            JasperPrint print = JasperFillManager.fillReport(reportPath, parameters, cn);
//            JasperViewer viewer = new JasperViewer(print, false);
//viewer.setTitle("Laporan Penjualan Bulanan");
//viewer.setVisible(true);
    }//GEN-LAST:event_btnCetakPendapatanBulanan1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PENDAPATANIsiUlangGalon;
    private javax.swing.JButton bersihPendapatan;
    private javax.swing.JButton btnCetakPendapatanBulanan;
    private javax.swing.JButton btnCetakPendapatanBulanan1;
    private javax.swing.JButton btnTampilPenjualanOpearasional1;
    private javax.swing.JButton btnTampilPenjualanOpearasional2;
    private javax.swing.JComboBox<String> cmbBulanPendapatan;
    private javax.swing.JComboBox<String> cmbTahunPendapatan;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JLabel labelOperasionalPendapatan;
    private javax.swing.JLabel labelPendapatan;
    private javax.swing.JLabel labelPenjualanPendapatan;
    private javax.swing.JTable tabelOperasionalforPendapatan;
    private javax.swing.JTable tabelPendapatan;
    private javax.swing.JTable tabelPenjualanforPendapatan;
    // End of variables declaration//GEN-END:variables
}
