package client.model.items;

import client.model.world.WorldMap;
import client.util.Settings;
import client.util.Utils;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Automated ground item management system.
 */
public class GroundItemManager {

    private final World world;
    private final WorldMap terrain;

    private String textures[];

    // Static props
    private List<GroundItem> items = new CopyOnWriteArrayList<GroundItem>();
    private AtomicInteger uid = new AtomicInteger(1);
    private long lastVisibilityUpdate = System.currentTimeMillis();

    public GroundItemManager(final String fileStorage, final World world, final WorldMap terrain) {
        this.world = world;
        this.terrain = terrain;

        /**
         * Load item textures:
         */
        this.textures = getTextureIndex(fileStorage);
        for (int i = 0; i < textures.length; i++) {
            if (!textures[i].equalsIgnoreCase("all")) { // not sure why, but this needed to be here..
               // TextureManager.getInstance().addTexture("item" + i, new Texture(fileStorage + "items" + File.separator + textures[i]));
            }
        }
        System.out.println("Loaded " + textures.length + " items.");
    }

    private String[] getTextureIndex(final String fileStorage) {
        String path = fileStorage + "items" + File.separator;
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

    /**
     * Cycle through the prop list, and update each one.
     *
     * @note Processes on a 36ms delay
     */
    private long lastCycle = 0L;

    public void cycle(final float delta) {
        if (lastCycle <= System.currentTimeMillis() - 36) {
            lastCycle = System.currentTimeMillis();
            for (GroundItem item : items) {
                if (item.isExpired()) {
                    world.removeObject(item);
                    items.remove(item);
                }
            }
        }
    }

    /**
     * Cycle through the prop list, and update the visibility of each one, based on distance.
     *
     * @note Processes on a 6 second delay (10x per minute)
     *
     */
    public void updateVisibilityList(final Camera camera, final boolean forceUpdate) {
        if (forceUpdate || lastVisibilityUpdate <= System.currentTimeMillis() - Settings.ENTITY_VISIBILITY_UPDATE_DELAY) {
            lastVisibilityUpdate = System.currentTimeMillis();
            for (GroundItem item : items) {
                if (!Utils.isInRange(Config.farPlane * 1f, camera.getPosition(), item.getTransformedCenter())) {
                    item.setVisibility(false);
                } else {
                    item.setVisibility(true);
                }
            }
        }
    }

    public void spawnItem(final int type, final SimpleVector location) {
        items.add(new GroundItem(type, uid.getAndIncrement(), location, world, terrain));
    }

    public GroundItem pickUp(final Object3D focusPoint) {
        for (GroundItem item : items) {
            if (item.getVisibility() && Utils.isInRange(10f, focusPoint, item)) {
                return item;
            }
        }
        return null;
    }

    public void spawnRandomItem(final SimpleVector location) {
        spawnItem(new Random().nextInt(textures.length), location);
    }

}
