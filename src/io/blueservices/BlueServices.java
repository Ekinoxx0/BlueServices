package io.blueservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import reactor.util.Logger;
import reactor.util.Loggers;

public class BlueServices {
	
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private DiscordClient client;
	private Logger log;
	
	public BlueServices() {
		this.log = Loggers.getLogger("BServices");
		
		this.client = new DiscordClientBuilder("NjE3MjI1MDk1MDc2NzczODg4.XWoBsA.eKYTHAqqbyxDLl8gqZuCerLKZIY").build();

		EventDispatcher ed = this.client.getEventDispatcher();
		ed.on(ReadyEvent.class).subscribe(this::onReady);
	}
	
	public void login() {
		this.client.login().block();
	}
	
	private void onReady(ReadyEvent ready) {
		log.info("BlueServices is now logged.");
        client.updatePresence(Presence.online(Activity.watching("delete les msg"))).subscribe();
        
        client.getChannelById(Snowflake.of(611229885586997258L)).cast(TextChannel.class).flatMapMany(t -> t.getMessagesBefore(Snowflake.of(617158083117842442L))).subscribe(msg -> {
        	msg.delete().subscribe();
        });
    }
	
}
