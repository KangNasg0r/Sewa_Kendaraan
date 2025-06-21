/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package form;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path; // Pastikan ini ada
import java.nio.file.Paths; // Pastikan ini ada
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID; // Untuk nama file unik
import javax.swing.ImageIcon;
import javax.swing.JFileChooser; // Untuk dialog pilih file
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter; // Untuk filter tipe file
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Ahmad Nur Latif P
 */
public class pelanggan extends javax.swing.JFrame {

    private DefaultTableModel model;
    private String pathGambarTerpilih = null;
    private String namaFileGambar = null;

    /**
     * Creates new form login
     */
    public pelanggan() {
        initComponents();
        setLocationRelativeTo(null);
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);

        model = new DefaultTableModel();
        table_pelanggan.setModel(model);
        model.addColumn("ID Pelanggan");
        model.addColumn("Nama Pelanggan");
        model.addColumn("Alamat");
        model.addColumn("No. Telepon");
        model.addColumn("Foto KTP");
        loadData();
        autogenerateIdPelanggan();
        txtId.setEditable(false);
        txtNama.requestFocus();
        clearForm();
    }

    private void autogenerateIdPelanggan() {
        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();

            if (conn == null) {
                txtId.setText("Error");
                return;
            }

            String sql = "SELECT MAX(CAST(SUBSTRING(id_pelanggan, 3) AS UNSIGNED)) AS max_id FROM pelanggan";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                String newId = String.format("PL%03d", maxId + 1);
                txtId.setText(newId);
            } else {
                txtId.setText("PL001");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengenerate ID Pelanggan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

            String sql = "SELECT id_pelanggan, nama_pelanggan, alamat, no_telepon, ktp FROM pelanggan";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] o = new Object[5]; // Ubah ukuran array menjadi 5
                o[0] = rs.getString("id_pelanggan");
                o[1] = rs.getString("nama_pelanggan");
                o[2] = rs.getString("alamat");
                o[3] = rs.getString("no_telepon");
                o[4] = rs.getString("ktp"); // Ambil nama file KTP
                model.addRow(o);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error memuat data pelanggan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            // e.printStackTrace(); // Komentar untuk production
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

    private void clearForm() {
        txtId.setText("");
        txtNama.setText("");
        txtAlamat.setText("");
        txtTelepon.setText("");
        label_tampil_gambar.setIcon(null); // Bersihkan gambar KTP
        pathGambarTerpilih = null;
        namaFileGambar = null;      // Reset nama file KTP
        autogenerateIdPelanggan();
    }

    private void tampilGambar(String fileName) {
         if (fileName != null && !fileName.isEmpty()) {
        String imagePath = "src/ktp/" + fileName;
        File imageFile = new File(imagePath);

        if (imageFile.exists()) {
            ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
            int lebar = label_tampil_gambar.getWidth();
            int tinggi = label_tampil_gambar.getHeight();

            java.awt.Image img = icon.getImage().getScaledInstance(lebar, tinggi, java.awt.Image.SCALE_SMOOTH);
            label_tampil_gambar.setIcon(new ImageIcon(img));
        } else {
            label_tampil_gambar.setIcon(null);
            JOptionPane.showMessageDialog(this, "File gambar KTP tidak ditemukan: " + fileName, "Informasi", JOptionPane.WARNING_MESSAGE); // Opsional
        }
    } else {
        label_tampil_gambar.setIcon(null);
    }
    }
    
    private void searchData(String keyword) {
    model.getDataVector().removeAllElements(); // Bersihkan data lama
    model.fireTableDataChanged(); // Beri tahu tabel untuk refresh
    Connection conn = null;
    try {
        koneksi db = new koneksi();
        conn = db.connect();

        if (conn == null) {
            return;
        }

        // --- QUERY PENCARIAN ---
        // Cari di kolom id_pelanggan, nama_pelanggan, alamat, atau no_telepon
        String sql = "SELECT id_pelanggan, nama_pelanggan, alamat, no_telepon, ktp FROM pelanggan WHERE "
                     + "id_pelanggan LIKE ? OR "
                     + "nama_pelanggan LIKE ? OR "
                     + "alamat LIKE ? OR "
                     + "no_telepon LIKE ?"; // ktp tidak perlu dicari

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, "%" + keyword + "%"); // Untuk id_pelanggan
        pst.setString(2, "%" + keyword + "%"); // Untuk nama_pelanggan
        pst.setString(3, "%" + keyword + "%"); // Untuk alamat
        pst.setString(4, "%" + keyword + "%"); // Untuk no_telepon
        // --- AKHIR QUERY PENCARIAN ---

        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            Object[] o = new Object[5]; // Ukuran array 5 (termasuk ktp)
            o[0] = rs.getString("id_pelanggan");
            o[1] = rs.getString("nama_pelanggan");
            o[2] = rs.getString("alamat");
            o[3] = rs.getString("no_telepon");
            o[4] = rs.getString("ktp"); // Ambil nama file KTP
            model.addRow(o);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error mencari data pelanggan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
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
        table_pelanggan = new javax.swing.JTable();
        bUbah = new javax.swing.JButton();
        bHapus = new javax.swing.JButton();
        bBatal = new javax.swing.JButton();
        bKembali = new javax.swing.JButton();
        cari_pelanggan = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        bSimpan = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtTelepon = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAlamat = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        bUpload = new javax.swing.JButton();
        label_tampil_gambar = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(243, 198, 35));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(16, 55, 92), 2));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("DATA PELANGGAN");
        jLabel4.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 2, 0, new java.awt.Color(16, 55, 92)));

        jPanel2.setBackground(new java.awt.Color(243, 198, 35));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        table_pelanggan.setModel(new javax.swing.table.DefaultTableModel(
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
        table_pelanggan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                table_pelangganMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table_pelanggan);

        bUbah.setBackground(new java.awt.Color(16, 55, 92));
        bUbah.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bUbah.setForeground(new java.awt.Color(255, 255, 255));
        bUbah.setText("UBAH");
        bUbah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bUbahActionPerformed(evt);
            }
        });

        bHapus.setBackground(new java.awt.Color(16, 55, 92));
        bHapus.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bHapus.setForeground(new java.awt.Color(255, 255, 255));
        bHapus.setText("HAPUS");
        bHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bHapusActionPerformed(evt);
            }
        });

        bBatal.setBackground(new java.awt.Color(16, 55, 92));
        bBatal.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bBatal.setForeground(new java.awt.Color(255, 255, 255));
        bBatal.setText("BATAL");
        bBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBatalActionPerformed(evt);
            }
        });

        bKembali.setBackground(new java.awt.Color(16, 55, 92));
        bKembali.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bKembali.setForeground(new java.awt.Color(255, 255, 255));
        bKembali.setText("KEMBALI");
        bKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bKembaliActionPerformed(evt);
            }
        });

        cari_pelanggan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cari_pelangganKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(bUbah, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bKembali, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cari_pelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cari_pelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bUbah)
                    .addComponent(bHapus)
                    .addComponent(bBatal)
                    .addComponent(bKembali))
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(243, 198, 35));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        bSimpan.setBackground(new java.awt.Color(16, 55, 92));
        bSimpan.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        bSimpan.setForeground(new java.awt.Color(255, 255, 255));
        bSimpan.setText("SIMPAN");
        bSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSimpanActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("No Telepon :");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Alamat :");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Nama :");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("ID Pelanggan :");

        txtAlamat.setColumns(20);
        txtAlamat.setRows(5);
        jScrollPane2.setViewportView(txtAlamat);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setText("Foto KTP :");

        bUpload.setBackground(new java.awt.Color(16, 55, 92));
        bUpload.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bUpload.setForeground(new java.awt.Color(255, 255, 255));
        bUpload.setText("UPLOAD");
        bUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bUploadActionPerformed(evt);
            }
        });

        label_tampil_gambar.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 3, true));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bUpload)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                        .addComponent(jLabel5)
                        .addComponent(txtTelepon, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel3)
                        .addComponent(jLabel2)
                        .addComponent(txtId, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(txtNama, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel6)
                        .addComponent(jLabel7)
                        .addComponent(label_tampil_gambar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(30, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(75, Short.MAX_VALUE)
                .addComponent(bSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(74, 74, 74))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTelepon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_tampil_gambar, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bUpload)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(bSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSimpanActionPerformed
        String idPelanggan = txtId.getText();
        String nama = txtNama.getText();
        String alamat = txtAlamat.getText();
        String noTelepon = txtTelepon.getText();
        if (idPelanggan.isEmpty() || nama.isEmpty() || alamat.isEmpty() || noTelepon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pathGambarTerpilih == null || namaFileGambar == null) {
            JOptionPane.showMessageDialog(this, "Silakan unggah foto KTP pelanggan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Path sourcePath = Paths.get(pathGambarTerpilih);
            Path targetDir = Paths.get("src/ktp");
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(namaFileGambar); // Gabungkan direktori tujuan dengan nama file
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyalin file KTP: " + e.getMessage(), "Error File", JOptionPane.ERROR_MESSAGE);
        }

        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();
            if (conn == null) {
                return;
            }
            String checkSql = "SELECT COUNT(*) FROM pelanggan WHERE id_pelanggan = ?";
            PreparedStatement checkPst = conn.prepareStatement(checkSql);
            checkPst.setString(1, idPelanggan);
            ResultSet rs = checkPst.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "ID Pelanggan sudah ada. Gunakan ID lain atau gunakan fitur Ubah.", "Duplikasi Data", JOptionPane.WARNING_MESSAGE);
                checkPst.close();
                rs.close();
                return;
            }
            rs.close();

            checkPst.close();

            // Pastikan kolom 'ktp' ada di tabel pelanggan Anda
            String insertSql = "INSERT INTO pelanggan (id_pelanggan, nama_pelanggan, alamat, no_telepon, ktp) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertPst = conn.prepareStatement(insertSql);
            insertPst.setString(1, idPelanggan);
            insertPst.setString(2, nama);
            insertPst.setString(3, alamat);
            insertPst.setString(4, noTelepon);
            insertPst.setString(5, namaFileGambar); // Simpan nama file KTP
            int affectedRows = insertPst.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Data pelanggan berhasil ditambahkan!");
                loadData();
                clearForm();
                txtNama.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan data pelanggan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            insertPst.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saat menyimpan data pelanggan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
// e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_bSimpanActionPerformed

    private void bUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bUbahActionPerformed
        String idPelanggan = txtId.getText();
        String nama = txtNama.getText();
        String alamat = txtAlamat.getText();
        String noTelepon = txtTelepon.getText();

        if (idPelanggan.isEmpty() || nama.isEmpty() || alamat.isEmpty() || noTelepon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pathGambarTerpilih != null && namaFileGambar != null) {
            try {
                Path sourcePath = Paths.get(pathGambarTerpilih);
                Path targetDir = Paths.get("src/ktp"); // Folder tujuan
                Files.createDirectories(targetDir); // Pastikan folder ada
                Path targetPath = targetDir.resolve(namaFileGambar); // Gabungkan direktori tujuan dengan nama file

                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal menyalin file KTP baru: " + e.getMessage(), "Error File", JOptionPane.ERROR_MESSAGE);
            }
        }

        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();
            if (conn == null) {
                return;
            }
            // Pastikan kolom 'ktp' ada di tabel pelanggan Anda
            String updateSql = "UPDATE pelanggan SET nama_pelanggan=?, alamat=?, no_telepon=?, ktp=? WHERE id_pelanggan=?";
            PreparedStatement updatePst = conn.prepareStatement(updateSql);
            updatePst.setString(1, nama);
            updatePst.setString(2, alamat);
            updatePst.setString(3, noTelepon);
            updatePst.setString(4, namaFileGambar); // Update nama file KTP
            updatePst.setString(5, idPelanggan); // WHERE clause
            int affectedRows = updatePst.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Data pelanggan berhasil diubah!");
                loadData();
                clearForm();
                txtNama.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengubah data. ID Pelanggan tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            updatePst.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saat mengubah data pelanggan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
// e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_bUbahActionPerformed

    private void bHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHapusActionPerformed
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus dari tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menghapus data ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                String idPelanggan = txtId.getText();
                // Opsional: Hapus file gambar terkait jika data dihapus
                if (namaFileGambar != null && !namaFileGambar.isEmpty()) {
                    File imgToDelete = new File("src/ktp/" + namaFileGambar);
                    if (imgToDelete.exists()) {
                        Files.deleteIfExists(imgToDelete.toPath());
                    }
                }

                koneksi db = new koneksi();
                conn = db.connect();

                if (conn == null) {
                    return;
                }

                String sql = "DELETE FROM pelanggan WHERE id_pelanggan=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, idPelanggan);

                int affectedRows = pst.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                    loadData();
                    clearForm();
                    txtNama.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus data. ID Pelanggan tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 1451) { // MySQL error code for foreign key constraint fail
                    JOptionPane.showMessageDialog(this, "Data tidak bisa dihapus karena masih ada transaksi yang terhubung dengan pelanggan ini!", "Error Hapus", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saat menghapus file gambar: " + e.getMessage(), "Error File", JOptionPane.ERROR_MESSAGE);
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
    }//GEN-LAST:event_bHapusActionPerformed

    private void bBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBatalActionPerformed
        clearForm();
    }//GEN-LAST:event_bBatalActionPerformed

    private void bKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bKembaliActionPerformed
        menu_utama rf = new menu_utama();
        rf.setVisible(true);
        rf.setLocationRelativeTo(null);
        this.dispose();
    }//GEN-LAST:event_bKembaliActionPerformed

    private void table_pelangganMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table_pelangganMouseClicked
        int selectedRow = table_pelanggan.getSelectedRow();
        if (selectedRow > -1) {
            txtId.setText(model.getValueAt(selectedRow, 0).toString());
            txtNama.setText(model.getValueAt(selectedRow, 1).toString());
            txtAlamat.setText(model.getValueAt(selectedRow, 2).toString());
            txtTelepon.setText(model.getValueAt(selectedRow, 3).toString());

            if (model.getValueAt(selectedRow, 4) != null) {
                namaFileGambar = model.getValueAt(selectedRow, 4).toString();
                tampilGambar(namaFileGambar);
            } else {
                label_tampil_gambar.setIcon(null);
                namaFileGambar = null;
            }
            pathGambarTerpilih = null;
        }
    }//GEN-LAST:event_table_pelangganMouseClicked

    private void bUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bUploadActionPerformed
    JFileChooser fileChooser = new JFileChooser();
        // Set direktori awal ke folder images/vehicles jika ada
        File initialDir = new File("src/ktp");
        if (initialDir.exists() && initialDir.isDirectory()) {
            fileChooser.setCurrentDirectory(initialDir);
        } else {
            // Jika tidak ada, coba buat
            initialDir.mkdirs(); // Buat direktori jika belum ada
            fileChooser.setCurrentDirectory(new File(".")); // Default ke root proyek
        }

        // Filter hanya file gambar
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Gambar (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            pathGambarTerpilih = file.getAbsolutePath(); // Simpan path lengkap sementara

            // Tampilkan gambar di JLabel
            ImageIcon icon = new ImageIcon(pathGambarTerpilih);
            int lebar = label_tampil_gambar.getWidth();
            int tinggi = label_tampil_gambar.getHeight();

            if (lebar > 0 && tinggi > 0) {
                java.awt.Image img = icon.getImage().getScaledInstance(lebar, tinggi, java.awt.Image.SCALE_SMOOTH);
                label_tampil_gambar.setIcon(new ImageIcon(img));
            } else {
                label_tampil_gambar.setIcon(icon);
            }

            // Ambil nama file untuk disimpan di database, pastikan unik
            String originalFileName = file.getName();
            String extension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                extension = originalFileName.substring(i);
            }
            namaFileGambar = UUID.randomUUID().toString() + extension;
        }
    }//GEN-LAST:event_bUploadActionPerformed

    private void cari_pelangganKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cari_pelangganKeyTyped
        searchData(cari_pelanggan.getText());
    }//GEN-LAST:event_cari_pelangganKeyTyped

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
            java.util.logging.Logger.getLogger(pelanggan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(pelanggan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(pelanggan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(pelanggan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                pelanggan login = new pelanggan();
                login.setVisible(true);
                login.setLocationRelativeTo(null);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBatal;
    private javax.swing.JButton bHapus;
    private javax.swing.JButton bKembali;
    private javax.swing.JButton bSimpan;
    private javax.swing.JButton bUbah;
    private javax.swing.JButton bUpload;
    private javax.swing.JTextField cari_pelanggan;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel label_tampil_gambar;
    private javax.swing.JTable table_pelanggan;
    private javax.swing.JTextArea txtAlamat;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtTelepon;
    // End of variables declaration//GEN-END:variables
}
