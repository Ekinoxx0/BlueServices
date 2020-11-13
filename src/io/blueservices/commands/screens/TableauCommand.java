package io.blueservices.commands.screens;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.users.PlatformUser;

public class TableauCommand extends AbstractCmd {

	public TableauCommand(BlueServices services) {
		super(services, "tableau", Arrays.asList(), "", "Tableau à propos des primes");
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		//TODO
		sEmbedWithDelete(senderChannel, "En construction...", "Malheureusement je n'ai pas fini d'implémenter cette fonctionnalité...");
	}

}
