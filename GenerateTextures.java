import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

public class GenerateTextures {
    public static void main(String[] args) throws Exception {
        String jarPath = "C:\\Users\\salo2\\.gradle\\caches\\neoformruntime\\artifacts\\minecraft_1.21.1_client.jar";
        String assetsItemDir = "src\\main\\resources\\assets\\beer\\textures\\item";
        String assetsEntityDir = "src\\main\\resources\\assets\\beer\\textures\\entity\\fish";
        String modelsItemDir = "src\\main\\resources\\assets\\beer\\models\\item";

        new File(assetsItemDir).mkdirs();
        new File(assetsEntityDir).mkdirs();
        new File(modelsItemDir).mkdirs();

        String[][] fishes = {
            {"cod", "cod.png", "cod.png"},
            {"salmon", "salmon.png", "salmon.png"},
            {"pufferfish", "pufferfish.png", "pufferfish.png"},
            {"tropical_fish", "tropical_fish.png", "tropical_a.png"}
        };

        try (ZipFile zipFile = new ZipFile(jarPath)) {
            for (String[] fish : fishes) {
                String name = fish[0];
                String itemFile = fish[1];
                String entityFile = fish[2];

                // Process item
                ZipEntry itemEntry = zipFile.getEntry("assets/minecraft/textures/item/" + itemFile);
                if (itemEntry != null) {
                    try (InputStream is = zipFile.getInputStream(itemEntry)) {
                        BufferedImage img = ImageIO.read(is);
                        ImageIO.write(processImage(img, "salted"), "PNG", new File(assetsItemDir, "salted_" + name + ".png"));
                        ImageIO.write(processImage(img, "dried"), "PNG", new File(assetsItemDir, "dried_" + name + ".png"));
                    }
                }

                // Process entity
                ZipEntry entityEntry = zipFile.getEntry("assets/minecraft/textures/entity/fish/" + entityFile);
                if (entityEntry != null) {
                    try (InputStream is = zipFile.getInputStream(entityEntry)) {
                        BufferedImage img = ImageIO.read(is);
                        ImageIO.write(processImage(img, "salted"), "PNG", new File(assetsEntityDir, "salted_" + entityFile));
                        ImageIO.write(processImage(img, "dried"), "PNG", new File(assetsEntityDir, "dried_" + entityFile));
                    }
                }
            }
        }
        System.out.println("Textures generated successfully.");
    }

    private static BufferedImage processImage(BufferedImage img, String mode) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = img.getRGB(x, y);
                int a = (pixel >> 24) & 0xff;
                if (a == 0) {
                    out.setRGB(x, y, pixel);
                    continue;
                }
                
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                
                if (mode.equals("salted")) {
                    if (Math.random() < 0.3) {
                        r = Math.min(255, r + 150);
                        g = Math.min(255, g + 150);
                        b = Math.min(255, b + 150);
                    } else {
                        r = Math.min(255, r + 80);
                        g = Math.min(255, g + 80);
                        b = Math.min(255, b + 80);
                    }
                } else if (mode.equals("dried")) {
                    r = (int)(r * 0.75);
                    g = (int)(g * 0.65);
                    b = (int)(b * 0.6);
                }
                
                int newPixel = (a << 24) | (r << 16) | (g << 8) | b;
                out.setRGB(x, y, newPixel);
            }
        }
        return out;
    }
}
