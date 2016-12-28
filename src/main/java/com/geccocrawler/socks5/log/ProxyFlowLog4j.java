package com.geccocrawler.socks5.log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geccocrawler.socks5.handler.ProxyChannelTrafficShapingHandler;

import io.netty.channel.ChannelHandlerContext;

public class ProxyFlowLog4j implements ProxyFlowLog {
	
	private static final Logger logger = LoggerFactory.getLogger(ProxyFlowLog4j.class);
	
	public void log(ChannelHandlerContext ctx) {
		ProxyChannelTrafficShapingHandler trafficShapingHandler = ProxyChannelTrafficShapingHandler.get(ctx);
		InetSocketAddress localAddress = (InetSocketAddress)ctx.channel().localAddress();
		InetSocketAddress remoteAddress = (InetSocketAddress)ctx.channel().remoteAddress();
		
		long readByte = trafficShapingHandler.trafficCounter().cumulativeReadBytes();
		long writeByte = trafficShapingHandler.trafficCounter().cumulativeWrittenBytes();
		
		logger.info("{},{},{},{}:{},{}:{},{},{},{}", 
				trafficShapingHandler.getUsername(),
				trafficShapingHandler.getBeginTime(),
				trafficShapingHandler.getEndTime(),
				getLocalAddress(), 
				localAddress.getPort(), 
				remoteAddress.getAddress().getHostAddress(), 
				remoteAddress.getPort(),
				readByte, 
				writeByte, 
				(readByte + writeByte));
	}

	/**
	 * 获取本机的IP
	 * 
	 * @return Ip地址
	 */
	private static String getLocalAddress() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                if (addresses.hasMoreElements()) {
                	InetAddress address = addresses.nextElement();
                	if(address instanceof Inet4Address) {
                		return address.getHostAddress();
                	}
                }
            }
        } catch (SocketException e) {
            logger.debug("Error when getting host ip address: <{}>.", e.getMessage());
        }
        return "127.0.0.1";
    }

}
