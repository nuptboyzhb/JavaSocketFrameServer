package com.serversocket.framework.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class ThreadServerTransferFileSend extends Thread{
	private ServerSocket transferFileServer=null;
	private int port=0;
	private String uploadFilePath;
	private int uploadFileSize;
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public ThreadServerTransferFileSend(int port,String uploadFilePath){
		this.uploadFilePath=uploadFilePath;
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
				sendFile(mSocket);
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private boolean sendFile(Socket mSocket) throws IOException{
		File mFile1 = new File(uploadFilePath);
		long file_l= mFile1.length();
		uploadFileSize = new Long(file_l).intValue();
		System.out.println("haibo"+uploadFilePath);
		System.out.println("haibo"+uploadFileSize);
		/////////////////////////////////////////////////////////////
		File mFile = new File(uploadFilePath);
		FileInputStream file =null;
		try {
			file = new FileInputStream(mFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		int nChunkCount = uploadFileSize / ServerConfig.CHUNK_SIZE;
		if (uploadFileSize % ServerConfig.CHUNK_SIZE != 0)
			nChunkCount++;
		byte date[] = new byte[ServerConfig.CHUNK_SIZE];
		for (int i = 0; i < nChunkCount; i++){
			int nLeft;
			if (i + 1 == nChunkCount)
				nLeft = uploadFileSize - ServerConfig.CHUNK_SIZE*(nChunkCount - 1);
			else
				nLeft = ServerConfig.CHUNK_SIZE;
			try {
				file.read(date,0,nLeft);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				file.close();
				return false;
			}
			try {
				mSocket.getOutputStream().write(date,0,nLeft);
				mSocket.getOutputStream().flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				file.close();
				return false;
			}
		}
		try {
			file.close();
			System.out.println("haibo"+ "上传完毕");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
