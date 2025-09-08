package utils;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "your_email@gmail.com";
    private static final String EMAIL_PASSWORD = "your_app_password";

    private static final ConcurrentHashMap<String, PinData> pendingPins = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static class PinData {
        final int pin;
        final long expiryTime;

        PinData(int pin) {
            this.pin = pin;
            this.expiryTime = System.currentTimeMillis() + (10 * 60 * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    public static int sendConfirmationPin(String email, String displayName) throws MessagingException {
        Random random = new Random();
        int pin = 100000 + random.nextInt(900000);

        pendingPins.put(email, new PinData(pin));

        scheduler.schedule(() -> pendingPins.remove(email), 10, TimeUnit.MINUTES);

        String gmailUsername = extractGmailUsername(email);

        sendPinEmail(email, displayName, pin, gmailUsername);

        return pin;
    }

    public static boolean verifyPin(String email, int inputPin) {
        PinData pinData = pendingPins.get(email);
        if (pinData == null || pinData.isExpired()) {
            pendingPins.remove(email);
            return false;
        }

        if (pinData.pin == inputPin) {
            pendingPins.remove(email);
            return true;
        }

        return false;
    }

    public static String extractGmailUsername(String email) {
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return email;
    }

    private static void sendPinEmail(String to, String displayName, int pin, String gmailUsername) throws MessagingException {
        String subject = "Tong - Email Verification Required";

        String htmlMessage = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden;">

                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0; font-size: 28px; font-weight: 300;">Welcome to Tong!</h1>
                        <p style="margin: 10px 0 0 0; opacity: 0.9;">IUTians' communication hub awaits</p>
                    </div>

                    <!-- Content -->
                    <div style="padding: 40px 30px;">
                        <p style="font-size: 16px; margin-bottom: 20px;">Dear <strong>%s</strong> (<em>%s</em>),</p>

                        <p style="margin-bottom: 25px;">Thank you for registering with Tong! To complete your registration & secure your account, please verify your email address using the PIN below:</p>

                        <!-- PIN Box -->
                        <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);">
                            <p style="color: white; margin: 0 0 10px 0; font-size: 14px; text-transform: uppercase; letter-spacing: 1px; opacity: 0.9;">Your Verification PIN</p>
                            <h1 style="color: white; font-size: 40px; margin: 0; letter-spacing: 5px; font-weight: bold; text-shadow: 0 2px 4px rgba(0,0,0,0.3);">%06d</h1>
                        </div>

                        <!-- Instructions -->
                        <div style="background-color: #f8f9ff; border-left: 4px solid #667eea; padding: 20px; margin: 25px 0; border-radius: 0 8px 8px 0;">
                            <h3 style="margin: 0 0 15px 0; color: #667eea; font-size: 16px;">📋 Important Instructions:</h3>
                            <ul style="margin: 0; padding-left: 20px; color: #555;">
                                <li style="margin-bottom: 8px;">This PIN will <strong>expire in 10 minutes</strong>.</li>
                                <li style="margin-bottom: 8px;">Enter this PIN in the verification screen to activate your account.</li>
                                <li style="margin-bottom: 8px;">Keep this PIN confidential and do not share it with anyone.</li>
                                <li>If you didn't request this registration, please ignore this email.</li>
                            </ul>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div style="background-color: #f8f9ff; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e6ff;">
                        <p style="color: #666; font-size: 12px; margin: 0; line-height: 1.4;">
                            This is an automated message from <strong>Tong App</strong>.<br>
                            Please do not reply to this email. This mailbox is not monitored.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, displayName, gmailUsername, pin);

        sendHtmlEmail(to, subject, htmlMessage);
    }

    private static void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        };

        Session session = Session.getInstance(props, authenticator);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME, "Tong App"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("Confirmation PIN sent successfully to: " + to);

        } catch (Exception e) {
            throw new MessagingException("Failed to send confirmation email: " + e.getMessage(), e);
        }
    }

    public static void cleanupExpiredPins() {
        pendingPins.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public static void shutdown() {
        scheduler.shutdown();
    }
}
