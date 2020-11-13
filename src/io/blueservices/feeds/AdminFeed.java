package io.blueservices.feeds;

import java.util.Arrays;

import io.blueservices.BlueServices;
import io.blueservices.commands.admin.MaintenanceCommand;
import io.blueservices.feeds.abst.AbstractChannelFeed;
import io.blueservices.stc.StaticID;

public class AdminFeed extends AbstractChannelFeed {
	
	public AdminFeed(BlueServices services) {
		super(services, true, 
				Arrays.asList(
						new MaintenanceCommand(services)
				), StaticID.ADMIN);
	}

}
