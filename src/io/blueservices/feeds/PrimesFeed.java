package io.blueservices.feeds;

import java.util.Arrays;

import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.entreprises.PlatformCompany;
import io.blueservices.feeds.abst.AbstractChannelFeed;

public class PrimesFeed extends AbstractChannelFeed {

	protected final PlatformCompany company;
	
	public PrimesFeed(BlueServices services, long channelId, PlatformCompany company) {
		super(services, true, Arrays.asList(), Snowflake.of(channelId));
		this.company = company;
	}
	
}
