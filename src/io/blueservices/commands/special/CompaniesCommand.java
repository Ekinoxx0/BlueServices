package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class CompaniesCommand extends AbstractCmd {

	public CompaniesCommand(BlueServices services) {
		super(services, "companies", 
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

		StringBuilder desc = new StringBuilder("\n");
		this.services.getCompanyDB().getCompanies().subscribe(company -> {
			desc.append(company.getEmoji() + " **__" + company.getCompanyName() + "__**\n");
			desc.append(" - **Patron** : " + company.getPatron().blockFirst().getDisplayName() + "\n");
			desc.append(" - **Prime** : " + company.getPrimePrice() + "\n");
			desc.append(" - **ABSize** : " + company.getAccountBook().size() + "\n");
			desc.append(" - **HasRunSystem** : " + company.hasRunSystem() + "\n\n");
		});
		sEmbedWithDelete(senderChannel, "**__Sociétés__**", desc.toString());
	}

}
