package io.blueservices.feeds.abst;

import java.util.List;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import io.blueservices.BlueServices;
import io.blueservices.commands.abst.AbstractCmd;
import reactor.core.publisher.Mono;

public class AbstractChannelFeed extends AbstractFeed {

	private final Snowflake channelId;
	
	public AbstractChannelFeed(BlueServices services, boolean acceptNonCommandMessages, List<AbstractCmd> cmds, Snowflake channelId) {
		super(services, acceptNonCommandMessages, cmds);
		if(channelId == null || channelId.asLong() <= 0) throw new IllegalArgumentException();
		this.channelId = channelId;
		services.getClient().getEventDispatcher().on(MessageCreateEvent.class)
			.filter(msg -> msg.getMember().isPresent())
			.filter(msg -> !msg.getMember().get().isBot())
			.filter(msg -> msg.getMessage().getChannelId().equals(channelId))	
			.subscribe(event -> this.onMessage(event));
		
		services.getClient().getEventDispatcher().on(ReactionAddEvent.class)
			.filter(reac -> reac.getChannel().block() instanceof TextChannel)
			.filter(msg -> msg.getChannelId().equals(channelId))
			.subscribe(event -> this.onReaction(event));
	}
	
	public boolean onReaction(ReactionAddEvent e) {
		if(e == null)
			return true;
		if(e.getUser().block().isBot())
			return true;
		
		return false;
	}
	
	public Mono<Message> createEmbed(Consumer<? super EmbedCreateSpec> embed){
		return this.getChannel().flatMap(channel -> channel.createEmbed(embed));
	}
	
	public Mono<Message> createMessage(String message){
		return this.getChannel().flatMap(channel -> channel.createMessage(message));
	}
	
	public Mono<TextChannel> getChannel(){
		return this.services.getClient().getChannelById(getChannelId()).cast(TextChannel.class);
	}

	public Snowflake getChannelId() {
		return channelId;
	}
	
}
