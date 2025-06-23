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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit; // Untuk perhitungan selisih tanggal (durasi)
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import org.jdesktop.swingx.JXDatePicker;

/**
 *
 * @author Ahmad Nur Latif P
 */
public class transaksi extends javax.swing.JFrame {
private Connection conn = new koneksi().connect();
    private DefaultTableModel model;
    private String selectedIdPelanggan;
    private String selectedIdKendaraan;

    /**
     * Creates new form transaksi
     */
    public transaksi() {
        initComponents();
        setLocationRelativeTo(null);
        model = new DefaultTableModel();
        table_transaksi.setModel(model);
        model.addColumn("ID Transaksi");
        model.addColumn("ID Kasir");
        model.addColumn("ID Pelanggan");
        model.addColumn("ID Kendaraan");
        model.addColumn("Tgl. Transaksi");
        model.addColumn("Tgl. Sewa");
        model.addColumn("Tgl. Kembali");
        model.addColumn("Durasi (Hari)");
        model.addColumn("Total Biaya");
        model.addColumn("Status");

        autogenerateIdTransaksi();
        tanggal_transaksi.setDate(new Date());
        tanggal_transaksi.setEditable(false);
        txtIdPelanggan.setEditable(false);
        txtNamaPelanggan.setEditable(false);
        txtAlamatPelanggan.setEditable(false);
        txtIdKendaraan.setEditable(false);
        txtPlat.setEditable(false);
        txtModel.setEditable(false);
        txtHargaSewa.setEditable(false);
        txtDurasi.setEditable(false);
        txtTotalBiaya.setEditable(false);
        loadKasirInfo();
        loadDataTransaksi();
        tanggal_sewa.addActionListener(e -> calculateRentalCost());
        tanggal_kembali.addActionListener(e -> calculateRentalCost());

        clearForm();
    }

    public void setSelectedPelanggan(String idPelanggan, String namaPelanggan, String alamatPelanggan) {
        this.selectedIdPelanggan = idPelanggan;
        txtIdPelanggan.setText(idPelanggan);
        txtNamaPelanggan.setText(namaPelanggan);
        txtAlamatPelanggan.setText(alamatPelanggan);
    }

    // Metode publik untuk menerima data kendaraan yang dipilih dari PilihKendaraan
    public void setSelectedKendaraan(String idKendaraan, String platNomor, String modelKendaraan, int hargaSewa, String gambar) {
        this.selectedIdKendaraan = idKendaraan;
        txtIdKendaraan.setText(idKendaraan);
        txtPlat.setText(platNomor);
        txtModel.setText(modelKendaraan);
        txtHargaSewa.setText(String.valueOf((long)hargaSewa));
        tampilGambar(gambar);
        calculateRentalCost();
    }

    // Metode untuk menghasilkan ID Transaksi otomatis (TR001, TR002, dst.)
    private void autogenerateIdTransaksi() {
        Connection conn = null;
        try {
            koneksi db = new koneksi(); // Buat objek koneksi Anda
            conn = db.connect();

            if (conn == null) {
                txtIdTransaksi.setText("Error");
                return;
            }

            String sql = "SELECT MAX(CAST(SUBSTRING(id_transaksi, 3) AS UNSIGNED)) AS max_id FROM transaksi";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                String newId = String.format("TR%03d", maxId + 1);
                txtIdTransaksi.setText(newId);
            } else {
                txtIdTransaksi.setText("TR001");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengenerate ID Transaksi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//            e.printStackTrace();
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

    // Metode untuk memuat informasi kasir yang sedang login
    private void loadKasirInfo() {
        String idKasir = UserID.getIdKasir();
        txtIdKasir.setText(idKasir);
        txtIdKasir.setEditable(false);
        if (idKasir == null || idKasir.isEmpty()) {
            txtNamaKasir.setText("N/A");
            txtNamaKasir.setEditable(false);
            return;
        }
        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();

            if (conn == null) {
                txtNamaKasir.setText("N/A");
                txtNamaKasir.setEditable(false);
                return;
            }
            String sql = "SELECT nama FROM kasir WHERE id_kasir = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, idKasir);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtNamaKasir.setText(rs.getString("nama"));
                txtNamaKasir.setEditable(false);
            } else {
                txtNamaKasir.setText("Nama Tidak Ditemukan");
                txtNamaKasir.setEditable(false);
            }
            rs.close();
            pst.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat nama kasir: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
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

    // Metode untuk memuat data transaksi dari database ke tabel
    private void loadDataTransaksi() {
        model.getDataVector().removeAllElements();
        model.fireTableDataChanged();
        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();

            if (conn == null) {
                return;
            }
            String sql = "SELECT id_transaksi, id_kasir, id_pelanggan, id_kendaraan, tanggal_transaksi, tanggal_sewa, tanggal_kembali, durasi_sewa, total_biaya, status_transaksi FROM transaksi";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] o = new Object[10];
                o[0] = rs.getString("id_transaksi");
                o[1] = rs.getString("id_kasir");
                o[2] = rs.getString("id_pelanggan");
                o[3] = rs.getString("id_kendaraan");
                o[4] = rs.getDate("tanggal_transaksi");
                o[5] = rs.getDate("tanggal_sewa");
                o[6] = rs.getDate("tanggal_kembali");
                o[7] = rs.getInt("durasi_sewa");
                o[8] = rs.getInt("total_biaya");
                o[9] = rs.getString("status_transaksi");
                model.addRow(o);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error memuat data transaksi: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
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

    // Metode untuk membersihkan semua field input di form
    private void clearForm() {
        autogenerateIdTransaksi();
        tanggal_transaksi.setDate(new Date());
        txtIdPelanggan.setText("");
        txtNamaPelanggan.setText("");
        txtAlamatPelanggan.setText("");
        selectedIdPelanggan = null;

        txtIdKendaraan.setText("");
        txtPlat.setText("");
        txtModel.setText("");
        txtHargaSewa.setText("");
        label_tampil_gambar.setIcon(null);
        selectedIdKendaraan = null;

        tanggal_sewa.setDate(null);
        tanggal_kembali.setDate(null);
        txtDurasi.setText("0");
        txtTotalBiaya.setText("0");
    }

    // Metode untuk menampilkan gambar kendaraan di JLabel
    private void tampilGambar(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            String imagePath = "src/vehicles/" + fileName;
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                int lebar = label_tampil_gambar.getWidth();
                int tinggi = label_tampil_gambar.getHeight();
                
                if (lebar == 0) lebar = 313; 
                if (tinggi == 0) tinggi = 212; 
                
                java.awt.Image img = icon.getImage().getScaledInstance(lebar, tinggi, java.awt.Image.SCALE_SMOOTH);
                label_tampil_gambar.setIcon(new ImageIcon(img));
            } else {
                label_tampil_gambar.setIcon(null);
                JOptionPane.showMessageDialog(this, "File gambar kendaraan tidak ditemukan: " + fileName, "Informasi", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            label_tampil_gambar.setIcon(null);
            label_tampil_gambar.setText("Tidak Ada Gambar");
        }

//                Skalakan gambar agar sesuai dengan ukuran JLabel
//                if (lebar > 0 && tinggi > 0) {
//                    java.awt.Image img = icon.getImage().getScaledInstance(lebar, tinggi, java.awt.Image.SCALE_SMOOTH);
//                    label_tampil_gambar.setIcon(new ImageIcon(img));
//                } else {
//                    label_tampil_gambar.setIcon(icon); // Jika ukuran JLabel belum di-render sempurna
//                }
//            } else {
//                label_tampil_gambar.setIcon(null); // Gambar tidak ditemukan
//                JOptionPane.showMessageDialog(this, "File gambar kendaraan tidak ditemukan: " + fileName, "Informasi", JOptionPane.WARNING_MESSAGE);
//            }
//        } else {
//            label_tampil_gambar.setIcon(null); // Tidak ada gambar
//        }
    }

    // Metode untuk menghitung durasi sewa dan total biaya
    private void calculateRentalCost() {
        Date tglSewa = tanggal_sewa.getDate();
        Date tglKembali = tanggal_kembali.getDate();
        long hargaSewaPerHari;

        try {
            String hargaTextBersih = txtHargaSewa.getText().trim()
                    .replace(".", "")
                    .replace(",", "");

            if (hargaTextBersih.isEmpty()) {
                hargaSewaPerHari = 0;
            } else {
                hargaSewaPerHari = Long.parseLong(hargaTextBersih);
            }
        } catch (NumberFormatException e) {
            hargaSewaPerHari = 0;
        }

        if (tglSewa != null && tglKembali != null && hargaSewaPerHari > 0) {
            if (tglKembali.before(tglSewa)) {
                JOptionPane.showMessageDialog(this, "Tanggal Kembali tidak boleh sebelum Tanggal Sewa.",
                        "Kesalahan Tanggal", JOptionPane.WARNING_MESSAGE);
                txtDurasi.setText("0");
                txtTotalBiaya.setText("0");
                return;
            }

            // Hitung selisih hari
            long diffInMillies = Math.abs(tglKembali.getTime() - tglSewa.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            if (diff == 0) {
                diff = 1; // Jika sewa dan kembali di hari yang sama = 1 hari
            } else {
                diff = diff + 1; // Hitung inklusif (hari awal + hari akhir)
            }

            long totalBiaya = diff * hargaSewaPerHari;
            txtDurasi.setText(String.valueOf(diff));
            txtTotalBiaya.setText(String.valueOf(totalBiaya));

            // Debug: Tambahkan ini untuk melihat nilai sebenarnya
            // System.out.println("Durasi: " + diff + " hari");
            // System.out.println("Harga per hari: " + hargaSewaPerHari);
            // System.out.println("Total: " + totalBiaya);

        } else {
            txtDurasi.setText("0");
            txtTotalBiaya.setText("0");
        }
    }
    
    public void cetak() {
        try{
            String path="./src/report/nota.jasper";
            HashMap parameter = new HashMap();
            parameter.put("id_transaksi",txtIdTransaksi.getText());
            JasperPrint print = JasperFillManager.fillReport(path,parameter,conn);
            JasperViewer.viewReport(print,false);
        }catch(Exception ex){
            JOptionPane.showMessageDialog(rootPane,"Dokumen Tidak Ada" +ex);   
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
        bPengembalian = new javax.swing.JButton();
        bHapus = new javax.swing.JButton();
        bBatal = new javax.swing.JButton();
        bKembali = new javax.swing.JButton();
        bSewaKendaraan = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtIdPelanggan = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        bCariPelanggan = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txtNamaPelanggan = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        bCariKendaraan = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        txtIdKendaraan = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        txtPlat = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtModel = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAlamatPelanggan = new javax.swing.JTextArea();
        jLabel19 = new javax.swing.JLabel();
        txtHargaSewa = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        txtDurasi = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        txtTotalBiaya = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        table_transaksi = new javax.swing.JTable();
        tanggal_sewa = new org.jdesktop.swingx.JXDatePicker();
        tanggal_kembali = new org.jdesktop.swingx.JXDatePicker();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        label_tampil_gambar = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtIdTransaksi = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        tanggal_transaksi = new org.jdesktop.swingx.JXDatePicker();
        jLabel8 = new javax.swing.JLabel();
        txtNamaKasir = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtIdKasir = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(243, 198, 35));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(16, 55, 92), 2));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(16, 55, 92));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("TRANSAKSI PENYEWAAN");
        jLabel4.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 2, 0, new java.awt.Color(16, 55, 92)));

        jPanel2.setBackground(new java.awt.Color(243, 198, 35));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        bPengembalian.setBackground(new java.awt.Color(16, 55, 92));
        bPengembalian.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        bPengembalian.setForeground(new java.awt.Color(255, 255, 255));
        bPengembalian.setText("PENGEMBALIAN");
        bPengembalian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bPengembalianActionPerformed(evt);
            }
        });

        bHapus.setBackground(new java.awt.Color(16, 55, 92));
        bHapus.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        bHapus.setForeground(new java.awt.Color(255, 255, 255));
        bHapus.setText("HAPUS TRANSAKSI");
        bHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bHapusActionPerformed(evt);
            }
        });

        bBatal.setBackground(new java.awt.Color(16, 55, 92));
        bBatal.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        bBatal.setForeground(new java.awt.Color(255, 255, 255));
        bBatal.setText("BATAL");
        bBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBatalActionPerformed(evt);
            }
        });

        bKembali.setBackground(new java.awt.Color(16, 55, 92));
        bKembali.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        bKembali.setForeground(new java.awt.Color(255, 255, 255));
        bKembali.setText("KEMBALI");
        bKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bKembaliActionPerformed(evt);
            }
        });

        bSewaKendaraan.setBackground(new java.awt.Color(16, 55, 92));
        bSewaKendaraan.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        bSewaKendaraan.setForeground(new java.awt.Color(255, 255, 255));
        bSewaKendaraan.setText("SEWA KENDARAAN");
        bSewaKendaraan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSewaKendaraanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bSewaKendaraan, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(bPengembalian, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(bHapus)
                .addGap(18, 18, 18)
                .addComponent(bBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bKembali, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bPengembalian)
                    .addComponent(bHapus)
                    .addComponent(bBatal)
                    .addComponent(bKembali)
                    .addComponent(bSewaKendaraan))
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(243, 198, 35));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(16, 55, 92));
        jLabel3.setText("Pelanggan :");

        txtIdPelanggan.setBackground(new java.awt.Color(255, 255, 204));
        txtIdPelanggan.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(16, 55, 92));
        jLabel12.setText("ID :");

        bCariPelanggan.setBackground(new java.awt.Color(16, 55, 92));
        bCariPelanggan.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bCariPelanggan.setForeground(new java.awt.Color(255, 255, 255));
        bCariPelanggan.setText("CARI");
        bCariPelanggan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCariPelangganActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(16, 55, 92));
        jLabel13.setText("Nama :");

        txtNamaPelanggan.setBackground(new java.awt.Color(255, 255, 204));
        txtNamaPelanggan.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(16, 55, 92));
        jLabel14.setText("Kendaraan :");

        bCariKendaraan.setBackground(new java.awt.Color(16, 55, 92));
        bCariKendaraan.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        bCariKendaraan.setForeground(new java.awt.Color(255, 255, 255));
        bCariKendaraan.setText("CARI");
        bCariKendaraan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCariKendaraanActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(16, 55, 92));
        jLabel15.setText("ID :");

        txtIdKendaraan.setBackground(new java.awt.Color(255, 255, 204));
        txtIdKendaraan.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(16, 55, 92));
        jLabel16.setText("Plat Nomor :");

        txtPlat.setBackground(new java.awt.Color(255, 255, 204));
        txtPlat.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(16, 55, 92));
        jLabel17.setText("Model :");

        txtModel.setBackground(new java.awt.Color(255, 255, 204));
        txtModel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(16, 55, 92));
        jLabel18.setText("Alamat :");

        txtAlamatPelanggan.setBackground(new java.awt.Color(255, 255, 204));
        txtAlamatPelanggan.setColumns(20);
        txtAlamatPelanggan.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        txtAlamatPelanggan.setRows(5);
        jScrollPane1.setViewportView(txtAlamatPelanggan);

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(16, 55, 92));
        jLabel19.setText("Harga Sewa/Hari:");

        txtHargaSewa.setBackground(new java.awt.Color(255, 255, 204));
        txtHargaSewa.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(16, 55, 92));
        jLabel20.setText("Tanggal Sewa :");

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(16, 55, 92));
        jLabel21.setText("Tanggal Kembali :");

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(16, 55, 92));
        jLabel22.setText("Durasi Sewa :");

        txtDurasi.setBackground(new java.awt.Color(255, 255, 204));
        txtDurasi.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(16, 55, 92));
        jLabel23.setText("Total Biaya :");

        txtTotalBiaya.setBackground(new java.awt.Color(255, 255, 204));
        txtTotalBiaya.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jPanel4.setBackground(new java.awt.Color(16, 55, 92));
        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jPanel4.setPreferredSize(new java.awt.Dimension(2, 2));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        table_transaksi.setModel(new javax.swing.table.DefaultTableModel(
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
        table_transaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                table_transaksiMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(table_transaksi);

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(16, 55, 92));
        jLabel24.setText("(Hari)");

        jLabel25.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(16, 55, 92));
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("(Foto Kendaraan)");

        jButton1.setBackground(new java.awt.Color(16, 55, 92));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("CETAK NOTA");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(16, 55, 92));
        jLabel26.setText("Rp ");

        jPanel5.setBackground(new java.awt.Color(16, 55, 92));

        label_tampil_gambar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_tampil_gambar.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(label_tampil_gambar, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(label_tampil_gambar, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtNamaPelanggan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(bCariPelanggan))
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtIdPelanggan, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane1))
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtHargaSewa)
                            .addComponent(txtModel)
                            .addComponent(txtIdKendaraan)
                            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtPlat)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel17)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bCariKendaraan)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addGap(142, 142, 142)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(tanggal_kembali, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tanggal_sewa, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                            .addComponent(txtDurasi, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel24))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                            .addComponent(jLabel26)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtTotalBiaya, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGap(0, 0, Short.MAX_VALUE))))
                        .addGap(53, 53, 53)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(39, 39, 39))))
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 995, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(bCariPelanggan))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIdPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNamaPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tanggal_sewa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tanggal_kembali, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtDurasi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel24))
                                .addGap(11, 11, 11)
                                .addComponent(jLabel23))
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTotalBiaya, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25)
                            .addComponent(jLabel26)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(bCariKendaraan))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIdKendaraan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPlat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHargaSewa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addGap(6, 6, 6))
        );

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(16, 55, 92));
        jLabel2.setText("ID Transaksi :");

        txtIdTransaksi.setBackground(new java.awt.Color(255, 255, 204));
        txtIdTransaksi.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(16, 55, 92));
        jLabel7.setText("Tanggal :");

        tanggal_transaksi.setBackground(new java.awt.Color(255, 255, 204));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(16, 55, 92));
        jLabel8.setText("ID Kasir :");

        txtNamaKasir.setBackground(new java.awt.Color(255, 255, 204));
        txtNamaKasir.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(16, 55, 92));
        jLabel11.setText("Nama Kasir :");

        txtIdKasir.setBackground(new java.awt.Color(255, 255, 204));
        txtIdKasir.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tanggal_transaksi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtIdTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtIdKasir, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNamaKasir, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(63, 63, 63)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtIdTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(txtIdKasir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(tanggal_transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtNamaKasir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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

    private void bCariPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCariPelangganActionPerformed
        // this.getParent() akan mendapatkan Frame induk dari form transaksi
        PilihPelanggan dialog = new PilihPelanggan(null, true, this);
        dialog.setVisible(true);
    }//GEN-LAST:event_bCariPelangganActionPerformed

    private void bCariKendaraanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCariKendaraanActionPerformed
        // Membuka dialog PilihKendaraan
        PilihKendaraan dialog = new PilihKendaraan(null, true, this);
        dialog.setVisible(true);
    }//GEN-LAST:event_bCariKendaraanActionPerformed

    private void bSewaKendaraanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSewaKendaraanActionPerformed
        // 1. Validasi Input
        if (txtIdPelanggan.getText().isEmpty() || txtIdKendaraan.getText().isEmpty()
                || tanggal_sewa.getDate() == null || tanggal_kembali.getDate() == null
                || txtDurasi.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field wajib diisi!", "Peringatan Sewa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date tglSewa = tanggal_sewa.getDate();
        Date tglKembali = tanggal_kembali.getDate();
        Date tglTransaksi = tanggal_transaksi.getDate();
        int durasiSewa = Integer.parseInt(txtDurasi.getText());
        
        long totalBiaya;
        long hargaSewaPerHariUntukPenyimpanan; // Variabel ini diperlukan jika harga sewa dari text field akan digunakan untuk penyimpanan

        try {
            
            String totalBiayaTextBersih = txtTotalBiaya.getText().trim()
                    .replace(".", "")
                    .replace(",", "");

            if (totalBiayaTextBersih.isEmpty()) {
                totalBiaya = 0L; // Gunakan 0L untuk long
            } else {
                totalBiaya = Long.parseLong(totalBiayaTextBersih);
            }

            String hargaSewaTextBersih = txtHargaSewa.getText().trim()
                    .replace(".", "")
                    .replace(",", "");

            if (hargaSewaTextBersih.isEmpty()) {
                hargaSewaPerHariUntukPenyimpanan = 0L;
            } else {
                hargaSewaPerHariUntukPenyimpanan = Long.parseLong(hargaSewaTextBersih);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Format angka untuk Harga Sewa atau Total Biaya tidak valid.\nPastikan hanya mengandung angka.", "Kesalahan Format Angka", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        if (totalBiaya <= 0) {
            JOptionPane.showMessageDialog(this, "Total biaya harus lebih dari 0!", "Peringatan Sewa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tglKembali.before(tglSewa)) {
            JOptionPane.showMessageDialog(this, "Tanggal Kembali tidak boleh sebelum Tanggal Sewa.", "Kesalahan Tanggal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();
            if (conn == null) {
                return;
            }

            // 2. Insert data ke tabel transaksi
            String insertSql = "INSERT INTO transaksi (id_transaksi, id_kasir, id_pelanggan, id_kendaraan, tanggal_transaksi, tanggal_sewa, tanggal_kembali, durasi_sewa, total_biaya, status_transaksi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertPst = conn.prepareStatement(insertSql);
            insertPst.setString(1, txtIdTransaksi.getText());
            insertPst.setString(2, txtIdKasir.getText()); // Dari kasir yang sedang login
            insertPst.setString(3, selectedIdPelanggan); // ID pelanggan yang dipilih
            insertPst.setString(4, selectedIdKendaraan); // ID kendaraan yang dipilih
            insertPst.setDate(5, new java.sql.Date(tglTransaksi.getTime()));
            insertPst.setDate(6, new java.sql.Date(tglSewa.getTime()));
            insertPst.setDate(7, new java.sql.Date(tglKembali.getTime()));
            insertPst.setInt(8, durasiSewa);
            insertPst.setLong(9, totalBiaya);
            insertPst.setString(10, "Berjalan");

            int affectedRows = insertPst.executeUpdate();
            insertPst.close();
            
            if (affectedRows > 0) {
                // 3. Update status kendaraan di tabel kendaraan menjadi 'Disewa'
                String updateKendaraanSql = "UPDATE kendaraan SET status = 'Disewa' WHERE id_kendaraan = ?";
                PreparedStatement updateKendaraanPst = conn.prepareStatement(updateKendaraanSql);
                updateKendaraanPst.setString(1, selectedIdKendaraan);
                updateKendaraanPst.executeUpdate();
                updateKendaraanPst.close();

                JOptionPane.showMessageDialog(this, "Penyewaan berhasil dicatat!", "Sewa Sukses", JOptionPane.INFORMATION_MESSAGE);
                cetak();
                loadDataTransaksi(); // Refresh tabel transaksi
                clearForm(); // Bersihkan form untuk entri baru
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mencatat penyewaan.", "Sewa Gagal", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Error kode MySQL untuk duplikasi primary key
                JOptionPane.showMessageDialog(this, "ID Transaksi sudah ada. Coba ulangi untuk ID baru.", "Error Duplikasi", JOptionPane.ERROR_MESSAGE);
            } else if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("for key 'plat_nomor'")) {
                JOptionPane.showMessageDialog(this, "Plat Nomor kendaraan sudah terdaftar. Mungkin ada data ganda di kendaraan.", "Error Database", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error saat mencatat penyewaan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            }
//            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_bSewaKendaraanActionPerformed

    private void bPengembalianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bPengembalianActionPerformed
        int selectedRow = table_transaksi.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi yang akan dikembalikan dari tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idTransaksi = model.getValueAt(selectedRow, 0).toString();
        String currentStatus = model.getValueAt(selectedRow, 9).toString(); // Ambil status transaksi dari tabel
        String idKendaraan = model.getValueAt(selectedRow, 3).toString(); // Ambil ID Kendaraan dari tabel

        if (!currentStatus.equalsIgnoreCase("Berjalan")) {
            JOptionPane.showMessageDialog(this, "Transaksi ini tidak dalam status 'Berjalan' dan tidak dapat dikembalikan.", "Pengembalian Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Konfirmasi pengembalian untuk transaksi " + idTransaksi + "?", "Konfirmasi Pengembalian", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                koneksi db = new koneksi();
                conn = db.connect();
                if (conn == null) {
                    return;
                }
                String updateTransaksiSql = "UPDATE transaksi SET tanggal_kembali = ?, status_transaksi = 'Selesai' WHERE id_transaksi = ?";
                PreparedStatement updateTransaksiPst = conn.prepareStatement(updateTransaksiSql);
                updateTransaksiPst.setDate(1, new java.sql.Date(new Date().getTime()));
                updateTransaksiPst.setString(2, idTransaksi);
                int affectedRows = updateTransaksiPst.executeUpdate();
                updateTransaksiPst.close();

                if (affectedRows > 0) {
                    // Update status kendaraan menjadi 'Tersedia'
                    String updateKendaraanSql = "UPDATE kendaraan SET status = 'Tersedia' WHERE id_kendaraan = ?";
                    PreparedStatement updateKendaraanPst = conn.prepareStatement(updateKendaraanSql);
                    updateKendaraanPst.setString(1, idKendaraan);
                    updateKendaraanPst.executeUpdate();
                    updateKendaraanPst.close();

                    JOptionPane.showMessageDialog(this, "Kendaraan berhasil dikembalikan dan transaksi selesai!", "Pengembalian Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadDataTransaksi(); // Refresh tabel transaksi
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal memproses pengembalian.", "Pengembalian Gagal", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saat memproses pengembalian: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
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
    }//GEN-LAST:event_bPengembalianActionPerformed

    private void bHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHapusActionPerformed
        int selectedRow = table_transaksi.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi yang akan dihapus dari tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idTransaksi = model.getValueAt(selectedRow, 0).toString();
        String currentStatus = model.getValueAt(selectedRow, 9).toString(); // Ambil status transaksi dari tabel
        String idKendaraan = model.getValueAt(selectedRow, 3).toString(); // Ambil ID Kendaraan dari tabel

        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menghapus transaksi " + idTransaksi + "?\nJika status 'Berjalan', kendaraan akan kembali 'Tersedia'.", "Konfirmasi Hapus Transaksi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                koneksi db = new koneksi();
                conn = db.connect();
                if (conn == null) {
                    return;
                }

                String deleteSql = "DELETE FROM transaksi WHERE id_transaksi = ?";
                PreparedStatement deletePst = conn.prepareStatement(deleteSql);
                deletePst.setString(1, idTransaksi);
                int affectedRows = deletePst.executeUpdate();
                deletePst.close();

                if (affectedRows > 0) {
                    // Jika transaksi berstatus 'Berjalan', ubah status kendaraan kembali menjadi 'Tersedia'
                    if (currentStatus.equalsIgnoreCase("Berjalan")) {
                        String updateKendaraanSql = "UPDATE kendaraan SET status = 'Tersedia' WHERE id_kendaraan = ?";
                        PreparedStatement updateKendaraanPst = conn.prepareStatement(updateKendaraanSql);
                        updateKendaraanPst.setString(1, idKendaraan);
                        updateKendaraanPst.executeUpdate();
                        updateKendaraanPst.close();
                    }
                    JOptionPane.showMessageDialog(this, "Transaksi berhasil dihapus!", "Hapus Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadDataTransaksi(); // Refresh tabel transaksi
                    clearForm(); // Bersihkan form
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus transaksi.", "Hapus Gagal", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saat menghapus transaksi: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
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

    private void table_transaksiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table_transaksiMouseClicked
        int selectedRow = table_transaksi.getSelectedRow();
        if (selectedRow > -1) {
            txtIdTransaksi.setText(model.getValueAt(selectedRow, 0).toString());
            txtIdKasir.setText(model.getValueAt(selectedRow, 1).toString());

            this.selectedIdPelanggan = model.getValueAt(selectedRow, 2).toString();
            txtIdPelanggan.setText(this.selectedIdPelanggan);

            this.selectedIdKendaraan = model.getValueAt(selectedRow, 3).toString();
            txtIdKendaraan.setText(this.selectedIdKendaraan);
            tanggal_transaksi.setDate((Date) model.getValueAt(selectedRow, 4));
            tanggal_sewa.setDate((Date) model.getValueAt(selectedRow, 5));
            tanggal_kembali.setDate((Date) model.getValueAt(selectedRow, 6));

            txtDurasi.setText(model.getValueAt(selectedRow, 7).toString());
            txtTotalBiaya.setText(model.getValueAt(selectedRow, 8).toString());

            // Ambil detail lainnya (Nama Pelanggan, Alamat, Plat, Model, Harga, Gambar) dari DB
            // karena tidak semuanya ada di model tabel transaksi
            populateDetailsFromDatabase(this.selectedIdPelanggan, this.selectedIdKendaraan);
        }
    }//GEN-LAST:event_table_transaksiMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cetak();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void populateDetailsFromDatabase(String idPelanggan, String idKendaraan) {
        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();
            if (conn == null) {
                return;
            }

            // Ambil detail Pelanggan
            String sqlPelanggan = "SELECT nama_pelanggan, alamat FROM pelanggan WHERE id_pelanggan = ?";
            PreparedStatement pstPelanggan = conn.prepareStatement(sqlPelanggan);
            pstPelanggan.setString(1, idPelanggan);
            ResultSet rsPelanggan = pstPelanggan.executeQuery();
            if (rsPelanggan.next()) {
                txtNamaPelanggan.setText(rsPelanggan.getString("nama_pelanggan"));
                txtAlamatPelanggan.setText(rsPelanggan.getString("alamat"));
            } else {
                txtNamaPelanggan.setText("Pelanggan Tidak Ditemukan");
                txtAlamatPelanggan.setText("N/A");
            }
            rsPelanggan.close();
            pstPelanggan.close();

            // Ambil detail Kendaraan
            String sqlKendaraan = "SELECT plat_nomor, model, harga_sewa, gambar FROM kendaraan WHERE id_kendaraan = ?";
            PreparedStatement pstKendaraan = conn.prepareStatement(sqlKendaraan);
            pstKendaraan.setString(1, idKendaraan);
            ResultSet rsKendaraan = pstKendaraan.executeQuery();
            if (rsKendaraan.next()) {
                txtPlat.setText(rsKendaraan.getString("plat_nomor"));
                txtModel.setText(rsKendaraan.getString("model"));
                txtHargaSewa.setText(String.valueOf(rsKendaraan.getLong("harga_sewa")));
                tampilGambar(rsKendaraan.getString("gambar"));
            } else {
                txtPlat.setText("Kendaraan Tidak Ditemukan");
                txtModel.setText("N/A");
                txtHargaSewa.setText("0.00");
                label_tampil_gambar.setIcon(null);
            }
            rsKendaraan.close();
            pstKendaraan.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error memuat detail transaksi dari database: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
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
            java.util.logging.Logger.getLogger(transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new transaksi().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBatal;
    private javax.swing.JButton bCariKendaraan;
    private javax.swing.JButton bCariPelanggan;
    private javax.swing.JButton bHapus;
    private javax.swing.JButton bKembali;
    private javax.swing.JButton bPengembalian;
    private javax.swing.JButton bSewaKendaraan;
    private javax.swing.JButton jButton1;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel label_tampil_gambar;
    private javax.swing.JTable table_transaksi;
    private org.jdesktop.swingx.JXDatePicker tanggal_kembali;
    private org.jdesktop.swingx.JXDatePicker tanggal_sewa;
    private org.jdesktop.swingx.JXDatePicker tanggal_transaksi;
    private javax.swing.JTextArea txtAlamatPelanggan;
    private javax.swing.JTextField txtDurasi;
    private javax.swing.JTextField txtHargaSewa;
    private javax.swing.JTextField txtIdKasir;
    private javax.swing.JTextField txtIdKendaraan;
    private javax.swing.JTextField txtIdPelanggan;
    private javax.swing.JTextField txtIdTransaksi;
    private javax.swing.JTextField txtModel;
    private javax.swing.JTextField txtNamaKasir;
    private javax.swing.JTextField txtNamaPelanggan;
    private javax.swing.JTextField txtPlat;
    private javax.swing.JTextField txtTotalBiaya;
    // End of variables declaration//GEN-END:variables
}
