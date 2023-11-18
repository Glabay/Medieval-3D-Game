package client.model.player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import client.model.Client;
import client.util.MathUtils;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureInfo;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

public class PlayerManager {

	private final World world;
	private final Client client;

	private List<RemotePlayer> remotePlayers = new CopyOnWriteArrayList<RemotePlayer>();

	public PlayerManager(final World world, final Client client) {
		this.world = world;
		this.client = client;
	}

	/**
	 * Registers a remote player instance.
	 */
	public void register(int uid, float x, float y, float z, float rotationY, int privileges) {
		boolean exists = false;
		for (RemotePlayer other : remotePlayers) {
			if (other.getUid() == uid) {
				exists = true;
			}
		}
		if (!exists) {
			RemotePlayer other = new RemotePlayer(world, client.getCharacterModel(), uid);
			other.translate(x, y, z);
			other.clearRotation();
			other.rotateY(rotationY);
			//other.setHeadIcon(privileges);
			remotePlayers.add(other);
			System.out.println("Registered remote player ("+uid+")");
		}
	}

	/**
	 * Unregisters a remote player instance.
	 */
	public void unregister(int uid) {
		for (RemotePlayer other : remotePlayers) {
			if (other.getUid() == uid) {
				world.removeObject(other);
				System.out.println("Unregistered remote player ("+uid+")");
				return;
			}
		}
	}

	public void updatePosition(LocalPlayer player, int uid, float x, float y, float z, float rotationY) {
		if (uid == -1) {
			player.getFocusPoint().clearTranslation(); // reset pos
			player.getFocusPoint().translate(x, y, z); // set new pos
			//player.updateHeadIconPosition(); // update head icon pos
			player.getFocusPoint().clearRotation(); // reset rot
			player.getFocusPoint().rotateY(rotationY); // set new rot
			return;
		}
		for (RemotePlayer other : remotePlayers) {
			if (other.getUid() == uid) {
				other.clearTranslation(); // reset pos
				other.translate(x, y, z); // set new pos
				other.clearRotation(); // reset rot
				other.rotateY(rotationY); // set new rot
				return;
			}
		}
	}

	public void updateAnimation(LocalPlayer player, long uid, int animationId) {
		if (uid == -1) {
			player.setAnimation(animationId);
			return;
		}
		for (RemotePlayer other : remotePlayers) {
			if (other.getUid() == uid) {
				other.setAnimation(animationId);
				return;
			}
		}
	}

	public void update() {
		for (RemotePlayer other : remotePlayers) {
			other.animate();
			//other.cycle(world);
		}
	}

	public int getPlayersOnline() {
		return remotePlayers.size() + 1;
	}


	public RemotePlayer getClosestRemotePlayer(SimpleVector position) {
		for (RemotePlayer other : remotePlayers) {
			if (MathUtils.isInRange(8, position, other.getTransformedCenter())) {
				return other;
			}
		}
		return null;
	}

	/**
	 * Represents a remote player.
	 */
	@SuppressWarnings("serial")
	public class RemotePlayer extends Object3D {
	
		private final int uid;
		private final Object3D headIcon;
		private final TextureInfo maleBaseTexture;
		
		public RemotePlayer(final World world, final Object3D model, final int uid) {
			super(model);
			this.uid = uid;
			maleBaseTexture = new TextureInfo(TextureManager.getInstance().getTextureID("male"));
			maleBaseTexture.add(TextureManager.getInstance().getTextureID("male-mask"), TextureInfo.MODE_ADD);
			this.setTexture(maleBaseTexture);
			this.compile(true);
			world.addObject(this);
			this.setCollisionMode(Object3D.COLLISION_CHECK_NONE);
			this.clearTranslation();
			this.clearRotation();
			this.rotateMesh();
	
			this.headIcon = Primitives.getPlane(4, 1);
			headIcon.setBillboarding(BILLBOARDING_ENABLED);
			world.addObject(headIcon);
			headIcon.setTransparency(12);
			headIcon.compile();
		}
		

	
		private final float FRAME_DIFFERENCE = 0.075f; // The different in frames <animationIndex> between animations.
		private float animationIndex = 0;
		private int animationId = 0;
		private long lastFrameUpdate = 0L;
		private long animationStartTime = 0L;
	
		public void animate() {
			if (lastFrameUpdate <= System.currentTimeMillis() - 10) {
				lastFrameUpdate = System.currentTimeMillis();
				long resetDelay = 500; // default
				if (animationStartTime <= System.currentTimeMillis() - resetDelay) {
					animationIndex = 1; // TODO : idle
				} else {
					animationIndex += 0.008f; // increment frame value
					if (animationIndex > (FRAME_DIFFERENCE * (animationId + 1) + FRAME_DIFFERENCE) + (animationId > 0 ? 0.1f  : -0.1f)) {
						this.animationIndex = (FRAME_DIFFERENCE * (animationId + 1)) + (animationId > 0 ? 0.1f  : -0.1f);
					}
				}
				this.animate(animationIndex, 1); // Let's animate the 3D Object
			}
		}
	
		public void setAnimation(int animationId) {
			this.animationStartTime = System.currentTimeMillis();
			if (this.animationId != animationId) {
				this.animationId = animationId;
				this.animationIndex = (FRAME_DIFFERENCE * (animationId + 1)) + (animationId > 0 ? 0.1f  : -0.1f);
				//System.out.println("performing animation #"+animationId+", starting frame="+animationIndex);
			}
		}
	
		public int getUid() {
			return uid;
		}
	
	}

}