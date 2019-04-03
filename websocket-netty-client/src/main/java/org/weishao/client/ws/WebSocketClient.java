package org.weishao.client.ws;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.URI;

public class WebSocketClient {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketClient.class);
	
    private final URI uri;
    private Channel ch;
    private static final EventLoopGroup group = new NioEventLoopGroup();

    public WebSocketClient(final String uri) throws Exception {
        this.uri = URI.create(uri);
        ch=null;
        this.open();
    }

    private void open() throws Exception {
        String protocol = uri.getScheme();
        if (!"ws".equals(protocol.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to  WebSocketHttpResponseDecoder in the pipeline.
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        WebSocketClientHandshaker handshaker=WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, httpHeaders, 1280000);
        final WSHandler handler =new WSHandler(handshaker);
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("http-codec", new HttpClientCodec());
				pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
				pipeline.addLast("ws-handler", handler);
			}
		});

		ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
		handler.getHandshakeFuture().sync();
		logger.info("WebSocket Client connect {} ok!",this.uri);
    }

    public void close() {
		logger.info("WebSocket Client sending close");
		if (null != ch) {
			try {
				ch.writeAndFlush(new CloseWebSocketFrame());
				ch.closeFuture().sync();
			} catch (InterruptedException e) {

			}
			
			ch=null;
		}
    }

    public void sendText(final String text) {
        TextWebSocketFrame frame = new TextWebSocketFrame(text);
		ch.writeAndFlush(frame).addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				if (channelFuture.isSuccess()) {
					logger.debug("text send success");
				} else {
					logger.error("text send failed,error: " + channelFuture.cause().getMessage());
				}
			}
		});
    }
    
    public void sendBytes(final byte[] bytes) {
        ByteBuf buffer = ch.alloc().buffer(bytes.length);
        BinaryWebSocketFrame frame=new BinaryWebSocketFrame(buffer.writeBytes(bytes));
		ch.writeAndFlush(frame).addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				if (channelFuture.isSuccess()) {
					logger.debug("binary send success");
				} else {
					logger.error("binary send failed,error: " + channelFuture.cause().getMessage());
				}
			}
		});
    }
}