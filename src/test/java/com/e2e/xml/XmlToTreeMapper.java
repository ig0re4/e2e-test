package com.e2e.xml;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.e2e.robot.ThrowingBiConsumer;
import com.e2e.xml.XmlNode.XmlType;

/**
 * 
 * @author igors
 *
 */

public class XmlToTreeMapper extends ObjectMapper {
	private static Logger logger = LoggerFactory.getLogger(XmlToTreeMapper.class);
	private static final String IS_FOLDER = "isFolder";
	private static final String CHILDREN = "children";
	private static final String TITLE = "title";
	private static final String VALUE = "value";
	
	public XmlToTreeMapper() {
		SimpleModule module = new SimpleModule(
				"PolymorphicXmlSerializerModule", new Version(1, 0, 0, null));
		module.addSerializer(XmlNode.class, new XmlNodeSerializer());
		registerModule(module);
	}

	/**
	 * 
	 * @author igors
	 *
	 */
	private class XmlNodeSerializer extends JsonSerializer<XmlNode> {
		private static final String TYPE = "type";
		private static final String PATH = "path";
		private static final String FILE = "file";
		private static final String IS_INHERITED = "isInherited";
		private Map<XmlType, ThrowingBiConsumer<JsonGenerator, XmlNode>> map;
		
		public XmlNodeSerializer() {
			super();
			map = new ConcurrentHashMap<XmlType, ThrowingBiConsumer<JsonGenerator, XmlNode>>();
			map.put(XmlType.STRING, (v, t) -> v.writeString(t.getValue()));
			map.put(XmlType.INT, (v, t) -> v.writeNumber(t.getValueAsInt()));
			map.put(XmlType.DOUBLE, (v, t) -> v.writeNumber(t.getValueAsDouble()));
			map.put(XmlType.BOOLEAN, (v, t) -> v.writeBoolean(t.getValueAsboolean()));
			map.put(XmlType.NULL, (v, t) -> v.writeNull());
		}

		@Override
		public void serialize(XmlNode node, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException {
			writeNode(node, jgen);
		}

		/**
		 * Recursively create json node from the tree 
		 * @param node
		 * @param jgen
		 * @throws JsonGenerationException
		 * @throws IOException
		 */
		private void writeNode(XmlNode node, JsonGenerator jgen)
				throws JsonGenerationException, IOException {
			writeName(node, jgen);
			if (!node.hasSons()) {
				writeValue(jgen, node);
			} else {
				writeStartArray(jgen);
				for (XmlNode son : node.getSons()) {
					writeNode(son, jgen);
				}
				jgen.writeEndArray();
			}
			jgen.writeEndObject();
		}

		/**
		 * 
		 * @param jgen
		 * @throws IOException
		 * @throws JsonGenerationException
		 * @throws JsonProcessingException
		 */
		private void writeStartArray(JsonGenerator jgen) throws IOException,
				JsonGenerationException, JsonProcessingException {
			jgen.writeFieldName(IS_FOLDER);
			jgen.writeObject(true);
			jgen.writeFieldName(CHILDREN);
			jgen.writeStartArray();
		}

		/**
		 * 
		 * @param node
		 * @param jgen
		 * @throws IOException
		 * @throws JsonGenerationException
		 */
		private void writeName(XmlNode node, JsonGenerator jgen)
				throws IOException, JsonGenerationException {
			jgen.writeStartObject();
			jgen.writeFieldName(TITLE);
			jgen.writeObject(node.getName());
			jgen.writeFieldName(IS_INHERITED);
			jgen.writeObject(node.isInherited());
			jgen.writeFieldName(FILE);
			jgen.writeObject(node.getFilePath());
			jgen.writeFieldName(PATH);
			jgen.writeObject(node.getId());
		}

		/**
		 * 
		 * @param jgen
		 * @param son
		 * @throws IOException
		 * @throws JsonGenerationException
		 */
		private void writeValue(JsonGenerator jgen, XmlNode son)
				throws IOException, JsonGenerationException {
			jgen.writeFieldName(TYPE);
			jgen.writeObject(son.getType());
			jgen.writeFieldName(VALUE);
			map.get(son.getType()).accept(jgen, son);
		}

		@Override
		public Class<XmlNode> handledType() {
			return XmlNode.class;
		}
	}
}