package com.serversocket.framework.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import com.serversocket.example.ServerConfig;

/** 
 * @author Zheng Haibo
 * @email zhb931706659@126.com
 * @version 2013年8月13日 上午9:54:22
 */
public class ThreadServerTransferFileRecv extends Thread{
	private ServerSocket transferFileServer=null;
	private int port=0;
	private String fileName;
	private int fileSize;
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public ThreadServerTransferFileRecv(int port,String fileName,int fileSize){
		this.fileName=fileName;
		this.fileSize=fileSize;
		Random ran= new Random();
		this.port = ran.nextInt(99)+port;
		try {
			transferFileServer=new ServerSocket(getPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				Random rant= new Random();
				this.port = rant.nextInt(99)+port;
				transferFileServer=new ServerSocket(getPort());
			} catch (IOException e1){
				// TODO Auto-generated catch block
				e1.printStackTrace();
				this.port=0;
				System.out.println("文件传输的socket启动失败！无法进行文件传输...");
			}
		}
	}
	public void run(){
			try {
				Socket mSocket=transferFileServer.accept();
				recvFile(mSocket);
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private boolean recvFile(Socket mSocket) throws IOException{
		//this.fileName=mSocket.getInetAddress().toString()+"_"+mSocket.getPort()+fileName;
		System.out.println("server recv:"+fileName);
		File mFile = new File(fileName);
		FileOutputStream file = new FileOutputStream(mFile);
		System.out.println("server begin recv...");
		int nChunkCount = fileSize / ServerConfig.CHUNK_SIZE;
		if (fileSize % ServerConfig.CHUNK_SIZE!= 0) {
			nChunkCount++;
		}
		for (int i = 0; i < nChunkCount; i++) {
			byte date[] = new byte[ServerConfig.CHUNK_SIZE];
			int nLeft;
			if (i + 1 == nChunkCount)// 最后一块
				nLeft = fileSize - ServerConfig.CHUNK_SIZE
						* (nChunkCount - 1);
			else
				nLeft = ServerConfig.CHUNK_SIZE;
			int idx = 0;
			int ret = 0;
			while (nLeft > 0) {
				ret = 0;
				try {
					ret = mSocket.getInputStream().read(date,
							idx, nLeft);
				} catch (Exception e) {
					
				}
				idx += ret;
				nLeft -= ret;
			}
			file.write(date, 0, idx);
			file.flush();
			System.out.println("WriteFiles :have recv count" + i);
		}
		System.out.println("server recv finished...");
		file.close();
		return true;
	}
}
