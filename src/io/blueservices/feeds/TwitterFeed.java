package io.blueservices.feeds;

import java.util.Arrays;

import discord4j.core.event.domain.message.MessageCreateEvent;
import io.blueservices.BlueServices;
import io.blueservices.feeds.abst.AbstractChannelFeed;
import io.blueservices.stc.StaticEmoji;
import io.blueservices.stc.StaticID;

public class TwitterFeed extends AbstractChannelFeed {
	
	public TwitterFeed(BlueServices services) {
		super(services, true, 
				Arrays.asList(
				), StaticID.TWITTER);
	}

	@Override
	public boolean onMessage(MessageCreateEvent e) {
		if(!super.onMessage(e)) {
			e.getMessage().addReaction(StaticEmoji.ArrowsCounterclockwise.re()).subscribe();
			e.getMessage().addReaction(StaticEmoji.Heart.re()).subscribe();
			return true;
		}
		
		return false;
	}
}
