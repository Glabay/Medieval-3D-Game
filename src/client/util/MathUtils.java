package client.util;

import com.threed.jpct.SimpleVector;

public class MathUtils {
	
	public static boolean isInRange(float distance, SimpleVector source, SimpleVector target) {
		SimpleVector p = new SimpleVector(source);
		SimpleVector t = new SimpleVector(target);
		p.y = 0;
		t.y = 0;
		return p.distance(t) <= distance;
	}
	
}

