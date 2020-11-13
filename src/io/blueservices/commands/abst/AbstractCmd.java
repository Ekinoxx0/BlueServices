package io.blueservices.commands.abst;

import java.awt.Color;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import io.blueservices.BlueServices;
import io.blueservices.stc.Maps;
import io.blueservices.users.PlatformUser;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public abstract class AbstractCmd {

	public final Logger log = Loggers.getLogger(this.getClass());
	protected final BlueServices services;
	private final String cmd;
	private final Set<String> aliases;
	private final String usage;
	private final String details;
	private final boolean doesHelp;
	private Map<String, String> aliasesDetails;

	public AbstractCmd(BlueServices services, String cmd, List<String> aliases, String usage, String details) {
		this(services, cmd, aliases, usage, details, true);
	}
	public AbstractCmd(BlueServices services, String cmd, List<String> aliases, String usage, String details, boolean doesHelp) {
		this.services = services;
		this.cmd = cmd.toLowerCase();
		this.usage = usage;
		this.details = details;
		this.doesHelp = doesHelp;
		if(details.length() > 36) log.warn("Details of !" + this.getCmd() + " are too long...");
		this.aliases = new HashSet<String>();
		aliases.forEach(alias -> this.aliases.add(alias.toLowerCase()));
		this.aliasesDetails = Maps.of();
	}

	public AbstractCmd(BlueServices services, String cmd, List<String> aliases, String usage, String details, Map<String, String> aliasesDetails) {
		this(services, cmd, aliases, usage, details);
		aliasesDetails.forEach((key, value) -> {
			if(!this.aliases.contains(key.toLowerCase()) && this.getCmd() != key.toLowerCase()) {
				throw new IllegalArgumentException("Aliases details incompatible with aliases simple.");
			}
			
			if(key.length() >= 20) log.warn("Too long alias : "  + key);
			if(value.length() > 36) log.warn("Too long details for aliasesDetails : " + key);
		});
		this.aliasesDetails = aliasesDetails;
	}
	
	public abstract void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args);

	/*
	 * Specials
	 */
	
	public void sEmbedWithDelete(TextChannel t, String desc) {
		sEmbedWithDelete(t, "", desc);
	}
	
	public Message sEmbed(TextChannel t, String title, String desc) {
		return t.createEmbed(spec -> {
			spec.setTitle(title);
			spec.setDescription(desc);
		}).block();
	}
	
	public void sEmbedWithDelete(TextChannel t, String title, String desc) {
		Mono.just(sEmbed(t, title, desc)).flatMap(Message::delete).delaySubscription(Duration.ofSeconds(30)).subscribe();
	}
	
	public void msgWithDelete(TextChannel t, Consumer<? super MessageCreateSpec> spec) {
		Message m = t.createMessage(spec).block();
		Mono.just(m).flatMap(Message::delete).delaySubscription(Duration.ofSeconds(30)).subscribe();
	}
	
	public void embedWithDelete(TextChannel t, Consumer<? super EmbedCreateSpec> spec) {
		Message m = t.createEmbed(spec).block();
		Mono.just(m).flatMap(Message::delete).delaySubscription(Duration.ofSeconds(30)).subscribe();
	}
	
	public void noPermission(TextChannel t) {
		embedWithDelete(t, embed -> {
			embed.setColor(Color.RED);
			embed.setTitle("Permission insuffisantes..");
			embed.setDescription("Vous n'avez pas les permissions suffisantes");
		});
	}
	
	public String fromArgs(String[] args) {
		return fromArgsExcept(args, 0);
	}
	
	public String fromArgsExcept(String[] args, int nToIgnore) {
		String txt = "";
		int n = 0;
		for(String arg : args) {
			n++;
			if(n <= nToIgnore) continue;
			txt += arg + " ";
		}
		
		return txt.substring(0, txt.length()-1);
	}
	
	protected void error(TextChannel channel) {
		channel.createEmbed(spec -> {
			spec.setTitle("**__MESSAGE PERDU..__**");
			spec.setDescription("Une erreur est survenue...\nUtilisez !report en cas de problème");
			spec.setColor(Color.RED);
		}).subscribe();
	}
	
	protected void successSent(TextChannel channel, String titleAdd, String description) {
		channel.createEmbed(spec -> {
			spec.setTitle(
					"**__ENVOYÉ AVEC SUCCES__**" + titleAdd);
			spec.setDescription(description);
			spec.setColor(Color.GREEN);
		}).subscribe();
	}
	
	/*
	 * Getters
	 */
	
	public String getCmd() {
		return cmd;
	}
	
	public boolean isCommand(String cmdName) {
		return cmd.equals(cmdName) || this.aliases.contains(cmdName);
	}

	protected String getArgUsage() {
		return usage;
	}

	protected String getCompleteUsage() {
		return ":x: :interrobang:\n" + 
				"**Utilisez :** " + this.getCommandUsage() + "\n" + 
				"Exemple : " + this.getExample();
	}

	protected String getCommandUsage() {
		return "``!" + this.getCmd() + " " + this.getArgUsage() + "``";
	}
	
	protected String getExample() {
		return "!" + this.getCmd() + " " + 
				this.getArgUsage()
							.replace("<ID>", "abc1")
							.replace("<MESSAGE>", "Un message plutôt cool..")
							.replace("<N/10>", "7")
							.replace("<SERVICE>", "WEED");
	}

	protected String getDetails() {
		return details;
	}
	
	protected Map<String, String> getAliasesDetails(){
		return this.aliasesDetails;
	}
	
	protected boolean doesHelp() {
		return this.doesHelp;
	}
	
}
