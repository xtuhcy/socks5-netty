package com.geccocrawler.socks5.handler.ss5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geccocrawler.socks5.ProxyServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;

public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {

	private static final Logger logger = LoggerFactory.getLogger(Socks5InitialRequestHandler.class);
	
	private ProxyServer proxyServer;
	
	public Socks5InitialRequestHandler(ProxyServer proxyServer) {
		this.proxyServer = proxyServer;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
		logger.debug("初始化ss5连接 : " + msg);
		if(msg.decoderResult().isFailure()) {
			logger.debug("不是ss5协议");
			ctx.fireChannelRead(msg);
		} else {
			if(msg.version().equals(SocksVersion.SOCKS5)) {
				if(proxyServer.isAuth()) {
					Socks5InitialResponse initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD);
					ctx.writeAndFlush(initialResponse);
				} else {
					Socks5InitialResponse initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
					ctx.writeAndFlush(initialResponse);
				}
			}
		}
	}

}
