package QR;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayInputStream;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class QRISPrinter {

    private static Connection cn = POS.koneksi.BukaKoneksi();
    private static final String MIDTRANS_SERVER_KEY = "SB-Mid-server-CVx-kkRFqRDlNiuYxvz1lTCq"; // Server Key dari akun midtrans
    private static final String MIDTRANS_API_URL = "https://app.sandbox.midtrans.com/snap/v1/transactions";

    public static boolean processSelectedTransaction(String orderId, int amount, String customerName,
            String customerEmail, String customerPhone) throws SQLException {
        try
        {
            if (orderId == null || orderId.trim().isEmpty())
            {
                orderId = "ORDER-" + UUID.randomUUID().toString().substring(0, 8);
            } else
            {
                orderId = orderId + "-" + System.currentTimeMillis();
            }

            JSONObject transactionResult = createQRISTransaction(orderId, amount,
                    customerName, customerEmail, customerPhone);

            if (transactionResult != null)
            {
                String token = transactionResult.getString("token");
                String paymentUrl = "https://app.sandbox.midtrans.com/snap/v4/redirection/" + token + "?force_payment_method=qris";

                BufferedImage qrImage = generateQRCode(paymentUrl, 300, 300);

                if (qrImage != null)
                {
                    int qrId = saveQRCodeToDatabase(qrImage);

                    if (qrId > 0)
                    {
                        showPaymentInfo(orderId, amount, paymentUrl);

                        printQRCodeWithJasper(qrId);

                        try
                        {
                            Desktop.getDesktop().browse(new URI(paymentUrl));
                        } catch (URISyntaxException e)
                        {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }
            }

            JOptionPane.showMessageDialog(null,
                    "Gagal membuat QRIS untuk pembayaran.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (HeadlessException | IOException | JSONException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static JSONObject createQRISTransaction(String orderId, int amount,
            String customerName, String customerEmail, String customerPhone) {
        try
        {
            String firstName = customerName;
            String lastName = "";
            if (customerName.contains(" "))
            {
                String[] nameParts = customerName.split(" ", 2);
                firstName = nameParts[0];
                lastName = nameParts[1];
            }

            JSONObject transactionDetails = new JSONObject();
            transactionDetails.put("order_id", orderId);
            transactionDetails.put("gross_amount", amount);

            JSONObject customerDetails = new JSONObject();
            customerDetails.put("first_name", firstName);
            customerDetails.put("last_name", lastName);
            customerDetails.put("email", customerEmail);
            customerDetails.put("phone", customerPhone);

            JSONObject qrisSpecific = new JSONObject();
            qrisSpecific.put("acquirer", "gopay");
            JSONArray enabledPayments = new JSONArray();
            enabledPayments.put("qris");
            JSONObject qrisOptions = new JSONObject();
            qrisOptions.put("qris", qrisSpecific);

            JSONObject requestBody = new JSONObject();
            requestBody.put("transaction_details", transactionDetails);
            requestBody.put("customer_details", customerDetails);
            requestBody.put("enabled_payments", enabledPayments);
            requestBody.put("payment_options", qrisOptions);
            requestBody.put("payment_type", "qris");

            String jsonBody = requestBody.toString();
            System.out.println("Request Body: " + jsonBody);

            return sendQRISRequest(jsonBody);

        } catch (JSONException | IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject sendQRISRequest(String jsonBody) throws IOException {
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

                    return new JSONObject(response.toString());
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

    private static void showPaymentInfo(String orderId, int amount, String paymentUrl) {
        JTextArea textArea = new JTextArea(
                "ID Pesanan: " + orderId + "\n"
                + "Total Pembayaran: Rp " + amount + "\n"
                + "Metode: QRIS\n"
                + "URL Pembayaran:\n" + paymentUrl);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 150));

        JButton copyButton = new JButton("Salin URL");
        copyButton.addActionListener(e ->
        {
            StringSelection selection = new StringSelection(paymentUrl);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            JOptionPane.showMessageDialog(null, "URL telah disalin ke clipboard.");
        });

        Box box = Box.createVerticalBox();
        box.setAlignmentX(Box.LEFT_ALIGNMENT);
        box.add(scrollPane);
        box.add(Box.createVerticalStrut(10));
        box.add(copyButton);

        JOptionPane.showMessageDialog(null, box, "Info Pembayaran QRIS", JOptionPane.INFORMATION_MESSAGE);
    }

    private static BufferedImage generateQRCode(String content, int width, int height) {
        try
        {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage qrImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            qrImage.createGraphics();

            Graphics2D graphics = (Graphics2D) qrImage.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < width; i++)
            {
                for (int j = 0; j < height; j++)
                {
                    if (bitMatrix.get(i, j))
                    {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            return qrImage;
        } catch (WriterException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static int saveQRCodeToDatabase(BufferedImage qrImage) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int generatedId = -1;

        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage) qrImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            String sql = "INSERT INTO barcode (QR_Image) VALUES (?)";
            pstmt = cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setBytes(1, imageBytes);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected in barcode: " + rowsAffected);

            if (rowsAffected > 0)
            {
                rs = pstmt.getGeneratedKeys();
                if (rs.next())
                {
                    generatedId = rs.getInt(1);
                    System.out.println("QR Code berhasil disimpan dengan ID: " + generatedId);
                } else
                {
                    System.out.println("Tidak bisa mendapatkan ID barcode");
                }
            }

            return generatedId;
        } catch (IOException | SQLException e)
        {
            e.printStackTrace();
            System.err.println("Error dalam saveQRCodeToDatabase: " + e.getMessage());
            throw new SQLException("Gagal menyimpan QR Code ke database: " + e.getMessage());
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (pstmt != null)
                {
                    pstmt.close();
                }
            } catch (Exception e)
            {
                System.err.println("Error saat menutup resources: " + e.getMessage());
            }
        }
    }

    private static void printQRCodeWithJasper(int qrId) {
        try
        {
            PreparedStatement pstmt = cn.prepareStatement(
                    "SELECT * FROM barcode WHERE id_barcode = ?"
            );
            pstmt.setInt(1, qrId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
            {
                byte[] qrImageData = rs.getBytes("QR_Image");

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("id_barcode", qrId);
                parameters.put("Image", new ByteArrayInputStream(qrImageData));

                String reportPath = "src/laporan/PembayaranQRIS.jasper";

                try
                {
                    JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, parameters, cn);

                    JasperViewer.viewReport(jasperPrint, false);

                    JOptionPane.showMessageDialog(null,
                            "QRIS berhasil dibuat dan siap untuk dicetak!",
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (JRException e)
                {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Error saat membuat report: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);

                    // Jika terjadi error, tampilkan QR code dalam dialog
                    showQRCodeDialog(qrImageData);
                }
            } else
            {
                JOptionPane.showMessageDialog(null,
                        "QR Code dengan ID " + qrId + " tidak ditemukan.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            pstmt.close();

        } catch (SQLException ex)
        {
            Logger.getLogger(QRISPrinter.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null,
                    "Error saat mencetak: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showQRCodeDialog(byte[] qrImageData) {
        try
        {
            BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrImageData));

            JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
            JPanel panel = new JPanel();
            panel.add(qrLabel);

            int option = JOptionPane.showConfirmDialog(null,
                    panel, "QRIS Payment", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION)
            {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPrintable(new Printable() {
                    @Override
                    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
                        if (pageIndex > 0)
                        {
                            return Printable.NO_SUCH_PAGE;
                        }

                        Graphics2D g2d = (Graphics2D) g;
                        g2d.translate(pf.getImageableX(), pf.getImageableY());

                        double xScale = pf.getImageableWidth() / qrImage.getWidth();
                        double yScale = pf.getImageableHeight() / qrImage.getHeight();
                        double scale = Math.min(xScale, yScale);

                        g2d.scale(scale, scale);
                        g2d.drawImage(qrImage, 0, 0, null);

                        return Printable.PAGE_EXISTS;
                    }
                });

                if (job.printDialog())
                {
                    job.print();
                }
            }
        } catch (IOException | PrinterException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error saat menampilkan QR code: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean processAndPrintSelectedTransaction(String id_transaksi) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT t.id_transaksi, t.total, t.nama_customer, c.alamat, c.telephone "
                    + "FROM transaksi t "
                    + "JOIN customer c ON t.id_customer = c.id_customer "
                    + "WHERE t.id_transaksi = ?";

            pstmt = cn.prepareStatement(sql);
            pstmt.setString(1, id_transaksi);
            rs = pstmt.executeQuery();

            if (rs.next())
            {
                String orderId = rs.getString("id_transaksi");
                int amount = rs.getInt("total");
                String customerName = rs.getString("nama_customer");
                String customerEmail = rs.getString("alamat"); // Using alamat as email
                String customerPhone = rs.getString("telephone");
                return processSelectedTransaction(orderId, amount, customerName, customerEmail, customerPhone);
            } else
            {
                JOptionPane.showMessageDialog(null,
                        "Transaksi dengan ID " + id_transaksi + " tidak ditemukan.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } finally
        {
            if (rs != null)
            {
                rs.close();
            }
            if (pstmt != null)
            {
                pstmt.close();
            }
        }
    }
}
