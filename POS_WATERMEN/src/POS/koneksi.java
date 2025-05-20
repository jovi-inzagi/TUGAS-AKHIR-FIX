package POS;

import java.sql.*;
import javax.swing.JOptionPane;

public class koneksi {

    static Connection koneksi;

    public static Connection BukaKoneksi() {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection cn = DriverManager.getConnection("jdbc:mysql://localhost/watermen", "root", "");
            return cn;
        } catch (ClassNotFoundException | SQLException e)
        {
            JOptionPane.showMessageDialog(null, e);
            return koneksi;
        }
    }
}
