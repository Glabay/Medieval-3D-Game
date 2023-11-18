package client.model.player;

import java.awt.event.KeyEvent;

import org.lwjgl.opengl.Display;

import client.model.Client;
import client.model.world.WorldMap;
import client.util.Settings;
import client.util.Utils;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.KeyState;

public class LocalPlayer {

	private final String fileStorage = Settings.getFileStorage(false, "glabtech");
	private final Camera camera;
	public String name;
	public int combatLevel;
	public int skullId;
	public int hintIcon;
	boolean visible;
	public final int[] equipment;
	int skill;
	private float rotationY = 0;
	
	private final SimpleVector ellipsoid = new SimpleVector(4.2f, 9f, 4.2f);
	
	private final float COLLISION_SPHERE_RADIUS = 9f;
	private final float THIRD_PERSON_CAMERA_ZOOM = 90;
	private final float THIRD_PERSON_CAMERA_HEIGHT = 45;
	private final float FIRST_PERSON_CAMERA_ZOOM = -2;
	private final float FIRST_PERSON_CAMERA_HEIGHT = ellipsoid.y; // "eye level"
	private final float DAMPING = 0.1f;
	private final float MINIMUM_SPEED = 1.2f;
	private final float MAXIMUM_SPEED = MINIMUM_SPEED + 1.3f;
	private final float ROTATION_SPEED = 3.2f;
	private boolean firstPerson = false;

	private final SimpleVector ELLIPSOID_RADIUS = new SimpleVector(COLLISION_SPHERE_RADIUS, firstPerson ? FIRST_PERSON_CAMERA_HEIGHT : THIRD_PERSON_CAMERA_HEIGHT / 2F ,COLLISION_SPHERE_RADIUS);

	// potential multi player variables
	private float currentRotation = (float) Math.PI * 1.5f; // Properly rotate the MD2 model
	private float currentSpeed = 0f;

	private boolean forward = false;
	private boolean backward = false;
	private boolean rotateLeft = false;
	private boolean rotateRight = false;
	private boolean moved = false;
	
	private boolean burning = false;
	
	private boolean swimming = false;
	public boolean positionUpdateFlag = false;
	private SimpleVector moveRes = new SimpleVector(0, 0, 0);
	private Matrix playerDirection = new Matrix();
	private SimpleVector tempVector = new SimpleVector();
	private final SimpleVector GRAVITY = new SimpleVector(0, .6f, 0);

	/**
	 * On the fly player model generation:
	 */
	private Object3D focusPoint; // Represents the local player's chosen model!!
	private World world;

	private float colorModifiers[][]; // Stores this player's appearance data (colors: skin tone, shirt, boots, pants, hair, etc)

	// Animation Related
	private int currentAnimation = 1; // idle
	private long lastAnimationUpdate = 0L;
	private float animationIndex;


	public void animate() {
		if (lastAnimationUpdate <= System.currentTimeMillis() - 10) {
			lastAnimationUpdate = System.currentTimeMillis();
			long resetDelay = 500;
			if (currentAnimation == 1) {
				resetDelay = positionUpdateFlag ? resetDelay : 250;
			}
			if (lastAnimationUpdate <= System.currentTimeMillis() - resetDelay) {
				animationIndex = 1;
			} else {
				animationIndex += 0.02f; // increment frame value
				if (animationIndex > 1) {
					animationIndex -= 1;
				}
			}
			focusPoint.animate(animationIndex, currentAnimation);
		}
	}
	
	/**
	 * Animate the Player
	 * 
	 * 1: Stand 
	 * 2: Walk 
	 * 3: Throw 
	 * 4: Been hit 
	 * 5: Death 
	 * 6: Spell 
	 * 7: Pick from floor 
	 * 8: Eat something 
	 * 9: Floating
	 */
	public void setAnimation(int animationId) {
		try {
			if (currentAnimation != animationId) {
				currentAnimation = animationId;
				this.animationIndex = 1;
			}
		} catch(Exception e) {
			System.err.println("AnimationRequestOutOfBounds");
		}
	}

	private final WorldMap terrain;

	public LocalPlayer(final Object3D model, final World world, final Camera camera, final WorldMap terrain, Client client) {
		this.camera = camera;
		this.terrain = terrain;
		this.world = world;
		this.focusPoint = model;
		this.focusPoint.rotateY(rotationY);
		equipment = new int[12];
		if (Display.getWidth() > 1024) {
			camera.setFOV(5f);
		} else if (Display.getWidth() > 800) {
			camera.setFOV(3f);
		} else {
			camera.setFOV(1.85f); // default
		}
		camera.moveCamera(Camera.CAMERA_MOVEOUT, firstPerson ? FIRST_PERSON_CAMERA_ZOOM : THIRD_PERSON_CAMERA_ZOOM);
		camera.moveCamera(Camera.CAMERA_MOVEUP, firstPerson ? FIRST_PERSON_CAMERA_HEIGHT : THIRD_PERSON_CAMERA_HEIGHT);
	}

	public void update(final float delta, final KeyState keyState) {
		SimpleVector camPos = camera.getPosition();
		camPos.add(new SimpleVector(0, firstPerson ? FIRST_PERSON_CAMERA_HEIGHT : THIRD_PERSON_CAMERA_HEIGHT / 2f, 0));
		SimpleVector dir = new SimpleVector(0, 4F, 0);
		dir = world.checkCollisionEllipsoid(camPos, dir, ELLIPSOID_RADIUS, 1);
		camPos.add(new SimpleVector(0, -(firstPerson ? FIRST_PERSON_CAMERA_HEIGHT : THIRD_PERSON_CAMERA_HEIGHT) / 2f, 0));
		dir.x = 0;
		dir.z = 0;
		camPos.add(dir);
		camera.setPosition(camPos);

		moved = false;
		if (keyState.getKeyCode() == KeyEvent.VK_W || keyState.getKeyCode() == KeyEvent.VK_UP) {
			forward = keyState.getState();
		}
		if (keyState.getKeyCode() == KeyEvent.VK_S || keyState.getKeyCode() == KeyEvent.VK_DOWN) {
			backward = keyState.getState();
		}
		if (keyState.getKeyCode() == KeyEvent.VK_A || keyState.getKeyCode() == KeyEvent.VK_LEFT) {
			rotateLeft = keyState.getState();
		}
		if (keyState.getKeyCode() == KeyEvent.VK_D || keyState.getKeyCode() == KeyEvent.VK_RIGHT) {
			rotateRight = keyState.getState();
		}
		if (keyState.getKeyCode() == KeyEvent.VK_F && keyState.getState()) {
			firstPerson = !firstPerson;
		}
		if (forward || backward || rotateLeft || rotateRight) {
			moved = true;
			move(delta);
		}

		if (moveRes.length() > MAXIMUM_SPEED) {
			moveRes.makeEqualLength(new SimpleVector(0, 0, MAXIMUM_SPEED));
		}
		
		if (focusPoint.getTransformedCenter().y > 100 && WorldMap.isInPvP(focusPoint)) {
			setBurning(true);
		}
		if (focusPoint.getTransformedCenter().y > 114 && !WorldMap.isInPvP(focusPoint)) {
			swimming = true;
		}

		if (moved && !swimming) {
			focusPoint.translate(0, -0.025f, 0); // prevent sinking through terrain
		}
		if (moved) {
			setAnimation(2);
		} else if(!moved) {
			setAnimation(1);
		}

		moveRes = focusPoint.checkForCollisionEllipsoid(moveRes, ellipsoid, 8);
		focusPoint.translate(moveRes);

		// go down (gravity)
		if (!swimming) {
			SimpleVector t = new SimpleVector(0, 4, 0);
			t = focusPoint.checkForCollisionEllipsoid(t, ellipsoid, 1);
			focusPoint.translate(t);
		}

		// damping
		if (moveRes.length() > DAMPING) {
			moveRes.makeEqualLength(new SimpleVector(0, 0, DAMPING));
		} else {
			moveRes = new SimpleVector(0, 0, 0);
		}

		// update camera position
		camera.setPositionToCenter(focusPoint);
		camera.align(focusPoint); // align the camera with the player
		focusPoint.setRotationPivot(new SimpleVector(0, 0, 0));
		
		camera.rotateCameraX((float) Math.toRadians(15));
		if (!firstPerson) {
			camera.moveCamera(Camera.CAMERA_MOVEOUT, THIRD_PERSON_CAMERA_ZOOM);
		} else {
			camera.moveCamera(Camera.CAMERA_MOVEUP, FIRST_PERSON_CAMERA_HEIGHT); // force specific camera height
			camera.moveCamera(Camera.CAMERA_MOVEIN, FIRST_PERSON_CAMERA_ZOOM); // move orb to the front of the player's face
		}

	}
	
	public void move(float delta) {
		if (delta == 0) {
			return;
		}
		if (forward) {
			if (currentSpeed < MAXIMUM_SPEED) {
				currentSpeed += 0.002f;
				if (currentSpeed > MAXIMUM_SPEED) {
					currentSpeed = MAXIMUM_SPEED;
				}
			}
			moveForward();
		} else if (backward) {
			moveBackward();
		} else {
			currentSpeed = 0;
			setAnimation(1);
		}
		if (rotateLeft) {
			rotateLeft(firstPerson);
		}
		if (rotateRight) {
			rotateRight(firstPerson);
		}
	}

	public void updateAppearance(final Client client, final boolean female, final float[][] colorModifiers, final World world) {
		this.setColorModifiers(colorModifiers);
		SimpleVector location = new SimpleVector();
		if (focusPoint != null) {
			location = focusPoint.getTransformedCenter();
			//world.removeObject(focusPoint);
			focusPoint = null;
		}
		this.focusPoint = client.generateCharacterModel(female, colorModifiers); // Update gender
		world.addObject(focusPoint);
		focusPoint.setCollisionMode(Object3D.COLLISION_CHECK_SELF);
		focusPoint.compile(true);

		// Stop the player from bouncing around after an apperance update..
		focusPoint.clearTranslation(); // Clear current location
		focusPoint.translate(location); // Teleport

		focusPoint.clearRotation();
		focusPoint.rotateY(currentRotation);
	}

	/**
	 * Teleports the local player anywhere, and executes a map visibility update.
	 */
	public void teleportTo(final SimpleVector destination) {
		focusPoint.clearTranslation(); // Clear current location
		focusPoint.translate(destination); // Teleport
		focusPoint.translate(0, -ellipsoid.y, 0); // Go up, because sometimes we get stuck inside the terrain..??
		Utils.dropEntityToGround(focusPoint, terrain.getMapChunk(focusPoint)); // Clamp to ground
		focusPoint.translate(0, -ellipsoid.y, 0); // Go up, because sometimes we get stuck inside the terrain..??
		terrain.updateMapChunkVisibility(focusPoint); // Update map chunk visibility
	}

	private void applyGravity() {
		SimpleVector t = GRAVITY;
		t = focusPoint.checkForCollisionEllipsoid(t, ellipsoid, 1);
		focusPoint.translate(t);
	}

	public void moveForward() {
		SimpleVector t = focusPoint.getZAxis();
		t.scalarMul(MINIMUM_SPEED / .50f);
		moveRes.add(t);
		tempVector = playerDirection.getZAxis();
		world.checkCameraCollisionEllipsoid(tempVector, ELLIPSOID_RADIUS, MAXIMUM_SPEED, 5);
		positionUpdateFlag = true;
	}

	public void moveBackward() {
		SimpleVector t = focusPoint.getZAxis();
		t.scalarMul(-(MINIMUM_SPEED / .25f));
		moveRes.add(t);
		tempVector = playerDirection.getZAxis();
		world.checkCameraCollisionEllipsoid(tempVector, ELLIPSOID_RADIUS, MAXIMUM_SPEED, 5);
		positionUpdateFlag = true;
	}

	public void rotateLeft(boolean doubleTheSpeed) {
		rotationY += (float) Math.toRadians(-(doubleTheSpeed ? ROTATION_SPEED * 2 : ROTATION_SPEED));
		focusPoint.rotateY((float) Math.toRadians(-(doubleTheSpeed ? ROTATION_SPEED * 2 : ROTATION_SPEED)));
		positionUpdateFlag = true;
	}

	public void rotateRight(boolean doubleTheSpeed) {
		rotationY += (float) Math.toRadians((doubleTheSpeed ? ROTATION_SPEED * 2 : ROTATION_SPEED));
		focusPoint.rotateY((float) Math.toRadians((doubleTheSpeed ? ROTATION_SPEED * 2 : ROTATION_SPEED)));
		positionUpdateFlag = true;
	}

	public void goUp() {
		if (focusPoint.getTransformedCenter().y < -Config.farPlane / 2) {
			return;
		}
		focusPoint.translate(0, -(GRAVITY.y + ellipsoid.y), 0); // go up...
	}

	public int getXTile() {
		return (int) getX() / 16;
	}
	
	public int getYTile() {
		return (int) getZ() / 16;
	}
	
	
	/**
	 * Returns X coordinate in 3D world space.
	 */
	public float getX() {
		return focusPoint.getTransformedCenter().x;
	}

	/**
	 * Returns Y (height) coordinate in 3D world space.
	 */
	public float getY() {
		return focusPoint.getTransformedCenter().y;
	}
	
	/**
	 * Returns Z coordinate in 3D world space.
	 */
	public float getZ() {
		return focusPoint.getTransformedCenter().z;
	}

	/**
	 * Returns the focusPoint instance.
	 * 
	 * @note The instance represents the player's model!
	 */
	public Object3D getFocusPoint() {
		return focusPoint;
	}

	public float getCurrentRotation() {
		return currentRotation;
	}
	
	public String getFileStorage() {
		return fileStorage;
	}

	public boolean isBurning() {
		return burning;
	}

	public void setBurning(boolean burning) {
		this.burning = burning;
	}

	public float[][] getColorModifiers() {
		return colorModifiers;
	}

	public void setColorModifiers(float colorModifiers[][]) {
		this.colorModifiers = colorModifiers;
	}
}
