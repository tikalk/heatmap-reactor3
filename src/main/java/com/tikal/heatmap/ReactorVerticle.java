package com.tikal.heatmap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class ReactorVerticle extends AbstractVerticle {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReactorVerticle.class);

	@Override
	public void start(){
		vertx.deployVerticle("com.tikal.heatmap.KafkaProducerVerticle",new DeploymentOptions().setConfig(config()));
		
		vertx.createHttpServer()
			.requestHandler(r -> r.bodyHandler(b ->	vertx.eventBus().send(config().getString("address"), b.toString()))
			.response().end())
			.listen(config().getInteger("http.server.port"));
		
		logger.info("HTTP server is listening on port {} ...",config().getInteger("http.server.port"));
	}

}
