package org.weishao.client.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class WSHandler extends SimpleChannelInboundHandler<Object> {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(WSHandler.class);
	
	private WebSocketClientHandshaker handshaker;
	
	private ChannelPromise handshakeFuture;
	
	public WSHandler(WebSocketClientHandshaker handshaker) {
		this.handshaker = handshaker;
	}

	public WebSocketClientHandshaker getHandshaker() {
		return handshaker;
	}

	public ChannelPromise getHandshakeFuture() {
		return handshakeFuture;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		this.handshakeFuture = ctx.newPromise();
	}

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
    	logger.debug("WebSocket Client disconnected!");
    }

    @Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    	logger.debug("channelRead0  " + this.handshaker.isHandshakeComplete());
    	
		Channel ch = (Channel) ctx.channel();
		FullHttpResponse response;
		
		if (!this.handshaker.isHandshakeComplete()) {
			try {
				response = (FullHttpResponse) msg;
				// 握手协议返回，设置结束握手
				this.handshaker.finishHandshake((io.netty.channel.Channel) ch, response);
				// 设置成功
				this.handshakeFuture.setSuccess();
				logger.debug("WebSocket Client connected! response headers[sec-websocket-extensions]:{}"
						+ response.headers());
			} catch (WebSocketHandshakeException var7) {
				FullHttpResponse res = (FullHttpResponse) msg;
				String errorMsg = String.format("WebSocket Client failed to connect,status:%s,reason:%s", res.status(),
						res.content().toString(CharsetUtil.UTF_8));
				this.handshakeFuture.setFailure(new Exception(errorMsg));
			}

			return;
		}

		if (msg instanceof FullHttpResponse) {
			response = (FullHttpResponse) msg;
			// this.listener.onFail(response.status().code(),
			// response.content().toString(CharsetUtil.UTF_8));
			throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content="
					+ response.content().toString(CharsetUtil.UTF_8) + ')');
		}

		WebSocketFrame frame = (WebSocketFrame) msg;
		if (frame instanceof TextWebSocketFrame) {
			TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
			// this.listener.onMessage(textFrame.text());
			logger.debug("TextWebSocketFrame,content text:" + textFrame.text());
		} else if (frame instanceof BinaryWebSocketFrame) {
			BinaryWebSocketFrame binFrame = (BinaryWebSocketFrame) frame;
			logger.debug("BinaryWebSocketFrame,content readable bytes size is :"+binFrame.content().readableBytes());
			System.out.println("BinaryWebSocketFrame,content readable bytes size is :"+binFrame.content().readableBytes());
		} else if (frame instanceof PongWebSocketFrame) {
			logger.debug("WebSocket Client received pong");
		} else if (frame instanceof CloseWebSocketFrame) {
			logger.debug("receive close frame");
			// this.listener.onClose(((CloseWebSocketFrame)frame).statusCode(),
			// ((CloseWebSocketFrame)frame).reasonText());
			ch.close();
		}

	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
