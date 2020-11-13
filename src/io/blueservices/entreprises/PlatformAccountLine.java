package io.blueservices.entreprises;

import java.util.UUID;

import reactor.util.Logger;
import reactor.util.Loggers;

public class PlatformAccountLine {
	
	private static final Logger log = Loggers.getLogger("Accounts");
	
	public final UUID uuid = UUID.randomUUID();
	public final long time = System.currentTimeMillis();
	
	private int amount;
	private String targetName;
	private String sourceName;
	private String reason;
	
	public PlatformAccountLine(int amount, String targetName, String sourceName, String reason) {
		this.amount = amount;
		this.targetName = targetName;
		this.sourceName = sourceName;
		this.reason = reason;
		log.info(uuid.toString().split("-")[0] + " " + targetName + " | " + (amount >= 0 ? "+" : "") + amount + " " + reason);
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public long getTime() {
		return this.time;
	}

	public int getAmount() {
		return this.amount;
	}

	public String getTargetName() {
		return this.targetName;
	}

	public String getSourceName() {
		return this.sourceName;
	}

	public String getReason() {
		return this.reason;
	}
	
}
