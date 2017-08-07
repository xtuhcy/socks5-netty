# socks5-netty
基于netty实现的socks5代理

## 安装

- 下载git代码 ，mvn install
 
- 或者直接下载bin/proxy

## 运行
- linux ： target/assembler/jsw/proxy/bin/proxy start
	
- windows ： target/assembler/jsw/proxy/bin/proxy.bat start

## 配置

- config.properties
	- port=11080   监听端口
	- auth=true    是否鉴权

- password.properties
	- user=password 鉴权用户密码，每行一个

- log4j.perperties
	- log4j.logger.com.geccocrawler.socks5=info  默认级别是info只输出流量日志

## 扩展
- 自定义鉴权方式

	实现PasswordAuth接口，通过proxyServer.passwordAuth()方法设置。系统自带的是PropertiesPasswordAuth，基于properties文件的鉴权

- 自定义代理日志

	实现ProxyFlowLog接口，通过proxyServer.proxyFlowLog()方法设置。系统自带的是ProxyFlowLog4j，基于log4j的日志记录
