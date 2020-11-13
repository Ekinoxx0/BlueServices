package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.users.PlatformUser;

public class LongCommand extends AbstractCmd {

	public LongCommand(BlueServices services) {
		super(services, "long", 
				Arrays.asList(), 
				"", 
				"Commande très longue.",
				false);
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		try {
			Thread.sleep(8000L);
			sEmbedWithDelete(senderChannel, "", "Fin du test (8sec)");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
