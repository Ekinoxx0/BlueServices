package io.blueservices.commands.admin;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.users.PlatformUser;

public class MaintenanceCommand extends AbstractCmd {

	public MaintenanceCommand(BlueServices services) {
		super(services, 
				"maintenance", 
				Arrays.asList(), 
				"", 
				"Diffuser un message de maintenance");
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
	}
	
}
