package com.tikal.heatmap;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class KafkaProducerVerticle extends AbstractVerticle {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(KafkaProducerVerticle.class);


	private KafkaProducer<String, String> producer;

	@Override
	public void start(final Future<Void> startFuture) throws Exception {
		logger.info("Starting KafkaProducerVerticle");
		final JsonObject kafkaProducerConfig = config().getJsonObject("kafkaProducer");
		setupProducer(kafkaProducerConfig);
		final MessageConsumer<String> outgoing = vertx.eventBus().<String> localConsumer(config().getString("address"));
		outgoing.handler(msg -> handleMsg(kafkaProducerConfig.getString("topicName"),msg));
		outgoing.completionHandler(complete -> startFuture.complete());
	}

	private void setupProducer(final JsonObject kafkaProducerConfig) {
		final Properties props = new Properties();		
		props.put("bootstrap.servers", kafkaProducerConfig.getString("bootstrap.servers"));
		props.put("key.serializer", kafkaProducerConfig.getString("key.serializer","org.apache.kafka.common.serialization.StringSerializer"));
		props.put("value.serializer", kafkaProducerConfig.getString("value.serializer","org.apache.kafka.common.serialization.StringSerializer"));
		props.put("acks", kafkaProducerConfig.getString("acks","1"));
		props.put("batch.size", kafkaProducerConfig.getString("batch.size","0"));

		producer = new KafkaProducer<>(props);
	}

	private void handleMsg(final String topicName,final Message<String> msg) {
		final String body = msg.body();
		logger.debug("Sending to Kafka:{}",body);
		final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, body);
		producer.send(
				producerRecord,
				(result, exception) -> handleKafkaCallback(exception));	
	}

	private void handleKafkaCallback(final Exception exception) {
		if(exception!=null)
			logger.error(exception.toString());
	}

	@Override
	public void stop() throws Exception {
		producer.close();
	}
}