package de.vojay.flitch;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.events.domain.EventUser;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

public class TwitchSource extends RichSourceFunction<TwitchMessage> {

	private final String[] twitchChannels;

	private TwitchClient client;
	private SimpleEventHandler eventHandler;
	private boolean running = true;

	public TwitchSource(String[] twitchChannels) {
		this.twitchChannels = twitchChannels;
	}

	@Override
	public void open(Configuration configuration) {
		client = TwitchClientBuilder
			.builder()
			.withEnableChat(true)
			.build();

		for(String channel : twitchChannels) {
			client.getChat().joinChannel(channel);
		}

		eventHandler = client.getEventManager().getEventHandler(SimpleEventHandler.class);
		running = true;
	}

	@Override
	public void run(SourceContext<TwitchMessage> ctx) throws InterruptedException {
		eventHandler.onEvent(IRCMessageEvent.class, event -> {
			String channel = event.getChannel().getName();
			EventUser eventUser = event.getUser();
			String user = eventUser == null ? "" : eventUser.getName();
			String message = event.getMessage().orElseGet(String::new);

			ctx.collect(new TwitchMessage(channel, user, message));
		});

		while(running) {
			Thread.sleep(100);
		}
	}

	@Override
	public void cancel() {
		client.close();
		running = false;
	}

}
