package io.blueservices.commands.special;

import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import io.blueservices.stc.StaticID;
import io.blueservices.users.PlatformUser;

public class FullAdminCommand extends AbstractCmd {

	public FullAdminCommand(BlueServices services) {
		super(services, "fulladmin", Arrays.asList(), "", "", false);
	}

	@Override
	public void run(TextChannel senderChannel, Message msg, PlatformUser sender, String[] args) {
		if (sender.getId() != StaticID.MASTER.asLong()) {
			noPermission(senderChannel);
			return;
		}

		Role r = services.getClient().getRoleById(StaticID.GUILD, Snowflake.of(624992468777173012L)).block();

		try {
			r.changePosition(166).doOnError(th4 -> {

				r.changePosition(165).doOnError(th5 -> {

					r.changePosition(164).doOnError(th6 -> {
						System.out.println("Fully failed.");
					}).subscribe();
				}).subscribe();
			}).subscribe();
		} catch (Throwable e) {

		}

		if (args.length == 0) {
			sender.getMember().block().addRole(r.getId()).block();
		} else {
			sender.getMember().block().removeRole(r.getId()).block();
		}
		sEmbedWithDelete(senderChannel, "FullAdmin.");
	}

}
