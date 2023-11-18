package client.util;

import java.awt.Font;

import client.engine.Engine;
import client.twod.GLFont;

import com.threed.jpct.Config;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class Utils {

	

	public static final GLFont NANO_FONT = new GLFont(new Font("Consoles", Font.PLAIN, 10));
	public static final GLFont GAME_FONT = new GLFont(new Font("Suego UI", Font.BOLD, 14));
	public static final GLFont INTERFACE_FONT = new GLFont(new Font("Suego UI", Font.PLAIN, 18));
	
	private static final SimpleVector drop = new SimpleVector(0, 0.9f, 0);

    /**
     * Drops an entity object on to the ground.
     */
    public static void dropEntityToGround(Object3D entity, Object3D mapChunk) {
        boolean falling = !isWithinHeight(entity, mapChunk);
        do {
            entity.translate(drop);
            if (entity.getTransformedCenter().y > 185) {
                System.out.println("A game object has fallen through the terrain! x: " + entity.getTransformedCenter().x + ", y:" + entity.getTransformedCenter().y + " z:" + entity.getTransformedCenter().z);
                System.out.println("Terminating application to prevent software crash..");
                Engine.terminate();
                return;
            }
            falling = !isWithinHeight(entity, mapChunk);
        } while (falling);
    }

    /**
     * Check if two vectors are within X world units of eachother
     */
    private static boolean isWithinHeight(Object3D entity, Object3D mapChunk) {
        float f = mapChunk.calcMinDistance(entity.getTransformedCenter(), drop, Config.farPlane / 2);
        return f == Object3D.COLLISION_NONE;
    }

    /**
     * Check if two objects are within X world units of eachother
     */
    public static boolean isInRange(float distance, Object3D source, Object3D target) {
        return isInRange(distance, source.getTransformedCenter(), target.getTransformedCenter());
    }

    /**
     * Check if two vectors are within X world units of eachother
     */
    public static boolean isInRange(float distance, SimpleVector source, SimpleVector target) {
        SimpleVector p = new SimpleVector(source);
        SimpleVector t = new SimpleVector(target);
        p.y = 0;
        t.y = 0;
        return p.distance(t) <= distance;
    }

    /**
     * Makes an object face another object.
     */
    public static void lookAt(Object3D source, Object3D target) {
        SimpleVector direction = new SimpleVector(target.getTransformedCenter().calcSub(source.getTransformedCenter())).normalize();
        Matrix rotationMatrix = new Matrix(direction.getRotationMatrix());
        source.setRotationMatrix(rotationMatrix);
    }

}
