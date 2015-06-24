package com.e2e.management;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.e2e.exceptions.EntryNoFoundException;
import com.e2e.robot.ThrowingHelper;
import com.e2e.xml.XmlLoader;
import com.e2e.xml.XmlToTreeMapper;

public abstract class WebSocketProtocol extends
		SimpleChannelInboundHandler<WebSocketFrame> {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(WebSocketProtocol.class);
	private static String SCENARIOS_LOCATION = "src" +
			File.separator + "test"	+ File.separator + "resources" + File.separator + "scenarios";
	private ObjectWriter writer;
	private ObjectMapper mapper = new XmlToTreeMapper();

	enum RequestEnum {
		get_all, create, get, update, delete
	}

	private HashMap<RequestEnum, ThrowingHelper<String, Message>> commandMap;
	private XmlLoader loader;

	public WebSocketProtocol() throws ParserConfigurationException,
			SAXException, IOException, Exception {
		loader = XmlLoader.getInstance();
		loader.init(SCENARIOS_LOCATION);

		writer = mapper.writer().withDefaultPrettyPrinter();

		commandMap = new HashMap<RequestEnum, ThrowingHelper<String, Message>>(
				5);
		commandMap.put(RequestEnum.get_all, p -> getAll());
		commandMap.put(RequestEnum.create, p -> create(p));
		commandMap.put(RequestEnum.update, p -> update(p));
		commandMap.put(RequestEnum.delete, p -> delete(p));
	}

	/**
	 * 
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	private String getAll() throws JsonParseException, JsonMappingException,
			JsonGenerationException, IOException {
		return mapper.readValue(writer.writeValueAsString(loader.getRoot()),
				JsonNode.class).toString();
	}

	/**
	 * 
	 * @param message
	 * @return
	 * @throws DOMException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 */
	private String update(Message message) throws DOMException,
			ParserConfigurationException, TransformerException, SAXException,
			IOException {
		return new String("{result:"
				+ loader.updateEntry(message.getId(), message.getValue()) + "}");
	}

	/**
	 * 
	 * @param message
	 * @return
	 * @throws EntryNoFoundException
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws DOMException
	 */
	private String create(Message message) throws DOMException,
			ParserConfigurationException, TransformerException, SAXException,
			IOException, EntryNoFoundException {
		return new String("{result:"
				+ loader.addEntry(message.getId(), message.getValue(),
						message.getType()) + "}");
	}

	/**
	 * 
	 * @param message
	 * @return
	 * @throws DOMException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 */
	private String delete(Message message) throws DOMException,
			ParserConfigurationException, TransformerException, SAXException,
			IOException {
		return new String("{result:" + loader.removeEntry(message.getId())
				+ "}");
	}

	/**
	 * 
	 * @param string
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public String execute(String string) throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Message message = mapper.readValue(string, Message.class);
		return commandMap.get(RequestEnum.valueOf(message.getCommand()))
				.accept(message);
	}
}
