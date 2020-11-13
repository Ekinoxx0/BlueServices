package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class ListMessagesCommand extends AbstractCmd {

	public ListMessagesCommand(BlueServices services) {
		super(services, "lcm", 
				Arrays.asList(), 
				"", 
				"LCM",
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
		
		Snowflake sf = Snowflake.of(Long.parseLong(args[0]));

		log.info(this.services.getClient().getChannelById(sf).cast(TextChannel.class).block().getName());
		this.services.getClient().getChannelById(sf).cast(TextChannel.class)
			.flatMapMany(tc -> tc.getMessagesBefore(this.services.getClient().getChannelById(sf).cast(TextChannel.class).block().getLastMessageId().get()))
			.subscribe(oldMsg -> {
				try {
					log.info(oldMsg.getAuthor().get().getUsername() + " : " + oldMsg.getContent().get());
					if(!oldMsg.getAttachments().isEmpty()) {
						for(Attachment att : oldMsg.getAttachments()) {
							log.info(att.getProxyUrl());
						}
					}
				} catch(Exception ex) {}
			});
		sEmbedWithDelete(senderChannel, "", "Check logs for more info");
	}

}
