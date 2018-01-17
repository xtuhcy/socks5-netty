package com.geccocrawler.socks5.handler;

import com.geccocrawler.socks5.log.ProxyFlowLog;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

public class ProxyChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {
	
	public static final String PROXY_TRAFFIC = "ProxyChannelTrafficShapingHandler";
	
	private long beginTime;
	
	private long endTime;
	
	private String username = "anonymous";
	
	private ProxyFlowLog proxyFlowLog;
	
	private ChannelListener channelListener;
	
	public static ProxyChannelTrafficShapingHandler get(ChannelHandlerContext ctx) {
		return (ProxyChannelTrafficShapingHandler)ctx.pipeline().get(PROXY_TRAFFIC);
	}
	
	public ProxyChannelTrafficShapingHandler(long checkInterval, ProxyFlowLog proxyFlowLog, ChannelListener channelListener) {
		super(checkInterval);
		this.proxyFlowLog = proxyFlowLog;
		this.channelListener = channelListener;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		beginTime = System.currentTimeMillis();
		if(channelListener != null) {
			channelListener.active(ctx);
		}
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		endTime = System.currentTimeMillis();
		if(channelListener != null) {
			channelListener.inActive(ctx);
		}
		proxyFlowLog.log(ctx);
		super.channelInactive(ctx);
	}

	public long getBeginTime() {
		return beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public static void username(ChannelHandlerContext ctx, String username) {
		get(ctx).username = username;
	}
	
	public String getUsername() {
		return username;
	}

}
