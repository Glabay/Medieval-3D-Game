package client.model.player.skills;

public class Skills {

	private static String[] SKILLS = {
			"Attack",
			"Strength",
			"Defence",
			"Range",
			"Magic",
			"Hitpoints"
			
	};
	
	public static int getSkillID(String skill) {
		int skillId = -1;
		for (int i = 0; i <= SKILLS.length; i++) {
			if (SKILLS[i].equalsIgnoreCase(skill)) {
				skillId = i;
			}
		}
		
		return skillId;
	}
	
	public static String getSkillName(int skillId) {
		return SKILLS[skillId];
	}
}
