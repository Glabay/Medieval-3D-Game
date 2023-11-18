package client.model.npcs;

import client.model.world.WorldMap;
import client.util.Utils;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import java.util.Random;

public class Npc extends Object3D {

    private static final long serialVersionUID = 6380748390067205373L;
    private final Random RANDOM = new Random();
    private final float DAMPING = 0.1f;
    private final int type;
    private final int uid;
    private final float heightDifference;

    private long lastAnimationUpdate = 0L;
    private float animationIndex;
    private int currentAnimation = 0;
    private float currentSpeed = 0.8f;
    private SimpleVector moveRes = new SimpleVector(0, 0, 0);
    /**
     * Sphere collision vector for this object.
     */
    private final SimpleVector ellipsoid;

    private Object3D leader;
    private final Object3D headIcon;

    public Npc(final Object3D model, final int type, final int uid, final SimpleVector location, final World world, final WorldMap terrain) {
        super(model);
        this.type = type;
        this.uid = uid;
        this.heightDifference = -10f;//model.getTransformedCenter().y;

        this.ellipsoid = getSize(type); // get the model size

        this.translate(location); // relocate
        world.addObject(this); // add to jpct world
        this.setCollisionMode(Object3D.COLLISION_CHECK_SELF);
        Utils.dropEntityToGround(this, terrain.getMapChunk(this)); // clamp to terrain
        this.translate(0, heightDifference, 0); // adjust height
        this.compile(true); // compile dynamic object

        this.currentAnimation = getIdleAnimation(type); // set idle animation

        this.headIcon = Primitives.getPlane(5, 1);
        headIcon.setBillboarding(BILLBOARDING_ENABLED);
        world.addObject(headIcon);
        headIcon.setTexture("head_icon" + RANDOM.nextInt(3));
        headIcon.setTransparency(20);
        headIcon.compile();
        headIcon.clearTranslation();
        headIcon.translate(getTransformedCenter().x, getTransformedCenter().y - (ellipsoid.y + 2.5f), getTransformedCenter().z);
    }

    private long lastRandomRotation = System.currentTimeMillis();
    private int randomRotationPause = 0;
    private long lastRandomMovement = System.currentTimeMillis();
    private int randomMovementPause = 0;
    private int random_stepsForward = 0;
    private int random_stepsToWalk = 0;

    public void update(final float delta) {
        boolean isMoving = false;
        boolean randomWalkEnabledForThisNpc = leader == null;
        /**
         * Randomize the npc rotation
         */
        if (randomWalkEnabledForThisNpc && lastRandomRotation <= System.currentTimeMillis() - randomRotationPause) {
            lastRandomRotation = System.currentTimeMillis();
            randomRotationPause = RANDOM.nextInt(2500) + RANDOM.nextInt(2500) * RANDOM.nextInt(10);
            if (RANDOM.nextBoolean()) {
                this.rotateY((float) Math.toRadians(RANDOM.nextInt(180))); // randomly rotate
                /**
                 * Random movement (if the model rotated)
                 */
                if (lastRandomMovement <= System.currentTimeMillis() - randomMovementPause) {
                    lastRandomMovement = System.currentTimeMillis();
                    randomMovementPause = RANDOM.nextInt(3000) + RANDOM.nextInt(3000) * RANDOM.nextInt(10);
                    random_stepsForward = 0;
                    random_stepsToWalk = 10 + RANDOM.nextInt(10) + RANDOM.nextInt(10) + RANDOM.nextInt(15);
                }
            }
        } else {
            if (leader != null) {
                random_stepsToWalk = 0;
                followTheLeader();
            }
        }

        /**
         * Move Forward..
         */
        if (random_stepsForward < random_stepsToWalk) {
            random_stepsForward++;
            SimpleVector t = this.getZAxis();
            t.scalarMul(currentSpeed / 2); // half speed
            moveRes.add(t);
            this.translate(0, -0.025f, 0); // prevent sinking
            moveRes = this.checkForCollisionEllipsoid(moveRes, ellipsoid, 8);
            this.translate(moveRes);
            isMoving = true;

            // finally apply the gravity:
            t = new SimpleVector(0, 1, 0);
            t = this.checkForCollisionEllipsoid(t, ellipsoid, 1);
            this.translate(t);

            // damping
            if (moveRes.length() > DAMPING) {
                moveRes.makeEqualLength(new SimpleVector(0, 0, DAMPING));
            } else {
                moveRes = new SimpleVector(0, 0, 0);
            }
        }
        currentAnimation = isMoving ? getMovementAnimation(type) : getIdleAnimation(type);

        /**
         * Update the head icon.
         */
        if (isMoving) {
            headIcon.clearTranslation();
            headIcon.translate(getTransformedCenter().x, getTransformedCenter().y - (ellipsoid.y + 2.5f), getTransformedCenter().z);
        }
        headIcon.setVisibility(this.getVisibility());

        /**
         * Perform animation.
         */
        if (lastAnimationUpdate <= System.currentTimeMillis() - 10) {
            lastAnimationUpdate = System.currentTimeMillis();
            animationIndex += 0.02f;
            if (animationIndex > 1) {
                animationIndex -= 1;
            }
            this.animate(animationIndex, currentAnimation);
        }
    }

    public void followTheLeader() {
        boolean isMoving = false;
        /**
         * Follow the leader.
         */
        if (Utils.isInRange(26, this, leader)) {
            // Don't need to move..
        } else if (!Utils.isInRange(250, this, leader)) {
            // Too far away! Teleport to the player.
            switch (RANDOM.nextInt(4) + 1) {
                case 1:
                    this.translate(5, 0, 0);
                    break;
                case 2:
                    this.translate(-5, 0, 0);
                    break;
                case 3:
                    this.translate(0, 0, 5);
                    break;
                case 4:
                    this.translate(0, 0, -5);
                    break;
            }
        } else if (!Utils.isInRange(10, this, leader)) {
            // Follow at the leader! (Move Forward)
            Utils.lookAt(this, leader); // Look at the leader
            // move forward
            SimpleVector t = this.getZAxis();
            t.scalarMul(currentSpeed);
            moveRes.add(t);
            isMoving = true;
        }

        this.translate(0, -0.025f, 0); // prevent sinking

        moveRes = this.checkForCollisionEllipsoid(moveRes, ellipsoid, 8);
        this.translate(moveRes);

        // finally apply the gravity:
        SimpleVector t = new SimpleVector(0, 1, 0);
        t = this.checkForCollisionEllipsoid(t, ellipsoid, 1);
        this.translate(t);

        // damping
        if (moveRes.length() > DAMPING) {
            moveRes.makeEqualLength(new SimpleVector(0, 0, DAMPING));
        } else {
            moveRes = new SimpleVector(0, 0, 0);
        }
        currentAnimation = isMoving ? getMovementAnimation(type) : getIdleAnimation(type);
    }

    public int getIdleAnimation(int type) {
        switch (type) {
            case 0: // warrior
            case 1: // archer
            case 2: // wizard
                return 3;
            case 3:
                return 1; // dragon
            case 4: // ape
                return 1;
            case 5: // villager
                return 1;
            case 6: // goblin
            case 7: // skeleton
            case 8: // protector
                return 1;
            default:
                return 0; // unsupported
        }
    }

    public int getMovementAnimation(int type) {
        switch (type) {
            case 0:
            case 1:
            case 2:
                return 4; // humans
            case 3:
			// 6 = walk
                // 7 = fly
                // maybe we should .... randomize which animation is used?
                return 6; // dragon
            case 4: // ape
                return 2;
            case 5: // zombie
                return 2;
            case 6: // goblin
            case 7: // skeleton
            case 8: // protector
                return 2; // XXX UNTESTED!!
            default:
                return 0; // unsupported
        }
    }

    public int getAttackAnimation(int type) {
        switch (type) {
            case 0: // warrior
            case 1: // archer
            case 2: // wizard
                return 6;
            case 3:
                return RANDOM.nextInt(3) + 1; // dragon
            case 4: // ape
                return 2; // XXX UNTESTED!!
            case 5: // villager
                return 3; // throwing something
            case 6: // goblin
            case 7: // skeleton
            case 8: // protector
                return 3; // XXX UNTESTED!!
            default:
                return 0; // unsupported
        }
    }

    /**
     * Returns the collision dimensions.
     *
     * @note Estimated model size, for spherical collision..
     */
    public SimpleVector getSize(int type) {
        // Math.abs = convert negative to positive
        switch (type) {
            case 0: // warior
            case 1: // archer
            case 2: // wizard
                return new SimpleVector(4.25f, 10.9f, 4.25f);
            case 3: // dragon
                return new SimpleVector(8f, 15.5f, 12f);
            case 4: // ape
                return new SimpleVector(4.25f, 11.5f, 5.25f);
            case 5: // villager
                return new SimpleVector(4.2f, 9f, 4.2f);
            case 6: // goblin
                return new SimpleVector(2.5f, 6.25f, 2.5f);
            case 7: // skeleton
                return new SimpleVector(4.25f, 12, 4.25f);
            case 8: // protector
                return new SimpleVector(6.5f, 14, 6.5f);
            default:
                return new SimpleVector(5f, 10, 5f);
        }
    }

    /**
     * Assign this npc to a leader .. example: as the player's familiar / pet / body guard
     */
    public void setLeader(Object3D leader) {
        this.leader = leader;
    }

    public int getType() {
        return type;
    }

    public int getUid() {
        return uid;
    }

}
