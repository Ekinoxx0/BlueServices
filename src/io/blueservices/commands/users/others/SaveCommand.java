package io.blueservices.commands.users.others;

import java.awt.Color;
import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.users.PlatformUser;

public class SaveCommand extends AbstractCmd {

	public SaveCommand(BlueServices services) {
		super(services, "save", 
				Arrays.asList(), 
				"", 
				"Commande de debug");
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		try {
			sender.save();
			senderChannel.createEmbed(embed -> {
				embed.setColor(Color.GREEN);
				embed.setTitle("**__Sauvegarde de vos données__**");
				embed.setDescription(":white_check_mark:");
			}).subscribe();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
