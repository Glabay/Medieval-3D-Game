package client.model.objects;

import client.model.world.WorldMap;
import client.util.Utils;

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

@SuppressWarnings({ "serial" })
public class Prop extends Object3D {
	
	private final boolean COLLISION_DISABLED = false;

	private World world;
	private int type;
	private int uid;
	private float heightDifference;
	
	// optional instances
	private FireParticle fireParticle;
	
	public Prop(final Object3D model, final int type, final int uid, final SimpleVector location, final World world, final WorldMap terrain) {
		super(model);
		this.type = type;
		this.uid = uid;
		this.heightDifference = model.getTransformedCenter().y;
		this.world = world;
		this.translate(location);
		this.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
		this.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		
		world.addObject(this);
		Utils.dropEntityToGround(this, terrain.getMapChunk(this));
		this.translate(0, heightDifference, 0); // used to correct height differences since we use random open source models.
		if (type == 15) {
			fireParticle = new FireParticle(world, location);
		}
		this.enableLazyTransformations();
		if (COLLISION_DISABLED) {
			this.setCollisionMode(Object3D.COLLISION_CHECK_NONE);
		}
		this.compileAndStrip();
	}

	public void update(final float delta) {
		// ....
		if (type == 15) {
			fireParticle.update(world);
		}
	}

	public int getType() {
		return type;
	}

	public int getUid() {
		return uid;
	}

}