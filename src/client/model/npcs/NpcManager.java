package client.model.npcs;

import client.model.world.WorldMap;
import client.util.Settings;
import client.util.Utils;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class NpcManager {

	// Model cache
	private final Map<Integer, Object3D> models = new HashMap<Integer, Object3D>();
	private final String fileStorage = Settings.getFileStorage(false, "glabtech");

	// Dynamic npcs
	private final List<Npc> npcs = new CopyOnWriteArrayList<Npc>();
	private AtomicInteger uid = new AtomicInteger(1);
	private long lastVisibilityUpdate = 0L;

	private final Object3D warrior; // (paid)
	private final Object3D archer; // (paid)
	private final Object3D wizard; // (paid)
	private final Object3D dragon; // (paid)
	private final Object3D zombie; // (paid)
	private final Object3D ape; // (free)
	private final Object3D goblin; // (free)
	private final Object3D skeleton; // (free)
	private final Object3D protector; // (free)
	private Texture texture;

	private final TextureManager TM = TextureManager.getInstance();

	private void loadModels(Object3D entity, String type, int index) {
		TM.addTexture(type, new Texture(fileStorage + "npc" + File.separator + type + ".jpg"));
		texture = TM.getTexture(type);
		texture.setMipmap(true);
		entity.setTexture(type);
		entity.rotateY((float) Math.PI * 1.5f); // Properly rotate the MD2 model
		entity.rotateMesh();
		entity.compile(true); // Compile dynamic object
		entity.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		models.put(index, entity);

	}
	
	public NpcManager(final World world, final WorldMap terrain) {
		this.warrior = Loader.loadMD2(fileStorage + "npc" + File.separator + "warrior.md2", 0.9f);
		this.archer = Loader.loadMD2(fileStorage + "npc" + File.separator + "archer.md2", 0.9f);
		this.wizard = Loader.loadMD2(fileStorage + "npc" + File.separator + "wizard.md2", 0.9f);
		this.dragon = Loader.loadMD2(fileStorage + "npc" + File.separator + "dragon.md2", .13f);
		this.ape = Loader.loadMD2(fileStorage + "npc" + File.separator + "ape.md2", .20f);
		
		loadModels(warrior, "warrior", 0);
		loadModels(archer, "archer", 1); 
		loadModels(wizard, "wizard", 2); 
		loadModels(dragon, "dragon", 3); 

		TM.addTexture("ape", new Texture(fileStorage + "npc" + File.separator + "ape.jpg"));
		texture = TM.getTexture("ape");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		ape.setTexture("ape");
		ape.rotateY((float) Math.PI * 1.5f); // Properly rotate the MD2 model
		ape.rotateMesh();
		ape.compile(true); // Compile dynamic object
		ape.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		models.put(4, ape);

		TM.addTexture("zombie", new Texture(fileStorage + "npc" + File.separator + "zombie.jpg"));
		this.zombie = Loader.loadMD2(fileStorage + "player" + File.separator + "male.md2", 0.10f);
		texture = TM.getTexture("zombie");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		zombie.setTexture("zombie");
		zombie.rotateY((float) Math.PI * 1.5f); // Properly rotate the MD2 model
		zombie.rotateMesh();
		zombie.compile(true); // Compile dynamic object
		zombie.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		models.put(5, zombie);

		this.goblin = Loader.loadMD2(fileStorage + "npc" + File.separator + "goblin.md2", 0.24f);
		TM.addTexture("goblin", new Texture(fileStorage + "npc" + File.separator + "goblin.png"));
		texture = TM.getTexture("goblin");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		goblin.setTexture("goblin");
		goblin.rotateY((float) Math.PI * 1.5f); // Properly rotate the MD2 model
		goblin.rotateMesh();
		for (int i = 4; i < 16; i++) {
			goblin.getAnimationSequence().remove(i); // unused animations..
		}
		goblin.compile(true); // Compile dynamic object
		goblin.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		models.put(6, goblin);

		this.skeleton = Loader.loadMD2(fileStorage + "npc" + File.separator + "skeleton.md2", 0.36f);
		TM.addTexture("skeleton", new Texture(fileStorage + "npc" + File.separator + "skeleton.jpg"));
		texture = TM.getTexture("skeleton");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		skeleton.setTexture("skeleton");
		skeleton.rotateY((float) Math.PI * 1.5f); // Properly rotate the MD2 model
		skeleton.rotateMesh();
		skeleton.compile(true); // Compile dynamic object
		skeleton.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		models.put(7, skeleton);

		this.protector = Loader.loadMD2(fileStorage + "npc" + File.separator + "protector.md2", 0.38f);
		TM.addTexture("protector", new Texture(fileStorage + "npc" + File.separator + "protector.jpg"));
		texture = TM.getTexture("protector");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		protector.setTexture("protector");
		protector.rotateY((float) Math.PI * 1.5f); // Properly rotate the MD2 model
		protector.rotateMesh();
		protector.compile(true); // Compile dynamic object
		protector.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		models.put(8, protector);

		for (int i = 0; i < models.size(); i++) {
			models.get(i).build();
		}

		/**
		 * Read the npc configuration file.
		 */
		try {
			File file = new File(fileStorage + "config" + File.separator + "npcs.cfg");
			if (!file.exists()) {
				System.out.println("Could not load: " + file.getAbsolutePath());
			}
			StreamTokenizer tokenizer = new StreamTokenizer(new BufferedReader(new FileReader(file)));
			while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
				int type = (int) tokenizer.nval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				int x = (int) tokenizer.nval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				int y = (int) tokenizer.nval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				int z = (int) tokenizer.nval;
				Npc npc = new Npc(getModelByType(type), type, uid.getAndIncrement(), new SimpleVector(x, y, z), world, terrain); // clone the model
				npcs.add(npc);
			}
		} catch (Exception e) {
			System.err.println("Error loading NPCManager");
			e.printStackTrace();
		}
		System.out.println("Loaded " + models.size() + " npc models, and " + npcs.size() + " spawns.");
	}

	/**
	 * Updates the visibility list every 6 seconds (10x per minute)
	 */
	private int numberOfVisibleNpcs = 0;

	public void updateVisibilityList(final Camera camera, final boolean forceUpdate) {
		if (forceUpdate || lastVisibilityUpdate <= System.currentTimeMillis() - Settings.ENTITY_VISIBILITY_UPDATE_DELAY) {
			lastVisibilityUpdate = System.currentTimeMillis();
			numberOfVisibleNpcs = 0;
			for (Npc npc : npcs) {
				if (!Utils.isInRange(Config.farPlane * 1.25f, camera.getPosition(), npc.getTransformedCenter())) {
					npc.setVisibility(false);
				} else {
					npc.setVisibility(true);
					numberOfVisibleNpcs++;
				}
			}
		}
	}

	public Npc getInteractionEntity(final Object3D focusPoint) {
		for (Npc npc : npcs) {
			if (npc.getVisibility() && Utils.isInRange(20f, focusPoint, npc)) {
				return npc;
			}
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
			for (Npc npc : npcs) {
				npc.update(delta);
			}
		}
	}

	public List<Npc> getNpcList() {
		return npcs;
	}

	public Object3D getModelByType(int type) {
		if (!models.containsKey(type)) {
			System.out.println("Npc (type: " + type + ") does not exist!!");
			return null;
		}
		return models.get(type).cloneObject();
	}

	public int getVisibleEntityCount() {
		return numberOfVisibleNpcs;
	}

}
