package io.mycat.proxy;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * 代表用户的会话，存放用户会话数据，如前端连接，后端连接，状态等数据
 * 
 * @author wuzhihui
 *
 */
public class UserSession {
	protected static Logger logger = Logger.getLogger(UserSession.class);	
	public BufferPool bufPool;
	public Selector nioSelector;
	// 前端连接
	public String frontAddr;
	public SocketChannel frontChannel;
	public SelectionKey frontKey;
	public ProxyBuffer frontBuffer;
	//后端连接
	public String backendAddr;
	public SocketChannel backendChannel;
	public SelectionKey backendKey;
	public ProxyBuffer backendBuffer;
	private boolean closed;
		
	public UserSession(BufferPool bufPool,Selector nioSelector,SocketChannel frontChannel)
	{
		this.bufPool=bufPool;
		this.nioSelector=nioSelector;
		this.frontChannel=frontChannel;
		frontBuffer=new ProxyBuffer(bufPool.allocByteBuffer());
		backendBuffer=new ProxyBuffer(bufPool.allocByteBuffer());
	}
	
	public  boolean isFrontOpen()
	{
		return frontChannel!=null && frontChannel.isConnected();
	}
	public  boolean isBackendOpen()
	{
		return backendChannel!=null && backendChannel.isConnected();
	}
	
	public String sessionInfo()
	{
		return " ["+this.frontAddr+"->"+this.backendAddr+']';
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public void close(String message) {
		if(!this.isClosed())
		{
		 logger.info("close session "+this.sessionInfo()+ " for reason "+message);
			closeSocket(frontChannel);
			bufPool.recycleBuf(frontBuffer.getBuffer());
			closeSocket(backendChannel);
			bufPool.recycleBuf(this.backendBuffer.getBuffer());
		}else
		{
			logger.warn("session already closed "+this.sessionInfo());
		}
		
	}
	private void closeSocket(Channel channel)
	{
		if(channel!=null && channel.isOpen())
		{
			try {
				channel.close();
			} catch (IOException e) {
				//
			}
			
		}
	}
}
