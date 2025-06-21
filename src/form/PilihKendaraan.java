/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package form;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Ahmad Nur Latif P
 */
public class PilihKendaraan extends javax.swing.JDialog {

    private DefaultTableModel model;
    private transaksi parentTransaksiForm;

    /**
     * Creates new form PilihKendaraan1
     */
    public PilihKendaraan(java.awt.Frame parent, boolean modal, transaksi parentForm) {
        super(parent, modal);
        initComponents();

        setLocationRelativeTo(null);
        this.parentTransaksiForm = parentForm;

        model = new DefaultTableModel();
        table_data.setModel(model);
        model.addColumn("ID Kendaraan");
        model.addColumn("Jenis");
        model.addColumn("Merk");
        model.addColumn("Model");
        model.addColumn("Tahun");
        model.addColumn("Plat Nomor");
        model.addColumn("Harga/Hari");
        model.addColumn("Gambar");

        loadData();
    }

    private void loadData() {
        model.getDataVector().removeAllElements();
        model.fireTableDataChanged();
        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();

            if (conn == null) {
                return;
            }

            // Hanya muat kendaraan dengan status 'Tersedia'
            String sql = "SELECT id_kendaraan, jenis, merk, model,tahun, plat_nomor, harga_sewa, gambar FROM kendaraan WHERE status = 'Tersedia'";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] o = new Object[8];
                o[0] = rs.getString("id_kendaraan");
                o[1] = rs.getString("jenis");
                o[2] = rs.getString("merk");
                o[3] = rs.getString("model");
                o[4] = rs.getString("tahun");
                o[5] = rs.getString("plat_nomor");
                o[6] = rs.getInt("harga_sewa");
                o[7] = rs.getString("gambar");
                model.addRow(o);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error memuat data kendaraan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void searchData(String keyword) {
        model.getDataVector().removeAllElements();
        model.fireTableDataChanged();
        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();

            if (conn == null) {
                return;
            }

            // Cari hanya kendaraan yang tersedia
            String sql = "SELECT id_kendaraan, jenis, merk, model, tahun, plat_nomor, harga_sewa, gambar FROM kendaraan WHERE status = 'Tersedia' AND ("
                    + "id_kendaraan LIKE ? OR "
                    + "jenis LIKE ? OR " // Tambah pencarian di kolom 'jenis'
                    + "merk LIKE ? OR "
                    + "model LIKE ? OR "
                    + "tahun LIKE ? OR "
                    + "plat_nomor LIKE ? OR "
                    + "harga_sewa LIKE ?)";    // Tambah pencarian di kolom 'harga_sewa'

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + keyword + "%"); // id_kendaraan
            pst.setString(2, "%" + keyword + "%"); // jenis
            pst.setString(3, "%" + keyword + "%"); // merk
            pst.setString(4, "%" + keyword + "%"); // model
            pst.setString(5, "%" + keyword + "%"); // tahun
            pst.setString(6, "%" + keyword + "%"); // plat_nomor
            pst.setString(7, "%" + keyword + "%"); // harga_sewa
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] o = new Object[8];
                o[0] = rs.getString("id_kendaraan");
                o[1] = rs.getString("jenis");
                o[2] = rs.getString("merk");
                o[3] = rs.getString("model");
                o[4] = rs.getString("tahun");
                o[5] = rs.getString("plat_nomor");
                o[6] = rs.getInt("harga_sewa");
                o[7] = rs.getString("gambar");
                model.addRow(o);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error mencari data kendaraan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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

        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table_data = new javax.swing.JTable();
        btnPilih = new javax.swing.JButton();
        bKembali = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtCari = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(243, 198, 35));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(16, 55, 92), 2));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("KENDARAAN TERSEDIA");
        jLabel4.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 2, 0, new java.awt.Color(16, 55, 92)));

        jPanel2.setBackground(new java.awt.Color(243, 198, 35));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        table_data.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(table_data);

        btnPilih.setBackground(new java.awt.Color(16, 55, 92));
        btnPilih.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnPilih.setForeground(new java.awt.Color(204, 204, 204));
        btnPilih.setText("PILIH");
        btnPilih.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPilihActionPerformed(evt);
            }
        });

        bKembali.setBackground(new java.awt.Color(16, 55, 92));
        bKembali.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bKembali.setForeground(new java.awt.Color(204, 204, 204));
        bKembali.setText("KEMBALI");
        bKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bKembaliActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("Cari Kendaraan :");

        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCariKeyReleased(evt);
            }
        });

        btnCari.setText("CARI");
        btnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCariActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCari))
                            .addComponent(btnPilih, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(bKembali, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCari))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPilih)
                    .addComponent(bKembali))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPilihActionPerformed
        int selectedRow = table_data.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kendaraan dari tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idKendaraan = model.getValueAt(selectedRow, 0).toString();
        // Index kolom disesuaikan dengan urutan kolom di model tabel ini
        String platNomor = model.getValueAt(selectedRow, 5).toString();
        String modelKendaraan = model.getValueAt(selectedRow, 3).toString();
        int hargaSewa = (int) model.getValueAt(selectedRow, 6);
        String gambar = (String) model.getValueAt(selectedRow, 7);

        // Mengirim data terpilih kembali ke form transaksi
        parentTransaksiForm.setSelectedKendaraan(idKendaraan, platNomor, modelKendaraan, hargaSewa, gambar);
        this.dispose();
    }

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }//GEN-LAST:event_btnPilihActionPerformed

    private void bKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bKembaliActionPerformed
        this.dispose();
    }//GEN-LAST:event_bKembaliActionPerformed

    private void txtCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKeyReleased
        searchData(txtCari.getText());
    }//GEN-LAST:event_txtCariKeyReleased

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        searchData(txtCari.getText());
    }//GEN-LAST:event_btnCariActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PilihKendaraan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PilihKendaraan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PilihKendaraan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PilihKendaraan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PilihKendaraan dialog = new PilihKendaraan(new javax.swing.JFrame(), true, null); // <<--- PERBAIKAN DI SINI
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bKembali;
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnPilih;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table_data;
    private javax.swing.JTextField txtCari;
    // End of variables declaration//GEN-END:variables
}
