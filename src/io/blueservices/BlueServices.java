package io.blueservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import io.blueservices.entreprises.CompanyDB;
import io.blueservices.feeds.AdminFeed;
import io.blueservices.feeds.TwitterFeed;
import io.blueservices.feeds.UsersFeed;
import io.blueservices.stc.StaticID;
import io.blueservices.users.UsersDB;
import reactor.util.Logger;
import reactor.util.Loggers;

@SuppressWarnings("unused")
public class BlueServices {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private DiscordClient client;
	private Logger log;

	private AdminFeed admin;
	private UsersFeed usersFeed;

	private UsersDB users;
	private CompanyDB companies;

	public BlueServices() {
		this.log = Loggers.getLogger("BServices");

		try {
			this.users = new UsersDB(this);
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Unable to proceed to UsersDB creation...");
			return;
		}

		try {
			this.companies = new CompanyDB(this);
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Unable to proceed to CompanyDB creation...");
			return;
		}

		this.client = new DiscordClientBuilder("NjE0ODc5MDA1MTU3NDkwNzM5.XWKjLA.0irr5wZ7tqTjXn-I8E_fzF8LWLk").build();

		try {
			this.admin = new AdminFeed(this);
			this.usersFeed = new UsersFeed(this);
			new TwitterFeed(this);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Unable to create feeds...");
			return;
		}

		EventDispatcher ed = this.client.getEventDispatcher();

		ed.on(ReadyEvent.class).subscribe(this::onReady);
		ed.on(MessageCreateEvent.class).filter(mce -> mce.getMessage().getChannel().block() instanceof PrivateChannel).subscribe(this::onDM);
		ed.on(MemberJoinEvent.class).subscribe(this::onJoin);
		ed.on(MemberLeaveEvent.class).subscribe(this::onLeave);
		ed.on(DisconnectEvent.class).subscribe(this::onDisconnect);
		ed.on(MemberUpdateEvent.class).subscribe(this::onMemberUpdate);
	}

	public void login() {
		this.client.login().block();
	}

	private void onReady(ReadyEvent ready) {
		log.info("BlueServices is now logged.");
		client.updatePresence(Presence.online(Activity.watching("les entreprises travailler"))).subscribe();

		/*
		 * this.client.getGuildById(StaticID.GUILD).flatMapMany(Guild::getChannels)
		 * .filter(g -> g.getId().asLong() == 614885520782262274L || g.getId().asLong()
		 * == 615185490806571018L).cast(TextChannel.class) .subscribe(guild -> {
		 * guild.createMessage(msg -> { msg.setContent("@Toxique#5825");
		 * msg.setEmbed(embed -> { embed.setColor(Color.RED);
		 * embed.setTitle("__**Correction des grades**__"); embed.setDescription("\n" +
		 * "Un problème est survenu !\n" +
		 * "Deux patrons chez l'entreprise **Bûcheron**.\n" +
		 * "Impossible de continuer la mise en place du bot avec cette incohérence...\n\n"
		 * + "**__Choix effectué :__** Sam Jovanic OU Remu\n" +
		 * "Aucun information suffisante dans #info-bs\n" +
		 * "Il semblerai qu'il soit impossible de déterminer qui est le véritable patron !"
		 * ); }); }).block(); });
		 */

		try {
			this.companies.init();
		} catch (Throwable th) {
			th.printStackTrace();
			System.exit(234242);
		}
	}

	private void onMemberUpdate(MemberUpdateEvent e) {
		this.users.getFromID(e.getMemberId().asLong()).subscribe(user -> {
			user.changeName(e.getMember().block().getDisplayName());
		});
	}

	private void onDisconnect(DisconnectEvent e) {
	}

	private void onDM(MessageCreateEvent msg) {
		if(!msg.getMessage().getContent().isPresent()) return;
		User u = msg.getMessage().getAuthor().get();
		if(u.equals(client.getSelf().block())) return;
		
		log.info(msg.getMessage().getContent().get());
		this.client.getUserById(StaticID.MASTER).flatMap(User::getPrivateChannel).subscribe(channel -> {
			Message m = channel.createEmbed(spec -> {
				spec.setTitle("**" + u.getUsername() + "** | " + u.getId().asLong());
				spec.setDescription(msg.getMessage().getContent().get());
			}).block();
			log.info(m.getId().asLong() + "");
		});
		
	}

	private void onJoin(MemberJoinEvent join) {
		this.users.get(join.getMember());
	}

	private void onLeave(MemberLeaveEvent join) {
		this.users.get(join.getUser().getId()).subscribe(user -> this.users.delete(user));
	}

	/*
	 * GETTER
	 */

	public UsersDB getUsersDB() {
		return this.users;
	}

	public CompanyDB getCompanyDB() {
		return this.companies;
	}

	public DiscordClient getClient() {
		return this.client;
	}

	public UsersFeed getUsersFeed() {
		return this.usersFeed;
	}

}
