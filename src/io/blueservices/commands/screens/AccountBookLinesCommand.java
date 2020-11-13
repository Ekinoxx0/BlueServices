package io.blueservices.commands.screens;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.users.PlatformUser;

public class AccountBookLinesCommand extends AbstractCmd {

	public AccountBookLinesCommand(BlueServices services) {
		super(services, 
				"accountbooklines", 
				Arrays.asList("ablines", "abline", "abl"), 
				"", 
				"Information de comptes");
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		PlatformCompany company = this.services.getCompanyDB().getFromAnyChannel(senderChannel).block();
		if(!company.isPatron(sender)) {
			noPermission(senderChannel);
			return;
		}
		
		sEmbedWithDelete(senderChannel, "__**Lignes dans le livre des comptes**__", company.getAccountBook().size() + " lignes");
	}

}
