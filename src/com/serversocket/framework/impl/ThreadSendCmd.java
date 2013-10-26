/*
 * $filename: ThreadSendData.java,v $
 * $Date: 2013-6-28  $
 * Copyright (C) ZhengHaibo, Inc. All rights reserved.
 * This software is Made by Zhenghaibo.
 */
package com.serversocket.framework.impl;

import java.io.IOException;
import java.net.Socket;


/*
 *@author: ZhengHaibo  
 *web:     http://blog.csdn.net/nuptboyzhb
 *mail:    zhb931706659@126.com
 *2013-6-28  Nanjing,njupt,China
 */
public class ThreadSendCmd extends Thread{
	NetDataCommand mCommand=null;
	Socket mSocket=null;
	public ThreadSendCmd(NetDataCommand mCommand,Socket mSocket){
          this.mCommand=mCommand;
          this.mSocket=mSocket;
	}
	public void run(){
		try {
			mSocket.getOutputStream().write(mCommand.getByteArrayData());
		} catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
