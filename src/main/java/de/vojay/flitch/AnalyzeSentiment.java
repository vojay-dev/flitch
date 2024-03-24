package de.vojay.flitch;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AnalyzeSentiment extends RichMapFunction<
	TwitchMessage,
	Tuple2<TwitchMessage, Tuple2<List<Integer>, List<String>>>
> {

	private StanfordCoreNLP pipeline;


	@Override
	public void open(Configuration configuration) {
		Properties properties = new Properties();
		properties.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		pipeline = new StanfordCoreNLP(properties);
	}

	@Override
	public Tuple2<TwitchMessage, Tuple2<List<Integer>, List<String>>> map(TwitchMessage twitchMessage) {
		return new Tuple2<>(twitchMessage, getSentiment(twitchMessage.getMessage()));
	}

	private Tuple2<List<Integer>, List<String>> getSentiment(String message) {
		List<Integer> scores = new ArrayList<>();
		List<String> classes = new ArrayList<>();

		if (message != null && !message.isEmpty()) {
			Annotation annotation = pipeline.process(message);

			annotation.get(CoreAnnotations.SentencesAnnotation.class).forEach(sentence -> {
				Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);

				classes.add(sentence.get(SentimentCoreAnnotations.SentimentClass.class));
				scores.add(RNNCoreAnnotations.getPredictedClass(tree));
			});
		}

		return new Tuple2<>(scores, classes);
	}

}
