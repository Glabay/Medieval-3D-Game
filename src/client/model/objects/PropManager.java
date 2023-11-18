package client.model.objects;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import client.model.world.WorldMap;
import client.util.Settings;
import client.util.Utils;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

/**
 * Automated static prop management system.
 *
 */
public class PropManager {

	// Model cache
	private final static Map<Integer, Object3D> models = new HashMap<Integer, Object3D>();
	private final String fileStorage = Settings.getFileStorage(false, "glabtech");

	// Static props
	private final List<Prop> props = new CopyOnWriteArrayList<Prop>();
	private static AtomicInteger uid = new AtomicInteger(1);

	private long lastVisibilityUpdate = System.currentTimeMillis();

	// Open source free models
	private final Object3D tree;
	private final Object3D tree2;
	private final Object3D tree3;
	private final Object3D tree4;
	private final Object3D firepit;
	private final Object3D house;
	private final Object3D gold;
	private final Object3D shop;
	private final Object3D shed1;
	private final Object3D castle;
	private final Object3D castle2;
	private final Object3D boulder;
	private final Object3D tree5;
	private final Object3D tree6;
	private final Object3D bed;
	private final Object3D log;
	private final Object3D grave1;
	private final Object3D grave2;
	private final Object3D shed2;
	private final Object3D shed3;
	private final Object3D townhouse;

	// Some game props (paid)
	private final Object3D divider; // paid
	private final Object3D lair;
	private final Object3D chest;
	private final Object3D portal;
	private final Object3D crate;
	private final Object3D satchel;
	private final Object3D stool;
	private final Object3D table;
	private final Object3D tower;
	
	private final TextureManager TM = TextureManager.getInstance();

	public PropManager(final World world, final WorldMap terrain) {
		Texture texture; // recycled
		
		this.tree = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator + "tree1.3ds", 4.2f));
		TM.addTexture("tree1", new Texture(fileStorage + "prop" + File.separator + "tree1.jpg"));
		texture = TM.getTexture("tree1");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		tree.rotateX((float) Math.PI);
		tree.rotateMesh();
		tree.setRotationMatrix(new Matrix());
		tree.setTexture("tree1");
		tree.setTransparency(40);
		tree.translate(0, 1, 0); // adjust the height to go down.
		//tree.setSortOffset(-250000);
		models.put(0, tree);

		this.tree2 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator + "tree2.3ds", 3.6f));
		TM.addTexture("tree2", new Texture(fileStorage + "prop" + File.separator + "tree2.jpg"));
		texture = TextureManager.getInstance().getTexture("tree2");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		tree2.rotateX((float) Math.PI);
		tree2.rotateMesh();
		tree2.setRotationMatrix(new Matrix());
		tree2.setTexture("tree2");
		tree2.setTransparency(40);
		tree2.translate(0, 1, 0); // adjust the height to go down.
		//tree2.setSortOffset(-250000);
		models.put(1, tree2);

		this.lair = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"lair.3ds", .004f));
		TM.addTexture("lair", new Texture(fileStorage + "prop" + File.separator +"lair.jpg"));
		texture = TextureManager.getInstance().getTexture("lair");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		lair.setTexture("lair");
		lair.rotateX(-(float) Math.PI / 2);
		lair.rotateY((float) Math.PI * 1.5f); // Properly rotate the model
		lair.translate(0, -8, 0); // adjust the height to go up.
		models.put(2, lair);

		this.chest = Loader.loadMD2(fileStorage + "prop" + File.separator +"chest.md2", 0.08f);
		TM.addTexture("chest", new Texture(fileStorage + "prop" + File.separator +"chest.jpg"));
		texture = TextureManager.getInstance().getTexture("chest");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		chest.setTexture("chest");
		chest.translate(0, -1, 0); // adjust the height to go up.
		models.put(3, chest);

		this.portal = Loader.loadMD2(fileStorage + "prop" + File.separator +"portal.md2", 0.06f);
		TM.addTexture("portal", new Texture(fileStorage + "prop" + File.separator +"portal.jpg"));
		texture = TextureManager.getInstance().getTexture("portal");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		portal.setTexture("portal");
		portal.translate(0, -1, 0); // adjust the height to go up.
		models.put(4, portal);

		this.crate = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"crate.3ds", .12f));
		TM.addTexture("crate", new Texture(fileStorage + "prop" + File.separator +"crate.jpg"));
		texture = TextureManager.getInstance().getTexture("crate");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		crate.setTexture("crate");
		crate.rotateX(-(float) Math.PI / 2);
		models.put(5, crate);

		this.shed1 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"shed1.3ds", 8f));
		TM.addTexture("shed1", new Texture(fileStorage + "prop" + File.separator +"shed1.jpg"));
		texture = TextureManager.getInstance().getTexture("shed1");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		shed1.setTexture("shed1");
		shed1.rotateX((float) Math.toRadians(180)); // correct model rotation
		shed1.rotateMesh();
		shed1.setRotationMatrix(new Matrix());
		shed1.setCulling(false); // show internals
		models.put(6, shed1);
		
		this.satchel = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"satchel.3ds", 2f));
		TM.addTexture("satchel", new Texture(fileStorage + "prop" + File.separator +"satchel.jpg"));
		texture = TextureManager.getInstance().getTexture("satchel");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		satchel.setTexture("satchel");
		satchel.rotateX(-(float) Math.PI / 2);
		models.put(7, satchel);

		this.shop = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"shop.3ds", 1.1f));
		TM.addTexture("shop", new Texture(fileStorage + "prop" + File.separator +"shop.jpg"));
		texture = TextureManager.getInstance().getTexture("shop");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		shop.setTexture("shop");
		shop.rotateX(-(float) Math.PI / 2);
		shop.rotateY((float) Math.PI * 1.5f); // rotate sideways..
		shop.translate(0, -45f, 0); // adjust the height to go up.
		models.put(8, shop);

		this.castle = Object3D.mergeAll(Loader.loadOBJ(fileStorage + "prop" + File.separator +"castle.obj", null, .10f));
		TM.addTexture("castle", new Texture(fileStorage + "prop" + File.separator +"castle.jpg"));
		texture = TextureManager.getInstance().getTexture("castle");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		castle.setTexture("castle");
		castle.rotateX((float) Math.PI); // model was upside down, we fix that here.
		castle.rotateY((float) Math.PI * 1.5f); // rotate sideways..
		castle.rotateMesh();
		castle.clearRotation();
		//castle.compileAndStrip();
		//castle.setCulling(false); // show internals
		castle.setAdditionalColor(Color.DARK_GRAY); // correct empty space
		models.put(9, castle);

		this.stool = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"stool.3ds", 0.20f));
		TM.addTexture("stool", new Texture(fileStorage + "prop" + File.separator +"stool.jpg"));
		texture = TextureManager.getInstance().getTexture("stool");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		stool.setTexture("stool");
		stool.rotateX(-(float) Math.PI / 2);
		stool.translate(0, -4f, 0); // adjust the height to go up.
		models.put(10, stool);

		this.table = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"table.3ds", 0.20f));
		TM.addTexture("table", new Texture(fileStorage + "prop" + File.separator +"table.jpg"));
		texture = TextureManager.getInstance().getTexture("table");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		table.setTexture("table");
		table.rotateX(-(float) Math.PI / 2);
		table.translate(0, -6f, 0); // adjust the height to go up.
		models.put(11, table);

		TM.addTexture("tree3", new Texture(fileStorage + "prop" + File.separator +"tree3.jpg"));
		tree3 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"tree3.3ds", 14f));
		texture = TextureManager.getInstance().getTexture("tree3");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		tree3.setTexture("tree3");
		tree3.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		tree3.rotateMesh();
		tree3.setRotationMatrix(new Matrix());
		tree3.setTransparency(40);
		models.put(12, tree3);

		TM.addTexture("tree4", new Texture(fileStorage + "prop" + File.separator +"tree4.jpg"));
		tree4 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"tree4.3ds", 14f));
		texture = TextureManager.getInstance().getTexture("tree4");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		tree4.setTexture("tree4");
		tree4.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		tree4.rotateMesh();
		tree4.setRotationMatrix(new Matrix());
		tree4.setTransparency(40);
		models.put(13, tree4);
		
		this.divider = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"divider.3ds", 1f));
		TM.addTexture("divider", new Texture(fileStorage + "prop" + File.separator +"divider.jpg"));
		texture = TextureManager.getInstance().getTexture("divider");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		divider.setTexture("divider");
		divider.rotateX((float) (-.5 * Math.PI)); // correct model rotation
		divider.rotateMesh();
		divider.setRotationMatrix(new Matrix());
		models.put(14, divider);

		this.firepit = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"firepit.3ds", 4f));
		TM.addTexture("firepit", new Texture(fileStorage + "prop" + File.separator +"firepit.jpg"));
		texture = TextureManager.getInstance().getTexture("firepit");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		firepit.setTexture("firepit");
		firepit.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		firepit.rotateMesh();
		firepit.setRotationMatrix(new Matrix());
		models.put(15, firepit);

		this.house = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"house.3ds", 0.22f));
		TM.addTexture("house", new Texture(fileStorage + "prop" + File.separator +"house.jpg"));
		texture = TextureManager.getInstance().getTexture("house");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		house.rotateX((float) (-.5 * Math.PI)); // correct model rotation
		house.rotateMesh();
		house.setRotationMatrix(new Matrix());
		house.setTexture("house");
		house.setCulling(false); // show internals
		house.setAdditionalColor(Color.GRAY); // repair black space
		house.clearTranslation();
		models.put(16, house);

		this.gold = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"gold.3ds", .020f));
		TM.addTexture("gold", new Texture(1, 1, new Color(0xf4ce00)));
		gold.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		gold.rotateMesh();
		gold.setRotationMatrix(new Matrix());
		gold.setTexture("gold");
		models.put(17, gold);
		
		this.boulder = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"boulder.3ds", 2.5f));
		TM.addTexture("boulder", new Texture(fileStorage + "prop" + File.separator +"boulder.jpg"));
		texture = TextureManager.getInstance().getTexture("house");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		//boulder.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		//boulder.rotateMesh();
		//boulder.setRotationMatrix(new Matrix());
		boulder.setTexture("boulder");
		models.put(18, boulder);

		TM.addTexture("tree5", new Texture(fileStorage + "prop" + File.separator +"tree5.jpg"));
		tree5 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"tree5.3ds", 14f));
		texture = TextureManager.getInstance().getTexture("tree5");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		tree5.setTexture("tree5");
		tree5.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		tree5.rotateMesh();
		tree5.setRotationMatrix(new Matrix());
		tree5.setTransparency(40);
		models.put(19, tree5);

		TM.addTexture("tree6", new Texture(fileStorage + "prop" + File.separator +"tree6.jpg"));
		tree6 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"tree6.3ds", 14f));
		texture = TextureManager.getInstance().getTexture("tree6");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		tree6.setTexture("tree6");
		tree6.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		tree6.rotateMesh();
		tree6.setRotationMatrix(new Matrix());
		tree6.setTransparency(40);
		models.put(20, tree6);

		this.bed = Object3D.mergeAll(Loader.loadOBJ(fileStorage + "prop" + File.separator +"bed.obj", null, 1f));
		TM.addTexture("bed", new Texture(fileStorage + "prop" + File.separator +"bed.jpg"));
		texture = TextureManager.getInstance().getTexture("bed");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		bed.setTexture("bed");
		bed.rotateX((float) Math.PI); // model was upside down, we fix that here.
		bed.rotateY((float) Math.PI * 1.5f); // rotate sideways..
		bed.rotateMesh();
		bed.clearRotation();
		models.put(21, bed);

		TM.addTexture("log", new Texture(fileStorage + "prop" + File.separator +"log.jpg"));
		log = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"log.3ds", 14f));
		texture = TextureManager.getInstance().getTexture("log");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		log.setTexture("log");
		log.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		log.rotateMesh();
		log.setRotationMatrix(new Matrix());
		models.put(22, log);

		TM.addTexture("grave", new Texture(fileStorage + "prop" + File.separator +"grave.jpg"));
		texture = TextureManager.getInstance().getTexture("grave");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);

		grave1 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"grave1.3ds", .36f));
		grave1.setTexture("grave");
		grave1.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		grave1.rotateMesh();
		grave1.setRotationMatrix(new Matrix());
		models.put(23, grave1);

		grave2 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"grave2.3ds", .38f));
		grave2.setTexture("grave");
		grave2.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		grave2.rotateMesh();
		grave2.setRotationMatrix(new Matrix());
		models.put(24, grave2);

		this.shed2 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"shed2.3ds", 0.140f));
		TM.addTexture("shed2", new Texture(fileStorage + "prop" + File.separator +"shed2.jpg"));
		texture = TextureManager.getInstance().getTexture("shed2");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		shed2.setTexture("shed2");
		shed2.rotateX((float) (-.5 * Math.PI)); // correct the rotation
		shed2.rotateMesh();
		shed2.setRotationMatrix(new Matrix());
		models.put(25, shed2);

		this.shed3 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"shed3.3ds", 6.5f));
		TM.addTexture("shed3", new Texture(fileStorage + "prop" + File.separator +"shed3.jpg"));
		texture = TextureManager.getInstance().getTexture("shed3");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		shed3.setTexture("shed3");
		shed3.rotateX(-(float) Math.toRadians(180)); // correct model rotation
		shed3.rotateMesh();
		shed3.setRotationMatrix(new Matrix());
		shed3.translate(0, 200, 0);
		models.put(26, shed3);

		this.townhouse = Object3D.mergeAll(Loader.loadOBJ(fileStorage + "prop" + File.separator +"townhouse.obj", null, 10f));
		TM.addTexture("townhouse", new Texture(fileStorage + "prop" + File.separator +"townhouse.jpg"));
		//t = TextureManager.getInstance().getTexture("townhouse");
		//t.setMipmap(!Settings.PERFORMANCE_MODE);
		townhouse.setTexture("townhouse");
		townhouse.rotateX(-(float) Math.toRadians(180)); // correct model rotation
		townhouse.rotateMesh();
		townhouse.setRotationMatrix(new Matrix());
		townhouse.setCulling(false); // show internals
		townhouse.setAdditionalColor(Color.WHITE); // repair black space
		models.put(27, townhouse);

		this.tower = Object3D.mergeAll(Loader.loadOBJ(fileStorage + "prop" + File.separator +"tower.obj", null, .50f));
		TM.addTexture("tower", new Texture(fileStorage + "prop" + File.separator +"tower.png"));
		texture = TextureManager.getInstance().getTexture("tower");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		tower.setTexture("tower");
		tower.rotateX(-(float) Math.toRadians(180)); // correct model rotation
		tower.rotateMesh();
		tower.setRotationMatrix(new Matrix());
		tower.translate(0, 1, 0);
		models.put(28, tower);

		this.castle2 = Object3D.mergeAll(Loader.load3DS(fileStorage + "prop" + File.separator +"castle.3ds", 25f));
		TM.addTexture("castle2", new Texture(fileStorage + "prop" + File.separator +"castle.jpg"));
		texture = TextureManager.getInstance().getTexture("castle2");
		texture.setMipmap(!Settings.PERFORMANCE_MODE);
		castle2.setTexture("castle2");
		castle2.rotateX(-(float) Math.PI /2);
		castle2.rotateY((float) Math.PI * 1.5f);
		castle2.rotateMesh();
		castle2.clearRotation();
		models.put(29, castle2);

		for (int i = 0; i < models.size(); i++) {
			models.get(i).build();
			models.get(i).compile();
			models.get(i).setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		}
		
		/**
		 * Read the prop configuration file.
		 */
		Prop prop = null;

		// wilderness barriar (goes east)
		for (int i = 1; i < 9; i++) {
			prop = new Prop(getModelByType(14), 14, uid.getAndIncrement(), new SimpleVector(9510 - (i * 151), 91, 9050), world, terrain);
		}
		// wilderness barriar (goes west)
		for (int i = 0; i < 15; i++) {
			prop = new Prop(getModelByType(14), 14, uid.getAndIncrement(), new SimpleVector(9620 + (i * 151), 91, 9050), world, terrain);
		}
		try {
			File file = new File(fileStorage + "config" + File.separator + "props.cfg");
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
				prop = new Prop(getModelByType(type), type, uid.getAndIncrement(), new SimpleVector(x, y, z), world, terrain); // clone the model
				props.add(prop);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Loaded " + models.size() + " prop models, and " + props.size() + " spawns.");
	}

	/**
	 * Cycle through the prop list, and update each one.
	 * @note Processes on a 36ms delay
	 */
	private long lastCycle = 0L;
	public void cycle(final float delta) {
		if (lastCycle <= System.currentTimeMillis() - 36) {
			lastCycle = System.currentTimeMillis();
			for (Prop prop : props) {
				prop.update(delta);
			}
		}
	}

	/**
	 * Cycle through the prop list, and update the visibility of each one, based on distance.
	 * @note Processes on a 6 second delay (10x per minute)
	 */
	private int numberOfVisibleProps = 0;
	
	public void updateVisibilityList(final Camera camera, final boolean forceUpdate) {
		if (forceUpdate || lastVisibilityUpdate <= System.currentTimeMillis() - Settings.ENTITY_VISIBILITY_UPDATE_DELAY) {
			lastVisibilityUpdate = System.currentTimeMillis();
			numberOfVisibleProps = 0;
			for (Prop prop : props) {
				if (!Utils.isInRange(Config.farPlane * 1.25f, camera.getPosition(), prop.getTransformedCenter())) {
					prop.setVisibility(false);
				} else {
					prop.setVisibility(true);
					numberOfVisibleProps++;
				}
			}
		}
	}

	public Prop getInteractionEntity(final Object3D focusPoint) {
		for (Prop prop : props) {
			if (prop.getVisibility() && Utils.isInRange(20f, focusPoint, prop)) {
				return prop;
			}
		}
		return null;
	}

	public static void spawnProp(int objID, float x, float y, float z, World world, WorldMap terrain) {
		new Prop(getModelByType(objID), objID, getUid().getAndIncrement(), new SimpleVector(x, y, z), world, terrain);
	}
	
	public static Object3D getModelByType(int type) {
		if (!models.containsKey(type)) {
			System.out.println("Prop (type: "+type+") does not exist!!");
			return null;
		}
		return models.get(type).cloneObject();
	}

	public int getVisibleEntityCount() {
		return numberOfVisibleProps;
	}

	public static AtomicInteger getUid() {
		return uid;
	}

}