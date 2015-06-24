package com.e2e.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.codehaus.jackson.JsonNode;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.e2e.exceptions.IncorrectCommandException;
import com.e2e.robot.Robot;
import com.e2e.robot.RobotListener;
import com.e2e.xml.XmlNode;


/**
 * 
 * @author igors
 *
 */
public class WebSocketRobot extends Robot implements SendHandler {
	private static Logger logger = LoggerFactory.getLogger(WebSocketRobot.class);

	private WebSocketContainer container = null;
	private Session session;

	/**
	 * 
	 * @param listener
	 * @param robotNode
	 * @throws IncorrectCommandException
	 * @throws IOException
	 * @throws DeploymentException
	 */
	public WebSocketRobot(RobotListener listener, XmlNode robotNode)
			throws IOException, IncorrectCommandException, DeploymentException {
		super(listener, robotNode);
		container = ContainerProvider.getWebSocketContainer();
	}

	/**
	 * 
	 * @param uri
	 * @throws DeploymentException
	 * @throws IOException
	 */
	@Override
	protected void connect(String uri) throws DeploymentException, IOException {
		session = container.connectToServer(new WebSocketClientEndpoint(),
				URI.create(uri));
		logger.info("Session = " + session.getId() + " connected");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Override
	protected void disconnect() throws Exception {
		if (container instanceof LifeCycle) {
			((LifeCycle) container).stop();
			session = null;
		}
	}

	/**
	 * @param messages
	 */
	@Override
	protected void send(List<JsonNode> messages) {
		if (session != null) {
			if (messages != null) {
				for (JsonNode node : messages) {
					logger.info("Scenario:" + getListenerName() + ", robot:"
							+ getName() + "---> send=" + node.toString());
					session.getAsyncRemote().sendObject(node.toString(), this);
				}
			} else {
				logger.error("Fail to send message because the message is null");
			}
		} else {
			logger.error("Fail to send message because the session is null");
		}
	}

	/**
	 * 
	 * @author igors
	 *
	 */
	class WebSocketClientEndpoint extends Endpoint implements
			MessageHandler.Whole<String> {
		@Override
		public void onOpen(Session session, EndpointConfig config) {
			logger.info("Connected ... " + session.getId());
			session.addMessageHandler(this);
		}

		@OnMessage
		public void onMessage(String response) {
			logger.info("Scenario:" + getListenerName() + ", robot:"
					+ getName() + "---> receive=" + response.toString());
			addResponse(response);
		}

		@Override
		public void onClose(Session session, CloseReason closeReason) {
			logger.info(String.format("Session %s close because of %s",
					session.getId(), closeReason.toString()));
		}
	}

	@Override
	public void onResult(SendResult result) {
		logger.info(String.format("Received message '%s'", result.toString()));
	}
}