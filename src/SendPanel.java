import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/*
 * 发送面板及发送和检测的后台线程
 */

class SendPanel extends JPanel implements MouseListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String filePath;
	String IP;
	Num state,finishSize,ok;
	File file;
	JLabel fileInfoLabel,stateLabel,speedLabel,timeLabel,stopLabel,cancelLabel;
	Font f;
	JProgressBar progressBar;
	String speedStr,timeStr,stateStr,sizeStr;
	SendSwingWorker sendSwingWorker;
	UpdataSwingWorker updataSwingWorker;
	Socket sendSocket,checkSocket;
	SendPanel(String filePath,String IP)
	{
		this.setPreferredSize(new Dimension(251,100));
		this.setBackground(new Color(230,230,230));
		this.setLayout(null);
		this.filePath=filePath;
		this.IP=IP;
		this.state=new Num(0);
		this.finishSize=new Num(0);
		this.ok=new Num(-1);
		file=new File(this.filePath);
		String gbkStr="";
		try {
				gbkStr= new String(this.filePath.substring(filePath.lastIndexOf('\\')+1).getBytes("gb2312"), "ISO8859_1");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		if(gbkStr.length()>13)
			gbkStr=gbkStr.substring(0,4)+"..."+gbkStr.substring(gbkStr.length()-6, gbkStr.length());
		long fileSize=file.length();
		sizeStr="";
		DecimalFormat de;
		if(fileSize<1024) sizeStr=String.valueOf(fileSize)+"B";
		else if(fileSize<1024*1000)
		{
			de=new DecimalFormat(".0");
			sizeStr=de.format((double)(fileSize/1024.0))+"K";
		}
		else if(fileSize<1024*1024)
		{
			de=new DecimalFormat(".0");
			sizeStr=de.format((double)(fileSize/1024.0))+"K";
		}
		else if(fileSize<1024*1024*1000)
		{
			de=new DecimalFormat(".00");
			sizeStr=de.format((double)(fileSize/1024.0/1024.0))+"M";
		}
		else if(fileSize<1024*1024*1024)
		{
			de=new DecimalFormat(".0");
			sizeStr=de.format((double)(fileSize/1024.0/1024.0))+"M";
		}
		else if(fileSize<(long)1024*1024*1024*1000)
		{
			de=new DecimalFormat(".00");
			sizeStr=de.format((double)(fileSize/1024.0/1024.0/1024.0))+"G";
		}
		else if(fileSize<(long)1024*1024*1024*1000)
		{
			de=new DecimalFormat(".0");
			sizeStr=de.format((double)(fileSize/1024.0/1024.0/1024.0))+"G";
		}
		else sizeStr="大于1T";
		
		try {
			fileInfoLabel=new JLabel(new String(gbkStr.getBytes("iso8859-1"),"gb2312")+"("+sizeStr+")");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		f=new Font("微软雅黑",0,13);
		JLabel IPLabel=new JLabel("To:"+IP);
		IPLabel.setBounds(26, 12, 200, 15);
		IPLabel.setForeground(new Color(141,32,32));
		IPLabel.setFont(f);
		this.add(IPLabel);
		fileInfoLabel.setFont(f);
		fileInfoLabel.setBounds(26, 30, 160, 15);
		this.add(fileInfoLabel);
		stateLabel=new JLabel("连接中");
		stateLabel.setFont(f);
		stateLabel.setBounds(185, 30, 40, 15);
		this.add(stateLabel);
		progressBar=new JProgressBar(0,100);
		progressBar.setBounds(26, 49, 200, 18);
		progressBar.setStringPainted(true);
		this.add(progressBar);
		
		
		speedLabel=new JLabel();
		speedLabel.setBounds(25, 72, 53, 12);
		speedLabel.setForeground(new Color(76,76,76));
		speedLabel.setFont(new Font("微软雅黑",0,10));
		timeLabel=new JLabel();
		timeLabel.setBounds(80, 72, 46, 12);
		timeLabel.setForeground(new Color(76,76,76));
		timeLabel.setFont(new Font("微软雅黑",0,10));
		stopLabel=new JLabel();
		stopLabel.setBounds(166, 71, 28, 14);
		stopLabel.setForeground(new Color(111,157,183));
		stopLabel.setFont(new Font("微软雅黑",0,13));
		stopLabel.addMouseListener(this);
		cancelLabel=new JLabel("取消");
		cancelLabel.setBounds(201, 71, 28, 14);
		cancelLabel.setForeground(new Color(111,157,183));
		cancelLabel.setFont(new Font("微软雅黑",0,13));		
		cancelLabel.addMouseListener(this);
		this.add(cancelLabel);
		this.add(speedLabel);
		this.add(timeLabel);
		this.add(stopLabel);
		this.updateUI();
		
		sendSocket=new Socket();
		checkSocket=new Socket();
		int flag=1;
		try {
			sendSocket.connect(new InetSocketAddress(InetAddress.getByName(IP),10408));
			checkSocket.connect(new InetSocketAddress(InetAddress.getByName(IP),20408));
		} catch (Exception e) {
			e.printStackTrace();
			stateLabel.setText("没连上");
			cancelLabel.setText("");
			flag=0;
			this.updateUI();
		}
		if(flag==1)
		{
			sendSwingWorker=new SendSwingWorker(sendSocket,stateLabel,cancelLabel,this,state,filePath,sizeStr,IP,fileSize,stopLabel,ok);
			updataSwingWorker=new UpdataSwingWorker(checkSocket,this,stateLabel,stopLabel,cancelLabel,speedLabel,timeLabel,finishSize,state,progressBar,fileSize,IP,ok);
			sendSwingWorker.execute();
			updataSwingWorker.execute();
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		JLabel la=(JLabel)(e.getSource());
		String str=la.getText();
		switch(str)
		{
		case "取消":
			state.setNum(-1);
			stateLabel.setText("已取消");
			stopLabel.setText("");
			cancelLabel.setText("");
			speedLabel.setText("");
			timeLabel.setText("");
			break;
		case "暂停":
			state.setNum(0);
			stateLabel.setText("已暂停");
			stopLabel.setText("开始");
			speedLabel.setText("");
			timeLabel.setText("");
			break;
		case "开始":
			state.setNum(1);
			stateLabel.setText("发送中");
			stopLabel.setText("暂停");
			break;
		}
		this.updateUI();
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		JLabel la=(JLabel)(e.getSource());
		la.setForeground(new Color(0,0,0));
		this.updateUI();
	}
	@Override
	public void mouseExited(MouseEvent e) {
		JLabel la=(JLabel)(e.getSource());
		la.setForeground(new Color(111,157,183));
		this.updateUI();
	}
	@Override
	public void mousePressed(MouseEvent e) {
		JLabel la=(JLabel)(e.getSource());
		if(la==stopLabel) la.setBounds(167, 72, 28, 14);
		else la.setBounds(202, 72, 28, 14);
		this.updateUI();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		JLabel la=(JLabel)(e.getSource());
		if(la==stopLabel) la.setBounds(166, 71, 28, 14);
		else la.setBounds(201, 71, 28, 14);
		this.updateUI();		
	}
	
	
	public void begin()
	{
		if(state.getNum()==0)
		{
			state.setNum(1);
			stateLabel.setText("发送中");
			stopLabel.setText("暂停");
		}
	}
	
	public void stop()
	{
		if(state.getNum()==1)
		{
			state.setNum(0);
			stateLabel.setText("已暂停");
			stopLabel.setText("开始");
			speedLabel.setText("");
			timeLabel.setText("");
		}
	}
	
	public void cancel()
	{
		if(state.getNum()!=-1)
		{
			state.setNum(-1);
			stateLabel.setText("已取消");
			stopLabel.setText("");
			cancelLabel.setText("");
			speedLabel.setText("");
			timeLabel.setText("");
		}
	}
}

class SendSwingWorker extends SwingWorker<Void,Integer>
{
	JLabel stateLabel,cancelLabel,stopLabel;
	SendPanel mainPanel;
	Num state,ok;
	String filePath,sizeStr,IP;
	long fileSize;
	Socket socket;
	SendSwingWorker(Socket socket,JLabel stateLabel,JLabel cancelLabel,SendPanel mainPanel,Num state,String filePath,String sizeStr,String IP,long fileSize,JLabel stopLabel,Num ok)
	{
		this.stateLabel=stateLabel;
		this.cancelLabel=cancelLabel;
		this.mainPanel=mainPanel;
		this.state=state;
		this.ok=ok;
		this.filePath=filePath;
		this.sizeStr=sizeStr;
		this.IP=IP;
		this.fileSize=fileSize;
		this.stopLabel=stopLabel;
		this.socket=socket;
	}
	
	protected void process(List<Integer> chunks) 
	{
		for (int number :chunks)
		{
			if(number==-1)
			{
				stateLabel.setText("没连上");
				cancelLabel.setText("");	
			}
			else if(number==-2)
			{
				stateLabel.setText("被拒绝");
				cancelLabel.setText("");
			}
			else if(number==-3)
			{
				stateLabel.setText("发送中");
				stopLabel.setText("暂停");
			}
			mainPanel.updateUI();
		}
	}	
	
	@SuppressWarnings("resource")
	public Void doInBackground() 
	{
		int readSize=0;
		byte[] readBuffer=new byte[1024*8];
		DataOutputStream outputStream;
		DataInputStream fis,inputStream;
        
        try {
			inputStream=new DataInputStream(socket.getInputStream());
			outputStream=new DataOutputStream(socket.getOutputStream());
			fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
			outputStream.writeUTF(filePath.substring(filePath.lastIndexOf('\\')+1));
			outputStream.writeInt(0);
			outputStream.writeUTF(sizeStr);
			outputStream.writeInt(0);
			outputStream.writeLong(fileSize);
		} catch (Exception e) {
			e.printStackTrace();
			publish(-1);
			state.setNum(-1);
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
        try {
			if(inputStream.readInt()==-1)
			{
				publish(-2);
				state.setNum(-1);
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			publish(-1);
			state.setNum(-1);
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
        if(state.getNum()!=-1) state.setNum(1);
        publish(-3);
        while(true)
        {
        	if(state.getNum()==1)
        	{
        		try {
					if((readSize=fis.read(readBuffer))!=-1)
					{
						outputStream.write(readBuffer,0,readSize);
					}
					else
					{
						fis.close();
						ok.setNum(1);
					}
				} catch (IOException e) {
					continue;
				}
        	}
        	else if(state.getNum()==-1)
        	{
        		try {
					socket.close();
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
        		break;
        	}
        	else if(state.getNum()==0)
        	{
        		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        }
        return null;
	}
}

class UpdataSwingWorker extends SwingWorker<Void,Integer>
{
	SendPanel mainPanel;
	JLabel stateLabel,stopLabel,cancelLabel,speedLabel,timeLabel;
	StringClass speedStr,timeStr;
	Num finishSize,state,ok;
	JProgressBar progressBar;
	long lastTime,nowTime,lastSize,fileSize;
	String IP;
	Socket socket;
	UpdataSwingWorker(Socket socket,SendPanel mainPanel,JLabel stateLabel,JLabel stopLabel,JLabel cancelLabel,JLabel speedLabel,JLabel timeLabel,Num finishSize,Num state,JProgressBar progressBar,long fileSize,String IP,Num ok)
	{
		this.mainPanel=mainPanel;
		this.stateLabel=stateLabel;
		this.stopLabel=stopLabel;
		this.cancelLabel=cancelLabel;
		this.speedLabel=speedLabel;
		this.timeLabel=timeLabel;
		this.finishSize=finishSize;
		this.state=state;
		this.ok=ok;
		this.progressBar=progressBar;
		this.fileSize=fileSize;
		this.IP=IP;
		this.speedStr=new StringClass("");
		this.timeStr=new StringClass("");
		this.socket=socket;
	}
	
	protected void process(List<Integer> chunks) 
	{
		for (int number :chunks)
		{
			if(number==-1)
			{
				stateLabel.setText("没连上");
				cancelLabel.setText("");
				speedLabel.setText("");
				timeLabel.setText("");
			}
			else if(number==-2)
			{
				stateLabel.setText("被拒绝");
				cancelLabel.setText("");
				speedLabel.setText("");
				timeLabel.setText("");
			}
			else if(number==-4)
			{
				stateLabel.setText("已断开");
				stopLabel.setText("");
				cancelLabel.setText("");
				speedLabel.setText("");
				timeLabel.setText("");
			}
			else if(number==100)
			{
				stateLabel.setText("已完成");
				progressBar.setValue(100);
				stopLabel.setText("");
				cancelLabel.setText("");
				speedLabel.setText("");
				timeLabel.setText("");
			}
			else
			{
				progressBar.setValue(number);
				speedLabel.setText(speedStr.getStr());
				timeLabel.setText(timeStr.getStr());
			}
			mainPanel.updateUI();
		}
	}	
	
	@SuppressWarnings("unused")
	public Void doInBackground()
	{
		
		DataInputStream inputStream;
		DataOutputStream outputStream;
		lastSize=0;
		long h,m,s;
		String hs,ms,ss;
		long needTime;
		try {	
			inputStream=new DataInputStream(socket.getInputStream());
			outputStream=new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			publish(-1);
			state.setNum(-1);
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
		try {
			inputStream.readInt();
		} catch (Exception e) {
			e.printStackTrace();
			publish(-1);
			state.setNum(-1);
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
		lastTime=System.currentTimeMillis();
		while(true)
		{
			if(state.getNum()==1)
			{
				try {
					finishSize.setNum(inputStream.readLong());
					nowTime=System.currentTimeMillis();
					double speed=(finishSize.getNum()-lastSize)*1000/(nowTime-lastTime);
        			DecimalFormat de=new DecimalFormat(".00");
        			if(speed<1024) speedStr.serStr(de.format(speed)+"B/s");
        			else if(speed<1024*1024) speedStr.serStr(de.format(speed/1024)+"K/s");
        			else if(speed<1024*1024*1024) speedStr.serStr(de.format(speed/1024/1024)+"M/s");
        			else speedStr.serStr("null");
        			if(speed==0) timeStr.serStr("99:99:99");
					else
					{
						needTime=(long)((fileSize-finishSize.getNum())/speed);
	        			s=needTime%60;
	        			m=(needTime-s)/60%60;
	        			h=((needTime-s)/60-m)/60;
	        			if(h>99)
	        			{
	        				h=m=s=99;
	        			}
	        			if(s<10) ss="0"+String.valueOf(s);
	        			else ss=String.valueOf(s);
	        			if(m<10) ms="0"+String.valueOf(m);
	        			else ms=String.valueOf(m);
	        			if(h<10) hs="0"+String.valueOf(h);
	        			else hs=String.valueOf(h);
	        			timeStr.serStr(hs+":"+ms+":"+ss);
					}		
        			lastTime=System.currentTimeMillis();
        			lastSize=finishSize.getNum();        			
        			if(state.getNum()==1) publish((int)(finishSize.getNum()*100/fileSize));
        			if((int)(finishSize.getNum()*100/fileSize)==100)
    				{
        				state.setNum(-1);
    				}
				} catch (IOException e) {
					if(ok.getNum()!=1)
					{
						e.printStackTrace();
						publish(-4);
						state.setNum(-1);
					}						
					else
					{
						publish(100);
						state.setNum(-1);
					}
				}
				
			}
			else if(state.getNum()==-1)
			{
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			else if(state.getNum()==0)
			{
				try {
					finishSize.setNum(inputStream.readLong());
				} catch (IOException e) {
					e.printStackTrace();
				}
				lastTime=System.currentTimeMillis();
			}
		}
		return null;
	}
}
