package io.blueservices.commands.screens;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.users.PlatformUser;

public class DisallowCommand extends AbstractCmd {

	public DisallowCommand(BlueServices services) {
		super(services, 
				"disallow", 
				Arrays.asList("refuse", "refuser"), 
				"", 
				"", false);
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		PlatformCompany company = this.services.getCompanyDB().getFromAnyChannel(senderChannel).block();
		if(!company.isPatron(sender)) {
			noPermission(senderChannel);
			return;
		}
		company.setAllowed(false);
		sEmbedWithDelete(senderChannel, "**__Autorisation d'accès supprimé !__**", 
				"Vous avez refusé le management de votre entreprise par Conseil Dagan. :white_check_mark:"
				+ "");
		log.info("Disallowed by " + sender.getRPName() + " | " + sender.getUsername() + " | " + sender.getId());
	}
}