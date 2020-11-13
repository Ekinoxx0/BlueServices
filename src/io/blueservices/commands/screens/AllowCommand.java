package io.blueservices.commands.screens;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.users.PlatformUser;

public class AllowCommand extends AbstractCmd {

	public AllowCommand(BlueServices services) {
		super(services, 
				"allow", 
				Arrays.asList("allowd", "accept"), 
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
		company.setAllowed(true);
		sEmbed(senderChannel, "**__Autorisation d'accès accordée !__**", 
				"Vous avez autorisé le management de votre entreprise par Conseil Dagan. :white_check_mark:\n"
				+ "**Merci de votre confiance !**");
		log.info("Allowed by " + sender.getRPName() + " | " + sender.getUsername() + " | " + sender.getId());
	}
}