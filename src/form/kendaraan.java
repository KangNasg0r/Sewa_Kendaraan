/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package form;

import form.koneksi;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Ahmad Nur Latif P
 */
public class kendaraan extends javax.swing.JFrame {

    private DefaultTableModel model;
    private String pathGambarTerpilih = null;
    private String namaFileGambar = null;

    /**
     * Creates new form login
     */
    public kendaraan() {
        initComponents();
        this.setLocationRelativeTo(null);
        model = new DefaultTableModel();
        table_kendaraan.setModel(model);
        model.addColumn("ID");
        model.addColumn("Jenis");
        model.addColumn("Merk");
        model.addColumn("Model");
        model.addColumn("Tahun");
        model.addColumn("Plat Nomor");
        model.addColumn("Harga/Hari");
        model.addColumn("Status");
        model.addColumn("Gambar");
        table_kendaraan.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        int modelColumnIndex = 3; // Indeks kolom Model
        int modelPreferredWidth = 200;
        int modelMinimumWidth = 100;
        int modelMaximumWidth = 300;
        table_kendaraan.getColumnModel().getColumn(modelColumnIndex).setPreferredWidth(modelPreferredWidth);
        table_kendaraan.getColumnModel().getColumn(modelColumnIndex).setMinWidth(modelMinimumWidth);
        table_kendaraan.getColumnModel().getColumn(modelColumnIndex).setMaxWidth(modelMaximumWidth);
        loadData();
        autogenerateIdKendaraan();
        txtIdKendaraan.setEditable(false);
        clearForm();
    }

    private void autogenerateIdKendaraan() {
        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();

            if (conn == null) {
                txtIdKendaraan.setText("Error");
                return;
            }

            String sql = "SELECT MAX(CAST(SUBSTRING(id_kendaraan, 3) AS UNSIGNED)) AS max_id FROM kendaraan";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                String newId = String.format("KD%03d", maxId + 1);
                txtIdKendaraan.setText(newId);
            } else {
                txtIdKendaraan.setText("KD001");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengenerate ID: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

            String sql = "SELECT id_kendaraan, jenis, merk, model, tahun, plat_nomor, harga_sewa, status, gambar FROM kendaraan";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] o = new Object[9];
                o[0] = rs.getString("id_kendaraan");
                o[1] = rs.getString("jenis");
                o[2] = rs.getString("merk");
                o[3] = rs.getString("model");
                o[4] = rs.getInt("tahun");
                o[5] = rs.getString("plat_nomor");
                o[6] = rs.getInt("harga_sewa");
                o[7] = rs.getString("status");
                o[8] = rs.getString("gambar");
                model.addRow(o);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    private void clearForm() {
        txtIdKendaraan.setText("");
        combo_jenis.setSelectedIndex(0);
        txtMerk.setText("");
        txtModel.setText("");
        txtKendaraan.setText("");
        txtPlat.setText("");
        txtSewa.setText("");
        status.clearSelection();
        radio_tersedia.setSelected(true);
        label_tampil_gambar.setIcon(null);
        pathGambarTerpilih = null;
        namaFileGambar = null;
        autogenerateIdKendaraan();
    }

    private void tampilGambar(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            // Pastikan folder 'images/vehicles' ada di root proyek
            String imagePath = "src/vehicles/" + fileName;
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                int lebar = label_tampil_gambar.getWidth();
                int tinggi = label_tampil_gambar.getHeight();

                // Pastikan lebar dan tinggi tidak nol untuk menghindari error scale
                if (lebar > 0 && tinggi > 0) {
                    java.awt.Image img = icon.getImage().getScaledInstance(lebar, tinggi, java.awt.Image.SCALE_SMOOTH);
                    label_tampil_gambar.setIcon(new ImageIcon(img));
                } else {
                    // Jika ukuran JLabel belum tersedia, tampilkan gambar asli (mungkin terlalu besar)
                    label_tampil_gambar.setIcon(icon);
                }
            } else {
                label_tampil_gambar.setIcon(null); // Gambar tidak ditemukan
                JOptionPane.showMessageDialog(this, "File gambar tidak ditemukan: " + fileName, "Informasi", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            label_tampil_gambar.setIcon(null); // Tidak ada gambar
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

            String sql = "SELECT id_kendaraan, jenis, merk, model, tahun, plat_nomor, harga_sewa, status, gambar FROM kendaraan WHERE "
                    + "id_kendaraan LIKE ? OR "
                    + "jenis LIKE ? OR "
                    + "merk LIKE ? OR "
                    + "model LIKE ? OR "
                    + "tahun LIKE ? OR "
                    + "plat_nomor LIKE ? OR "
                    + "harga_sewa LIKE ? OR "
                    + "status LIKE ?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");
            pst.setString(3, "%" + keyword + "%");
            pst.setString(4, "%" + keyword + "%");
            pst.setString(5, "%" + keyword + "%");
            pst.setString(6, "%" + keyword + "%");
            pst.setString(7, "%" + keyword + "%");
            pst.setString(8, "%" + keyword + "%");

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] o = new Object[9];
                o[0] = rs.getString("id_kendaraan");
                o[1] = rs.getString("jenis");
                o[2] = rs.getString("merk");
                o[3] = rs.getString("model");
                o[4] = rs.getInt("tahun");
                o[5] = rs.getString("plat_nomor");
                o[6] = rs.getInt("harga_sewa");
                o[7] = rs.getString("status");
                o[8] = rs.getString("gambar");
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

        status = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table_kendaraan = new javax.swing.JTable();
        bUbah = new javax.swing.JButton();
        bHapus = new javax.swing.JButton();
        bBatal = new javax.swing.JButton();
        bKembali = new javax.swing.JButton();
        bSimpan = new javax.swing.JButton();
        cari_kendaraan = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtModel = new javax.swing.JTextField();
        txtMerk = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtIdKendaraan = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtSewa = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtPlat = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtKendaraan = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        combo_jenis = new javax.swing.JComboBox<>();
        radio_tersedia = new javax.swing.JRadioButton();
        radio_tersedia1 = new javax.swing.JRadioButton();
        jLabel11 = new javax.swing.JLabel();
        label_tampil_gambar = new javax.swing.JLabel();
        bUnggahGambar = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(243, 198, 35));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(16, 55, 92), 2));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("DATA KENDARAAN");
        jLabel4.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 2, 0, new java.awt.Color(16, 55, 92)));

        jPanel2.setBackground(new java.awt.Color(243, 198, 35));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        table_kendaraan.setModel(new javax.swing.table.DefaultTableModel(
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
        table_kendaraan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                table_kendaraanMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table_kendaraan);

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

        bSimpan.setBackground(new java.awt.Color(16, 55, 92));
        bSimpan.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bSimpan.setForeground(new java.awt.Color(255, 255, 255));
        bSimpan.setText("SIMPAN");
        bSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSimpanActionPerformed(evt);
            }
        });

        cari_kendaraan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cari_kendaraanKeyTyped(evt);
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
                        .addComponent(bSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bUbah, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(bKembali, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cari_kendaraan, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(cari_kendaraan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bUbah)
                    .addComponent(bHapus)
                    .addComponent(bBatal)
                    .addComponent(bKembali)
                    .addComponent(bSimpan))
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(243, 198, 35));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Model :");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Merk :");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Jenis :");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("ID Kendaraan:");

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setText("Status :");

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setText("Harga Sewa Per-Hari :");

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel9.setText("Plat Nomor :");

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel10.setText("Tahun Kendaraan :");

        combo_jenis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Jenis Kendaraan", "Motor", "Mobil" }));

        radio_tersedia.setBackground(new java.awt.Color(243, 198, 35));
        status.add(radio_tersedia);
        radio_tersedia.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        radio_tersedia.setText("Tersedia");

        radio_tersedia1.setBackground(new java.awt.Color(243, 198, 35));
        status.add(radio_tersedia1);
        radio_tersedia1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        radio_tersedia1.setText("Disewa");

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel11.setText("Gambar Kendaraan :");

        label_tampil_gambar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_tampil_gambar.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        bUnggahGambar.setBackground(new java.awt.Color(16, 55, 92));
        bUnggahGambar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bUnggahGambar.setForeground(new java.awt.Color(255, 255, 255));
        bUnggahGambar.setText("UNGGAH");
        bUnggahGambar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bUnggahGambarActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Rp");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(171, 171, 171)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(txtPlat, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtMerk, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtIdKendaraan, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtModel, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(combo_jenis, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtKendaraan))
                    .addComponent(jLabel9))
                .addGap(50, 50, 50)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                    .addComponent(radio_tersedia, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(radio_tersedia1, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(bUnggahGambar, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(45, 45, 45)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtSewa, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(label_tampil_gambar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 114, Short.MAX_VALUE)))
                .addGap(37, 37, 37))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSewa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(radio_tersedia)
                                .addComponent(radio_tersedia1))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(31, 31, 31)))
                        .addGap(12, 12, 12)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label_tampil_gambar, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bUnggahGambar))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIdKendaraan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(combo_jenis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtMerk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtKendaraan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPlat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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

    private void bUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bUbahActionPerformed
        if (txtIdKendaraan.getText().isEmpty() || txtMerk.getText().isEmpty() || txtModel.getText().isEmpty()
                || txtKendaraan.getText().isEmpty() || txtPlat.getText().isEmpty() || txtSewa.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (combo_jenis.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Silakan pilih jenis kendaraan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String statusKendaraan = "";
        if (radio_tersedia.isSelected()) {
            statusKendaraan = "Tersedia";
        } else if (radio_tersedia1.isSelected()) { // Asumsi radio_tersedia1 adalah "Disewa"
            statusKendaraan = "Disewa";
        } else {
            JOptionPane.showMessageDialog(this, "Silakan pilih status kendaraan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            // Salin gambar jika ada yang baru dipilih
            if (pathGambarTerpilih != null && namaFileGambar != null) {
                Path sourcePath = Paths.get(pathGambarTerpilih);
                Path targetDir = Paths.get("src/vehicles");
                Files.createDirectories(targetDir);
                Path targetPath = targetDir.resolve(namaFileGambar);

                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            koneksi db = new koneksi();
            conn = db.connect();

            if (conn == null) {
                return;
            }

            String sql = "UPDATE kendaraan SET jenis=?, merk=?, model=?, tahun=?, plat_nomor=?, harga_sewa=?, status=?, gambar=? WHERE id_kendaraan=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, combo_jenis.getSelectedItem().toString());
            pst.setString(2, txtMerk.getText());
            pst.setString(3, txtModel.getText());
            pst.setInt(4, Integer.parseInt(txtKendaraan.getText())); // txtKendaraan adalah txtTahun
            pst.setString(5, txtPlat.getText());
            pst.setDouble(6, Double.parseDouble(txtSewa.getText())); // txtSewa adalah txtHargaSewa
            pst.setString(7, statusKendaraan);
            pst.setString(8, namaFileGambar); // Simpan nama file gambar yang baru/lama
            pst.setString(9, txtIdKendaraan.getText());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil diubah!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengubah data. ID Kendaraan tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // MySQL error code for duplicate entry
                JOptionPane.showMessageDialog(this, "Plat Nomor sudah terdaftar!", "Error Duplikasi", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengubah data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Input Tahun Kendaraan dan Harga Sewa harus berupa angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saat operasi file: " + e.getMessage(), "Error File", JOptionPane.ERROR_MESSAGE);
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
    }//GEN-LAST:event_bUbahActionPerformed

    private void bHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHapusActionPerformed
        if (txtIdKendaraan.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus dari tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menghapus data ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                String idKendaraan = txtIdKendaraan.getText();
                // Opsional: Hapus file gambar terkait jika data dihapus
                if (namaFileGambar != null && !namaFileGambar.isEmpty()) {
                    File imgToDelete = new File("src/vehicles/" + namaFileGambar);
                    if (imgToDelete.exists()) {
                        Files.deleteIfExists(imgToDelete.toPath());
                    }
                }

                koneksi db = new koneksi();
                conn = db.connect();

                if (conn == null) {
                    return;
                }

                String sql = "DELETE FROM kendaraan WHERE id_kendaraan=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, idKendaraan);

                int affectedRows = pst.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                    loadData();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus data. ID Kendaraan tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 1451) { // MySQL error code for foreign key constraint fail
                    JOptionPane.showMessageDialog(this, "Data tidak bisa dihapus karena masih ada transaksi yang terhubung dengan kendaraan ini!", "Error Hapus", JOptionPane.ERROR_MESSAGE);
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

    private void bSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSimpanActionPerformed
        if (txtIdKendaraan.getText().isEmpty() || txtMerk.getText().isEmpty() || txtModel.getText().isEmpty()
                || txtKendaraan.getText().isEmpty() || txtPlat.getText().isEmpty() || txtSewa.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (combo_jenis.getSelectedIndex() == 0) { // Cek jika "Pilih Jenis Kendaraan" masih terpilih
            JOptionPane.showMessageDialog(this, "Silakan pilih jenis kendaraan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String statusKendaraan = "";
        if (radio_tersedia.isSelected()) {
            statusKendaraan = "Tersedia";
        } else if (radio_tersedia1.isSelected()) { // Asumsi radio_tersedia1 adalah "Disewa"
            statusKendaraan = "Disewa";
        } else {
            JOptionPane.showMessageDialog(this, "Silakan pilih status kendaraan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (statusKendaraan.equals("Disewa")) {
            JOptionPane.showMessageDialog(this, "Status kendaraan baru tidak boleh 'Disewa'. Silakan pilih 'Tersedia'.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            // Salin gambar ke folder proyek
            if (pathGambarTerpilih != null && namaFileGambar != null) {
                Path sourcePath = Paths.get(pathGambarTerpilih);
                Path targetDir = Paths.get("src/vehicles"); // Folder tujuan
                Files.createDirectories(targetDir); // Pastikan folder ada
                Path targetPath = targetDir.resolve(namaFileGambar); // Gabungkan direktori tujuan dengan nama file

                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            koneksi db = new koneksi();
            conn = db.connect();

            if (conn == null) {
                return;
            }

            String sql = "INSERT INTO kendaraan (id_kendaraan, jenis, merk, model, tahun, plat_nomor, harga_sewa, status, gambar) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtIdKendaraan.getText());
            pst.setString(2, combo_jenis.getSelectedItem().toString());
            pst.setString(3, txtMerk.getText());
            pst.setString(4, txtModel.getText());
            pst.setInt(5, Integer.parseInt(txtKendaraan.getText())); // txtKendaraan adalah txtTahun
            pst.setString(6, txtPlat.getText());
            pst.setDouble(7, Double.parseDouble(txtSewa.getText())); // txtSewa adalah txtHargaSewa
            pst.setString(8, statusKendaraan);
            pst.setString(9, namaFileGambar); // Simpan nama file gambar

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan data.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // MySQL error code for duplicate entry
                JOptionPane.showMessageDialog(this, "Plat Nomor sudah terdaftar!", "Error Duplikasi", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Input Tahun Kendaraan dan Harga Sewa harus berupa angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) { // Untuk menangani error copy file
            JOptionPane.showMessageDialog(this, "Error saat operasi file: " + e.getMessage(), "Error File", JOptionPane.ERROR_MESSAGE);
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
    }//GEN-LAST:event_bSimpanActionPerformed

    private void bUnggahGambarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bUnggahGambarActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        // Set direktori awal ke folder images/vehicles jika ada
        File initialDir = new File("src/vehicles");
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
            // Sesuaikan ukuran gambar agar pas di JLabel (opsional, tapi disarankan)
            int lebar = label_tampil_gambar.getWidth();
            int tinggi = label_tampil_gambar.getHeight();

            if (lebar > 0 && tinggi > 0) {
                java.awt.Image img = icon.getImage().getScaledInstance(lebar, tinggi, java.awt.Image.SCALE_SMOOTH);
                label_tampil_gambar.setIcon(new ImageIcon(img));
            } else {
                label_tampil_gambar.setIcon(icon); // Jika JLabel belum di-render sempurna
            }

            // Ambil nama file untuk disimpan di database, pastikan unik
            String originalFileName = file.getName();
            String extension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                extension = originalFileName.substring(i);
            }
            // Gunakan UUID untuk membuat nama file unik
            namaFileGambar = UUID.randomUUID().toString() + extension;
        }
    }//GEN-LAST:event_bUnggahGambarActionPerformed

    private void table_kendaraanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table_kendaraanMouseClicked
        int i = table_kendaraan.getSelectedRow();
        if (i > -1) {
            txtIdKendaraan.setText(model.getValueAt(i, 0).toString());
            combo_jenis.setSelectedItem(model.getValueAt(i, 1).toString());
            txtMerk.setText(model.getValueAt(i, 2).toString());
            txtModel.setText(model.getValueAt(i, 3).toString());
            txtKendaraan.setText(model.getValueAt(i, 4).toString()); // txtKendaraan adalah txtTahun
            txtPlat.setText(model.getValueAt(i, 5).toString());
            txtSewa.setText(model.getValueAt(i, 6).toString()); // txtSewa adalah txtHargaSewa

            String statusDariDB = model.getValueAt(i, 7).toString();
            if (statusDariDB.equalsIgnoreCase("Tersedia")) {
                radio_tersedia.setSelected(true);
            } else if (statusDariDB.equalsIgnoreCase("Disewa")) {
                radio_tersedia1.setSelected(true);
            } else {
                status.clearSelection(); // Clear selection if status is "Servis" or other
            }

            // Tampilkan gambar jika ada
            if (model.getValueAt(i, 8) != null) {
                namaFileGambar = model.getValueAt(i, 8).toString();
                tampilGambar(namaFileGambar);
            } else {
                label_tampil_gambar.setIcon(null); // Kosongkan jika tidak ada gambar
                namaFileGambar = null;
            }
            pathGambarTerpilih = null; // Reset path terpilih
        }

    }//GEN-LAST:event_table_kendaraanMouseClicked

    private void cari_kendaraanKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cari_kendaraanKeyTyped
        searchData(cari_kendaraan.getText());
    }//GEN-LAST:event_cari_kendaraanKeyTyped

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
            java.util.logging.Logger.getLogger(kendaraan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(kendaraan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(kendaraan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(kendaraan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                kendaraan login = new kendaraan();
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
    private javax.swing.JButton bUnggahGambar;
    private javax.swing.JTextField cari_kendaraan;
    private javax.swing.JComboBox<String> combo_jenis;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel label_tampil_gambar;
    private javax.swing.JRadioButton radio_tersedia;
    private javax.swing.JRadioButton radio_tersedia1;
    private javax.swing.ButtonGroup status;
    private javax.swing.JTable table_kendaraan;
    private javax.swing.JTextField txtIdKendaraan;
    private javax.swing.JTextField txtKendaraan;
    private javax.swing.JTextField txtMerk;
    private javax.swing.JTextField txtModel;
    private javax.swing.JTextField txtPlat;
    private javax.swing.JTextField txtSewa;
    // End of variables declaration//GEN-END:variables
}
