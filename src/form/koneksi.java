package form;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class koneksi {
public static Connection koneksi;
public Connection connect(){
    try{
        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("");
    }
    catch(ClassNotFoundException ex){
        System.out.println("gagal connect"+ex);
    }
    String url= "jdbc:mysql://localhost:3306/kendaraan_sewa";
    try{
        koneksi=DriverManager.getConnection(url,"root","");
        System.out.println("");
    }
    catch (SQLException ex){
        System.out.println("gagal koneksi database"+ex);
    }
    return koneksi;
}
}
