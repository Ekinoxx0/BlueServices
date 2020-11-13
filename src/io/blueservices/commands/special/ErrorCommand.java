package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.users.PlatformUser;

public class ErrorCommand extends AbstractCmd {

	public ErrorCommand(BlueServices services) {
		super(services, "error", 
				Arrays.asList(), 
				"", 
				"Déclenche une erreur.",
				false);
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		throw new IllegalStateException("ERREUR DE TEST");
	}

}
