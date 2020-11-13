package io.blueservices.commands.screens;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.stc.StaticFunction;
import io.blueservices.users.PlatformUser;

public class SettingsCommand extends AbstractCmd {

	public SettingsCommand(BlueServices services) {
		super(services, "settings", Arrays.asList("option", "options", "setting"), "<SETTINGS>",
				"Modication des options");
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		PlatformCompany company = this.services.getCompanyDB().getFromAnyChannel(senderChannel).block();
		if (!company.isPatron(sender)) {
			noPermission(senderChannel);
			return;
		}

		if (args.length == 0) {
			sEmbedWithDelete(senderChannel, "Usage:", "\n``!settings primes``"
					+ "\n``!settings roles``" /*
												 * + "\n``!settings aaaaa``" + "\n``!settings bbbbb``" +
												 * "\n``!settings ccccc``"
												 */
			);
			return;
		}

		switch (args[0].toLowerCase()) {

		case "run":
			runSettings(senderChannel, msg, sender, args, company);
			break;

		case "primes":
		case "prime":
		case "screen":
		case "screens":
			primesSettings(senderChannel, msg, sender, args, company);
			break;

		case "role":
		case "roles":
		case "ranks":
		case "rank":
		case "grade":
		case "grades":
			rolesSettings(senderChannel, msg, sender, args, company);
			break;

		default:
			sEmbedWithDelete(senderChannel, "Usage:", "\nUsage inconnue, utilisez !settings");
			break;

		}
	}

	public void primesSettings(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args,
			PlatformCompany company) {
		if (args.length <= 2) {
			sEmbedWithDelete(senderChannel, "Usage:",
					"\n``!settings primes verif <manual/auto>`` Verif des screens manuel ou auto"
							+ "\n``!settings primes mode <daily/weekly>`` Mode des primes semaines ou journalière"
							+ "\n``!settings primes montant <MONTANT$>`` Montant à donner pour chaque RUN");
			return;
		}

		switch (args[1].toLowerCase()) {

		case "mode":
		case "mod":
		case "modes":

			switch (args[2].toLowerCase()) {

			case "daily":
				company.setDailyPrimes(true);
				sEmbedWithDelete(senderChannel, "**__Mode de primes__**", "\nMode journalier activé");
				return;

			case "weekly":
				company.setDailyPrimes(false);
				sEmbedWithDelete(senderChannel, "**__Mode de primes__**", "\nMode hebdomadaire activé");
				return;

			default:
				sEmbedWithDelete(senderChannel, "Usage:", "\n``!settings primes mode <daily/weekly>``");
				return;

			}

		case "verifs":
		case "verification":
		case "verif":

			switch (args[2].toLowerCase()) {

			case "manuelle":
			case "manuel":
			case "manual":
				company.setManualValidation(true);
				sEmbedWithDelete(senderChannel, "**__Mode de primes__**", "\nMode vérification manuelle activé");
				return;

			case "automatic":
			case "automatique":
			case "auto":
				company.setManualValidation(false);
				sEmbedWithDelete(senderChannel, "**__Mode de primes__**", "\nMode vérification automatique activé");
				return;

			default:
				sEmbedWithDelete(senderChannel, "Usage:", "\n``!settings primes verif <manual/auto>``");
				return;

			}

		case "amount":
		case "montant":
		case "prime":
			try {
				int amount = Integer.parseInt(args[2]);

				if (amount <= 0 || amount > StaticFunction.getDailyPrimeMaximum()) {
					sEmbedWithDelete(senderChannel, "Erreur", "\nMontant impossible code #564564685486748465132.");
					return;
				}

				company.setPrimeAmount(amount);
				sEmbedWithDelete(senderChannel, "**__Montant de prime__**", "\nMontant défini à " + amount + "$");
			} catch (NumberFormatException ex) {
				sEmbedWithDelete(senderChannel, "Erreur", "\nMerci de préciser un nombre.");
			}

			return;

		default:
			sEmbedWithDelete(senderChannel, "En construction...",
					"Malheureusement je n'ai pas fini d'implémenter cette fonctionnalité...");
			break;

		}
	}

	public void runSettings(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args,
			PlatformCompany company) {
		if (args.length <= 2) {
			sEmbedWithDelete(senderChannel, "Usage:", "\n``!settings run received <MONTANT>`` Défini le montant perçu");
			return;
		}

		switch (args[1].toLowerCase()) {

		case "received":
		case "recu":
			try {
				int amount = Integer.parseInt(args[2]);

				if (amount <= 0 || amount > StaticFunction.getDailyPrimeMaximum()) {
					sEmbedWithDelete(senderChannel, "Erreur", "\nMontant impossible code #6456456.");
					return;
				}

				company.setReceivedByRun(amount);
				sEmbedWithDelete(senderChannel, "**__Montant reçu par run__**", "\nMontant défini à " + amount + "$");
			} catch (NumberFormatException ex) {
				sEmbedWithDelete(senderChannel, "Erreur", "\nMerci de préciser un nombre.");
			}
			return;

		default:
			sEmbedWithDelete(senderChannel, "En construction...",
					"Malheureusement je n'ai pas fini d'implémenter cette fonctionnalité...");
			break;

		}
	}

	public void rolesSettings(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args,
			PlatformCompany company) {
		// TODO
		sEmbedWithDelete(senderChannel, "En construction...",
				"Malheureusement je n'ai pas fini d'implémenter cette fonctionnalité...");
	}

}
