package io.blueservices.feeds.abst;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.commands.abst.HelpCommand;
import io.blueservices.users.PlatformUser;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public abstract class AbstractFeed {

	private static final List<Character> cmdCharacter = Arrays.asList('!', ',', '.', '/', '\\', '_', '%', '=', '\'',
			'-', '?', ':');
	public final Logger log = Loggers.getLogger(this.getClass());
	protected final BlueServices services;
	private final List<AbstractCmd> cmds;
	private final boolean acceptNonCommandMessages;

	public AbstractFeed(BlueServices services, boolean acceptNonCommandMessages, List<AbstractCmd> cmds) {
		if(services == null) throw new IllegalArgumentException("Cannot have null services");
		if(cmds == null) throw new IllegalArgumentException("Cannot have null cmds");
		this.services = services;
		this.acceptNonCommandMessages = acceptNonCommandMessages;
		this.cmds = new ArrayList<AbstractCmd>(cmds);
		
		Reflections reflections = new Reflections("io.blueservices.commands.special");

		Set<Class<? extends AbstractCmd>> allClasses = reflections.getSubTypesOf(AbstractCmd.class);
		for(Class<? extends AbstractCmd> clazz : allClasses) {
			try {
				this.cmds.add((AbstractCmd) clazz.getConstructors()[0].newInstance(services));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		this.cmds.add(new HelpCommand(services, this));
	}

	public boolean onMessage(MessageCreateEvent e) {
		if (e == null)
			return true;
		if (!e.getMember().isPresent())
			return true;
		if (e.getMember().get().isBot())
			return true;
		if (!e.getMessage().getContent().isPresent())
			return false;

		return this.processMsg(e.getMessage().getChannel().cast(TextChannel.class).block(), e.getMessage(),
				e.getMember().get());
	}

	private boolean processMsg(TextChannel senderChannel, Message msg, Member sender) {
		String rawText = msg.getContent().get();
		log.info(sender.getUsername() + "[" + senderChannel.getName() + "]:" + rawText);

		if (!cmdCharacter.contains(rawText.charAt(0))) {
			if (!acceptNonCommandMessages) {
				msg.delete().doOnError(t -> {}).subscribe();

				sEmbedWithDelete(senderChannel, "", "Utilisez ``!help`` pour connaître les commandes...");
				return true;
			}
			return false;
		}

		String[] splitted = rawText.substring(1).split(" ");
		String cmdName = splitted[0].toLowerCase();
		String[] args = Arrays.copyOfRange(splitted, 1, splitted.length);
		final PlatformUser senderUser = this.services.getUsersDB().get(sender).block();

		for (AbstractCmd cmd : this.cmds) {
			if (cmd.isCommand(cmdName)) {
				try {
					msg.addReaction(ReactionEmoji.unicode("🔄")).subscribe();
					cmd.run(senderChannel, msg, senderUser, args);

					msg.delete().subscribe();
				} catch (Throwable ex) {
					ex.printStackTrace();
					try {
						msg.removeAllReactions().doOnSuccess(m -> {
							msg.addReaction(ReactionEmoji.unicode("❌")).subscribe();
						}).subscribe();
						msg.delete().delaySubscription(Duration.ofSeconds(30L)).subscribe();
					} catch (Throwable e) {
					}
				}
				return true;
			}
		}

		sEmbedWithDelete(senderChannel, "Commande ``" + rawText + "`` inconnue !",
				"Utilisez ``!help`` pour connaître les commandes...");
		try {
			msg.delete().doOnError(t -> {}).subscribe();
		} catch (Throwable ex) {}
		return true;
	}

	private void sEmbedWithDelete(TextChannel t, String title, String desc) {
		if(t == null) throw new IllegalArgumentException("Null channel is invalid.");
		if(title == null) throw new IllegalArgumentException("Null title is invalid.");
		t.createEmbed(spec -> {
			String til = title;
			if(til.length() > 60) til = title.substring(0, 60);
			spec.setTitle(til);
			spec.setDescription(desc);
		}).doOnError(th -> {
			th.printStackTrace();
			log.error("t :" + t); 
			log.error("title :" + title); 
			log.error("desc :" + desc); 
		})
		.doOnSuccess(msg -> {
			Mono.just(msg).flatMap(Message::delete).delaySubscription(Duration.ofSeconds(30)).subscribe();
		}).subscribe();
	}

	/*
	 * GETTERS
	 */

	public List<AbstractCmd> getCmds() {
		return this.cmds;
	}

}
