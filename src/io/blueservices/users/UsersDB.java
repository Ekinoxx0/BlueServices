package io.blueservices.users;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.stc.StaticID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

@SuppressWarnings("unused")
public class UsersDB {

	public static final File saveFolder = new File("." + File.separator + "users" + File.separator);
	private Logger log = Loggers.getLogger(UsersDB.class);
	private ArrayList<PlatformUser> users = null;
	private final BlueServices services;

	public UsersDB(BlueServices services)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		this.services = services;
		if (!saveFolder.exists()) {
			saveFolder.mkdirs();
		}

		this.load();
		this.log.info("Finished loading " + this.users.size() + " users file into UsersDB..");
	}

	private void load() throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		this.users = new ArrayList<PlatformUser>();
		for (File userFile : saveFolder.listFiles()) {
			try {
				PlatformUser user = PlatformUser.loadFromFile(userFile);
				if (user != null) {
					users.add(user);
				} else {
					log.error("User " + userFile.getName() + " is null, failed to load.");
				}
			} catch (Throwable th) {
				th.printStackTrace();
				log.error("Failed to load user file : " + userFile.getName());
			}
		}
	}

	private void saveAll() {
		try {
			for (PlatformUser user : users) {
				user.save();
			}
			log.info("Succesfuly saved UsersDB");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/*
	 * Others
	 */
	
	public Flux<PlatformUser> getUsers(){
		return Flux.fromIterable(this.users);
	}
	
	public Mono<PlatformUser> get(Member member){
		return this.getFromID(member.getId().asLong());
	}
	
	public Mono<PlatformUser> get(Snowflake id) {
		return this.getFromID(id.asLong());
	}
	
	public Mono<PlatformUser> getFromID(long id) {
		for (PlatformUser puser : users) {
			if (puser.getId() == id) {
				return Mono.just(puser);
			}
		}
		
		Member m = this.services.getClient().getMemberById(StaticID.GUILD, Snowflake.of(id))
				.doOnError(th -> {})
				.block();
		if(m == null) throw new IllegalArgumentException();
		return this.createUser(m);
	}
	
	@SuppressWarnings("unchecked")
	public Set<PlatformUser> searchPlayer(String value){
		Set<PlatformUser> users = new HashSet<PlatformUser>();
		log.info("Launching search on \'" + value + "\'");

		for (PlatformUser puser : (ArrayList<PlatformUser>) this.users.clone()) {
			if (puser.getId() + "" == value) {
				users.add(puser);
			}
			
			if(puser.getRPName().toLowerCase().contains(value.toLowerCase())) {
				users.add(puser);
			}
			
			if(puser.getUsername().toLowerCase().contains(value.toLowerCase())) {
				users.add(puser);
			}
		}
		
		return users;
	}

	public boolean exist(Member member) {
		try {
			this.get(member).block();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Mono<PlatformUser> createUser(Member mono) {
		long id = mono.getId().asLong();
		for (PlatformUser puser : this.users) {
			if (puser.getId() == id) {
				throw new IllegalStateException();
			}
		}
		
		PlatformUser user = new PlatformUser(mono);
		this.users.add(user);
		return Mono.just(user);
	}
	
	public void delete(PlatformUser user) {
		user.getSaveFile().delete();
		this.users.remove(user);
	}
	
}
