import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/*
 * 接收面板及接收与检测的后台线程
 */

public class AcceptPanel extends JPanel implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Socket acceptSocket,checkSocket;
	Num state;
	DataOutputStream dataOut,checkOut;
	DataInputStream dataIn,checkIn;
	String savePath,fileSizeStr,fileName;
	StringClass defaultPath;
	Font f;
	String IP;
	JLabel stateLabel,cancelLabel,speedLabel,timeLabel;
	long fileSize;
	JProgressBar progressBar;
	CheckSwingWorker checkSwingWorker;
	AcceptSwingWorker acceptSwingWorker;
	AcceptPanel(Socket acceptSocket,Socket checkSocket,StringClass defaultPath)
	{	
		this.state=new Num(0);
		this.acceptSocket=acceptSocket;
		this.checkSocket=checkSocket;
		this.defaultPath=defaultPath;
		try {
			dataIn=new DataInputStream(acceptSocket.getInputStream());
			checkIn=new DataInputStream(checkSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			dataOut=new DataOutputStream(acceptSocket.getOutputStream());
			checkOut=new DataOutputStream(checkSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fileName=dataIn.readUTF();
			dataIn.readInt();
			fileSizeStr=dataIn.readUTF();
			dataIn.readInt();
			fileSize=dataIn.readLong();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		savePath="";
		int result=JOptionPane.showConfirmDialog(null,"是否保存\""+fileName+"\"("+fileSizeStr+")","保存",JOptionPane.YES_NO_OPTION);
		if(result==JOptionPane.YES_OPTION)
		{
			JFileChooser fileChooser=new JFileChooser(defaultPath.getStr());			
			fileChooser.setSelectedFile(new File(fileName));
			if(fileChooser.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
			{
				defaultPath.serStr(fileChooser.getSelectedFile().getParent());
				savePath=fileChooser.getSelectedFile().getAbsolutePath();
				fileName=fileChooser.getSelectedFile().getName();
			}
		}
		if(savePath.equals(""))
		{
			try {
				dataOut.writeInt(-1);
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			state.setNum(-1);
			this.setPreferredSize(new Dimension(0,0));
			return;
		}
		IP=acceptSocket.getInetAddress().getHostAddress();
		this.setPreferredSize(new Dimension(251,100));
		this.setBackground(new Color(230,230,230));
		this.setLayout(null);
		f=new Font("微软雅黑",0,13);
		JLabel IPLabel=new JLabel("From:"+IP);
		IPLabel.setBounds(26, 12, 200, 15);
		IPLabel.setForeground(new Color(141,32,32));
		IPLabel.setFont(f);
		this.add(IPLabel);
		JLabel fileNameLabel=null;
		String gbkStr="";
		try {
				gbkStr= new String(this.fileName.getBytes("gb2312"), "ISO8859_1");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		if(gbkStr.length()>13)
			gbkStr=gbkStr.substring(0,4)+"..."+gbkStr.substring(gbkStr.length()-6, gbkStr.length());
		try {
			fileNameLabel=new JLabel(new String(gbkStr.getBytes("iso8859-1"),"gb2312")+"("+fileSizeStr+")");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		fileNameLabel.setFont(f);
		fileNameLabel.setBounds(26, 30, 160, 15);
		this.add(fileNameLabel);
		
		stateLabel=new JLabel("接收中");
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
		cancelLabel=new JLabel("取消");
		cancelLabel.setBounds(201, 71, 28, 14);
		cancelLabel.setForeground(new Color(111,157,183));
		cancelLabel.setFont(new Font("微软雅黑",0,13));		
		cancelLabel.addMouseListener(this);
		this.add(cancelLabel);
		this.add(speedLabel);
		this.add(timeLabel);
		this.updateUI();
		checkSwingWorker=new CheckSwingWorker(this,cancelLabel,stateLabel,speedLabel,timeLabel,state,checkOut,checkIn,checkSocket,progressBar,savePath,fileSize);
		acceptSwingWorker=new AcceptSwingWorker(this,cancelLabel,stateLabel,state,savePath,dataOut,dataIn,acceptSocket);
		checkSwingWorker.execute();
		acceptSwingWorker.execute();
	}
	public void cancel()
	{
		if(state.getNum()!=-1)
		{
			state.setNum(-1);
			stateLabel.setText("已取消");
			cancelLabel.setText("");
			speedLabel.setText("");
			timeLabel.setText("");
			this.updateUI();
		}
	}
	public void mouseClicked(MouseEvent e) {
		state.setNum(-1);
		stateLabel.setText("已取消");
		cancelLabel.setText("");
		speedLabel.setText("");
		timeLabel.setText("");
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
		la.setBounds(202, 72, 28, 14);
		this.updateUI();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		JLabel la=(JLabel)(e.getSource());
		la.setBounds(201, 71, 28, 14);
		this.updateUI();		
	}
}

class AcceptSwingWorker extends SwingWorker<Void,Integer>
{
	AcceptPanel mainPanel;
	JLabel cancelLabel,stateLabel;
	Num state;
	String savePath;
	DataOutputStream dataOut;
	DataInputStream dataIn;
	Socket socket;
	AcceptSwingWorker(AcceptPanel mainPanel,JLabel cancelLabel,JLabel stateLabel,Num state,String savePath,DataOutputStream dataOut,DataInputStream dataIn,Socket socket)
	{
		this.mainPanel=mainPanel;
		this.cancelLabel=cancelLabel;
		this.stateLabel=stateLabel;
		this.state=state;
		this.savePath=savePath;
		this.dataOut=dataOut;
		this.dataIn=dataIn;
		this.socket=socket;
	}
	
	protected void process(List<Integer> chunks) 
	{
		for (int number :chunks)
		{
			if(number==-1)
			{
				stateLabel.setText("有异常");
				cancelLabel.setText("");
			}
			mainPanel.updateUI();
		}
	}
	
	public Void doInBackground() 
	{
		DataOutputStream fos;
		try {
			fos=new DataOutputStream(new FileOutputStream(savePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			try {
				dataOut.writeInt(-1);
				socket.close();
				state.setNum(-1);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			publish(-1);
			return null;
		}
		try {
			dataOut.writeInt(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(state.getNum()!=-1) state.setNum(1);
		byte[] by = new byte[8*1024];
		int amount;
		while(true)
		{
			if(state.getNum()==1)
			{
				try {
					if((amount = dataIn.read(by)) != -1)
					{
						fos.write(by, 0, amount);
						fos.flush();
					}
					else
					{
						fos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(state.getNum()==-1)
			{
				try {
					socket.close();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return null;
	}
}

class CheckSwingWorker extends SwingWorker<Void,Integer>
{
	AcceptPanel mainPanel;
	JLabel cancelLabel,stateLabel,speedLabel,timeLabel;
	Num state;
	DataOutputStream checkOut;
	DataInputStream checkIn;
	Socket checkSocket;
	StringClass speedStr,timeStr;
	JProgressBar progressBar;
	String savePath;
	long fileSize;
	CheckSwingWorker(AcceptPanel mainPanel,JLabel cancelLabel,JLabel stateLabel,JLabel speedLabel,JLabel timeLabel,Num state,DataOutputStream checkOut,DataInputStream checkIn,Socket checkSocket,JProgressBar progressBar,String savePath,long fileSize)
	{
		this.mainPanel=mainPanel;
		this.cancelLabel=cancelLabel;
		this.stateLabel=stateLabel;
		this.speedLabel=speedLabel;
		this.timeLabel=timeLabel;
		this.state=state;
		this.checkOut=checkOut;
		this.checkIn=checkIn;
		this.checkSocket=checkSocket;
		this.progressBar=progressBar;
		this.savePath=savePath;
		this.fileSize=fileSize;
		this.speedStr=new StringClass("");
		this.timeStr=new StringClass("");
		
	}
	
	protected void process(List<Integer> chunks) 
	{
		for (int number :chunks)
		{
			if(number==-1)
			{
				stateLabel.setText("有异常");
				cancelLabel.setText("");
			}
			else if(number==100)
			{
				progressBar.setValue(100);
				stateLabel.setText("已完成");
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
	
	public Void doInBackground() 
	{
		long lastTime,nowTime,finishSize,lastfinishSize,needTime;
		double speed;
		long h,m,s;
		String hs,ms,ss;
		DecimalFormat de=new DecimalFormat(".00");
		try {
			checkOut.writeInt(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		lastTime=System.currentTimeMillis();
		lastfinishSize=0;
		while(true)
		{
			if(state.getNum()==1)
			{
				nowTime=System.currentTimeMillis();
				if(nowTime-lastTime>=1000)
				{
					finishSize=(new File(savePath)).length();//获取已经传输的大小
					speed=(finishSize-lastfinishSize)*1000/(nowTime-lastTime);
					if(speed<1024) speedStr.serStr(de.format(speed)+"B/s");
        			else if(speed<1024*1024) speedStr.serStr(de.format(speed/1024)+"K/s");
        			else if(speed<1024*1024*1024) speedStr.serStr(de.format(speed/1024/1024)+"M/s");
        			else speedStr.serStr("null");
					if(speed==0) timeStr.serStr("99:99:99");
					else
					{
						needTime=(long)((fileSize-finishSize)/speed);
	        			
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
        			lastfinishSize=finishSize;
        			if(state.getNum()!=-1)
        			publish((int)(finishSize*100/fileSize));//把当前进度推送给Swing
        			try {
						checkOut.writeLong((new File(savePath)).length());
						checkOut.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
        			if((int)(finishSize*100/fileSize)==100)
    				{
        				state.setNum(-1);
    				}
				}
			}
			else if(state.getNum()==-1)
			{
				try {
					checkSocket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return null;
	}
}
