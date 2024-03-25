package de.vojay.flitch;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class App {

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment
			.createLocalEnvironmentWithWebUI(new Configuration());

		ParameterTool parameters = ParameterTool.fromArgs(args);
		String[] twitchChannels = parameters
			.getRequired("twitchChannels")
			.trim()
			.split(",");

		env
			.addSource(new TwitchSource(twitchChannels))
			.map(new AnalyzeSentiment())
			.print();

		env.execute("Flitch");
		env.close();
	}

}
