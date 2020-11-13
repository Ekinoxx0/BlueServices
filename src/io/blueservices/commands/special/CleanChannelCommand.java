package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class CleanChannelCommand extends AbstractCmd {

	public CleanChannelCommand(BlueServices services) {
		super(services, "cleanchannel", 
				Arrays.asList("pop"), 
				"", 
				"Nettoie un salon.",
				false);
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		if (sender.getId() != StaticID.MASTER.asLong()) {
			noPermission(senderChannel);
			return;
		}
		int i = Integer.MAX_VALUE;
		if(args.length == 1) {
			try {
				i = Integer.parseInt(args[0]);
			} catch(NumberFormatException ex) {}
		}
		for(Message m : senderChannel.getMessagesBefore(msg.getId()).collectList().block()) {
			if(i-- <= 0) return;
			m.delete().subscribe();
		}
	}

}
