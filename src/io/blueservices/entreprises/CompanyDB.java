package io.blueservices.entreprises;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.stc.StaticID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

@SuppressWarnings("unused")
public class CompanyDB {

	public static final File saveFolder = new File("." + File.separator + "company" + File.separator);
	private Logger log = Loggers.getLogger(CompanyDB.class);
	private ArrayList<PlatformCompany> companies = null;
	private final BlueServices services;

	public CompanyDB(BlueServices services)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		this.services = services;
		if (!saveFolder.exists()) {
			saveFolder.mkdirs();
		}

		this.load();
		this.log.info("Finished loading " + this.companies.size() + " companies file into CompanyDB..");
	}

	private void load() throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		this.companies = new ArrayList<PlatformCompany>();
		for (File companyFile : saveFolder.listFiles()) {
			try {
				PlatformCompany company = PlatformCompany.loadFromFile(companyFile);
				if (company != null) {
					companies.add(company);
				} else {
					log.error("Company " + companyFile.getName() + " is null, failed to load.");
				}
			} catch (Throwable th) {
				log.error("Failed to load company file : " + companyFile.getName());
				throw th;
			}
		}
	}

	private void saveAll() {
		try {
			for (PlatformCompany user : companies) {
				user.save();
			}
			log.info("Succesfuly saved CompanyDB");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/*
	 * Others
	 */
	
	@SuppressWarnings("unchecked")
	public void init() {
		cat:
		for(Category cat : this.services.getClient()
				.getGuildById(StaticID.GUILD)
				.flatMapMany(Guild::getChannels)
				.filter(c -> c instanceof Category)
				.cast(Category.class)
				.filter(c -> !c.getName().contains("Salon")).collectList().block()) {
			log.info("Detecting : " + cat.getName());
			if(!cat.getName().contains("「") || !cat.getName().contains("」")) {
				throw new IllegalStateException("Category has changed names !!!");
			}
			String[] raw = cat.getName().replace("「", "").split("」");
			String emoji = raw[0];
			String name = raw[1];
			
			boolean alreadyExist = false;
			for(PlatformCompany existingCompany : this.companies) {
				if(existingCompany.getCategoryId() != cat.getId().asLong()) continue;
				
				if(!existingCompany.getEmoji().equals(emoji)) {
					existingCompany.setEmoji(emoji);
				}
				
				if(!existingCompany.getCompanyName().equals(name)) {
					existingCompany.setCompanyName(name);
				}
				
				existingCompany.verifyValidity();
				
				continue cat;
			}

			for(PlatformCompany existingCompany : this.companies) {
				if(existingCompany.getCompanyName().equals(name) || existingCompany.getEmoji().equals(emoji)) {
					throw new IllegalStateException("Existing company changed category !!!");
				}
			}
			
			//Only if not existing
			
			PlatformCompany newCompany = this.createCompany(cat, emoji, name);
			newCompany.verifyValidity();
		
		}
		
		log.info("________________________");

		for(Role role : this.services.getClient()
				.getGuildById(StaticID.GUILD)
				.flatMapMany(Guild::getRoles).collectList().block()) {
			List<PlatformCompany> companies = this.getCompanies().filter(company -> role.getName().contains(company.getEmoji())).collectList().block();
			if(companies.size() > 1) {
				throw new IllegalStateException("Multiple companies corresponding to role " + role.getName());
			} else if(companies.size() == 0) {
				log.info("IGNORING: " + (role.getName()));
				continue;
			}
			if(role.getName().contains("Cat")) continue;
			
			PlatformCompany correspondingCompany = companies.get(0);
			if(role.getName().split("」")[1].equals(correspondingCompany.getCompanyName())) {
				log.info("In Company Role : " + role.getName().split("」")[1] + "");
				continue;
			}
			
			log.info("[" + role.getRawPosition() + "] Role: " + role.getName().split("」")[1] + "");
			correspondingCompany.addRole(role);
		
		}
		
		this.saveAll();
		
		for(PlatformCompany company : (ArrayList<PlatformCompany>) this.companies.clone()) {
			try {
				company.verifyRoles();
				if(company.hasRunSystem()) {
					company.setupFeeds(services);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				this.companies.remove(company);
			}
		}
		
		this.getCompanies().subscribe(company -> {
		});
		
	}
	
	private PlatformCompany createCompany(Category cat, String emoji, String name) {
		log.info("Creating new company " + name + " with emoji (" + emoji + ") at category " + cat.getId());
		PlatformCompany c = new PlatformCompany(cat, emoji, name);
		this.companies.add(c);
		return c;
	}
	
	/*
	 * 
	 */

	public Flux<PlatformCompany> getCompanies(){
		return Flux.fromIterable(this.companies);
	}
	
	public Mono<PlatformCompany> getFromCategoryID(long id) {
		for (PlatformCompany company : companies) {
			if (company.getCategoryId() == id) {
				return Mono.just(company);
			}
		}

		throw new NullPointerException();
	}

	public Mono<PlatformCompany> getFromAnyChannel(TextChannel senderChannel) {
		for (PlatformCompany company : companies) {
			if (company.getCategoryId() == senderChannel.getCategory().block().getId().asLong()) {
				return Mono.just(company);
			}
		}

		throw new NullPointerException();
	}

}
