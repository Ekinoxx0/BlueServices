package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class TtCommand extends AbstractCmd {

	public TtCommand(BlueServices services) {
		super(services, "tt", 
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
		
		log.info(this.services.getClient().getChannelById(StaticID.TWITTER).cast(TextChannel.class).block().getName());
		this.services.getClient().getChannelById(StaticID.TWITTER).cast(TextChannel.class)
			.flatMapMany(tc -> tc.getMessagesBefore(this.services.getClient().getChannelById(StaticID.TWITTER).cast(TextChannel.class).block().getLastMessageId().get()))
			.subscribe(m -> {
				try {
					m.addReaction(ReactionEmoji.unicode("🔄")).subscribe();
					m.addReaction(ReactionEmoji.unicode("❤")).subscribe();
				} catch(Exception ex) {}
			});
		sEmbedWithDelete(senderChannel, "Confirmed! :white_check_mark:");
	}

}
