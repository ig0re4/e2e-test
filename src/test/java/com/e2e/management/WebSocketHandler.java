package com.e2e.management;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class WebSocketHandler extends WebSocketProtocol {
	
	public WebSocketHandler() throws ParserConfigurationException,
			SAXException, IOException, Exception {
		super();
	}

	private static Logger logger = LoggerFactory
			.getLogger(WebSocketHandler.class);

	@Override
	public void messageReceived(ChannelHandlerContext ctx, WebSocketFrame frame)
			throws Exception {
		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			HttpSocketHandler.getHandshaker().close(ctx.channel(),
					(CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(
					new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format(
					"%s frame types not supported", frame.getClass().getName()));
		}
		// Send the uppercase string back.
		String request = ((TextWebSocketFrame) frame).text();
		ChannelFuture future = ctx.channel().write(
				new TextWebSocketFrame(execute(request)));
		future.addListener(new GenericFutureListener<Future<Void>>() {
			@Override
			public void operationComplete(Future<Void> future) throws Exception {
				logger.info("write to channels successful");
			}
		});

	}
}
