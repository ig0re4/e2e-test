package com.e2e.ams;


import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.Client;
//import javax.ws.rs.client.Client;

//import mock.AmsTestServer;
//import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.e2e.websocket.WebSocketScenarioManager;
import com.e2e.xml.XmlNode;

/**
 * 
 * @author igors
 */
public class AmsScenarioManager extends WebSocketScenarioManager {

	private static final String TARGET = "http://localhost:8080/api/messaging/rest/management/consumer/token";
	private static final Logger logger = LoggerFactory.getLogger(AmsScenarioManager.class);
	
	protected static Server server;
	protected static WebSocketContainer container;
	private static Client jerseyClient;
	private static String token = "";

	public AmsScenarioManager(XmlNode scenario) {
		super(scenario);
	}

	@BeforeClass
	public static void startUp() throws Exception {
		/*
		server = AmsTestServer.runTestsServer(ChatServerEndPoint.class, "index.html",
				ManagementResource.class.getPackage().getName(), 8080);
		container = ContainerProvider.getWebSocketContainer();
		jerseyClient = ClientBuilder.newClient();
		final Response postResponse = jerseyClient.target(TARGET)
				.request(MediaType.APPLICATION_JSON_TYPE).post(null);
		if (postResponse.getStatus() == HttpStatus.OK_200) {
			final String entity = postResponse.readEntity(String.class);
			token = new ObjectMapper().readTree(entity).findValue("token")
					.asText();
		}*/
	}

	@AfterClass
	public static void tearDown() throws Exception {
//		server.stop();
	}

	@Override
	protected String getToken() {
		return token;
	}
}