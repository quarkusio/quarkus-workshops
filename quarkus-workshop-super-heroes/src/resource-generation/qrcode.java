///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS com.google.zxing:core:3.4.0
//DEPS com.google.zxing:javase:3.4.0

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * To invoke this, run jbang https://github
 * .com/quarkusio/quarkus-workshops/blob/main/quarkus-workshop-super-heroes/docs/src/resource
 * -generation/qrcode.java https://shattereddisk.github.io/rickroll/ mycode.png
 */
class qrcode {

    // We can do either 1color or rgb
    static final String LOGO_URL = "https://design.jboss" +
        ".org/quarkus/logo/final/PNG/quarkus_icon_1color_128px_default.png";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("No arguments to encode in the QR code");
            System.exit(1);
        } else {
            String text = args[0];
            String filePath = args[1];
            int width = 640;
            writeQrCode(text, filePath, width);
        }
    }

    private static void writeQrCode(String text, String filePath, int width) throws Exception {
        // Specify the error correction, to allow the QR code to tolerate errors, such as
        // a great big logo plunked in the middle
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        int height = width;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width,
            height, hints);
        // Load QR image
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, getMatrixConfig());

        // Initialize combined image
        BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) combined.getGraphics();

        // Write QR code to new image at position 0/0
        g.drawImage(qrImage, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        try {
            // Load logo image
            BufferedImage overlay = getOverlay(LOGO_URL);

            // Calculate the delta height and width between QR code and the logo
            // Note that we don't do any scaling, so the sizes need to kind of
            // work together without obscuring too much logo
            int deltaHeight = qrImage.getHeight() - overlay.getHeight();
            int deltaWidth = qrImage.getWidth() - overlay.getWidth();
            // The Quarkus logo is transparent and it doesn't look great over the QR code,
            // so blank out some of the middle
            int woffset = Math.round(deltaWidth / 2);
            int hoffset = Math.round(deltaHeight / 2);

            // shrink the blanking panel a bit so it looks better
            int shrinking = 4;

            g.setColor(new Color(255, 255, 255));
            g.fillRect(woffset + 2, hoffset + 2, overlay.getWidth() - 2 * shrinking,
                overlay.getHeight() - 2 * shrinking);

            // Write the logo into the combines image at position (deltaWidth / 2) and
            // (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
            // the same space for the logo to be centered
            g.drawImage(overlay, woffset, hoffset,
                null);
        } catch (IOException e) {
            System.err.println(
                "Could not download " + LOGO_URL + ". Will generate a QR code without an embedded" +
                    " logo.");
        }

        ImageIO.write(combined, "png", new File(filePath));
    }

    private static BufferedImage getOverlay(String logoUrl) throws IOException {
        URL url = new URL(logoUrl);
        return ImageIO.read(url);
    }

    private static MatrixToImageConfig getMatrixConfig() {
        // ARGB Colors
        // Check Colors ENUM
        return new MatrixToImageConfig(Colour.BLACK.getArgb(),
            Colour.WHITE.getArgb());
    }

    private static enum Colour {
        WHITE(0xFFFFFFFF),
        BLACK(0xFF000000);

        private final int argb;

        Colour(final int argb) {
            this.argb = argb;
        }

        public int getArgb() {
            return argb;
        }
    }
}
