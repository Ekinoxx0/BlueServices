package io.blueservices.feeds;

import java.util.Arrays;

import io.blueservices.BlueServices;
import io.blueservices.commands.users.others.SaveCommand;
import io.blueservices.feeds.abst.AbstractFeed;

public class UsersFeed extends AbstractFeed {

	public UsersFeed(BlueServices services) {
		super(services, false, Arrays.asList(
				new SaveCommand(services)
				));
	}


}
