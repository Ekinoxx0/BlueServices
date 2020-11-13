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

public class PrivateMsgCommand extends AbstractCmd {

	public PrivateMsgCommand(BlueServices services) {
		super(services, "pm", 
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
		if(args.length < 2) {
			noPermission(senderChannel);
			return;
		}
		this.services.getClient().getUserById((Snowflake.of(Long.parseLong(args[0])))).flatMap(User::getPrivateChannel).block().createEmbed(embed -> {
			embed.setTitle(fromArgsExcept(args, 1).split("%%")[0].replace("=n", "\n"));
			embed.setDescription(fromArgsExcept(args, 1).split("%%")[1].replace("=n", "\n"));
		}).block();
		sEmbedWithDelete(senderChannel, "Confirmed! :white_check_mark:");
	}

}
