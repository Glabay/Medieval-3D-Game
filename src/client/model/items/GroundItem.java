package client.model.items;

import client.model.Client;
import client.model.world.WorldMap;
import client.util.Utils;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

@SuppressWarnings({ "serial" })
public class GroundItem extends Object3D {
	
	private final static Object3D billboard;
	private final int type;
	private final int uid;
	private long spawnTime;

	static {
		billboard = Primitives.getPlane(6, 1);
		billboard.setBillboarding(Object3D.BILLBOARDING_ENABLED);
		billboard.setTransparency(99); // 12
		billboard.compile();
	}

	public GroundItem(final int type, final int uid, final SimpleVector location, final World world, final WorldMap terrain) {
		super(billboard);
		this.type = type;
		this.uid = uid;
		this.setTexture("item"+type);
		this.clearTranslation();
		this.translate(location);
		world.addObject(this);
		this.setCollisionMode(Object3D.COLLISION_CHECK_SELF);
		Utils.dropEntityToGround(this, terrain.getMapChunk(this));
		this.setCollisionMode(Object3D.COLLISION_CHECK_NONE);
		this.translate(0, -5, 0);
		this.enableLazyTransformations();
		this.compileAndStrip();
		this.spawnTime = System.currentTimeMillis();
	}

	public int getType() {
		return type;
	}

	public int getUid() {
		return uid;
	}
	
	public boolean isExpired() {
		return spawnTime <= System.currentTimeMillis() - 60000;
	}

	public void expire(final Client client) {
		spawnTime = System.currentTimeMillis() - 60000;
	}

}