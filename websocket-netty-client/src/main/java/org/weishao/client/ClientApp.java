package org.weishao.client;

import java.io.File;
import java.io.FileInputStream;

import org.weishao.client.ws.WebSocketClient;

public class ClientApp {
	
	public static String uri = "ws://127.0.0.1:8081/ws";
	
	public static void testSendText() {
		WebSocketClient client=null;
		try {
			client = new WebSocketClient(ClientApp.uri);
			for (int i = 0; i < 100; ++i) {
				System.out.println("text send");
				client.sendText("hello");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(null!=client) {
				client.close();
			}
		}
	}
	
	public static void testSendBinary() {
		WebSocketClient client=null;
		try {
			client = new WebSocketClient(ClientApp.uri);

			File file = new File("E:\\smartdm.vsd");
			FileInputStream fin =new FileInputStream(file);
			byte[] data = new byte[1024];
			while ((fin.read(data)) > 0) {
				client.sendBytes(data);
				System.out.println("send bytes count is "+data.length);
			}
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(null!=client) {
				client.close();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		testSendBinary();
	}
}
