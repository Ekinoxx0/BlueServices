package io.blueservices.users;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.Interval;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.Main;
import io.blueservices.stc.StaticID;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class PlatformUser {

	private static final PeriodFormatter formatter = new PeriodFormatterBuilder()
			 .appendYears()
			 .appendSuffix(" année ", " années ")
			 .appendMonths()
			 .appendSuffix(" mois ")
		     .appendDays()
		     .appendSuffix(" jour ", " jours ")
		     .appendHours()
		     .appendSuffix(" heure ", " heures ")
		     .appendMinutes()
		     .appendSuffix(" minute ", " minutes ")
		     .toFormatter();
	private static final Logger log = Loggers.getLogger(PlatformUser.class);
	
	public static PlatformUser loadFromFile(File file) throws JsonSyntaxException, JsonIOException, FileNotFoundException, IllegalStateException {
		if(file == null || !file.exists() || !file.canRead()) throw new IllegalStateException("Unable to load from " + file.getName());
		PlatformUser user = BlueServices.GSON.fromJson(new FileReader(file), PlatformUser.class);
		
		//New fields

		if(user.arrivalDate == 0) {
			user.arrivalDate = System.currentTimeMillis();
		}
		
		if(user.rpName == null) {
			user.getRPName();
		}
		
		return user;
	}

	/*
	 * 
	 */
	
	
	private long id;
	private final String username;
	private String rpName = null;
	
	private long arrivalDate = System.currentTimeMillis();
	
	private boolean isPremium = false;
	

	public PlatformUser(Member member) throws IllegalStateException {
		this.id = member.getId().asLong();
		this.username = member.getUsername();
		this.rpName = member.getDisplayName();
		this.save();
	}
	
	public void save() {
		try {
			FileWriter writer = new FileWriter(getSaveFile());
			writer.write(BlueServices.GSON.toJson(this));
			writer.close();
			log.info("Saved " + this.getUsername() + " successfuly");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public File getSaveFile() {
		return new File(UsersDB.saveFolder, this.id + ".json");
	}

	public long getId() {
		return this.id;
	}

	public String getUsername() {
		return this.username;
	}
	
	public String getRPName() {
		return this.rpName;
	}
	
	public Mono<Member> getMember(){
		return Main.SERVICES.getClient().getMemberById(StaticID.GUILD, Snowflake.of(this.id));
	}
	
	public long getArrivalDate() {
		return this.arrivalDate;
	}
	
	public String onPlatformSince() {
		return formatter.print(new Interval(this.getArrivalDate(), System.currentTimeMillis()).toPeriod());
	}

	public boolean isPremium() {
		return isPremium;
	}

	public void changeName(String displayName) {
		this.rpName = displayName;
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof PlatformUser)) return false;
		return ((PlatformUser) o).getId() == this.getId();
	}
	
}
