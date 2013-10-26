package com.serversocket.example;

public class ServerConfig {
	public static final int TransferFileRecvForServerCmd = 0x219;
	public static final int TransferSendForServerPort = 0x220;
	public static final int TransferFileSendForServerCmd = 0x319;
	public static final int FileTransferBaseSocketPort = 5234;
	////////////////////////////////////////////////////////////////
	public static final int CommandLen = 2052;
	public static final int CHUNK_SIZE=1024*32;
	public static final int ServerCmdSocketPort = 14783;
	private static String Password="123";
	public static String getPassword() {
		return Password;
	}
	public static void setPassword(String password) {
		Password = password;
	}
}
