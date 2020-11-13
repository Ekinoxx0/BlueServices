package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class ListChannelCommand extends AbstractCmd {

	public ListChannelCommand(BlueServices services) {
		super(services, "lcc", 
				Arrays.asList(), 
				"", 
				"LCC",
				false);
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		if (sender.getId() != StaticID.MASTER.asLong()) {
			noPermission(senderChannel);
			return;
		}
		
		this.services.getClient().getGuildById(StaticID.GUILD).flatMapMany(Guild::getChannels).subscribe(gC -> {
			log.info(gC.getType() + " | " + gC.getName() + " | " + gC.getId().asLong());
		});
		sEmbedWithDelete(senderChannel, "", "Check logs for more info");
	}

}
