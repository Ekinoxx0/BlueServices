package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class DelMessageCommand extends AbstractCmd {

	public DelMessageCommand(BlueServices services) {
		super(services, "delm", 
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
		this.services.getClient().getMessageById(Snowflake.of(Long.parseLong(args[0])), Snowflake.of(Long.parseLong(args[1]))).flatMap(Message::delete).block();
		sEmbedWithDelete(senderChannel, "Confirmed! :white_check_mark:");
	}

}
