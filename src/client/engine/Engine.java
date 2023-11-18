package client.engine;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import client.model.Client;
import client.net.Connection;
import client.net.packet.Packet;
import client.util.Settings;

import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.Logger;

public class Engine implements Runnable {

	private final int FRAME_WIDTH = 800;
	private final int FRAME_HEIGHT = 600;
	private final long TIMER_RESOLUTION = 1000000000;
	private final int DELAYS_PER_YIELD = 10;
	private final int MAX_FRAME_SKIPS = 10;
	private long totalUpdateCount = 0L;
	private float lastUpdateCount = 0L;
	private long secondsRunning = 0L;
	private final int requestedFPS = 30;
	private long lastFrameCount = 0L;
	private long averageFrameCount = 0L;
	private static AtomicBoolean running = new AtomicBoolean(true);

	private final FrameBuffer frameBuffer;
	private final Client client;
	private static Connection connection;
	private String password;

	public Engine(String name, String password) {
		this.password = password;
		Logger.log("Initializing Engine...");
		
		Config.glVerbose = true;
		Logger.setLogLevel(Logger.LL_ONLY_ERRORS);
		Logger.setOnError(Logger.ON_ERROR_THROW_EXCEPTION);
		Config.glColorDepth = 24;
		//Config.useMultipleThreads = Runtime.getRuntime().availableProcessors() > 1;
		Config.autoBuild = true;
		Config.glAvoidTextureCopies = true;
		Config.oldStyle3DSLoader = true;
		Config.glTrilinear = !Settings.PERFORMANCE_MODE;
		Config.texelFilter = !Settings.PERFORMANCE_MODE;
		Config.glUseFBO = true;
		Config.nativeBufferSize = Settings.PERFORMANCE_MODE ? 512 : 1024;
		Config.useBB = true;
		Config.glFullscreen = Settings.FULL_SCREEN;
		Config.glWindowName = "Pokey's Adventure";
		Config.farPlane = 750;
		Config.maxPolysVisible = 36500;

		/**
		 * Crashes client.. 
		 * int numberOfProcs = Runtime.getRuntime().availableProcessors();
		 * Config.useMultipleThreads = numberOfProcs > 1;
		 * Config.useMultiThreadedBlitting = numberOfProcs > 1; 
		 * Config.loadBalancingStrategy = 1;
		 * Config.maxNumberOfCores =  numberOfProcs;
		 */
		try {
			Config.glSkipInitialization = true;
			Config.glVSync = false;
			if (!Settings.FULL_SCREEN) {
				Display.setDisplayMode(new DisplayMode(FRAME_WIDTH, FRAME_HEIGHT));
			}
			Display.setTitle(Config.glWindowName);
			Display.sync(60);
			Display.setResizable(false);
			Display.setFullscreen(Settings.FULL_SCREEN);
			Display.create();
			
		} catch (LWJGLException e) {
			System.err.println("Fatal error creating display! Exception: " + e.toString());
			System.exit(1);
		}
		
		client = new Client(name, password);
		frameBuffer = new FrameBuffer(Settings.FULL_SCREEN ? Display.getWidth() : FRAME_WIDTH, Settings.FULL_SCREEN ? Display.getHeight() : FRAME_HEIGHT, FrameBuffer.SAMPLINGMODE_NORMAL);
		frameBuffer.disableRenderer(IRenderer.RENDERER_SOFTWARE);
		frameBuffer.enableRenderer(IRenderer.RENDERER_OPENGL, IRenderer.MODE_OPENGL);
	}

	@Override
	public void run() {
		long startTime = 0L;
		long endTime = 0L;
		long timeDiff = 0L;
		long sleepTime = 0L;
		long overSleepTime = 0L;
		long excess = 0L;
		int numberOfDelays = 0;
		int frameSkips = 0;
		int frameCount = 0;
		long period = TIMER_RESOLUTION / requestedFPS;
		long totalFrameCount = 0L;
		float currentUPS = 0;
		float delta = 0;
		long previousSampleTime = System.nanoTime();
		
		while (running.get() && !Display.isCloseRequested()) {
			startTime = System.nanoTime();
			if (Mouse.isCreated() && frameBuffer.isInitialized() && Display.isCreated()) {
				if (Display.wasResized()) {
					frameBuffer.resize(Display.getWidth(), Display.getHeight());
				}

				// Update loop
				currentUPS = totalUpdateCount / (float) (secondsRunning == 0 ? 1 : secondsRunning);
				delta = requestedFPS / currentUPS;
				update(delta);

				if (client.loggedIn) {
					// Rendering
					frameBuffer.clear(Color.BLACK); // Clear last frame
					// Render new frame
					Config.glTransparencyOffset = 0.0f;
					Config.glTransparencyMul = 0.025f;
					client.render3D(frameBuffer); // 3D first
					Config.glTransparencyOffset = 0.7f;
					Config.glTransparencyMul = 0.06f;
					client.render2D(frameBuffer, lastFrameCount, (int) lastUpdateCount); // 2D second
					frameBuffer.update();
					frameBuffer.displayGLOnly(); // End of cycle
				} else {
					frameBuffer.clear(Color.BLACK);
					if (client.reconnectionAttempt > 5) {
					} else {
					}
					if (client.lastReconnectionAttempt <= System.currentTimeMillis() - 5000) {
						client.lastReconnectionAttempt = System.currentTimeMillis();
						client.sendLoginRequest(password);
					}
					
				}
			}
			endTime = System.nanoTime();
			timeDiff = endTime - startTime;
			sleepTime = (period - timeDiff) - overSleepTime;
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime / (TIMER_RESOLUTION / 1000));
				} catch (final InterruptedException ignore) {
					Thread.currentThread().interrupt();
					break;
				}
				overSleepTime = (System.nanoTime() - endTime) - sleepTime;
			} else {
				excess -= sleepTime;
				overSleepTime = 0L;
				numberOfDelays++;
				if (numberOfDelays > DELAYS_PER_YIELD) {
					Thread.yield();
					numberOfDelays = 0;
				}
			}
			frameSkips = 0;
			while ((excess > period) && (frameSkips < MAX_FRAME_SKIPS)) {
				excess -= period;
				update(delta);
				frameSkips++;
			}
			frameCount++;
			totalFrameCount++;
			totalUpdateCount += frameSkips + 1;
			if (System.nanoTime() - previousSampleTime > TIMER_RESOLUTION) {
				secondsRunning++;
				lastFrameCount = frameCount;
				lastUpdateCount = currentUPS;
				frameCount = 0;
				setAverageFrameCount(totalFrameCount / secondsRunning);
				previousSampleTime = System.nanoTime();
			}
		}
		Mouse.destroy();
		frameBuffer.dispose();
		Display.destroy();
		Runtime.getRuntime().runFinalization();
		Runtime.getRuntime().gc();
		Runtime.getRuntime().exit(0);
	}

	private void update(float delta) {
		if (delta <= 0) {
			return;
		}
		client.update(delta, frameBuffer);
	}

	public static int getMouseX() {
		return Mouse.getX();
	}

	public static int getMouseY() {
		return Display.getHeight() - Mouse.getY();// - (Mouse.getY() - mouseYOffset);
	}

	/**
	 * A static method that will save a screen shot.
	 */
	public static void screenShot() {
		String prefix = "./cgi" + File.separator;
		try {
			int x = 0;
			int y = 0;
			int w = Display.getWidth();
			int h = Display.getHeight();
			Robot robot = new Robot();
			Rectangle captureSize = new Rectangle(x, y, w, h);
			BufferedImage bufferedimage = robot.createScreenCapture(captureSize);
			long picNumber = System.nanoTime();
			File file = new File((new StringBuilder()).append(prefix + "ss").append(picNumber).append(".png").toString());
			ImageIO.write(bufferedimage, "png", file);
			System.out.println("A moment in time has been captured.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void terminate() {
		System.out.println("Client terminated by user.");
		Packet packet = new Packet(9);
		packet.putInt(0);
		connection.sendPacket(packet);
		//running.set(false);
	}

	public long getAverageFrameCount() {
		return averageFrameCount;
	}

	public void setAverageFrameCount(long averageFrameCount) {
		this.averageFrameCount = averageFrameCount;
	}
}
