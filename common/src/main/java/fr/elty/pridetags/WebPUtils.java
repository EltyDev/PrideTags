package fr.elty.pridetags;

import com.mojang.blaze3d.platform.NativeImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class WebPUtils {

    public static boolean isWebP(InputStream stream) {
        try {
            BufferedInputStream bis = new BufferedInputStream(stream);
            bis.mark(12);
            byte[] header = new byte[12];
            int bytesRead = bis.read(header);
            bis.reset();
            if (bytesRead < 12) return false;
            return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
        } catch (IOException error) {
            error.printStackTrace();
            return false;
        }
    }

    public static NativeImage loadWebP(InputStream stream) throws IOException {
        ImageInputStream imageInput = ImageIO.createImageInputStream(stream);
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("webp");
        if (!readers.hasNext()) throw new RuntimeException("No WebP reader found");
        ImageReader reader = readers.next();
        reader.setInput(imageInput);
        BufferedImage buffered = reader.read(0);
        NativeImage image = new NativeImage(buffered.getWidth(), buffered.getHeight(), true);
        for (int y = 0; y < buffered.getHeight(); y++) {
            for (int x = 0; x < buffered.getWidth(); x++) {
                int argb = buffered.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = (argb) & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                image.setPixelRGBA(x, y, abgr);
            }
        }
        return image;
    }
}
