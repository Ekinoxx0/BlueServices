package io.blueservices.commands.special;

import java.util.Arrays;
import java.util.List;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class InputAllUsersCommand extends AbstractCmd {

	public InputAllUsersCommand(BlueServices services) {
		super(services, "iau", 
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

		List<Member> listMembers = this.services.getClient().getGuildById(StaticID.GUILD).block().getMembers().collectList().block();
		
		for(Member m : listMembers) {
			PlatformUser u = this.services.getUsersDB().get(m).block();
			if(!u.getRPName().equals(m.getDisplayName())) {
				u.changeName(m.getDisplayName());
			}
		}
		
		sEmbedWithDelete(senderChannel, "Confirmed! :white_check_mark:");
	}

}
