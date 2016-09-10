import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/*
 * 程序主窗口
 */
class FileTransferFrame extends JFrame implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel selectionPanel,transferPanel,bottomPanel,transferListPanel;
	JTable fileTable,IPTable;
	JScrollPane fileScrollPane,IPScrollPane,transferScrollPane;
	JButton addButton,cleanFileTableButton,cleanIPTableButton,sendButton,pauseButton,startButton,cancelButton;
	Font f;
	FontMetrics fontmetrics;
	AcceptThread acceptThread;
	CheckThread checkThread;
	AddSocketThread addSocketThread;
	ArrayList<AcceptPanel> acceptList;
	ArrayList<SendPanel> sendList;
	ArrayList<String> filesPath;
	ArrayList<String> IP;
	Queue<Socket> acceptQueue,checkQueue;
	FileTransferFrame()
	{	
		
		//设置Frame属性
		this.setLayout(null);
		this.setSize(895, 485);
		this.setLocation(200, 200);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Java文件传输");
		this.getContentPane().setBackground(new Color(96,96,96));
		this.setResizable(false);
		
		//创建选择面板
		selectionPanel=new JPanel();
		selectionPanel.setLayout(null);
		selectionPanel.setBackground(new Color(199,199,199));
		selectionPanel.setBounds(0, 0, 575, 435);
		
		fileTable=new JTable(30,1);
		fileTable.setFillsViewportHeight(true);
		fileTable.setCellSelectionEnabled(false);
		fileTable.setTableHeader(null);
		fileTable.setBackground(new Color(240,240,240));
		
		f=new Font("微软雅黑",0,14);
		fontmetrics=selectionPanel.getFontMetrics(f);
		fileTable.setFont(f);
		fileTable.setRowHeight(fontmetrics.getHeight()+2);
		fileScrollPane=new JScrollPane(fileTable);
		fileScrollPane.setBounds(27, 23, 248, 350);
		JLabel fileLable=new JLabel("文件：");
		fileLable.setFont(f);
		fileLable.setBounds(27, 388,50,25);
		addButton=new JButton("浏览添加");
		addButton.setFont(f);
		addButton.setBackground(new Color(199,199,199));
		addButton.setBounds(81, 388, 88, 25);
		cleanFileTableButton=new JButton("清空列表");
		cleanFileTableButton.setFont(f);
		cleanFileTableButton.setBackground(new Color(199,199,199));
		cleanFileTableButton.setBounds(188, 388, 88, 25);
		
		IPTable=new JTable(30,1);
		IPTable.setFillsViewportHeight(true);
		IPTable.setCellSelectionEnabled(false);
		IPTable.setTableHeader(null);
		IPTable.setBackground(new Color(240,240,240));
		IPTable.setFont(f);
		IPTable.setRowHeight(fontmetrics.getHeight()+2);
		IPScrollPane=new JScrollPane(IPTable);
		IPScrollPane.setBounds(295, 23, 248, 350);
		JLabel IPLable=new JLabel("IP：");
		IPLable.setFont(f);
		IPLable.setBounds(295, 388,50,25);
		cleanIPTableButton=new JButton("清空列表");
		cleanIPTableButton.setFont(f);
		cleanIPTableButton.setBackground(new Color(199,199,199));
		cleanIPTableButton.setBounds(350, 388, 88, 25);
		sendButton=new JButton("发送");
		sendButton.setFont(f);
		sendButton.setBackground(new Color(199,199,199));
		sendButton.setBounds(457, 388, 88, 25);
		
		selectionPanel.add(fileScrollPane);
		selectionPanel.add(fileLable);
		selectionPanel.add(addButton);
		selectionPanel.add(cleanFileTableButton);
		selectionPanel.add(IPScrollPane);
		selectionPanel.add(IPLable);
		selectionPanel.add(cleanIPTableButton);
		selectionPanel.add(sendButton);
		
		//创建传输面板
		transferPanel=new JPanel();
		transferPanel.setLayout(null);
		transferPanel.setBackground(new Color(199,199,199));
		transferPanel.setBounds(578, 0, 317, 435);
		pauseButton=new JButton("全部暂停");
		pauseButton.setFont(f);
		pauseButton.setBackground(new Color(199,199,199));
		pauseButton.setBounds(20, 21, 88, 25);
		startButton=new JButton("全部开始");
		startButton.setFont(f);
		startButton.setBackground(new Color(199,199,199));
		startButton.setBounds(113, 21, 88, 25);
		cancelButton=new JButton("全部清空");
		cancelButton.setFont(f);
		cancelButton.setBackground(new Color(199,199,199));
		cancelButton.setBounds(206, 21, 88, 25);
		
		transferListPanel=new JPanel();
		transferListPanel.setBackground(new Color(230,230,230));
		transferListPanel.setPreferredSize(new Dimension(251, 600));
		transferScrollPane=new JScrollPane(transferListPanel);
		transferScrollPane.setBounds(21, 59, 273, 354);
		
		 
		transferPanel.add(pauseButton);
		transferPanel.add(startButton);
		transferPanel.add(cancelButton);
		transferPanel.add(transferScrollPane);
		
		//创建底面板
		bottomPanel=new JPanel();
		bottomPanel.setLayout(null);
		bottomPanel.setBackground(new Color(155,155,155));
		bottomPanel.setBounds(0, 435, 890, 30);		
		JLabel localIP,progamerName;
		try {
			localIP=new JLabel("本机IP："+InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			localIP=new JLabel("本机IP：127.0.0.1");
		}
		localIP.setBounds(48,0,300,20);
		localIP.setFont(f);
		
		progamerName=new JLabel("作者：小神弟弟    QQ：493150792");
		progamerName.setFont(f);
		progamerName.setBounds(600, 0, 300, 20);
		
		bottomPanel.add(localIP);
		bottomPanel.add(progamerName);
		
		addButton.addActionListener(this);
		cleanFileTableButton.addActionListener(this);
		cleanIPTableButton.addActionListener(this);
		sendButton.addActionListener(this);
		pauseButton.addActionListener(this);
		startButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		this.add(selectionPanel);
		this.add(transferPanel);
		this.add(bottomPanel);
		this.setVisible(true);
		
		sendList=new ArrayList<SendPanel>();
		acceptList=new ArrayList<AcceptPanel>();
		acceptQueue=new ConcurrentLinkedQueue<Socket>();
		checkQueue=new ConcurrentLinkedQueue<Socket>();
		acceptThread=new AcceptThread(acceptQueue);
		checkThread=new CheckThread(checkQueue);
		addSocketThread=new AddSocketThread(acceptQueue,checkQueue,transferListPanel,acceptList);
		if(acceptThread.getCanAccept() && checkThread.getCanAccept())
		{
			acceptThread.start();
			checkThread.start();
			addSocketThread.start();
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==addButton)
		{
			JFileChooser fileChooser=new JFileChooser();
			fileChooser.setMultiSelectionEnabled(true);
			File[] files;
			if(fileChooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
			{
				files=fileChooser.getSelectedFiles();
				ArrayList<String> oldPath=new ArrayList<String>();
				for(int i=0;i<fileTable.getRowCount();i++)
				{
					Object obj=fileTable.getValueAt(i, 0);
					if(!(obj==null || ((String)obj).equals(""))) oldPath.add((String)obj);
				}
				fileTable=new JTable((files.length+oldPath.size())>30?(files.length+oldPath.size()):30,1);
				fileTable.setFillsViewportHeight(true);
				fileTable.setCellSelectionEnabled(false);
				fileTable.setTableHeader(null);
				fileTable.setBackground(new Color(240,240,240));
				fileTable.setFont(f);
				fileTable.setRowHeight(fontmetrics.getHeight()+2);
				fileScrollPane.setViewportView(fileTable);		
				for(int i=0;i<oldPath.size();i++)
				{
					fileTable.setValueAt(oldPath.get(i), i, 0);
				}
				for(int i=0;i<files.length;i++)
				{				
					fileTable.setValueAt(files[i].getAbsolutePath(), i+oldPath.size(), 0);
				}
			}			
		}
		else if(e.getSource()==cleanFileTableButton)
		{
			fileTable=new JTable(30,1);
			fileTable.setFillsViewportHeight(true);
			fileTable.setCellSelectionEnabled(false);
			fileTable.setTableHeader(null);
			fileTable.setBackground(new Color(240,240,240));
			fileTable.setFont(f);
			fileTable.setRowHeight(fontmetrics.getHeight()+2);
			fileScrollPane.setViewportView(fileTable);
		}
		else if(e.getSource()==cleanIPTableButton)
		{
			IPTable=new JTable(30,1);
			IPTable.setFillsViewportHeight(true);
			IPTable.setCellSelectionEnabled(false);
			IPTable.setTableHeader(null);
			IPTable.setBackground(new Color(240,240,240));
			IPTable.setFont(f);
			IPTable.setRowHeight(fontmetrics.getHeight()+2);
			IPScrollPane.setViewportView(IPTable);
		}
		else if(e.getSource()==sendButton)
		{
			int rowCount=fileTable.getRowCount();
			filesPath=new ArrayList<String>();
			IP=new ArrayList<String>();
			for(int i=0;i<rowCount;i++)
			{
				String thisPath=null;
				if(fileTable.getValueAt(i, 0)!=null) thisPath=fileTable.getValueAt(i, 0).toString();
				if(thisPath!=null && !thisPath.equals(""))
				if(new File(thisPath).exists()) filesPath.add(thisPath);
				else JOptionPane.showMessageDialog(this,"\""+thisPath+"\"没找到","消息",JOptionPane.PLAIN_MESSAGE);
			}
			rowCount=IPTable.getRowCount();
			for(int i=0;i<rowCount;i++)
			{
				String thisIP=null;
				if(IPTable.getValueAt(i, 0)!=null) thisIP=IPTable.getValueAt(i, 0).toString();
				if(thisIP!=null && !thisIP.equals("")) IP.add(thisIP);
			}
			for(int i=0;i<IP.size();i++)
			{
				for(int j=0;j<filesPath.size();j++)
				{
					SendPanel p=new SendPanel(filesPath.get(j),IP.get(i));
					sendList.add(p);
					transferListPanel.add(p);
					transferListPanel.updateUI();	
				}
			}
		}
		else if(e.getSource()==pauseButton)
		{
			for(SendPanel p:sendList)
			{
				p.stop();
			}
		}
		else if(e.getSource()==startButton)
		{
			for(SendPanel p:sendList)
			{
				p.begin();
			}
		}
		else if(e.getSource()==cancelButton)
		{
			for(SendPanel p:sendList)
			{
				p.cancel();
			}
			for(AcceptPanel p:acceptList)
			{
				p.cancel();
			}
			transferListPanel.removeAll();
			transferListPanel.setPreferredSize(new Dimension(251,600));
			transferListPanel.updateUI();
		}
	}
}

class Num
{
	long num;
	Num(long a)
	{
		num=a;
	}
	void setNum(long a)
	{
		num=a;
	}
	long getNum()
	{
		return num;
	}
}

class StringClass
{
	String str;
	StringClass(String s)
	{
		str=s;
	}
	void serStr(String s)
	{
		str=s;
	}
	String getStr()
	{
		return str;
	}
}

