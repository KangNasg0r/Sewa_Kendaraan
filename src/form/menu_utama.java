/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package form;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
/**
 *
 * @author Ahmad Nur Latif P
 */
public class menu_utama extends javax.swing.JFrame {
private Connection conn = new koneksi().connect();
    /**
     * Creates new form menu_utama
     */
    public menu_utama() {
        initComponents();
        setLocationRelativeTo(null);
        loadDashboardInfo();
        loadKasirInfo(); 
        tampilkanTanggal_hariini();
        tampilkanWaktuSekarang();
        waktuBerjalan();
    }
    
    private void tampilkanTanggal_hariini() {
        Date tanggalSaatIni = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd-MM-yyyy");
        String tanggalFormatted = formatter.format(tanggalSaatIni);
        label_tanggal.setText(tanggalFormatted);
    }

    private void tampilkanWaktuSekarang() {
        Date waktuSaatIni = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String waktuFormatted = formatter.format(waktuSaatIni);
        label_waktu.setText(waktuFormatted);
    }

    private void waktuBerjalan() {
        int delay = 1000;
        Timer waktu = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tampilkanWaktuSekarang();
            }
        });
        waktu.start();
    }
    
     private void loadDashboardInfo() {
        Connection conn = null;
        try {
            koneksi db = new koneksi(); // Create an instance of your koneksi class
            conn = db.connect();        // Get the database connection

            if (conn == null) {
                // If connection fails, set labels to N/A and return
                label_kendaraan_tersedia.setText("N/A");
                label_kendaraan_disewa.setText("N/A");
                label_transaksi.setText("N/A");
                return;
            }

            PreparedStatement pst;
            ResultSet rs;
            int count;

            // 1. Count Available Vehicles
            String sqlTersedia = "SELECT COUNT(*) AS total FROM kendaraan WHERE status = 'Tersedia'";
            pst = conn.prepareStatement(sqlTersedia);
            rs = pst.executeQuery();
            if (rs.next()) {
                count = rs.getInt("total");
                label_kendaraan_tersedia.setText(String.valueOf(count));
            } else {
                label_kendaraan_tersedia.setText("0");
            }
            rs.close();
            pst.close();

            // 2. Count Rented Vehicles
            String sqlDisewa = "SELECT COUNT(*) AS total FROM kendaraan WHERE status = 'Disewa'";
            pst = conn.prepareStatement(sqlDisewa);
            rs = pst.executeQuery();
            if (rs.next()) {
                count = rs.getInt("total");
                label_kendaraan_disewa.setText(String.valueOf(count));
            } else {
                label_kendaraan_disewa.setText("0");
            }
            rs.close();
            pst.close();

            // 3. Count Today's Transactions
            // Get current date in yyyy-MM-dd format for SQL DATE comparison
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String todayDate = sdf.format(new Date());

            // Assuming 'tanggal_sewa' is the column for transaction date
            // And 'status_transaksi' for 'Berjalan'
            String sqlTransaksi = "SELECT COUNT(*) AS total FROM transaksi WHERE tanggal_sewa = ? AND status_transaksi = 'Berjalan'";
            pst = conn.prepareStatement(sqlTransaksi);
            pst.setString(1, todayDate);
            rs = pst.executeQuery();
            if (rs.next()) {
                count = rs.getInt("total");
                label_transaksi.setText(String.valueOf(count));
            } else {
                label_transaksi.setText("0");
            }
            rs.close();
            pst.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data dashboard: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close(); // Close the connection after use
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Method to load cashier information
    private void loadKasirInfo() {
        String idKasir = UserID.getIdKasir(); // Get the ID from your UserID class
        
        if (idKasir == null || idKasir.isEmpty()) {
            label_id_kasir.setText("N/A");
            label_nama_kasir.setText("N/A");
            label_telepon.setText("N/A");
            JOptionPane.showMessageDialog(this, "ID Kasir tidak ditemukan. Pastikan Anda sudah login.", "Info Kasir", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Connection conn = null;
        try {
            koneksi db = new koneksi();
            conn = db.connect();

            if (conn == null) {
                label_id_kasir.setText(idKasir);
                label_nama_kasir.setText("N/A");
                label_telepon.setText("N/A");
                return;
            }

            String sql = "SELECT nama, no_telepon FROM kasir WHERE id_kasir = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, idKasir);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                label_id_kasir.setText(idKasir);
                label_nama_kasir.setText(rs.getString("nama"));
                label_telepon.setText(rs.getString("no_telepon"));
            } else {
                label_id_kasir.setText(idKasir);
                label_nama_kasir.setText("Nama Tidak Ditemukan");
                label_telepon.setText("Telepon Tidak Ditemukan");
                JOptionPane.showMessageDialog(this, "Data kasir dengan ID " + idKasir + " tidak ditemukan di database.", "Info Kasir", JOptionPane.WARNING_MESSAGE);
            }
            rs.close();
            pst.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat informasi kasir: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
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
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        label_kendaraan_tersedia = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        label_kendaraan_disewa = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        label_transaksi = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        label_nama_kasir = new javax.swing.JLabel();
        label_telepon = new javax.swing.JLabel();
        label_id_kasir = new javax.swing.JLabel();
        label_id_kasir1 = new javax.swing.JLabel();
        label_id_kasir2 = new javax.swing.JLabel();
        label_id_kasir3 = new javax.swing.JLabel();
        label_id_kasir4 = new javax.swing.JLabel();
        label_id_kasir5 = new javax.swing.JLabel();
        label_id_kasir6 = new javax.swing.JLabel();
        label_id_kasir7 = new javax.swing.JLabel();
        label_id_kasir8 = new javax.swing.JLabel();
        label_tanggal = new javax.swing.JLabel();
        label_id_kasir9 = new javax.swing.JLabel();
        label_id_kasir10 = new javax.swing.JLabel();
        label_waktu = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(243, 198, 35));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(16, 55, 92), 2));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(16, 55, 92));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("RENTAL KENDARAAN");
        jLabel1.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 2, 0, new java.awt.Color(16, 55, 92)));

        jPanel2.setBackground(new java.awt.Color(255, 255, 204));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(16, 55, 92));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("KENDARAAN TERSEDIA");

        label_kendaraan_tersedia.setFont(new java.awt.Font("Tahoma", 1, 40)); // NOI18N
        label_kendaraan_tersedia.setForeground(new java.awt.Color(16, 55, 92));
        label_kendaraan_tersedia.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_kendaraan_tersedia.setText("1");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
            .addComponent(label_kendaraan_tersedia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(43, 43, 43)
                .addComponent(label_kendaraan_tersedia, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 204));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(16, 55, 92));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("KENDARAAN DISEWA");

        label_kendaraan_disewa.setFont(new java.awt.Font("Tahoma", 1, 40)); // NOI18N
        label_kendaraan_disewa.setForeground(new java.awt.Color(16, 55, 92));
        label_kendaraan_disewa.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_kendaraan_disewa.setText("1");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(label_kendaraan_disewa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(58, 58, 58)
                .addComponent(label_kendaraan_disewa, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 204));
        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(16, 55, 92));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("TRANSAKSI SEDANG BERJALAN");

        label_transaksi.setFont(new java.awt.Font("Tahoma", 1, 40)); // NOI18N
        label_transaksi.setForeground(new java.awt.Color(16, 55, 92));
        label_transaksi.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_transaksi.setText("1");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(label_transaksi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(58, 58, 58)
                .addComponent(label_transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 204));
        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 2, true));

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/YaRental_logo.png"))); // NOI18N
        jLabel9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 3, true));

        label_nama_kasir.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_nama_kasir.setForeground(new java.awt.Color(16, 55, 92));
        label_nama_kasir.setText("Nama Kasir");

        label_telepon.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_telepon.setForeground(new java.awt.Color(16, 55, 92));
        label_telepon.setText("No Telepon");

        label_id_kasir.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir.setText("ID Kasir");

        label_id_kasir1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir1.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir1.setText("ID Kasir");

        label_id_kasir2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir2.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir2.setText("Telepon");

        label_id_kasir3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir3.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir3.setText("Nama");

        label_id_kasir4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir4.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_id_kasir4.setText(":");

        label_id_kasir5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir5.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_id_kasir5.setText(":");

        label_id_kasir6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir6.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_id_kasir6.setText(":");

        label_id_kasir7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir7.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir7.setText("Tanggal");

        label_id_kasir8.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir8.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_id_kasir8.setText(":");

        label_tanggal.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_tanggal.setForeground(new java.awt.Color(16, 55, 92));
        label_tanggal.setText("Tanggal");

        label_id_kasir9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir9.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir9.setText("Waktu");

        label_id_kasir10.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_id_kasir10.setForeground(new java.awt.Color(16, 55, 92));
        label_id_kasir10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_id_kasir10.setText(":");

        label_waktu.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        label_waktu.setForeground(new java.awt.Color(16, 55, 92));
        label_waktu.setText("Waktu");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(label_id_kasir2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(label_id_kasir3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(label_id_kasir1)
                    .addComponent(label_id_kasir7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(label_id_kasir9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(label_id_kasir6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label_telepon, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(label_id_kasir10, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label_waktu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label_id_kasir4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label_id_kasir5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label_nama_kasir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(label_id_kasir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(label_id_kasir8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label_tanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(label_id_kasir)
                            .addComponent(label_id_kasir1)
                            .addComponent(label_id_kasir4))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(label_nama_kasir)
                            .addComponent(label_id_kasir3)
                            .addComponent(label_id_kasir5))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(label_telepon)
                            .addComponent(label_id_kasir2)
                            .addComponent(label_id_kasir6))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(label_id_kasir7)
                            .addComponent(label_id_kasir8)
                            .addComponent(label_tanggal))
                        .addGap(0, 18, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(label_id_kasir9)
                            .addComponent(label_id_kasir10)
                            .addComponent(label_waktu)))
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(16, 55, 92));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Jl. Raya Tengah No.80, RT.6/RW.1, Gedong, Kec. Ps.Rebo, Kota Jakarta Timur, DKI Jakarta 13760, Indonesia");
        jLabel3.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 2, 0, new java.awt.Color(16, 55, 92)));

        jPanel6.setBackground(new java.awt.Color(243, 198, 35));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13))
        );

        jMenu1.setText("| Data Master |");
        jMenu1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItem8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem8.setText("Kasir");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem8);

        jMenuItem1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem1.setText("Pelanggan");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem2.setText("Kendaraan");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("| Transaksi |");
        jMenu2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenu2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu2MouseClicked(evt);
            }
        });
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });
        jMenuBar1.add(jMenu2);

        jMenu3.setText("| Laporan |");
        jMenu3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItem5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem5.setText("Data Kasir");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem5);

        jMenuItem6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem6.setText("Data Pelanggan");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem6);

        jMenuItem7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem7.setText("Data Transaksi");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem7);

        jMenuBar1.add(jMenu3);

        jMenu4.setText("| Keluar |");
        jMenu4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItem3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem3.setText("Keluar Akun");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem3);

        jMenuItem4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem4.setText("Keluar Aplikasi");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem4);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

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

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        pelanggan p = new pelanggan();
        p.setVisible(true);
        p.setLocationRelativeTo(null);
        this.dispose();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        kendaraan k = new kendaraan();
        k.setVisible(true);
        k.setLocationRelativeTo(null);
        this.dispose();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        login k = new login();
        k.setVisible(true);
        k.setLocationRelativeTo(null);
        this.dispose();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin keluar dari aplikasi?", "Konfirmasi Keluar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            
            System.exit(0);
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
        transaksi k = new transaksi();
        k.setVisible(true);
        k.setLocationRelativeTo(null);
        this.dispose();
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void jMenu2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu2MouseClicked
        transaksi k = new transaksi();
        k.setVisible(true);
        k.setLocationRelativeTo(null);
        this.dispose();
    }//GEN-LAST:event_jMenu2MouseClicked

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        try {
            String loginId = UserID.getIdKasir();
            String kasir = "Tidak Diketahui";

            try (PreparedStatement kasir_nama = conn.prepareStatement("SELECT nama FROM kasir WHERE id_kasir = ?")) {
                kasir_nama.setString(1, loginId);
                try (ResultSet rsNama = kasir_nama.executeQuery()) {
                    if (rsNama.next()) {
                        kasir = rsNama.getString("nama");
                    }
                }
            }

            String reportPath = "./src/report/report_kasir.jasper";
            HashMap parameter = new HashMap();
            parameter.put("kasir", kasir);

            JasperPrint print = JasperFillManager.fillReport(reportPath,parameter,conn);
            JasperViewer.viewReport(print,false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak report: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        try {
            String loginId = UserID.getIdKasir();
            String kasir = "Tidak Diketahui";

            try (PreparedStatement kasir_nama = conn.prepareStatement("SELECT nama FROM kasir WHERE id_kasir = ?")) {
                kasir_nama.setString(1, loginId);
                try (ResultSet rsNama = kasir_nama.executeQuery()) {
                    if (rsNama.next()) {
                        kasir = rsNama.getString("nama");
                    }
                }
            }

            String reportPath = "./src/report/report_pelanggan.jasper";
            HashMap parameter = new HashMap();
            parameter.put("kasir", kasir);

            JasperPrint print = JasperFillManager.fillReport(reportPath,parameter,conn);
            JasperViewer.viewReport(print,false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak report: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        try {
            String loginId = UserID.getIdKasir();
            String kasir = "Tidak Diketahui";

            try (PreparedStatement kasir_nama = conn.prepareStatement("SELECT nama FROM kasir WHERE id_kasir = ?")) {
                kasir_nama.setString(1, loginId);
                try (ResultSet rsNama = kasir_nama.executeQuery()) {
                    if (rsNama.next()) {
                        kasir = rsNama.getString("nama");
                    }
                }
            }

            String reportPath = "./src/report/report_transaksi.jasper";
            HashMap parameter = new HashMap();
            parameter.put("kasir", kasir);

            JasperPrint print = JasperFillManager.fillReport(reportPath,parameter,conn);
            JasperViewer.viewReport(print,false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak report: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        kasir k = new kasir();
        k.setVisible(true);
        k.setLocationRelativeTo(null);
        this.dispose();
    }//GEN-LAST:event_jMenuItem8ActionPerformed

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
            java.util.logging.Logger.getLogger(menu_utama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(menu_utama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(menu_utama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(menu_utama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new menu_utama().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel label_id_kasir;
    private javax.swing.JLabel label_id_kasir1;
    private javax.swing.JLabel label_id_kasir10;
    private javax.swing.JLabel label_id_kasir2;
    private javax.swing.JLabel label_id_kasir3;
    private javax.swing.JLabel label_id_kasir4;
    private javax.swing.JLabel label_id_kasir5;
    private javax.swing.JLabel label_id_kasir6;
    private javax.swing.JLabel label_id_kasir7;
    private javax.swing.JLabel label_id_kasir8;
    private javax.swing.JLabel label_id_kasir9;
    private javax.swing.JLabel label_kendaraan_disewa;
    private javax.swing.JLabel label_kendaraan_tersedia;
    private javax.swing.JLabel label_nama_kasir;
    private javax.swing.JLabel label_tanggal;
    private javax.swing.JLabel label_telepon;
    private javax.swing.JLabel label_transaksi;
    private javax.swing.JLabel label_waktu;
    // End of variables declaration//GEN-END:variables
}
