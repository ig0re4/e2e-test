package com.e2e.http;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.websocket.DeploymentException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.e2e.exceptions.IncorrectCommandException;
import com.e2e.robot.Robot;
import com.e2e.robot.RobotListener;
import com.e2e.xml.XmlNode;


/**
 * 
 * @author igors
 */
public class HttpRobot extends Robot {
	private static Logger logger = LoggerFactory.getLogger(HttpRobot.class);
	private WebTarget target;
	private Client client;
	
	static {
		// for localhost testing only
		HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
			public boolean verify(String hostname, SSLSession sslSession) {
				if (hostname.equals("localhost")) {
					return true;
				}
				return false;
			}
		});
	}
	
	/**
	 * 
	 * @param listener
	 * @param robot
	 * @throws IOException
	 * @throws IncorrectCommandException
	 * @throws DeploymentException
	 */
	public HttpRobot(RobotListener listener, XmlNode robot) throws IOException,
			IncorrectCommandException, DeploymentException {
		super(listener, robot);
		
	}

	@Override
	protected void connect(String uri) throws DeploymentException, IOException {
		client = ClientBuilder.newBuilder().build();
		target = client.target(URI.create(uri));
		logger.info("Scenario:" + getListenerName() + ", robot:"
				+ getName() + "---> connected ");
	}

	@Override
	protected void disconnect() throws Exception {
		client.close();
		logger.info("Scenario:" + getListenerName() + ", robot:"
				+ getName() + "---> disconnected ");
	}

	@Override
	protected void send(List<JsonNode> messages) {
		if (messages != null) {
			for (JsonNode node : messages) {
				logger.info("Scenario:" + getListenerName() + ", robot:"
						+ getName() + "---> send=" + node.toString());
				target.path("/").request().async()
				.post(Entity.json(node),
					new InvocationCallback<String>() {
						@Override
						public void completed(String response) {
							logger.info("Scenario:" + getListenerName() + ", robot:"
									+ getName() + "---> receive=" + response.toString());
							addResponse(response);
						}
						@Override
						public void failed(final Throwable throwable) {
							logger.error("Scenario:" + getListenerName() + ", robot:"
									+ getName() + "---> failed:", throwable);
							
						}
					});
			}
		} else {
			logger.error("Fail to send message because the message is null");
		}		
	}
}
