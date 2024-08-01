package com.mini.codec.proto;

import lombok.Data;

@Data
public class MessageScale {
	
	/**
	 * 指令
	 */
	private String command;
	
	/**
	 * 接收消息的端口Id
	 */
	private String code;
	
	
	/**
	 * 接收消息内容
	 */
	private String msgInfo;
	
	/**
	 * 发送消息端
	 */
	private String workCode;

}
