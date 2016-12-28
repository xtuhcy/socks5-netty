package com.geccocrawler.socks5.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class ProxyIdleHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
				ctx.channel().close();
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
	
}
