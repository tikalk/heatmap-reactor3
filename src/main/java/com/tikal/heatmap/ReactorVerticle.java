package com.tikal.heatmap;

import com.cyngn.kafka.MessageProducer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.buffer.Buffer;

public class ReactorVerticle extends AbstractVerticle {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReactorVerticle.class);

	@Override
	public void start(){
		vertx.deployVerticle(MessageProducer.class.getName(),new DeploymentOptions().setConfig(config()));		
		
		vertx.createHttpServer()
			.requestHandler(r -> r.bodyHandler(b ->	sendToKafka(b))
			.response().end())
			.listen(config().getInteger("http.server.port"));
		
		logger.info("HTTP server is listening on port {} ...",config().getInteger("http.server.port"));
	}

	private void sendToKafka(final Buffer b) {
		logger.debug("Sending to Kafka:{}",b.toString());
		vertx.eventBus().send(MessageProducer.EVENTBUS_DEFAULT_ADDRESS, b.toString());
	}
}
