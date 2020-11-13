package io.blueservices.feeds;

import java.awt.Color;
import java.time.Duration;
import java.util.Arrays;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.commands.screens.AccountBookCommand;
import io.blueservices.commands.screens.AccountBookLinesCommand;
import io.blueservices.commands.screens.AllowCommand;
import io.blueservices.commands.screens.DisallowCommand;
import io.blueservices.commands.screens.InfoCommand;
import io.blueservices.commands.screens.PayCommand;
import io.blueservices.commands.screens.PrimeCommand;
import io.blueservices.commands.screens.SettingsCommand;
import io.blueservices.commands.screens.TableauCommand;
import io.blueservices.commands.screens.VerifyCommand;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.feeds.abst.AbstractChannelFeed;
import io.blueservices.stc.StaticEmoji;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;
import reactor.core.publisher.Mono;

public class GestionPrimesFeed extends AbstractChannelFeed {

	protected final PlatformCompany company;
	
	public GestionPrimesFeed(BlueServices services, long channelId, PlatformCompany company) {
		super(services, true, Arrays.asList(
					new AccountBookCommand(services),
					new AccountBookLinesCommand(services),
					new InfoCommand(services),
					new PayCommand(services),
					new PrimeCommand(services),
					new SettingsCommand(services),
					new TableauCommand(services),
					new AllowCommand(services),
					new DisallowCommand(services),
					new VerifyCommand(services)
				), Snowflake.of(channelId));
		this.company = company;
	}
	
	@Override
	public boolean onMessage(MessageCreateEvent e) {
		if(!super.onMessage(e)) {
			return msgProcess(e.getMessage(), e.getMember().get());
		}
		return false;
	}
	
	public boolean msgProcess(Message m, Member member) {
		if(m.getAttachments().isEmpty()) return false;
		User self = this.services.getClient().getSelf().block();
		
		if(m.getReactors(StaticEmoji.WhiteCheckMark.re()).any(user -> user.equals(self)).block()) {
			return true;
		}

		if(!company.isAllowed()) {
			log.info("Screen on unALLOWED SYSTEM from " + company.getCompanyName());
			m.addReaction(ReactionEmoji.unicode("❓")).subscribe();
			return false;
		}
		
		if(!company.workIn(member)) {
			this.createEmbed(embed -> {
				embed.setTitle("**__Erreur__**");
				embed.setColor(Color.RED);
				embed.setDescription("Vous ne faites pas partie de l'entreprise...");
			})
			.doOnSuccess(msg -> {
				Mono.just(msg).delaySubscription(Duration.ofSeconds(30)).subscribe(sent -> {
					sent.delete();
					m.delete();
				});
			})
			.subscribe();
			return true;
		}
		
		for(Attachment a : m.getAttachments()) {
			log.info(member.getDisplayName() + " : " + a.getProxyUrl());
		}
		
		if(company.isManualValidation()) {
			m.addReaction(StaticEmoji.WhiteCheckMark.re()).subscribe();
			m.addReaction(StaticEmoji.X.re()).subscribe();
		} else {
			m.addReaction(StaticEmoji.WhiteCheckMark.re()).subscribe();
			this.validateMessageRun(company.getPatron().blockFirst(), m.getAuthorAsMember().block(), m);
		}
		
		return true;
	}
	
	@Override
	public boolean onReaction(ReactionAddEvent e) {
		if(!super.onReaction(e)) {
			Message m = e.getMessage().block();
			if(!company.isAllowed()) return false;
			if(!company.isManualValidation()) return false;
			if(m.getAttachments().isEmpty()) return false;
			if(!e.getEmoji().equals(StaticEmoji.WhiteCheckMark.re()) && !e.getEmoji().equals(ReactionEmoji.unicode("❌"))) {
				e.getMessage().flatMap(ms-> ms.removeReaction(e.getEmoji(), e.getUserId())).subscribe();
				return true;
			}
			
			User self = this.services.getClient().getSelf().block();
			Member member = m.getClient().getMemberById(StaticID.GUILD, e.getUserId()).block();
			
			if(!m.getReactors(StaticEmoji.WhiteCheckMark.re()).any(user -> user.equals(self)).block()
			|| !m.getReactors(StaticEmoji.X.re()).any(user -> user.equals(self)).block()) {
				return false;
			}

			//TODO Verify if co patron
			if(!company.isPatron(member)) {
				e.getMessage().flatMap(ms-> ms.removeReaction(e.getEmoji(), e.getUserId())).subscribe();
				return true;
			}
			
			this.validateMessageRun(member, m.getAuthorAsMember().block(), m);
			return true;
		}
		
		return false;
	}
	
	private void validateMessageRun(Member validator, Member worker, Message m) {
		PlatformUser workerUser = this.services.getUsersDB().get(worker).block();
		company.run(workerUser);
		
		m.addReaction(StaticEmoji.Dollar.re()).subscribe();
	}
	

}
