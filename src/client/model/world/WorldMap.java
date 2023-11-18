package client.model.world;

import client.twod.TwoD;
import client.util.Utils;

import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GLSLShader;
import com.threed.jpct.Lights;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.PolygonManager;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureInfo;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.LensFlare;
import com.threed.jpct.util.Light;
import com.threed.jpct.util.SkyBox;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;

public class WorldMap {

	/**
	 * The exact center of the world map.
	 */
	public final static SimpleVector CENTER_OF_THE_UNIVERSE = new SimpleVector(10000, 0, 10000);

	/**
	 * The player's house location.
	 * Where the player should spawn by default.
	 * @note MAKE SURE THIS IS THE CENTER OF THE POH!
	 */
	public final static SimpleVector HOUSE_LOCATION = new SimpleVector(9507.678, 90.9, 9622.343);

	public final static SimpleVector CAVE_ENTRANCE = new SimpleVector(10431.028, 90.9, 8535.057);
	public final static SimpleVector BURNING_ISLAND = new SimpleVector(11523.639, 90.9, 8279.529);
	public final static SimpleVector BURNING_DUNGEON = new SimpleVector(19264.777, 90.9, 19363.05);

	private final World world;
	private SkyBox skybox;
	private LensFlare lensFlare;
	private final boolean LENS_FLARE_ENABLED = false;
	private final Color OPEN_WORLD = new Color(0xd1d3d4);//ebecec);//579ff4);
	private final Color DUNGEON = new Color(0x6b0200);//320003);//Color.BLACK;
	private Color clearColor = OPEN_WORLD;

	private final Object3D worldMapObject;
	private final Object3D dungeonObject;

	/**
	 * Creates a new terrain, with optional random generated foliage using
	 * resources collected from the jPCT community.
	 */
	public WorldMap(final String fileStorage, final World world) {
		this.world = world;
		
		Config.lightMul = 1;
		world.setFoggingMode(World.FOGGING_PER_PIXEL);
		world.setFogging(World.FOGGING_ENABLED);
		world.getLights().setRGBScale(Lights.RGB_SCALE_2X);
		world.setClippingPlanes(1, Config.farPlane);
		
		world.setAmbientLight(30, 30, 30);
		
		TextureManager tm = TextureManager.getInstance();

		/**
		 * 0: "splat demo"
		 * 1: "new terrain"
		 * 2: "realism"
		 */
		final int tileType = 1;

		tm.addTexture("grass", new Texture(fileStorage + "tile" + File.separator + "grass" + tileType + ".jpg"));
		tm.addTexture("sand", new Texture(fileStorage + "tile" + File.separator + "sand" + tileType + ".jpg"));
		tm.addTexture("rocks", new Texture(fileStorage + "tile" + File.separator + "rocks" + tileType + ".jpg"));
		
		Config.maxTextureLayers = 4;
		tm.addTexture("terrain", new Texture(fileStorage + "terrain" + File.separator + "terrain.png"));
		String terrain = fileStorage + "terrain" + File.separator + "map.obj";
		worldMapObject = Object3D.mergeAll(Loader.loadOBJ(terrain, null, .50f));
		worldMapObject.rotateX(-(float) Math.PI / 2f);
		worldMapObject.rotateMesh();
		worldMapObject.clearRotation();
		setTexture(worldMapObject);
		worldMapObject.compile();
		worldMapObject.build();
		GLSLShader shader = new GLSLShader(Loader.loadTextFile(fileStorage + "terrain" + File.separator + "splatter.vert"), Loader.loadTextFile(fileStorage + "terrain" + File.separator + "splatter.frag"));
		shader.setStaticUniform("map0", 0);
		shader.setStaticUniform("map1", 1);
		shader.setStaticUniform("map2", 2);
		shader.setStaticUniform("map3", 3);
		shader.setUniform("splatTransform", new Matrix());
		worldMapObject.setRenderHook(shader);
		worldMapObject.translate(CENTER_OF_THE_UNIVERSE);
		worldMapObject.translate(0, 100, 0); // required for the box-style maps
		worldMapObject.enableLazyTransformations();
		worldMapObject.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		worldMapObject.compileAndStrip();
		world.addObject(worldMapObject);

		tm.addTexture("dungeon_tile", new Texture(fileStorage + "tile" + File.separator + "dungeon.png"));
		String dungeon = fileStorage + "tile" + File.separator + "dungeon.obj";
		dungeonObject = Object3D.mergeAll(Loader.loadOBJ(dungeon, null, 0.25f));
		dungeonObject.rotateX(-(float) Math.PI / 2f);
		dungeonObject.rotateMesh();
		dungeonObject.clearRotation();
		dungeonObject.translate(20000, 0, 20000);
		dungeonObject.translate(0, 100, 0); // required for the box-style maps
		dungeonObject.enableLazyTransformations();
		dungeonObject.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		dungeonObject.setTexture("dungeon_tile");
		PolygonManager pm = dungeonObject.getPolygonManager();
		int end = pm.getMaxPolygonID();
		for (int i = 0; i < end; i++) {
			int t1 = pm.getPolygonTexture(i);
			SimpleVector uv0 = pm.getTextureUV(i, 0);
			SimpleVector uv1 = pm.getTextureUV(i, 1);
			SimpleVector uv2 = pm.getTextureUV(i, 2);
			uv0.scalarMul(10);
			uv1.scalarMul(10);
			uv2.scalarMul(10);
			pm.setPolygonTexture(i, new TextureInfo(t1, uv0.x, uv0.y, uv1.x, uv1.y, uv2.x, uv2.y));
		}
		dungeonObject.compileAndStrip();
		world.addObject(dungeonObject);

		TextureManager.getInstance().addTexture("magma", new Texture(fileStorage + "terrain" + File.separator + "magma.jpg"));
		Object3D magma = Primitives.getPlane(20, 50);
		magma.scale(2f);
		magma.rotateX((float) Math.PI / 2f);
		magma.setTexture("magma");
		magma.translate(11137, 109, 8461); // renders 1 height unit above the water level
		pm = magma.getPolygonManager();
		end = pm.getMaxPolygonID();
		for (int i = 0; i < end; i++) {
			int t1 = pm.getPolygonTexture(i);
			SimpleVector uv0 = pm.getTextureUV(i, 0);
			SimpleVector uv1 = pm.getTextureUV(i, 1);
			SimpleVector uv2 = pm.getTextureUV(i, 2);
			uv0.scalarMul(200);
			uv1.scalarMul(200);
			uv2.scalarMul(200);
			pm.setPolygonTexture(i, new TextureInfo(t1, uv0.x, uv0.y, uv1.x, uv1.y, uv2.x, uv2.y));
		}
		magma.enableLazyTransformations();
		magma.compileAndStrip();
		world.addObject(magma);
		
		Object3D dungeon_magma = magma.cloneObject();
		dungeon_magma.clearTranslation();
		dungeon_magma.translate(dungeonObject.getTransformedCenter().x, 99, dungeonObject.getTransformedCenter().z);
		dungeon_magma.setScale(4);
		dungeon_magma.enableLazyTransformations();
		dungeon_magma.compileAndStrip();
		world.addObject(dungeon_magma);

		/**
		 * Configure the lighting
		 */
		Config.isIndoor = false;
		Config.tuneForOutdoor();

		Light terrainLightSource = new Light(world);
		terrainLightSource.setPosition(new SimpleVector(worldMapObject.getTransformedCenter().x, worldMapObject.getTransformedCenter().y - 1000, worldMapObject.getTransformedCenter().z));
		terrainLightSource.setAttenuation(-1);
		terrainLightSource.setIntensity(200, 255, 255);

		Light dungeonLightSource = new Light(world);
		dungeonLightSource.setPosition(new SimpleVector(19002, 34 - 500, 19250));
		dungeonLightSource.setAttenuation(-1);
		dungeonLightSource.setIntensity(255, 170, 0); // fire / golden lighting
		
		letThereBeLight(fileStorage); // lights.cfg

		Config.lightMul = 1;
		if (LENS_FLARE_ENABLED) {
			// load lens flare textures
			Texture burst = new Texture(fileStorage + "flare" + File.separator + "0.jpg", true);
			Texture halo1 = new Texture(fileStorage + "flare" + File.separator + "1.jpg", true);
			Texture halo2 = new Texture(fileStorage + "flare" + File.separator + "2.jpg", true);
			Texture halo3 = new Texture(fileStorage + "flare" + File.separator + "3.jpg", true);
			TextureManager.getInstance().addTexture("burst", burst);
			TextureManager.getInstance().addTexture("halo1", halo1);
			TextureManager.getInstance().addTexture("halo2", halo2);
			TextureManager.getInstance().addTexture("halo3", halo3);
			this.lensFlare = new LensFlare(new SimpleVector(worldMapObject.getTransformedCenter().x, worldMapObject.getTransformedCenter().y - Config.farPlane, worldMapObject.getTransformedCenter().z), "burst", "halo1", "halo2", "halo3");
			lensFlare.setTransparency(12);
			lensFlare.setGlobalScale(3);
		}

		boolean clouds = true;
		if (clouds) {
			TextureManager.getInstance().addTexture("back", new Texture(fileStorage + "clouds" + File.separator + "back.jpg"));
			TextureManager.getInstance().addTexture("down", new Texture(fileStorage + "clouds" + File.separator + "down.jpg"));
			TextureManager.getInstance().addTexture("front", new Texture(fileStorage + "clouds" + File.separator + "front.jpg"));
			TextureManager.getInstance().addTexture("left", new Texture(fileStorage + "clouds" + File.separator + "left.jpg"));
			TextureManager.getInstance().addTexture("right", new Texture(fileStorage + "clouds" + File.separator + "right.jpg"));
			TextureManager.getInstance().addTexture("up", new Texture(fileStorage + "clouds" + File.separator + "up.jpg"));
			skybox = new SkyBox(Config.farPlane * 10);
			skybox.compile();
			Logger.log("Loaded the SkyBox.");
		}

		//TextureManager.getInstance().addTexture("water", new Texture(fileStorage + "tile" + File.separator + "water.jpg"));
		TextureManager.getInstance().addTexture("water", new Texture(32, 32, new Color(0x155d97)));
		Object3D water = Primitives.getPlane(20, 50);
		water.scale(2f);
		water.rotateX((float) Math.PI / 2f);
		water.setTexture("water");
		water.translate(8770, 110, 11350);
		water.setTransparency(15);
		water.setSortOffset(-200000);
		pm = water.getPolygonManager();
		end = pm.getMaxPolygonID();
		for (int i = 0; i < end; i++) {
			int t1 = pm.getPolygonTexture(i);
			SimpleVector uv0 = pm.getTextureUV(i, 0);
			SimpleVector uv1 = pm.getTextureUV(i, 1);
			SimpleVector uv2 = pm.getTextureUV(i, 2);
			uv0.scalarMul(32);
			uv1.scalarMul(32);
			uv2.scalarMul(32);
			pm.setPolygonTexture(i, new TextureInfo(t1, uv0.x, uv0.y, uv1.x, uv1.y, uv2.x, uv2.y));
		}
		water.enableLazyTransformations();
		water.compileAndStrip();
		world.addObject(water);
	}

	/**
	 * Render the sky box.
	 * @note Must be called BEFORE drawing the main world.
	 */
	public void renderSkyBox(final FrameBuffer frameBuffer, final TwoD twod) {
		if (clearColor == DUNGEON) {
			twod.drawImage(frameBuffer, "dungeon_background", 0, 0, false);
			return;
		}
		if (skybox != null) {
			skybox.render(world, frameBuffer);
		} else {
			twod.drawImage(frameBuffer, "gradient", 0, 0, false);
		}
	}

	/**
	 * Render the lens flare.
	 * @note Must be called AFTER drawing the main world.
	 */
	@SuppressWarnings("unused")
	public void renderLensFlare(final FrameBuffer frameBuffer) {
		if (LENS_FLARE_ENABLED && lensFlare != null) {
			lensFlare.update(frameBuffer, world);
			lensFlare.render(frameBuffer);
		}
	}

	private float[] getMapChunkDimensions(Object3D mapChunk) {
		float[] objectSpaceBounds = mapChunk.getMesh().getBoundingBox();
		SimpleVector mins = new SimpleVector(objectSpaceBounds[0], objectSpaceBounds[2], objectSpaceBounds[4]);
		SimpleVector maxs = new SimpleVector(objectSpaceBounds[1], objectSpaceBounds[3], objectSpaceBounds[5]);
		SimpleVector[] p = new SimpleVector[8];
		p[0] = new SimpleVector(mins.x, mins.y, maxs.z); p[1] = new SimpleVector(mins.x, mins.y, mins.z); p[2] = new SimpleVector(maxs.x, mins.y, mins.z);
		p[3] = new SimpleVector(maxs.x, mins.y, maxs.z); p[4] = new SimpleVector(maxs.x, maxs.y, mins.z);
		p[5] = new SimpleVector(maxs.x, maxs.y, maxs.z); p[6] = new SimpleVector(mins.x, maxs.y, mins.z); p[7] = new SimpleVector(mins.x, maxs.y, maxs.z);
		float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
		for (int i = 0; i < 8; i++) {
			p[i].matMul(mapChunk.getWorldTransformation());
			if (p[i].x < minX)
				minX = p[i].x;
			if (p[i].y < minY)
				minY = p[i].y;
			if (p[i].z < minZ)
				minZ = p[i].z;
			if (p[i].x > maxX)
				maxX = p[i].x;
			if (p[i].y > maxY)
				maxY = p[i].y;
			if (p[i].z > maxZ)
				maxZ = p[i].z;
		}
		float[] worldSpaceBounds = new float[6];
		worldSpaceBounds[0] = minX;
		worldSpaceBounds[2] = minY;
		worldSpaceBounds[4] = minZ;
		worldSpaceBounds[1] = maxX;
		worldSpaceBounds[3] = maxY;
		worldSpaceBounds[5] = maxZ;
		return worldSpaceBounds;
	}

	/**
	 * This method generates texture coordinates for the mesh based on the
	 * coordinates in object space. The normal texture layers are tiled while
	 * the splatting texture (which is the last layer) covers the mesh exactly.
	 * 
	 * @param obj
	 */
	private void setTexture(Object3D obj) {
		TextureManager tm = TextureManager.getInstance();
		obj.calcBoundingBox();
		float[] bb = obj.getMesh().getBoundingBox();
		float minX = bb[0];
		float maxX = bb[1];
		float minZ = bb[4];
		float maxZ = bb[5];
		float dx = maxX - minX;
		float dz = maxZ - minZ;
		float dxs = dx;
		float dzs = dz;
		dx /= 200f;
		dz /= 200f;
		float dxd = dx;
		float dzd = dz;
		PolygonManager pm = obj.getPolygonManager();
		for (int i = 0; i < pm.getMaxPolygonID(); i++) {
			SimpleVector v0 = pm.getTransformedVertex(i, 0);
			SimpleVector v1 = pm.getTransformedVertex(i, 1);
			SimpleVector v2 = pm.getTransformedVertex(i, 2);

			// Assign textures for the first three layers (the "normal" textures)...
			TextureInfo ti = new TextureInfo(tm.getTextureID("grass"), v0.x / dx, v0.z / dz, v1.x / dx, v1.z / dz, v2.x / dx, v2.z / dz);
			ti.add(tm.getTextureID("rocks"), v0.x / dxd, v0.z / dzd, v1.x / dxd, v1.z / dzd, v2.x / dxd, v2.z / dzd, TextureInfo.MODE_ADD);
			ti.add(tm.getTextureID("sand"), v0.x / dxd, v0.z / dzd, v1.x / dxd, v1.z / dzd, v2.x / dxd, v2.z / dzd, TextureInfo.MODE_ADD);

			// Assign the splatting texture...
			ti.add(tm.getTextureID("terrain"), -(v0.x - minX) / dxs, (v0.z - minZ) / dzs, -(v1.x - minX) / dxs, (v1.z - minZ) / dzs, -(v2.x - minX) / dxs, (v2.z - minZ) / dzs, TextureInfo.MODE_ADD);
			pm.setPolygonTexture(i, ti);
		}
	}


	/**
	 * Call this method when you enter a new map chunk.
	 */
	public void updateMapChunkVisibility(final Object3D focusPoint) {
		// Center of Universe
		/*
		if (!Utils.isInRange(5000, focusPoint.getTransformedCenter(), CENTER_OF_THE_UNIVERSE)) {
			worldMapObject.setVisibility(false);
		} else {
			worldMapObject.setVisibility(true);
		}
		if (Utils.isInRange(5000, focusPoint.getTransformedCenter(), BURNING_DUNGEON)) {
			dungeonObject.setVisibility(false);
		} else {
			dungeonObject.setVisibility(true);
		}
		 */
	}

	public Color getClearColor() {
		return clearColor;
	}

	public Object3D getMapChunk(Object3D entity) {
		if (Utils.isInRange(4000, entity, worldMapObject)) {
			clearColor = OPEN_WORLD;
			world.setAmbientLight(120, 120, 120);
			world.setFogParameters(Config.farPlane - 150, Config.farPlane - 100, clearColor.getRed(), clearColor.getGreen(), clearColor.getBlue());
			return worldMapObject;
		}
		if (Utils.isInRange(1500, entity.getTransformedCenter(), BURNING_DUNGEON)) {
			clearColor = DUNGEON;
			world.setAmbientLight(30, 30, 30);
			world.setFogParameters(Config.farPlane / 2, Config.farPlane - (Config.farPlane / 4), clearColor.getRed(), clearColor.getGreen(), clearColor.getBlue());
			return dungeonObject;
		}
		return null;
	}

	public static boolean isInPvP(final Object3D entity) {
		return entity.getTransformedCenter().x > 8197 && entity.getTransformedCenter().x < 11803 && entity.getTransformedCenter().z > 8198 && entity.getTransformedCenter().z < 9039;

	}

	private void letThereBeLight(final String fileStorage) {
		int count = 0;
		try {
			File file = new File(fileStorage + "config" + File.separator + "lights.cfg");
			if (!file.exists()) {
				System.err.println("Could not load: " + file.getAbsolutePath());
			}
			StreamTokenizer tokenizer = new StreamTokenizer(new BufferedReader(new FileReader(file)));
			while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
				int x = (int) tokenizer.nval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				int y = (int) tokenizer.nval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				int z = (int) tokenizer.nval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				int r = (int) tokenizer.nval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				int g = (int) tokenizer.nval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				int b = (int) tokenizer.nval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				int distance = (int) tokenizer.nval;
				Light light = new Light(world);
				light.setAttenuation(-1);
				light.setIntensity(r, g, b);
				light.setDiscardDistance(distance);
				light.setPosition(new SimpleVector(x, y, z));
				light.enable();
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Loaded " + count + " static light sources.");
	}
}

