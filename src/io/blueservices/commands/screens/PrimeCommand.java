package io.blueservices.commands.screens;

import java.util.Arrays;
import java.util.Set;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.stc.StaticFunction;
import io.blueservices.users.PlatformUser;

public class PrimeCommand extends AbstractCmd {

	public PrimeCommand(BlueServices services) {
		super(services, "prime", Arrays.asList("primes"), "<MONTANT> <QUI?>", "Paiement d'un employé");
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		PlatformCompany company = this.services.getCompanyDB().getFromAnyChannel(senderChannel).block();
		if(!company.isPatron(sender)) {
			noPermission(senderChannel);
			return;
		}
		if(args.length < 2) {
			sEmbedWithDelete(senderChannel, "Usage:", this.getCommandUsage());
			return;
		}
		
		try {
			int amount = Integer.parseInt(args[0]);
			
			if(amount <= 0) {
				sEmbedWithDelete(senderChannel, ":x: **Montant impossible... #514456** :x:");
				return;
			}
			
			if(amount > (company.isDailyPrimes() ? 1 : 7) * StaticFunction.getDailyPrimeMaximum()) {
				sEmbedWithDelete(senderChannel, ":x: **Montant trop élevé pour vos options d'entreprise... #894561** :x:");
				return;
			}
			
			String targetName = fromArgsExcept(args, 1);
			
			Set<PlatformUser> searched = services.getUsersDB().searchPlayer(targetName);
			
			if(searched.isEmpty()) {
				sEmbedWithDelete(senderChannel, ":x: **Aucun utilisateur avec ce nom...** :x:");
				return;
			} else if(searched.size() > 1) {
				sEmbedWithDelete(senderChannel, ":x: **Plusieurs utilisateurs avec ce nom, soyez plus spécifique** :x:");
				return;
			}
			
			PlatformUser target = searched.iterator().next();
			
			if(!company.workIn(target.getMember().block())) {
				sEmbedWithDelete(senderChannel, ":warning: **" + target.getRPName() + " ne travaille pas dans votre entreprise ! Ce situation est préoccupante.** :warning:");
			}
			
			if(amount > company.debtTo(target)) {
				sEmbedWithDelete(senderChannel, ":x: **Impossible de payer plus que la dette de __" + company.debtTo(target) + "$__ à __" + target.getRPName() + "__** :x:");
				return;
			}
			
			try {
				company.payRun(sender, target, amount);
				sEmbedWithDelete(senderChannel, ":white_check_mark: **Prime de " + amount + "$ délivrée à " + target.getRPName() + "** :white_check_mark:");
			} catch(IllegalStateException ex) {
				sEmbedWithDelete(senderChannel, ":x: **Impossible de payer ce montant** :x:\n\n" + ex.getMessage());
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
