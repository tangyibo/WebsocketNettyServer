package org.weishao.server.handler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;

import org.weishao.pojo.ParameterMap;
import org.weishao.pojo.Session;
import org.weishao.annotation.OnBinary;
import org.weishao.annotation.OnClose;
import org.weishao.annotation.OnError;
import org.weishao.annotation.OnEvent;
import org.weishao.annotation.OnOpen;
import org.weishao.annotation.OnMessage;
import org.weishao.annotation.ServerEndpoint;

@ServerEndpoint(prefix="netty-websocket")
@Component
public class WebSocketHandler {
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static Map<String, Session> onlineSessions = new ConcurrentHashMap<>();
	
    //每隔2秒执行一次
    @Scheduled(fixedRate = 5000)
    public void simpleTimerTask() {
        System.out.println("简单定时任务执行时间：" + dateFormat.format(new Date())+"当前连接数:"+onlineSessions.size());
        onlineSessions.forEach((id, session) -> {
            try {
                session.sendText("["+dateFormat.format(new Date())+"] :" +"hearted message:");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //每天3：05执行
    //@Scheduled(cron = "0 05 03 ? * *")
    //public void crontTimeTask() {
    //    System.out.println("复杂定时任务执行时间：" + dateFormat.format(new Date()));
    //}


	@OnOpen
	public void onOpen(Session session, HttpHeaders headers, ParameterMap parameterMap) throws IOException {
		System.out.println("new connection");
		onlineSessions.put(session.channel().id().asLongText(), session);
		String paramValue = parameterMap.getParameter("paramKey");
        System.out.println("get param paramKey="+paramValue);
	}

	@OnClose
	public void onClose(Session session) throws IOException {
		System.out.println("one connection closed");
		onlineSessions.remove(session.channel().id().asLongText());
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		throwable.printStackTrace();
	}

	@OnMessage
	public void OnMessage(Session session, String message) {
		System.out.println(message);
		session.sendText("["+dateFormat.format(new Date())+"]: " + message);
	}

	@OnBinary
	public void onBinary(Session session, byte[] bytes) {
		for (byte b : bytes) {
			System.out.println(b);
		}
		session.sendBinary(bytes);
	}

	@OnEvent
	public void onEvent(Session session, Object evt) {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
			switch (idleStateEvent.state()) {
			case READER_IDLE:
				System.out.println("read idle");
				break;
			case WRITER_IDLE:
				System.out.println("write idle");
				break;
			case ALL_IDLE:
				System.out.println("all idle");
				break;
			default:
				break;
			}
		}
	}
}
