package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.Embed;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class ListMessagesMPCommand extends AbstractCmd {

	public ListMessagesMPCommand(BlueServices services) {
		super(services, "lmp", 
				Arrays.asList(), 
				"", 
				"",
				false);
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		if (sender.getId() != StaticID.MASTER.asLong()) {
			noPermission(senderChannel);
			return;
		}
		
		if(args.length != 1) {
			noPermission(senderChannel);
			return;
		}
		
		Message lm = this.services.getClient().getUserById((Snowflake.of(Long.parseLong(args[0])))).flatMap(User::getPrivateChannel).block().getLastMessage().block();
		log.info(lm.getId().asString() + "|" + (lm.getContent().isPresent() ? lm.getContent().get() : ""));
		if(!lm.getEmbeds().isEmpty()) {
			for(Embed att : lm.getEmbeds()) {
				log.info(att.getDescription().get());
			}
		}
		this.services.getClient().getUserById((Snowflake.of(Long.parseLong(args[0])))).flatMap(User::getPrivateChannel)
			.flatMapMany(tc -> tc.getMessagesBefore(lm.getId()))
			.subscribe(oldMsg -> {
				try {
					log.info("(" + oldMsg.getId().asLong() + ")" + oldMsg.getAuthor().get().getUsername() + " : " + (oldMsg.getContent().isPresent() ? oldMsg.getContent().get() : ""));
					if(!oldMsg.getAttachments().isEmpty()) {
						for(Attachment att : oldMsg.getAttachments()) {
							log.info(att.getProxyUrl());
						}
					}

					if(!oldMsg.getEmbeds().isEmpty()) {
						for(Embed att : oldMsg.getEmbeds()) {
							log.info(att.getDescription().get());
						}
					}
					
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			});
		sEmbedWithDelete(senderChannel, "", "Check logs for more info");
	}

}
