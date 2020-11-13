package io.blueservices.commands.screens;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.entreprises.PlatformAccountLine;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.users.PlatformUser;

public class AccountBookCommand extends AbstractCmd {

	public AccountBookCommand(BlueServices services) {
		super(services, 
				"accountbook", 
				Arrays.asList("ledger", "livrecomptes", "livrecompte", "comptelivre", "compteslivre", "livredescomptes", "comptelivre", "bookaccount", "livre"), 
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
		
		try {
			File f = new File("ledger.csv");
			if(f.exists()) f.delete();
			FileWriter writer = new FileWriter(f);

			writer.append("UUID;Temps;Source;Cible;Raison;Montant\n");
			for(PlatformAccountLine line : company.getAccountBook()) {
				writer.append(line.getUUID().toString() + ";" + new Date(line.getTime()).toString() + ";" + line.getSourceName() + ";" + line.getTargetName() + ";" + line.getReason() + ";" + line.getAmount() + "\n");
			}
			writer.close();
			
			msgWithDelete(senderChannel, spec -> {
				spec.setContent("__**Livre des comptes**__");
				try {
					spec.addFile("ldc-" + company.getCompanyName().toLowerCase().replace(" ", "_") + ".csv", new FileInputStream(f));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					spec.setContent("__**Livre des comptes**__\nUne erreur est survenue ! :x:");
				}
			});
			if(f.exists()) f.delete();
		} catch (IOException e) {
			e.printStackTrace();
			sEmbedWithDelete(senderChannel, "Erreur", ":x: " + e.getMessage());
		}
		
	}

}
