package io.blueservices.commands.abst;

import java.awt.Color;
import java.util.Arrays;
import java.util.Map.Entry;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import io.blueservices.BlueServices;
import io.blueservices.feeds.abst.AbstractFeed;
import io.blueservices.users.PlatformUser;

public class HelpCommand extends AbstractCmd {

	private final AbstractFeed feed;
	
	public HelpCommand(BlueServices services, AbstractFeed feed) {
		super(services, "help", 
				Arrays.asList("aide", "helps", "aides", "aid", "hlp"), 
				"", 
				"Liste des commandes");
		this.feed = feed;
	}

	@Override
	public void run(TextChannel senderChannel, Message NULL, PlatformUser sender, String[] args) {
		senderChannel.createEmbed(spec -> {
			spec.setTitle("**__:question: Liste des commandes de " + feed.getClass().getName() + " :question:__**");
			
			String desc = "```\n";
			
			for(AbstractCmd cmd : feed.getCmds()) {
				if(!cmd.doesHelp()) continue;
				String cmdDesc = "!" + cmd.getCmd() + " " + cmd.getArgUsage();
				while(cmdDesc.length() < 22) cmdDesc += " ";
				desc += cmdDesc + cmd.getDetails() + "\n";
				
				for(Entry<String, String> entry : cmd.getAliasesDetails().entrySet()) {
					String aliasDesc = "!" + entry.getKey();
					while(aliasDesc.length() < 22) aliasDesc += " ";
					desc += aliasDesc + entry.getValue() + "\n";
				}
			}
			
			desc += "\n```";
			spec.setDescription(desc);
			spec.setColor(Color.CYAN);
		}).subscribe();
		}

}
