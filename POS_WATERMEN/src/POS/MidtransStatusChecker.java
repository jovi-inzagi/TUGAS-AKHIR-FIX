package POS;

/**
 *
 * @author Jovii
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class MidtransStatusChecker {

    private static final String MIDTRANS_SERVER_KEY = "SB-Mid-server-CVx-kkRFqRDlNiuYxvz1lTCq"; // Server Key dari akun midtrans saya
    private static final String MIDTRANS_API_URL = "https://api.sandbox.midtrans.com/v2/";

    public static String checkStatus(String orderId) {
        try
        {
            URL url = new URL(MIDTRANS_API_URL + orderId + "/status");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            String auth = MIDTRANS_SERVER_KEY + ":";
            String encodedAuth = javax.xml.bind.DatatypeConverter.printBase64Binary(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                {
                    response.append(line);
                }
                br.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String transactionStatus = jsonResponse.optString("transaction_status", "");

                // Jika pembayaran berhasil, update database
                if ("settlement".equals(transactionStatus) || "capture".equals(transactionStatus))
                {
                    updatePaymentStatus(orderId, "Lunas");
                }

                return transactionStatus;
            } else
            {
                System.out.println("GET request failed with response code: " + responseCode);
                return "error";
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return "error";
        }
    }

    private static void updatePaymentStatus(String orderId, String status) {
        try
        {
            java.sql.Connection conn = koneksi.BukaKoneksi();
            String sql = "UPDATE transaksi SET status = ? WHERE id_transaksi = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, status);
            pst.setString(2, orderId);
            pst.executeUpdate();
            pst.close();
            conn.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
