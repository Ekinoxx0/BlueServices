package io.blueservices.commands.screens;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.users.PlatformUser;

public class VerifyCommand extends AbstractCmd {

	public VerifyCommand(BlueServices services) {
		super(services, "verify", Arrays.asList(), "", "Vérifier les messages précédent.");
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		PlatformCompany company = this.services.getCompanyDB().getFromAnyChannel(senderChannel).block();
		if(!company.isPatron(sender)) {
			noPermission(senderChannel);
			return;
		}
		
		senderChannel.getMessagesBefore(msg.getId()).subscribe(old -> {
			company.getScreensFeed().msgProcess(old, old.getAuthorAsMember().block());
		});
		
		sEmbedWithDelete(senderChannel, "Good!");
	}

}
