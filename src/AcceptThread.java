import java.awt.Dimension;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

/*
 * 接收与检测的服务线程
 * 异步处理的两个Socket队列的线程
 */

class AcceptThread extends Thread
{
	ServerSocket serverSocket;
	Socket acceptSocket;
	boolean canAccept;
	Queue<Socket> acceptQueue;
	AcceptThread(Queue<Socket> acceptQueue)
	{
		this.acceptQueue=acceptQueue;
		canAccept=true;
		try {
			serverSocket=new ServerSocket(10408);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("10408接收端口被占用，该程序将无法接收文件");
			canAccept=false;
		}
		
	}
	public boolean getCanAccept()
	{
		return canAccept;
	}
	
	public void run()
	{
		//System.out.println("开始监听");
		while(true)
		{
			try {
				acceptSocket=serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			//System.out.println(acceptSocket.getInetAddress()+"已连接");
			acceptQueue.offer(acceptSocket);
		}
	}
}

class CheckThread extends Thread
{
	ServerSocket serverSocket;
	Socket checkSocket;
	boolean canAccept;
	Queue<Socket> checkQueue;
	CheckThread(Queue<Socket> checkQueue)
	{
		this.checkQueue=checkQueue;
		canAccept=true;
		try {
			serverSocket=new ServerSocket(20408);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("20408接收端口被占用，该程序将无法接收文件");
			canAccept=false;
		}
		
	}
	public boolean getCanAccept()
	{
		return canAccept;
	}
	
	public void run()
	{
		System.out.println("开始监听");
		while(true)
		{
			try {
				checkSocket=serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			System.out.println(checkSocket.getInetAddress()+"已连接");
			checkQueue.offer(checkSocket);
		}
	}
}

class AddSocketThread extends Thread
{
	Queue<Socket> acceptQueue;
	Queue<Socket> checkQueue;
	JPanel transferListPanel;
	ArrayList<AcceptPanel> acceptList;
	StringClass defaultPath;
	AddSocketThread(Queue<Socket> acceptQueue,Queue<Socket> checkQueue,JPanel transferListPanel,ArrayList<AcceptPanel> acceptList)
	{
		this.acceptQueue=acceptQueue;
		this.checkQueue=checkQueue;
		this.acceptList=acceptList;
		this.transferListPanel=transferListPanel;
		defaultPath=new StringClass("C:/");
	}
	public void run()
	{
		while(true)
		{
			if(!acceptQueue.isEmpty() && !checkQueue.isEmpty())
			{
				transferListPanel.setPreferredSize(new Dimension(251, transferListPanel.getHeight()+100));
				AcceptPanel p=new AcceptPanel(acceptQueue.poll(),checkQueue.poll(),defaultPath);
				transferListPanel.add(p);
				acceptList.add(p);
				transferListPanel.updateUI();
			}
		}
	}
}


