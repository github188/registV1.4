package com.megaeyes.regist.other;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NonblockingSingleFileHTTPServer {
	private ByteBuffer contentBuffer;
	private int port = 80;

	public NonblockingSingleFileHTTPServer(ByteBuffer data, String encoding,
			String MIMEType, int port) throws UnsupportedEncodingException {
		this.port = port;
		String header = "HTTP/1.0 200 OK\r\n" + "Server: OneFile 2.0\r\n"
				+ "Content-length:" + data.limit() + "\r\n" + "Content-type:"
				+ MIMEType + " " + "\r\n\r\n";
		byte[] headerData = header.getBytes("ASCII");
		ByteBuffer buffer = ByteBuffer.allocate(data.limit()
				+ headerData.length);
		buffer.put(headerData);
		buffer.put(data);
		buffer.flip();
		this.contentBuffer = buffer;
	}

	public void run() throws IOException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		ServerSocket serverSocket = serverChannel.socket();
		Selector selector = Selector.open();
		InetSocketAddress localPort = new InetSocketAddress(port);
		serverSocket.bind(localPort);
		serverChannel.configureBlocking(false);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (true) {
			selector.select();
			Iterator keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = (SelectionKey) keys.next();
				keys.remove();
				try {
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key
								.channel();
						SocketChannel channel = server.accept();
						channel.configureBlocking(false);
						SelectionKey newKey = channel.register(selector,SelectionKey.OP_READ);
					} else if (key.isWritable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						if (buffer.hasRemaining()) {
							channel.write(buffer);
						} else {
							channel.close();
						}
					} else if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(4096);
						channel.read(buffer);

						key.interestOps(SelectionKey.OP_WRITE);
						key.attach(contentBuffer.duplicate());
					}
				} catch (IOException ex) {
					key.cancel();
					try {
						key.channel().close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
	public static void main(String[] args) {
		String contentType = "text/exe";
		try{
			FileInputStream fin = new FileInputStream("d:/FB3_win1.exe");
			FileChannel in = fin.getChannel();
			ByteBuffer input = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
			
			int port = 8080;
			String encoding ="ASCII";
			
			NonblockingSingleFileHTTPServer server = new NonblockingSingleFileHTTPServer(input, encoding, contentType, port);
			server.run();
		}catch(Exception e){
			e.printStackTrace();
			System.err.println(e);
		}
		
		
	}
}
