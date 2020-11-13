package io.blueservices.stc;

import reactor.util.Logger;
import reactor.util.Loggers;

public class StaticFunction {
	
	@SuppressWarnings("unused")
	private static final Logger log = Loggers.getLogger("BlueServices");
	
	public static boolean containsURL(String txt) {
		return txt.contains("http:") || txt.contains("://") || txt.contains("https:") ||
			   txt.contains(".fr") || txt.contains(".com") || txt.contains("discord.gg");
	}
	
	public static int getDailyPrimeMaximum() {
		return 10_000;
	}
	
}
