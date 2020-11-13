package io.blueservices.entreprises;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.Main;
import io.blueservices.feeds.GestionPrimesFeed;
import io.blueservices.feeds.PrimesFeed;
import io.blueservices.feeds.ScreensFeed;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class PlatformCompany {

	private static final Logger log = Loggers.getLogger("PCompany");
	
	public static PlatformCompany loadFromFile(File file) throws JsonSyntaxException, JsonIOException, FileNotFoundException, IllegalStateException {
		if(file == null || !file.exists() || !file.canRead()) throw new IllegalStateException("Unable to load from " + file.getName());
		PlatformCompany company = BlueServices.GSON.fromJson(new FileReader(file), PlatformCompany.class);
		
		//New fields
		
		if(company.roles == null) {
			company.roles =  new TreeMap<Integer, Long>();
		}
		
		if(company.runs == null) {
			company.runs = new ArrayList<PlatformRun>();
		}
		
		if(company.accountBook == null) {
			company.accountBook = new ArrayList<PlatformAccountLine>();
		}

		return company;
	}

	/*
	 * 
	 */

	private final long categoryId;
	private String emoji;
	private String name;
	
	private transient long recrutementId = -1;
	private transient long primeId = -1;
	private transient long gestionPrimeId = -1;
	private transient long screenId = -1;

	private boolean allowed = false;
	private boolean hasRunSystem = false;
	
	private boolean dailyPrimes = false;
	private int prime = 10;
	private int receivedByRun = 10;
	private boolean manualValidation = false;

	private transient ScreensFeed screensFeed = null;
	private transient GestionPrimesFeed gestionPrimesFeed = null;
	private transient PrimesFeed primesFeed = null;
	
	private transient TreeMap<Integer, Long> roles = new TreeMap<Integer, Long>();
	private ArrayList<PlatformRun> runs = new ArrayList<PlatformRun>();
	private ArrayList<PlatformAccountLine> accountBook = new ArrayList<PlatformAccountLine>();
	

	public PlatformCompany(Category category, String emoji, String name) throws IllegalStateException {
		this.categoryId = category.getId().asLong();
		this.emoji = emoji;
		this.name = name;
		this.save();
	}
	
	public void save() {
		try {
			FileWriter writer = new FileWriter(getSaveFile());
			writer.write(BlueServices.GSON.toJson(this));
			writer.close();
			log.info("Saved " + this.getCompanyName() + " successfuly");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public File getSaveFile() {
		return new File(CompanyDB.saveFolder, this.categoryId + ".json");
	}

	public void setEmoji(String emoji) {
		log.info("Updating emoji for " + this.name);
		this.emoji = emoji;
		this.save();
	}

	public void setCompanyName(String name) {
		log.info("Updating name for " + this.name);
		this.name = name;
		this.save();
	}

	public void verifyValidity() {
		this.recrutementId = -1;
		this.primeId = -1;
		this.screenId = -1;
		
		this.getCategory()
			.flatMapMany(Category::getChannels)
			.filter(channel -> channel instanceof TextChannel)
			.subscribe(txt -> {
				if(txt.getName().contains("recru")) {
					this.recrutementId = txt.getId().asLong();
				}
				if(txt.getName().contains("prime") && !txt.getName().contains("gestion")) {
					this.primeId = txt.getId().asLong();
				}
				if(txt.getName().contains("gestion-primes")) {
					this.gestionPrimeId = txt.getId().asLong();
				}
				if(txt.getName().contains("screen")) {
					this.screenId = txt.getId().asLong();
				}
			});
		
		if(this.recrutementId < 0) {
			throw new IllegalStateException("Invalid company \"" + this.getCompanyName() + "\" : no recrutement");
		}
		
		if(this.screenId > 0) {
			log.info("Company " + this.name + " has run system");
			if(!hasRunSystem) {
				log.warn("New company with run system !");
				hasRunSystem = true;
			}
		} else if(hasRunSystem) {
			log.warn("Delete run system of company : " + this.getCompanyName());
			hasRunSystem = false;
			//TODO No more run system
		}
		
		
	}
	
	public boolean isAllowed() {
		return this.allowed;
	}
	
	public void setAllowed(boolean b) {
		this.allowed = b;
		this.save();
	}

	public void addRole(Role role) {
		if(!role.getName().contains(this.emoji)) throw new IllegalArgumentException();
		roles.put(role.getRawPosition(), role.getId().asLong());
	}
	
	public void verifyRoles() {
		if(roles.isEmpty()) throw new IllegalStateException("No role in entreprise : " + this.getCompanyName());
		if(roles.size() < 5) throw new IllegalStateException("No 5 role in entreprise : " + this.getCompanyName());
		
		List<Member> list = Main.SERVICES.getClient().getGuildById(StaticID.GUILD).flatMapMany(Guild::getMembers).filter(m -> m.getRoleIds().contains(Snowflake.of(this.getPatronRoleId()))).collectList().block();
		if(list == null) {
			throw new IllegalStateException("Patron error for " + this.getCompanyName() + ": " + "null");
		} else if(list.size() > 1) {
			for(Member m : list) {
				log.info(m.getDisplayName());
			}
			log.info(Main.SERVICES.getClient().getRoleById(StaticID.GUILD, Snowflake.of(this.getPatronRoleId())).block().getName() + " : " + this.getPatronRoleId());
			log.error("Patron error for " + this.getCompanyName() + ": " + list.size());
		} else if(list.isEmpty()) {
			log.info(Main.SERVICES.getClient().getRoleById(StaticID.GUILD, Snowflake.of(this.getPatronRoleId())).block().getName() + " : " + this.getPatronRoleId());
			throw new IllegalStateException("Patron error for " + this.getCompanyName() + ": No patron !");
		}  else {
			log.info("Patron of " + this.getCompanyName() + " is : " + list.get(0).getDisplayName());
		}
	}
	
	public void setupFeeds(BlueServices services) {
		if(this.hasRunSystem()) {
			this.screensFeed = new ScreensFeed(services, this.screenId, this);
			if(this.gestionPrimeId > 0) {
				this.gestionPrimesFeed = new GestionPrimesFeed(services, this.gestionPrimeId, this);
			}
			this.primesFeed = new PrimesFeed(services, this.primeId, this);
		}
	}

	/*
	 * Getters
	 */

	public boolean isPatron(PlatformUser target) {
		return isPatron(target.getMember().block()) || target.getId() == StaticID.MASTER.asLong();
	}

	public boolean isPatron(Member target) {
		return this.getPatron().any(patron -> patron.getId().equals(target.getId())).block();
	}
	
	public Flux<Member> getPatron() {
		return Main.SERVICES.getClient().getGuildById(StaticID.GUILD).flatMapMany(Guild::getMembers).filter(m -> m.getRoleIds().contains(Snowflake.of(this.getPatronRoleId())));
	}
	
	public Long getPatronRoleId() {
		return roles.lastEntry().getValue();
	}

	public long getCategoryId() {
		return this.categoryId;
	}
	
	public String getEmoji() {
		return this.emoji;
	}

	public String getCompanyName() {
		return this.name;
	}
	
	public Mono<Category> getCategory(){
		return Main.SERVICES.getClient().getChannelById(Snowflake.of(this.categoryId)).cast(Category.class);
	}
	
	public boolean hasRunSystem() {
		return hasRunSystem;
	}
	
	public boolean isManualValidation() {
		return this.manualValidation;
	}

	public boolean workIn(Member member) {
		return member.getRoles().any(role -> this.roles.containsValue(role.getId().asLong())).block();
	}

	public void payRun(PlatformUser source, PlatformUser target, int amountPaid) {
		List<PlatformRun> paidRun = new ArrayList<PlatformRun>();
		int mustProvide = 0;
		
		try {
			for(PlatformRun run : this.runs) {
				if(run.runnerId != target.getId()) continue;
				if(run.isPaid()) continue;
				if(amountPaid <= 0) break;
				if(amountPaid - run.howMuch() < 0) throw new IllegalStateException("Montant pas rond...\nPayez plutôt " + (mustProvide > 0 ? mustProvide + "$ ou " : "") + (mustProvide + run.howMuch()) + "$");

				amountPaid -= run.howMuch();
				mustProvide += run.howMuch();
				run.pay();
				paidRun.add(run);
			}
			this.pay(source, target, -mustProvide, "Paiement primes");
		} catch(Exception ex) {
			for(PlatformRun r : paidRun) {
				r.cancel();
			}
			throw ex;
		}
		
		this.save();
	}

	public void pay(PlatformUser source, PlatformUser target, int amount, String reason) {
		if(amount == 0) throw new IllegalStateException("Erreur de montant vers le livre des comptes.");
		if(target == null) throw new IllegalStateException("Erreur de cible vers le livre des comptes.");
		log.info("New account line in " + this.getCompanyName() + ":");
		log.info("    TARGET " + target.getRPName());
		log.info("    TARGET " + target.getId());
		log.info("    SOURCE " + source.getRPName());
		log.info("    SOURCE " + source.getId());
		log.info("    AMOUNT " + amount + "$");
		log.info("    REASON " + reason);
		accountBook.add(new PlatformAccountLine(amount, target.getRPName(), source.getRPName(), reason));
		this.save();
	}

	public void run(PlatformUser target) {
		runs.add(new PlatformRun(target.getId(), this));
		accountBook.add(new PlatformAccountLine(receivedByRun, "Entreprise" + this.getCompanyName(), target.getRPName(), "Run effectuée"));
		this.save();
	}

	public List<PlatformAccountLine> getAccountBook() {
		return this.accountBook;
	}

	public void setDailyPrimes(boolean value) {
		this.dailyPrimes = value;
		this.save();
	}

	public boolean isDailyPrimes() {
		return this.dailyPrimes;
	}

	public void setManualValidation(boolean value) {
		this.manualValidation = value;
		this.save();
	}

	public void setPrimeAmount(int value) {
		this.prime = value;
		this.save();
	}

	public void setReceivedByRun(int value) {
		this.receivedByRun = value;
		this.save();
	}

	public int debtTo(PlatformUser target) {
		int debt = 0;
		for(PlatformRun run : this.runs) {
			if(run.runnerId != target.getId()) continue;
			if(!run.isPaid()) debt += run.howMuch();
		}
		return debt;
	}
	
	public int runNumber(PlatformUser target) {
		int n = 0;
		for(PlatformRun run : this.runs) {
			if(run.runnerId != target.getId()) continue;
			n++;
		}
		return n;
	}

	public int alreadyPaidTo(PlatformUser target) {
		int debt = 0;
		for(PlatformRun run : this.runs) {
			if(run.runnerId != target.getId()) continue;
			if(run.isPaid()) debt += run.howMuch();
		}
		return debt;
	}

	public int getPrimePrice() {
		return this.prime;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof PlatformCompany)) return false;
		return ((PlatformCompany) o).getCategoryId() == this.getCategoryId();
	}

	public ScreensFeed getScreensFeed() {
		return this.screensFeed;
	}

}
