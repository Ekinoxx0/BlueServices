package io.blueservices.stc;

import java.util.HashMap;
import java.util.Map;

public class Maps {

	public static Map<String, String> of(){
		return new HashMap<String, String>();
	}
	
	public static Map<String, String> of(String s1, String s2){
		HashMap<String, String> m = new HashMap<String, String>();
		m.put(s1, s2);
		return m;
	}
	
}
