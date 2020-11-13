package io.blueservices.commands.screens;

import java.util.Arrays;
import java.util.Set;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.users.PlatformUser;

public class InfoCommand extends AbstractCmd {

	public InfoCommand(BlueServices services) {
		super(services, 
				"info", 
				Arrays.asList("infos", "information", "informations", "search", "cherche", "recherche"), 
				"<QUI?>", 
				"Information à propos d'un employé");
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		PlatformCompany company = this.services.getCompanyDB().getFromAnyChannel(senderChannel).block();
		if(args.length == 0) {
			sEmbedWithDelete(senderChannel, "Usage:", this.getCommandUsage());
			return;
		}

		String targetName = fromArgs(args);
		Set<PlatformUser> searched = services.getUsersDB().searchPlayer(targetName);
		
		if(searched.isEmpty()) {
			sEmbedWithDelete(senderChannel, ":x: **Aucun utilisateur avec ce nom...** :x:");
			return;
		} else if(searched.size() > 1) {
			String listName = "";
			if(searched.size() > 5) {
				listName = "Trop de nom pour les afficher.";
			} else {
				for(PlatformUser user : searched) {
					listName += " - " + user.getRPName() + "\n";
				}
			}
			sEmbedWithDelete(senderChannel, ":x: **Plusieurs utilisateurs avec ce nom, soyez plus spécifique** :x:\n\n" + listName);
			return;
		}
		
		PlatformUser target = searched.iterator().next();
		StringBuilder info = new StringBuilder("\n");
		
		if(company.workIn(target.getMember().block())) {
			info.append("**Travaille dans votre entreprise :** OUI\n");

			info.append("**Nbr de Run :** " + company.runNumber(target) + "\n");
			if(company.alreadyPaidTo(target) == 0) {
				info.append("**Déjà payé dans le passé :** 0$\n");
			}
		} else {
			info.append("**Travaille dans votre entreprise :** NON\n");
		}
		
		if(company.alreadyPaidTo(target) != 0) {
			info.append("**Déjà payé dans le passé :** " + company.alreadyPaidTo(target) + "$\n");
		}
		
		if(company.debtTo(target) != 0) {
			info.append("**Dette restante :** " + company.debtTo(target) + "$\n");
		}
		
		sEmbedWithDelete(senderChannel, 
				"**" + target.getRPName() + "** *(" + target.getId() + ")*", 
				info.toString());
	}

}
