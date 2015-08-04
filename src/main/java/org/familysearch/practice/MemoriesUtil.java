package org.familysearch.practice;

import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.UUID;

/**
 * @author Ryan Heaton
 * altered by Tygan Shelton
 * This class downloads an image at the given url and modifies it slightly to create a unique image.
 * This is necessary because FamilySearch uploaded sources must be unique.
 */
public class MemoriesUtil {

  public static DataSource createUniqueImage() throws IOException {
    return createUniqueImage("TweedleDum.jpg");
  }

  public static DataSource createUniqueImage(String urlString) throws IOException {
    //Lifted from http://www.codebeach.com/2008/02/watermarking-images-in-java-servlet.html
    URL url = new URL(urlString);
    Image image = ImageIO.read(url);
    ImageIcon photo = new ImageIcon(image);
    BufferedImage bi = new BufferedImage(photo.getIconWidth(), photo.getIconHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = (Graphics2D) bi.getGraphics();
    g2d.drawImage(photo.getImage(), 0, 0, null);
    AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    g2d.setComposite(alpha);
    g2d.setColor(Color.WHITE);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2d.setFont(new Font("Arial", Font.BOLD, 10));
    FontMetrics fontMetrics = g2d.getFontMetrics();
    String watermark = String.format("%s at %s", UUID.randomUUID(), System.currentTimeMillis());
    Rectangle2D rect = fontMetrics.getStringBounds(watermark, g2d);
    g2d.drawString(watermark, (photo.getIconWidth() - (int) rect.getWidth()) / 2, (photo.getIconHeight() - (int) rect.getHeight()) / 2);
    g2d.dispose();

    final ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
    ImageIO.write(bi, "jpg", imageOut);
    imageOut.close();
    return new DataSource() {

      public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(imageOut.toByteArray());
      }

      public OutputStream getOutputStream() throws IOException {
        return null;
      }

      public String getContentType() {
        return "image/jpg";
      }

      public String getName() {
        return "face.jpg";
      }
    };
  }

  public static DataSource createUniqueStory() throws IOException {
    final UUID randomness = UUID.randomUUID();
    return new DataSource() {

      public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(("Here is a bunch of text that is intended to tell a story about " + randomness + ".").getBytes("utf-8"));
      }

      public OutputStream getOutputStream() throws IOException {
        return null;
      }

      public String getContentType() {
        return "text/plain";
      }

      public String getName() {
        return randomness + ".txt";
      }
    };
  }
}