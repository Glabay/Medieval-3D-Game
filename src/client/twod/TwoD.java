package client.twod;

import com.threed.jpct.FrameBuffer;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * 2D utilities that make rendering text and images a whole lot easier.
 */
public class TwoD {

    // 2D data
    private TexturePack texturePack;
    private String spriteIndex[];

    public TwoD(final String fileStorage) {
        try {
            this.texturePack = new TexturePack();
            this.spriteIndex = getSpriteIndex(fileStorage);
            for (String spriteIndex1 : spriteIndex) {
                try {
                    File file = new File(fileStorage + "2D" + File.separator + spriteIndex1);
                    texturePack.addImage(ImageIO.read(file));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Unable to pack sprite: " + spriteIndex1);
                    System.exit(0);
                }
            }
            if (spriteIndex.length > 0) {
                texturePack.pack(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private int getSpriteId(String sprite) {
        sprite = sprite.replaceAll(".png", "");
        String cleanSprite;
        for (int i = 0; i < spriteIndex.length; i++) {
            cleanSprite = spriteIndex[i].replaceAll(".png", "");
            if (cleanSprite.equalsIgnoreCase(sprite)) {
                return i;
            }
        }
        System.err.println("Sprite '" + sprite + "' does not exist!");
        System.exit(1);
        return -1;
    }

    public void drawImage(FrameBuffer frameBuffer, String sprite, int x, int y, boolean transparent) {
        try {
            texturePack.blit(frameBuffer, getSpriteId(sprite), x, y, transparent);
        } catch (Exception e) {
            System.err.println("Error drawing sprite: " + sprite);
            System.exit(1);
        }
    }

    private String[] getSpriteIndex(final String assetPath) {
        String path = assetPath + "2D" + File.separator;
        //System.out.println(path);
        File file = new File(path);
        if (file.isDirectory()) {
            File images[] = file.listFiles();
            String sprites[] = new String[images.length];
            String sprite;
            for (int i = 0; i < images.length; i++) {
                sprite = images[i].getName();
                sprite.replaceAll(".png", "");
                sprites[i] = sprite;
            }
            return sprites;
        }
        return null;
    }

}
