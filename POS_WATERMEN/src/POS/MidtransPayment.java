package POS;

/**
 *
 * @author Jovii
 */
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import javax.swing.JOptionPane;
import org.json.JSONException;
import org.json.JSONObject;

public class MidtransPayment {

    private static final String MIDTRANS_SERVER_KEY = "SB-Mid-server-CVx-kkRFqRDlNiuYxvz1lTCq"; // Server Key dari akun midtrans
    private static final String MIDTRANS_API_URL = "https://app.sandbox.midtrans.com/snap/v1/transactions";

    public static boolean processPayment(String orderId, int amount, String customerName,
            String customerEmail, String customerPhone) {

        try
        {
            
          if (orderId == null || orderId.trim().isEmpty()) {
          orderId = "ORDER-" + UUID.randomUUID().toString().substring(0, 8);
          } else {
        
          orderId = orderId + "-" + System.currentTimeMillis();
          }

            JSONObject transactionDetails = new JSONObject();
            transactionDetails.put("order_id", orderId);
            transactionDetails.put("gross_amount", amount);

            String firstName = customerName;
            String lastName = "";
            if (customerName.contains(" "))
            {
                String[] nameParts = customerName.split(" ", 2);
                firstName = nameParts[0];
                lastName = nameParts[1];
            }

            JSONObject customerDetails = new JSONObject();
            customerDetails.put("first_name", firstName);
            customerDetails.put("last_name", lastName);
            customerDetails.put("email", customerEmail);
            customerDetails.put("phone", customerPhone);

            JSONObject requestBody = new JSONObject();
            requestBody.put("transaction_details", transactionDetails);
            requestBody.put("customer_details", customerDetails);

            String jsonBody = requestBody.toString();

            String snapToken = sendMidtransRequest(jsonBody);

            if (snapToken != null)
            {
                String redirectUrl = "https://app.sandbox.midtrans.com/snap/v2/vtweb/" + snapToken;
                openBrowser(redirectUrl);
                return true;
            } else
            {
                JOptionPane.showMessageDialog(null,
                        "Gagal membuat transaksi Midtrans.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

        } catch (HeadlessException | IOException | JSONException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static String sendMidtransRequest(String jsonBody) throws IOException {
        try
        {
            URL url = new URL(MIDTRANS_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");

            String auth = MIDTRANS_SERVER_KEY + ":";
            String encodedAuth = javax.xml.bind.DatatypeConverter.printBase64Binary(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream())
            {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300)
            {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8")))
                {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null)
                    {
                        response.append(responseLine.trim());
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getString("token");
                }
            } else
            {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), "utf-8")))
                {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null)
                    {
                        response.append(responseLine.trim());
                    }
                    System.err.println("Response code: " + responseCode);
                    System.err.println("Error response: " + response.toString());
                }
                return null;
            }
        } catch (IOException | JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static void openBrowser(String url) {
        try
        {
            Desktop.getDesktop().browse(new URI("https://simulator.sandbox.midtrans.com/v2/qris/index"));
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException | IOException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Tidak dapat membuka browser. URL: " + url,
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
