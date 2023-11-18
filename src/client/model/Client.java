package client.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import client.engine.Engine;
import client.model.items.GroundItem;
import client.model.items.GroundItemManager;
import client.model.npcs.NpcManager;
import client.model.objects.Prop;
import client.model.objects.PropManager;
import client.model.player.LocalPlayer;
import client.model.player.PlayerManager;
import client.model.world.WorldMap;
import client.net.Connection;
import client.net.packet.Packet;
import client.net.packet.PacketSender;
import client.twod.GLFont;
import client.twod.TwoD;
import client.util.Settings;
import client.util.TextFormatter;
import client.util.Utils;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GLSLShader;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureInfo;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.threading.WorkLoad;
import com.threed.jpct.threading.Worker;
import com.threed.jpct.util.KeyMapper;
import com.threed.jpct.util.KeyState;

public class Client {

	private final String fileStorage = Settings.getFileStorage(false, "glabtech");
	private SimpleVector direction;
	private final World world;
	private final Camera camera;
	private final KeyMapper keyMapper;
	private KeyState keyState;
	private final TwoD twod;
	private final WorldMap terrain;
	private final LocalPlayer player;
	private Object3D AVATAR;
	private final Object3D femaleModel;
	private TextureInfo maleBaseTexture;
	private final TextureInfo femaleBaseTexture;
	private final Runtime runtime = Runtime.getRuntime();
	private GLSLShader shader;
	private PropManager propManager;
	private NpcManager npcManager;
	private PacketSender packetSender;
	private final GroundItemManager groundItemManager;
	private boolean forceVisibilityUpdate;
	int snapped_x = -1;
	int snapped_y = -1;
	int snapped_z = -1;
	private String mouseText = "";

	private final Color GUI_BACKGROUND_COLOR = new Color(0x484a40);

	private final Color GUI_INTERFACE_OVERLAY_COLOR = new Color(0xcdcac2);
	private final Color GUI_TEXT_COLOR = new Color(0xfef167);

	private boolean showCursor = false;

	// Toggle-able ui components
	private boolean inputConsole = false;
	private boolean playerHud = true;
	private boolean potionHud = false;

	// Welcome interface
	private boolean welcomeInterface;
	private String[] welcomeMessage = {
				"", 
				"Welcome to " + Config.glWindowName + " (Beta)", 
				"", 
				"You will need to understand some basic controls:", 
				"Use [WASD] or [Arrow keys] to move around the terrain", 
				"Use your mouse to left click an icon at the bottom left.", 
				"Press [F] to toggle point of view", 
				"", 
				"", 
				"", 
				"", 
				"", 
				"Press [ENTER] to continue." 
	};

	private boolean dialogueInterface;
	private String[] dialogueMessage = { "", "", "", "", "", "", "", "", "", "", "", "", "" };

	private boolean inventoryInterface;
	private boolean skillTreeInterface;
	private boolean objectiveInterface;
	private boolean logoutRequest;
	private boolean logoutRequested;
	private long lastAppearanceUpdate = 0L;
	private String currentLocation = "Unknown";

	private String username;
	private String password;

	private final GLFont CHAT_BOX_TEXT = new GLFont(new Font("Consoles", Font.PLAIN, 12));
	private final GLFont CHAT_BOX = new GLFont(new Font("Consoles", Font.BOLD, 12));
	private String[] messages = { "", "", "", "", "", "", "", "" };
	private String consoleInput = "";
	private boolean consoleInputFlag = false;

	public void setConsoleInputFlag(boolean consoleInputFlag) {
		this.consoleInputFlag = consoleInputFlag;
	}

	private long consoleInputThrottler = 0L;
	private Connection connection;
	public boolean loggedIn = false;
	public int coins = 0;
	public int currentHealth = 0;
	public int cachedHealth = 0;
	public int reconnectionAttempt = 0;
	public long lastReconnectionAttempt;
	private PlayerManager playerManager;

	public Client(String username, String password) {
		setUsername(username);
		setPassword(password);
		twod = new TwoD(fileStorage);
		keyMapper = new KeyMapper();
		packetSender = new PacketSender();
		terrain = new WorldMap(fileStorage, world = new World());

		try {
			Mouse.create();
			Mouse.setGrabbed(false);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		/**
		 * Load the head icons.
		 */
		for (int i = 0; i < 3; i++) {
			TextureManager.getInstance().addTexture("head_icon" + i, new Texture(fileStorage + "game" + File.separator + "head_icon" + i + ".png"));
		}
		TextureManager.getInstance().addTexture("white", new Texture(1, 1, Color.WHITE));
		TextureManager.getInstance().addTexture("splat-baked", new Texture(fileStorage + "terrain" + File.separator + "splat-baked.png"));

		// Fire particle related.
		Config.polygonBufferSize = 10;
		TextureManager.getInstance().addTexture("smoke", new Texture(fileStorage + "game" + File.separator + "smoke.jpg"));
		TextureManager.getInstance().addTexture("fire", new Texture(fileStorage + "game" + File.separator + "flame.jpg"));

		/**
		 * Build the male / female models.
		 */
		this.AVATAR = Loader.loadMD2(fileStorage + "player" + File.separator + "male.md2", .10f);
		TextureManager.getInstance().addTexture("male", new Texture(fileStorage + "player" + File.separator + "male.jpg"));
		TextureManager.getInstance().addTexture("male-mask", new Texture(fileStorage + "player" + File.separator + "male.png"));
		maleBaseTexture = new TextureInfo(TextureManager.getInstance().getTextureID("male"));
		maleBaseTexture.add(TextureManager.getInstance().getTextureID("male-mask"), TextureInfo.MODE_ADD);
		AVATAR.setTexture(maleBaseTexture);
		// maleModel.setTexture("male");
		AVATAR.rotateY((float) Math.PI * 1.5f);
		AVATAR.rotateMesh();
		AVATAR.compile(true); // Compile dynamic object

		this.femaleModel = Loader.loadMD2(fileStorage + "player" + File.separator + "female.md2", .091f);
		TextureManager.getInstance().addTexture("female", new Texture(fileStorage + "player" + File.separator + "female.jpg"));
		TextureManager.getInstance().addTexture("female-mask", new Texture(fileStorage + "player" + File.separator + "female.png"));
		this.femaleBaseTexture = new TextureInfo(TextureManager.getInstance().getTextureID("female"));
		femaleBaseTexture.add(TextureManager.getInstance().getTextureID("female-mask"), TextureInfo.MODE_ADD);
		femaleModel.setTexture(femaleBaseTexture);
		// femaleModel.setTexture("female");
		femaleModel.rotateY((float) Math.PI * 1.5f);
		femaleModel.rotateMesh();
		femaleModel.compile(true); // Compile dynamic object

		/**
		 * Initialize the local player
		 */
		player = new LocalPlayer(getCharacterModel(), world, this.camera = world.getCamera(), terrain, this);
		float[][] randomColors = new float[][] { { new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat() }, { new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat() }, { new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat() } };
		player.updateAppearance(this, false, randomColors, world);

		// World.setDefaultThread(Thread.currentThread());
		/**
		 * Load prop models and spawns
		 */
		Worker worker = new Worker(1);
		worker.add(new WorkLoad() {
			@Override
			public void doWork() {
				propManager = new PropManager(world, terrain);
			}

			@Override
			public void done() {
			}

			@Override
			public void error(Exception arg0) {
			}
		});
		worker.waitForAll();
		worker.dispose();
		worker = null;

		/**
		 * Load npc models and spawns
		 */
		worker = new Worker(1);
		worker.add(new WorkLoad() {
			@Override
			public void doWork() {
				npcManager = new NpcManager(world, terrain);
			}

			@Override
			public void done() {
			}

			@Override
			public void error(Exception arg0) {
			}
		});
		worker.waitForAll();
		worker.dispose();

		this.groundItemManager = new GroundItemManager(fileStorage, world, terrain);
		playerManager = new PlayerManager(world, this);

		/**
		 * TODO Load or create the player's profile.
		 */
		forceVisibilityUpdate = true;
		player.teleportTo(WorldMap.HOUSE_LOCATION);
	}

	/**
	 * 3D rendering!!
	 */
	public void render3D(final FrameBuffer frameBuffer) {
		frameBuffer.clear(terrain.getClearColor());
		terrain.renderSkyBox(frameBuffer, twod);
		world.renderScene(frameBuffer);
		world.draw(frameBuffer);
		// world.drawWireframe(frameBuffer, Color.GREEN);
		terrain.renderLensFlare(frameBuffer);
	}

	/**
	 * 2D rendering!!
	 */
	public void render2D(final FrameBuffer frameBuffer, final long lastFrameCount, final int lastUpdateCount) {

		if (Settings.DEBUG) {
			Utils.NANO_FONT.drawShadowedString(frameBuffer, "Camera X:" + camera.getPosition().x + " Y: " + camera.getPosition().y + " Z:" + camera.getPosition().z, 5, 86, GUI_TEXT_COLOR);
			Utils.NANO_FONT.drawShadowedString(frameBuffer, "Player X:" + player.getX() + " Y: " + player.getY() + " Z:" + player.getZ(), 5, 98, GUI_TEXT_COLOR);
			Utils.NANO_FONT.drawShadowedString(frameBuffer, "Props In View: " + propManager.getVisibleEntityCount(), 5, 110, GUI_TEXT_COLOR);
			Utils.NANO_FONT.drawShadowedString(frameBuffer, "Npcs In View: " + npcManager.getVisibleEntityCount(), 5, 122, GUI_TEXT_COLOR);
			Utils.NANO_FONT.drawShadowedString(frameBuffer, "Memory Usage: " + ((runtime.totalMemory() - runtime.freeMemory()) >> 20) + "M", 5, 134, GUI_TEXT_COLOR);
			Utils.NANO_FONT.drawShadowedString(frameBuffer, "Tile X: " + player.getXTile() + " Y: " + player.getYTile(), 5, 146, GUI_TEXT_COLOR);
			Utils.NANO_FONT.drawShadowedString(frameBuffer, "Mouse Tile X: " + getPickX() + " Y: " + getPickY() + " Z: " + getPickZ(), 5, 158, GUI_TEXT_COLOR);
			Utils.NANO_FONT.drawShadowedString(frameBuffer, "Mouse Text: " + getMouseText(), 5, 170, GUI_TEXT_COLOR);
		}

		/**
		 * Render the MMO/RPG game frame!
		 */
		if (inputConsole) {
			setConsoleInputFlag(true);
			twod.drawImage(frameBuffer, "chatbox", 0, Display.getHeight() - 165, !Settings.PERFORMANCE_MODE);
			for (int i = 0; i < messages.length; i++) {
				CHAT_BOX.drawString(frameBuffer, messages[messages.length - i - 1], 2, Display.getDisplayMode().getHeight() - 58 - (i * 13), Color.BLACK);
			}

			CHAT_BOX.drawString(frameBuffer, username + ":", 1, Display.getDisplayMode().getHeight() - 45, Color.BLUE);
			CHAT_BOX_TEXT.drawString(frameBuffer, consoleInput + "*", (int)(CHAT_BOX.getStringBounds(username).getWidth()) + 5, Display.getDisplayMode().getHeight() - 45, Color.BLUE);

		}
		twod.drawImage(frameBuffer, "ui_game", 0, Display.getHeight() - 42, true);
		Utils.GAME_FONT.drawShadowedString(frameBuffer, String.valueOf(coins), 248, Display.getHeight() - 24, GUI_TEXT_COLOR);
		Utils.GAME_FONT.drawShadowedString(frameBuffer, "Console", 244, Display.getHeight() - 5, Color.WHITE);

		// frameBuffer.blit(TextureManager.getInstance().getTexture("splat-baked"), 0, 0, Display.getWidth() - 126, 10, 512, 512, 136, 136, -1, true);
		// twod.drawImage(frameBuffer, "ui_map", Display.getWidth() - 164, -8, true);

		/**
		 * Render the current interface!
		 */
		if (potionHud) {
			for (int i = 0; i < 4; i++) {
				twod.drawImage(frameBuffer, "potion" + i, 2, Display.getHeight() / 2 - 72 + (i * 27), true);
			}
		}
		if (playerHud) {
			twod.drawImage(frameBuffer, "overlay1", Display.getWidth() - 247, Display.getHeight() - 127, true);// Exp Bar
			twod.drawImage(frameBuffer, "overlay0", Display.getWidth() - 247, Display.getHeight() - 90, true); // Mana Bar
			twod.drawImage(frameBuffer, "overlay2", Display.getWidth() - 247, Display.getHeight() - 50, true); // HP Bar
			
		}
		if (inventoryInterface) {
			twod.drawImage(frameBuffer, "iface2", 800 / 2 - 266, 600 / 2 - 175, true);
		}
		if (skillTreeInterface) {
			twod.drawImage(frameBuffer, "iface4", 800 / 2 - 266, 600 / 2 - 175, true);
			
		}
		if (objectiveInterface) {
			twod.drawImage(frameBuffer, "iface3", 800 / 2 - 266, 600 / 2 - 175, true);
		}
		if (logoutRequest) {
			logoutRequested = true;
			dialogueInterface = true;
			dialogueMessage[1] = "Are you sure you wish to leave?";
			dialogueMessage[3] = "Loging out will close the game window";
			dialogueMessage[5] = "press 'Enter' to logout.";
		}
		if (welcomeInterface || dialogueInterface) {
			twod.drawImage(frameBuffer, "iface3", Display.getWidth() / 2 - 266, Display.getHeight() / 2 - 175, false);
			if (welcomeInterface) {
				for (int i = 0; i < welcomeMessage.length; i++) {
					Utils.INTERFACE_FONT.drawShadowedString(frameBuffer, welcomeMessage[i], Display.getWidth() / 2 - 200, Display.getHeight() / 2 - 110 + (i * 20), GUI_TEXT_COLOR);
				}
			} else if (dialogueInterface) {
				for (int i = 0; i < dialogueMessage.length; i++) {
					Utils.INTERFACE_FONT.drawShadowedString(frameBuffer, dialogueMessage[i], Display.getWidth() / 2 - 200, Display.getHeight() / 2 - 110 + (i * 20), GUI_TEXT_COLOR);
				}
			}
		}

		currentLocation = "Open Terrain";

		if (player.getX() > 9225 && player.getX() < 9800 && player.getZ() > 10077 && player.getZ() < 10200) {
			currentLocation = "Lumber Ridge"; // trail to castle
		}
		if (player.getX() > 8926 && player.getX() < 9165 && player.getZ() > 9996 && player.getZ() < 10432) {
			currentLocation = "Lumber Ridge Castle";
		}
		if (player.getX() > 8197 && player.getX() < 11803 && player.getZ() > 8198 && player.getZ() < 9039) {
			twod.drawImage(frameBuffer, "pvp", Display.getWidth() / 2 - 64, 0, true);
			currentLocation = "Uncharted Region (PvP)";
		}
		if (player.getX() > 9947 && player.getX() < 10070 && player.getZ() > 9933 && player.getZ() < 10050) {
			currentLocation = "Center of the Universe";
		}
		if (player.getX() > 9812 && player.getX() < 10137 && player.getZ() > 10386 && player.getZ() < 10809) {
			currentLocation = "Lumber Ridge Village";
		}
		if (player.getX() > 9436 && player.getX() < 9593 && player.getZ() > 9542 && player.getZ() < 9745) {
			currentLocation = "Home";
		}
		if (player.getX() > 9637 && player.getX() < 9732 && player.getZ() > 10316 && player.getZ() < 10430) {
			currentLocation = "General Shop";
		}
		if (player.getX() > 10776 && player.getX() < 11793 && player.getZ() > 11344 && player.getZ() < 11791) {
			currentLocation = "Goblin Hideout";
		}
		if (Utils.isInRange(275, player.getFocusPoint().getTransformedCenter(), WorldMap.BURNING_ISLAND)) {
			currentLocation = "Burning Island";
		}
		if (Utils.isInRange(126, player.getFocusPoint().getTransformedCenter(), WorldMap.CAVE_ENTRANCE)) {
			currentLocation = "Cave Entrance";
		}
		// TODO : add a lot more areas! this is an original feature. and fits nicely with the open terrain setup.
		if (player.getX() > 0 && player.getX() < 0 && player.getZ() > 0 && player.getZ() < 0) {
		}

		Utils.GAME_FONT.drawShadowedString(frameBuffer, "Location: " + currentLocation, 5, 15, GUI_TEXT_COLOR);

		int mouseX = Mouse.getX();
		int mouseY = (frameBuffer.getOutputHeight() - Mouse.getY());
		if (showCursor) {
			twod.drawImage(frameBuffer, "cursor", mouseX - 16, mouseY - 16, true);
		}
		/**
		 * Poll mouse input for the game frame.
		 */
		while (Mouse.next()) {
			mouseX = Mouse.getX();
			mouseY = (frameBuffer.getOutputHeight() - Mouse.getY());
			direction = new SimpleVector(Interact2D.reproject2D3DWS(camera, frameBuffer, mouseX, mouseY)).normalize();
			float distance = world.calcMinDistance(camera.getPosition(), direction, Config.farPlane);
			if (distance == Object3D.COLLISION_NONE) {
				setMouseText("");
			} else {
				SimpleVector collisionPoint = new SimpleVector(direction);
				collisionPoint.scalarMul(distance);
				collisionPoint.add(camera.getPosition());
				snapped_x = (int) (Math.floor(collisionPoint.x) - (Math.floor(collisionPoint.x) % 2));
				snapped_y = (int) (Math.floor(collisionPoint.y) - (Math.floor(collisionPoint.y) % 2)) - 1;
				snapped_z = (int) (Math.floor(collisionPoint.z) - (Math.floor(collisionPoint.z) % 2));
				setMouseText("Test Line: "); // used to show info on where the curser is on the screen
				
			}
			if (Mouse.getEventButton() > -1) {
				if (Mouse.getEventButtonState()) {
					/**
					 * Mouse Buttons 0 = Left Click 1 = Right Click 2 = Middle Mouse button (Scroll wheel)
					 */
					if (Mouse.getEventButton() == 1) { // Right Click
						/**
						 * TODO Add a context menu when right clicked
						 */
						
					}
					// System.out.println("PRESSED MOUSE BUTTON: " + Mouse.getEventButton());
				} else {
					// System.out.println("RELEASED MOUSE BUTTON: " + Mouse.getEventButton());
					/**
					 * The ui buttons
					 */
					for (int i = 0; i < 5; i++) {
						if (mouseX > (i * 45) && mouseX < 42 + (i * 45) && mouseY > Display.getHeight() - 40 && mouseY < Display.getHeight()) {
							System.out.println("ui button #" + i);
							switch (i) {
								case 0: // inventory
									inventoryInterface = !inventoryInterface;
									break;
								case 1: // character
									if (lastAppearanceUpdate <= System.currentTimeMillis() - 1500) {
										lastAppearanceUpdate = System.currentTimeMillis();
										float[][] randomColors = new float[][] { { new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat() }, { new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat() }, { new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat() } };
										player.updateAppearance(this, false, randomColors, world);
									}
									break;
								case 2: // skill tree
									skillTreeInterface = !skillTreeInterface;
									break;
								case 3: // adventure log
									objectiveInterface = !objectiveInterface;
									break;
								case 4: // logout
									logoutRequest = true;
									break;
								default:
									System.err.println("Unregistered button pressed: " + i);
									break;
										
							}
						} // end ui buttons
						/**
						 * The console buttons
						 */
						for (int j = 0; j < 2; j++) {
							if (mouseX > 227 + (j * 90) && mouseX < 227 + ((j + 1) * 90) && mouseY > Display.getHeight() - 20 && mouseY < Display.getHeight()) {
								if (j == 0) {
									inputConsole = !inputConsole;
									if (inputConsole == false) {
										setConsoleInputFlag(false);
									}
								}
							}
						} // end console buttons
						/**
						 * The inventory interface buttons
						 */
						if (inventoryInterface) {
							// show inventory
							// 532 350
							for (int j = 0; j < 4; j++) { // row 1
								// 207, 149
								// 287, 224
								// gap between each button = 15px
								// button size = 80px
								int m = (j * 82) + (j * 17);
								if (mouseX > 207 + m && mouseX < 287 + m && mouseY > 149 && mouseY < 224) {
									// valid clicking coordinates of each item
									// slot
									System.out.println("inventory debug: row 1, slot " + (j + 1));
								}
							}
							for (int j = 0; j < 4; j++) { // row 2
								// 207, 249
								// 287, 324
								int m = (j * 82) + (j * 17);
								if (mouseX > 207 + m && mouseX < 287 + m && mouseY > 249 && mouseY < 324) {
									// valid clicking coordinates of each item
									// slot
									System.out.println("inventory debug: row 2, slot " + (j + 1));
								}
							}
							for (int j = 0; j < 4; j++) { // row 3
								// 207, 349
								// 287, 424
								int m = (j * 82) + (j * 17);
								if (mouseX > 207 + m && mouseX < 287 + m && mouseY > 349 && mouseY < 424) {
									// valid clicking coordinates of each item
									// slot
									System.out.println("inventory debug: row 3, slot " + (j + 1));
								}
							}
							if (mouseX > 343 && mouseX < 454 && mouseY > 442 && mouseY < 462) {
								// close the interface
								inventoryInterface = false;
							}
						} // end inventory clicking

					}
				}
			}
		}

		player.animate();
	}

	private int getPickY() {
		return snapped_y;
	}

	private int getPickX() {
		return snapped_x;
	}

	private int getPickZ() {
		return snapped_z;
	}

	private String getMouseText() {
		return mouseText;
	}

	private void setMouseText(String mouseText) {
		this.mouseText = mouseText;
	}

	/**
	 * Update the game!!
	 */
	public void update(final float delta, final FrameBuffer frameBuffer) {
		keyState = null;
		/**
		 * Poll keyboard input first.
		 */
		if ((keyState = keyMapper.poll()) != KeyState.NONE) {
			/**
			 * Handle game input
			 */
			if (consoleInputFlag) {
				if (keyState.getKeyCode() == KeyEvent.VK_ENTER && consoleInputThrottler <= System.currentTimeMillis() - 100) {
					consoleInputThrottler = System.currentTimeMillis();
					consoleInputFlag = false;
					if (consoleInput.length() >= 1) {
						if (consoleInput.startsWith("/")) {
							String cmdInput = consoleInput.substring(1);
							String[] cmd = cmdInput.split(" ");
							if (cmd[0].equalsIgnoreCase("spawn")) {
								PropManager.spawnProp(Integer.parseInt(cmd[1]), getPickX(), getPickY(), getPickZ(), world, terrain);
							}
							if (cmd[0].equalsIgnoreCase("anim")) {
								player.setAnimation(Integer.parseInt(cmd[1]));
							}
							if (cmd[0].equalsIgnoreCase("debug")) {
								Settings.DEBUG = !Settings.DEBUG;
							}
							return;
						}
						Packet packet = new Packet(4);
						packet.putString(consoleInput);
						connection.sendPacket(packet);
						consoleInput = "";
					}

				} else {
					consoleInput = TextFormatter.updateText(consoleInput, keyState.getKeyCode(), keyState.getChar(), 64);
				}
			}

			if (keyState.getKeyCode() == KeyEvent.VK_ENTER && keyState.getState()) {
				if (welcomeInterface) {
					welcomeInterface = false;
				}
				if (logoutRequested) {
					Engine.terminate();
				}
			}
			if (isInterfaceOpened()) {
				return;
			}
			switch (keyState.getKeyCode()) {
				case KeyEvent.VK_0:
					if (keyState.getState()) {
						Engine.screenShot();
					}
					break;
				case KeyEvent.VK_1:
					player.teleportTo(WorldMap.BURNING_ISLAND);
					break;
				case KeyEvent.VK_2:
					player.teleportTo(WorldMap.BURNING_DUNGEON);
					break;
				case KeyEvent.VK_3: // goblin village
					player.teleportTo(new SimpleVector(11532.492, 90.9, 11606.823));
					break;
				case KeyEvent.VK_4:
					player.teleportTo(WorldMap.CENTER_OF_THE_UNIVERSE);
					break;
				case KeyEvent.VK_I:
					if (keyState.getState()) {
						groundItemManager.spawnRandomItem(player.getFocusPoint().getTransformedCenter());
					}
					break;
				case KeyEvent.VK_ENTER:
					if (keyState.getState()) {
						// int hardcode_id = 18;// = new Random().nextInt(13) == 6 ? 19 : 20;
						// System.out.println(hardcode_id + ", " + localPlayer.getX() + ", " + localPlayer.getY() + ", " + localPlayer.getZ() + " // ");

						GroundItem pickUp = groundItemManager.pickUp(player.getFocusPoint());
						if (pickUp != null) {
							System.out.println("picked up item (type #" + pickUp.getType() + ", uid #" + pickUp.getUid() + ")");
							pickUp.expire(this);
						}

						/**
						 * Check if the interaction is with an NPC
						 */
						if (npcManager.getInteractionEntity(player.getFocusPoint()) != null) {
							// Npc npc = npcManager.getInteractionEntity(localPlayer.getFocusPoint());
							// npc.setLeader(localPlayer.getFocusPoint());
							if (dialogueInterface) {
								dialogueInterface = false;
							} else {
								dialogueInterface = true;
								dialogueMessage[0] = "The NPC has nothing interesting to say..";
							}
						}

						/**
						 * Check if the interaction is with a dynamic prop
						 */
						if (propManager.getInteractionEntity(player.getFocusPoint()) != null) {
							Prop prop = propManager.getInteractionEntity(player.getFocusPoint());
							if (Utils.isInRange(30, player.getFocusPoint().getTransformedCenter(), prop.getTransformedCenter())) {
								switch (prop.getType()) {
									case 0:
									case 1:
									case 12:
									case 13:
									case 19:
									case 20: // trees
										System.out.println("It's a tree.");
										break;
									case 15:
										System.out.println("It's a fire pit.");
										break;
									case 23:
									case 24:
										System.out.println("It's a grave stone.");
										break;
									case 21:
										System.out.println("It's a bed.");
										break;
									case 2:
										System.out.println("Dragon cave entrance.");
										break;
									case 3:
										System.out.println("It's an item chest.");
										break;
									case 4:
										System.out.println("It's a portal.");
										break;
									case 8:
										System.out.println("The general shop.");
										break;
								}
							}
						}
					}
					break;
			}
		}

		/**
		 * Update the list of entities in view 10x / minute
		 */
		propManager.updateVisibilityList(camera, forceVisibilityUpdate);
		propManager.cycle(delta);
		npcManager.updateVisibilityList(camera, forceVisibilityUpdate);
		npcManager.cycle(delta);
		groundItemManager.updateVisibilityList(camera, forceVisibilityUpdate);
		groundItemManager.cycle(delta);
		forceVisibilityUpdate = false;

		/**
		 * Update the local player
		 */
		player.update(delta, keyState);
		if (player.positionUpdateFlag) {
			try {
				Packet packet = new Packet(2);
				packet.putFloat(player.getFocusPoint().getTransformedCenter().x);
				packet.putFloat(player.getFocusPoint().getTransformedCenter().y);
				packet.putFloat(player.getFocusPoint().getTransformedCenter().z);
				packet.putFloat(player.getCurrentRotation());
				connection.sendPacket(packet);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns a fe/male object.
	 */
	public Object3D generateCharacterModel(final boolean female, final float[][] colorModifiers) {
		final Object3D object;
		if (female) {
			object = new Object3D(femaleModel, true);
			object.shareCompiledData(femaleModel);
		} else {
			object = new Object3D(AVATAR, true);
			object.shareCompiledData(AVATAR);
		}
		this.shader = new GLSLShader(Loader.loadTextFile(fileStorage + "player" + File.separator + "player.vert"), Loader.loadTextFile(fileStorage + "player" + File.separator + "player.frag")) {
			@Override
			public void setCurrentObject3D(Object3D obj) {
				setUniform("colorMul0", colorModifiers[0]);
				setUniform("colorMul1", colorModifiers[1]);
				setUniform("colorMul2", colorModifiers[2]);
				setStaticUniform("map0", 0);
				setStaticUniform("map1", 1);
			}
		};
		object.setRenderHook(shader);
		return object;
	}

	public void addMessage(String message) {
		for (int i = 0; i < messages.length; i++) {
			if (i == messages.length - 1) {
				messages[i] = message;
			} else {
				messages[i] = messages[i + 1];
			}
		}
	}

	public void sendLoginRequest(String password) {
		if (loggedIn || reconnectionAttempt > 5) {
			return;
		}
		reconnectionAttempt++;
		if (connection == null) {
			connection = new Connection(this);
		}
		connection.connect(Settings.HOST_IP, 43594);

		Packet packet = new Packet(1);
		packet.putInt(new Random().nextBoolean() ? 36 : 42);
		packet.putString(getUsername());
		packet.putString(password);
		connection.sendPacket(packet);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	private void setPassword(String password) {
		this.password = password;
	}

	public PlayerManager getPlayerManager() {
		return playerManager;
	}

	public Connection getConnection() {
		return connection;
	}

	private boolean isInterfaceOpened() {
		return welcomeInterface || inventoryInterface || objectiveInterface || skillTreeInterface || dialogueInterface || inputConsole;
	}

	public void updateAnimation(long uid, int animationId) {
		playerManager.updateAnimation(player, uid, animationId);
	}

	public void updatePlayerPosition(int uid, float x, float y, float z, float rotationY) {
		playerManager.updatePosition(player, uid, x, y, z, rotationY);

	}

	public Object3D getCharacterModel() {
		Object3D model = new Object3D(AVATAR, true);
		model.shareCompiledData(AVATAR);
		return model;
	}

	public PacketSender getPacketSender() {
		return packetSender;
	}

	public Color getGUI_BACKGROUND_COLOR() {
		return GUI_BACKGROUND_COLOR;
	}

	public Color getGUI_INTERFACE_OVERLAY_COLOR() {
		return GUI_INTERFACE_OVERLAY_COLOR;
	}

}
