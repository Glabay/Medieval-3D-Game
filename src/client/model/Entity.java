package client.model;

public abstract class Entity {

	protected float xPos, yPos, zPos;
	
	private long uid;
	private int combatLevel;
	
	public int getCombatLevel() {
		return combatLevel;
	}
	
	public void setCombatLevel(int combatLevel) {
		this.combatLevel = combatLevel;
	}
	
	public long getUid() {
		return uid;
	}
	
	public void setUid(long uid) {
		this.uid = uid;
	}
}
