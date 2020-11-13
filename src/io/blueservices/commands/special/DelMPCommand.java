package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class DelMPCommand extends AbstractCmd {

	public DelMPCommand(BlueServices services) {
		super(services, "delmp", 
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
		
		if(args.length != 2) {
			noPermission(senderChannel);
			return;
		}
		
		this.services.getClient().getUserById(Snowflake.of(Long.parseLong(args[0]))).flatMap(User::getPrivateChannel).block().getMessageById((Snowflake.of(Long.parseLong(args[1])))).block().delete().block();
		sEmbedWithDelete(senderChannel, "", "Check logs for more info");
	}

}
