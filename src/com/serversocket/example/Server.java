/*
 * $filename: Server.java,v $
 * $Date: 2013-6-20  $
 * Copyright (C) ZhengHaibo, Inc. All rights reserved.
 * This software is Made by Zhenghaibo.
 */
package com.serversocket.example;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.serversocket.framework.impl.NetDataCommand;
import com.serversocket.framework.impl.NetDataTypeTransform;
import com.serversocket.framework.impl.ThreadSendCmd;
import com.serversocket.framework.impl.ThreadServerTransferFileRecv;
import com.serversocket.framework.impl.ThreadServerTransferFileSend;
/*
 *@author: ZhengHaibo  
 *web:     http://blog.csdn.net/nuptboyzhb
 *mail:    zhb931706659@126.com
 *2013-6-20  Nanjing,njupt,China
 */
public class Server extends Thread {
	private ServerSocket server = null;
	private Map<String,Socket> clientSockets=new HashMap<String,Socket>();
	private boolean isAccept = true;
	public boolean isLive = false;
	private ThreadReadNetData mThreadReadNetData = null;
	NetDataTypeTransform mNetDataTypeTransform = new NetDataTypeTransform();
	public String getIP(){
		InetAddress IP=null;
		try {
			IP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("IP of my system is := "+IP.getHostAddress());
		String ipString=IP.getHostAddress();
		return ipString;
		
	}
    public void addClientSocket(Socket mSocket){
    	String tagsString=mSocket.getInetAddress().toString()+":"+mSocket.getPort();
    	tagsString=tagsString.substring(1);
    	clientSockets.put(tagsString, mSocket);
    	System.out.println("Add in map :"+tagsString);
    }
    public void removeClientSocket(Socket mSocket){
    	String tagsString=mSocket.getInetAddress().toString()+":"+mSocket.getPort();
    	Iterator<Map.Entry<String, Socket>> it = clientSockets.entrySet().iterator();  
        while(it.hasNext()){  
        	Map.Entry<String, Socket> entry=it.next();  
            String key=entry.getKey();
            if(key.equals(tagsString)){
                it.remove();
            }
        }
    }
    public Socket getOnLineClientSocket(){
    	Iterator<Map.Entry<String, Socket>> it = clientSockets.entrySet().iterator();  
        while(it.hasNext()){  
        	Map.Entry<String, Socket> entry=it.next();  
            String key=entry.getKey();  
            if(clientSockets.get(key).isConnected()){
            	return clientSockets.get(key);
            }
        }
        return null;
    }
	public Server() {
		try {
			server = new ServerSocket(ServerConfig.ServerCmdSocketPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isAccept = false;
			System.out.println("生成服务器失败，你可能已经启动了一个服务器！");
		}
		System.out.println("Server is Starting ... The IP:"+getIP()+" --port:"+ServerConfig.ServerCmdSocketPort);
	}

	public Server(int port) {
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isAccept = false;
			System.out.println("生成服务器失败，你可能已经启动了一个服务器！");
		}
		System.out.println("Server is Starting ... The IP:"+getIP()+" --port:"+port);
	}

	public void run() {
		while (isAccept) {
			try {
				isLive = true;
				Socket mSocket = server.accept();
				String passwordString = "Password";
				mSocket.getOutputStream()
						.write(mNetDataTypeTransform
								.StringToByteArray(passwordString));
				byte recvPassword[] = new byte[100];
				int recvLen = mSocket.getInputStream().read(recvPassword);
				if (mNetDataTypeTransform.ByteArraytoString(recvPassword,
						recvLen).equals(ServerConfig.getPassword())) {
					String okString = "OK";
					mSocket.getOutputStream().write(
							mNetDataTypeTransform.StringToByteArray(okString));
					System.out.println("客户端输入密码正确");
					mThreadReadNetData = new ThreadReadNetData(mSocket);
					mThreadReadNetData.start();
					addClientSocket(mSocket);
				} else {
					String okString = "NOOK";
					mSocket.getOutputStream().write(
							mNetDataTypeTransform.StringToByteArray(okString));
					System.out.println("客户端输入密码错误");
					mSocket.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		isLive = false;
	}
	private class ThreadReadNetData extends Thread {
		private boolean isRuning = true;
		private byte[] byteArrayData = new byte[ServerConfig.CommandLen];
		Socket mLocalSocket = null;
		public ThreadReadNetData(Socket mSocket) {
			// TODO Auto-generated constructor stub
			mLocalSocket = mSocket;
		}
		public void run() {
			while (isRuning) {
				try {
					mLocalSocket.getInputStream().read(byteArrayData);
					NetDataCommand mCommand = new NetDataCommand(byteArrayData);
					if (mCommand.getID() == 0) {
						continue;
					}
					switch (mCommand.getID()) {
					case ServerConfig.TransferFileRecvForServerCmd:
						TransferFileRecvForServerCmdPro(mLocalSocket, mCommand);
						break;
					default:
						break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("A client is closed...");
					mClose();
					return;
				}
			}
		}
		public void mClose() {
			isRuning = false;
			try {
				removeClientSocket(mLocalSocket);
				mLocalSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			interrupt();
		}
	}
	private void TransferFileRecvForServerCmdPro(Socket mLocalSocket2,
			NetDataCommand mCommand) {
		// TODO Auto-generated method stub
		System.out.println(mCommand.getID() + " ," + mCommand.getLparam());
		String content = mCommand.getLparam();
		int pos = content.indexOf('{');
		String fileName = content.substring(0, pos);
		String fileSizeStr = content.substring(pos + 1);
		int fileSize = Integer.parseInt(fileSizeStr);
		String tagsString=mLocalSocket2.getInetAddress().toString()+"_"+mLocalSocket2.getPort()+"_";
    	fileName=tagsString.substring(1)+fileName;
		ThreadServerTransferFileRecv mThreadTransferFile = new ThreadServerTransferFileRecv(
				ServerConfig.FileTransferBaseSocketPort, fileName, fileSize);
		mThreadTransferFile.start();
		int port = mThreadTransferFile.getPort();
		NetDataCommand commd = new NetDataCommand(ServerConfig.TransferSendForServerPort, port
				+ "\0");
		ThreadSendCmd mSendNetData = new ThreadSendCmd(commd,
				mLocalSocket2);
		mSendNetData.start();
	}
	public void CloseServer() {
		isAccept = false;
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mThreadReadNetData != null) {
			mThreadReadNetData.mClose();
		}
		interrupt();
	}
	/*
	 * 向客户端发送文件，调用示例为如下：
	 * sendFileToClient(getOnLineClientSocket(),"D://aa.jpg","aa.jpg");
	 * 注意：前提是客户端必须已与服务器建立连接
	 */
    public void sendFileToClient(Socket clientSocket,String uploadFilePath,String filename){
    	File mFile1 = new File(uploadFilePath);
		long file_l= mFile1.length();
		int fileSize = new Long(file_l).intValue();
    	ThreadServerTransferFileSend tstfs=new ThreadServerTransferFileSend(ServerConfig.FileTransferBaseSocketPort, uploadFilePath);
    	int port=tstfs.getPort();
    	tstfs.start();
    	NetDataCommand commd = new NetDataCommand(ServerConfig.TransferFileSendForServerCmd, 
    			filename+"{"+fileSize+"}"+port+"\0");
		ThreadSendCmd mSendNetData = new ThreadSendCmd(commd,clientSocket);
		mSendNetData.start();
    }
	public static void main(String[] args) {
		Server server=new Server();
		server.start();
		//server.sendFileToClient(server.getOnLineClientSocket(),"D://lire//imagecache//coreImageDatabase//1.jpg","1.jpg");
	}
}
