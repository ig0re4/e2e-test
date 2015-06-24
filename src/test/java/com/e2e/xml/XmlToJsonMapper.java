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

public class XmlToJsonMapper extends ObjectMapper {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(XmlToJsonMapper.class);

	public XmlToJsonMapper() {
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
			jgen.writeStartObject();
			for(XmlNode son: node.getSonsExceptDefault()){
				writeNode(son, jgen);
			}
			jgen.writeEndObject();
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
			jgen.writeFieldName(node.getName());
			if (!node.hasSons()) {
				writeValue(jgen, node);
			} else {
				boolean isArray = node.getType().equals(XmlType.ARRAY);
				if(isArray){
					jgen.writeStartArray();
				}else{
					jgen.writeStartObject();
				}
				for (XmlNode son : node.getSonsExceptDefault()) {
					if (son.hasSons()) {
						writeNode(son, jgen);
					} else {
						jgen.writeFieldName(son.getName());
						writeValue(jgen, son);
					}
				}
				if(isArray){
					jgen.writeEndArray();
				}else{
					jgen.writeEndObject();
				}
			}
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
			map.get(son.getType()).accept(jgen, son);
		}

		@Override
		public Class<XmlNode> handledType() {
			return XmlNode.class;
		}
	}
}