package io.blueservices.commands.screens;

import java.util.Arrays;
import java.util.Set;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.users.PlatformUser;

public class PayCommand extends AbstractCmd {

	public PayCommand(BlueServices services) {
		super(services, "pay", Arrays.asList(), "<MONTANT> <QUI?> <RAISON>", "Paiement autre");
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		PlatformCompany company = this.services.getCompanyDB().getFromAnyChannel(senderChannel).block();
		if(!company.isPatron(sender)) {
			noPermission(senderChannel);
			return;
		}

		if(args.length < 3) {
			sEmbedWithDelete(senderChannel, "Usage:", this.getCommandUsage());
			return;
		}
		
		try {
			int amount = Integer.parseInt(args[0]);
			
			if(amount <= 0) {
				sEmbedWithDelete(senderChannel, ":x: **Montant impossible... #514456** :x:");
				return;
			}

			PlatformUser target = null;
			boolean nameWithTwoWords = false;
			Set<PlatformUser> searched1Arg = services.getUsersDB().searchPlayer(args[1]);
			
			if(searched1Arg.isEmpty()) {
				sEmbedWithDelete(senderChannel, ":x: **Aucun utilisateur avec ce nom...** :x:");
				return;
			} else if(searched1Arg.size() > 1) {
				if(args.length < 4) {
					sEmbedWithDelete(senderChannel, ":x: **Plusieurs utilisateurs avec ce nom, soyez plus spécifique** :x:");
					return;
				}
			} else {
				target = searched1Arg.iterator().next();
			}
			
			if(args.length >= 4) {
				Set<PlatformUser> searched2Arg = services.getUsersDB().searchPlayer(args[1] + " " + args[2]);
				services.getUsersDB().searchPlayer(args[2] + " " + args[1]).forEach(u -> { if(!searched2Arg.contains(u)) searched2Arg.add(u); });

				log.info("searched1Arg");
				for(PlatformUser user : searched1Arg) {
					log.info(user != null ? user.getRPName() : "Null");
				}
				log.info("searched2Arg");
				for(PlatformUser user : searched2Arg) {
					log.info(user != null ? user.getRPName() : "Null");
				}
				
				if(searched1Arg.size() > 1 && searched2Arg.size() > 1) {
					sEmbedWithDelete(senderChannel, ":x: **Plusieurs utilisateurs avec ce nom, soyez plus spécifique** :x:");
					return;
				} else if(searched1Arg.size() > 1 && searched2Arg.isEmpty()) {
					sEmbedWithDelete(senderChannel, ":x: **Impossible de reconnaître cette personne (Plusieurs personnes avec le premier mot '" + args[1] + "' mais personne avec '" + args[1] + " " + args[2] + "')** :x:");
					return;
				} else if(searched2Arg.size() == 1) {
					target = searched2Arg.iterator().next();
					nameWithTwoWords = true;
				} else if(searched1Arg.size() == 1) {
					target = searched1Arg.iterator().next();
				} else {
					sEmbedWithDelete(senderChannel, ":x: **Une erreur est survenue ! Code #89471352** :x:");
					return;
				}
			}
			
			try {
				company.pay(sender, target, (amount > 0 ? -amount : amount), fromArgsExcept(args, nameWithTwoWords ? 3 : 2));
				sEmbedWithDelete(senderChannel, ":white_check_mark: **Nouvelle ligne de compte " + (amount > 0 ? -amount : amount) + "$ délivrée à " + target.getRPName() + "** :white_check_mark:");
			} catch(Exception ex) {
				sEmbedWithDelete(senderChannel, ":x: **Une erreur est survenue, les paiements ont été annulés pour éviter les problèmes.** :x:");
				ex.printStackTrace();
			}
			
		} catch(NumberFormatException e) {
			sEmbedWithDelete(senderChannel, ":x: **Montant inconnu...** :x:");
			return;
		}
	}

}
