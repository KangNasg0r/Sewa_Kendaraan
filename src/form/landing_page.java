/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package form;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 *
 * @author Ahmad Nur Latif P
 */
public class landing_page extends javax.swing.JFrame {

    private List<String> allVehicleImagePaths = new ArrayList<>(); // Menyimpan semua path gambar kendaraan
    private int slideStartOffset = 0; // Indeks awal untuk set gambar yang sedang ditampilkan (offset)
    private Timer slideTimer;
    private final int SLIDE_DELAY_MS = 5000;

    /**
     * Creates new form landing_page
     */
    public landing_page() {
        initComponents();
        setLocationRelativeTo(null);
        loadLogo();
        loadAllVehicleImagePaths();
        startImageSlideShow();
    }

    @Override
    public void dispose() {
        if (slideTimer != null && slideTimer.isRunning()) {
            slideTimer.stop();
        }
        super.dispose();
    }

    private void loadLogo() {
        String logoPath = "src/gambar/YaRental_200x200.png";
        File logoFile = new File(logoPath);
        if (logoFile.exists()) {
            ImageIcon icon = new ImageIcon(logoFile.getAbsolutePath());
            int lebar = lblLogo.getWidth();
            int tinggi = lblLogo.getHeight();
            if (lebar == 0) {
                lebar = 200;
            }
            if (tinggi == 0) {
                tinggi = 200;
            }

            Image img = icon.getImage().getScaledInstance(lebar, tinggi, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(img));
        } else {
            lblLogo.setText("Logo Tidak Ditemukan");
            lblLogo.setIcon(null);
        }
    }

    // Metode untuk memuat semua nama file gambar kendaraan dari database
    private void loadAllVehicleImagePaths() {
        Connection conn = null;
        allVehicleImagePaths.clear();

        try {
            koneksi db = new koneksi();
            conn = db.connect();
            if (conn == null) {
                return;
            }
            String sql = "SELECT gambar FROM kendaraan WHERE gambar IS NOT NULL AND gambar != ''";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String imageName = rs.getString("gambar");
                File imageFile = new File("src/vehicles/" + imageName);
                if (imageFile.exists()) {
                    allVehicleImagePaths.add(imageName);
                }
            }
            rs.close();
            pst.close();
            Collections.shuffle(allVehicleImagePaths);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error memuat daftar gambar kendaraan: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
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

    // Metode untuk memulai slide show
    private void startImageSlideShow() {
        if (allVehicleImagePaths.isEmpty()) {
            lblRandomVehicle1.setText("Tidak Ada Gambar Kendaraan");
            lblRandomVehicle1.setIcon(null);
            lblRandomVehicle2.setText("");
            lblRandomVehicle2.setIcon(null);
            lblRandomVehicle3.setText("");
            lblRandomVehicle3.setIcon(null);
            return;
        }

        displayCurrentSlideImages();
        slideTimer = new Timer(SLIDE_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slideStartOffset++;
                if (slideStartOffset >= allVehicleImagePaths.size()) {
                    slideStartOffset = 0;
                }
                displayCurrentSlideImages();
            }
        });
        slideTimer.start();
    }

    private void displayCurrentSlideImages() {
        int totalImages = allVehicleImagePaths.size();
        if (totalImages > 0) {
            displayImageInSpecificLabel(lblRandomVehicle1, allVehicleImagePaths.get(slideStartOffset % totalImages));
        } else {
            lblRandomVehicle1.setText("N/A");
            lblRandomVehicle1.setIcon(null);
        }

        if (totalImages > 1) {
            displayImageInSpecificLabel(lblRandomVehicle2, allVehicleImagePaths.get((slideStartOffset + 1) % totalImages));
        } else {
            lblRandomVehicle2.setText("N/A");
            lblRandomVehicle2.setIcon(null);
        }

        if (totalImages > 2) {
            displayImageInSpecificLabel(lblRandomVehicle3, allVehicleImagePaths.get((slideStartOffset + 2) % totalImages));
        } else {
            lblRandomVehicle3.setText("N/A");
            lblRandomVehicle3.setIcon(null);
        }
    }

    // Metode helper untuk menampilkan gambar di JLabel tertentu
    private void displayImageInSpecificLabel(JLabel label, String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            String imagePath = "src/vehicles/" + fileName;
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                int lebar = label.getWidth();
                int tinggi = label.getHeight();

                if (lebar == 0) {
                    lebar = 275;
                }
                if (tinggi == 0) {
                    tinggi = 224;
                }

                Image img = icon.getImage().getScaledInstance(lebar, tinggi, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
                label.setText("");
            } else {
                label.setIcon(null);
                label.setText("Gambar Tdk Ditemukan");
            }
        } else {
            label.setIcon(null);
            label.setText("Tidak Ada Gambar");
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
        lblLogo = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblRandomVehicle1 = new javax.swing.JLabel();
        lblRandomVehicle3 = new javax.swing.JLabel();
        lblRandomVehicle2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(243, 198, 35));
        jPanel1.setPreferredSize(new java.awt.Dimension(836, 840));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 40)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(16, 55, 92));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("SELAMAT DATANG DI YaRental");
        jLabel1.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(16, 55, 92)));

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/YaRental_200x200.png"))); // NOI18N
        lblLogo.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(16, 55, 92), 3, true));

        jLabel2.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(16, 55, 92));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Jl. Raya Tengah No.80, RT.6/RW.1, Gedong, Kec. Ps.Rebo, Kota Jakarta Timur, DKI Jakarta 13760, Indonesia");
        jLabel2.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(16, 55, 92)));

        jPanel2.setBackground(new java.awt.Color(16, 55, 92));
        jPanel2.setPreferredSize(new java.awt.Dimension(847, 224));

        lblRandomVehicle1.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(16, 55, 92)));

        lblRandomVehicle3.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(16, 55, 92)));

        lblRandomVehicle2.setBackground(new java.awt.Color(16, 55, 92));
        lblRandomVehicle2.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(16, 55, 92)));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(lblRandomVehicle1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRandomVehicle3, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRandomVehicle2, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblRandomVehicle2, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
            .addComponent(lblRandomVehicle3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblRandomVehicle1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(243, 198, 35));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 3, 0, new java.awt.Color(16, 55, 92)));

        jButton1.setBackground(new java.awt.Color(16, 55, 92));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("MASUK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(357, 357, 357))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(16, 55, 92));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Buka Pukul 08.00 - 17.00 WIB | Telp : 021 - 12341234 | IG : @YaRental");
        jLabel3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 3, 0, new java.awt.Color(16, 55, 92)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(304, 304, 304))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 837, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 837, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        login loginForm = new login();
        loginForm.setVisible(true);
        loginForm.setLocationRelativeTo(null);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(landing_page.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(landing_page.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(landing_page.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(landing_page.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new landing_page().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblRandomVehicle1;
    private javax.swing.JLabel lblRandomVehicle2;
    private javax.swing.JLabel lblRandomVehicle3;
    // End of variables declaration//GEN-END:variables
}
