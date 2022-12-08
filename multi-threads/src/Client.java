import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import client_serveur.FileManager;

public class Client implements ActionListener{
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String userName;
	private String userDir;
	private String nameDossier = "";
	private String nanoMessage = "";
	private String id ;
	private String nanoFile = "";
	private boolean takeFile;
	private boolean nanoMode;
	static int count = 0;
	JTextArea area ;
	JFrame nano = new JFrame();
	
	
	public Client(Socket socket,String username,String userDir,String userID) {
		try {
			this.socket = socket;
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.userName = username;
			this.userDir = userDir;
			this.id = userID;
		} catch (Exception e) {
			System.err.println("client");
			closeEverything(socket , bufferedReader, bufferedWriter);
		}
	}
	
	Thread th =  new Thread(new Runnable() {
		@Override
		public void run() {
			String msg;
			Vector<String> tabmsg = new Vector<String>();
			File fichier = null;
  			FileManager manage = null ;
			int i = 0;
			while (socket.isConnected()) {
				try {
					i=0;
					while (bufferedReader.ready()) {
						msg = bufferedReader.readLine();
						tabmsg.addElement(msg);
					}
					if (takeFile && tabmsg.size() != 0) {
						fichier = new File(nameDossier);
						manage = new FileManager();
						if(!fichier.exists()) {
		          			System.out.println("creation");
		        			fichier.createNewFile();
		        		}
						else {
							fichier.delete();
							fichier.createNewFile();
						}
					}
					if (nanoMode && tabmsg.size() != 0) {
						String nanodata = "";
						for (int j = 0; j < tabmsg.size(); j++) {
							nanodata += tabmsg.elementAt(j) + "\n";
						}
						nano(nanodata);
						while (nano.isActive()) {}
//						System.out.println(nanoMessage);
						bufferedWriter.write(nanoMessage);
						bufferedWriter.newLine();
						nanoMode = false;
						nanodata = "";
						nanoMessage = "";
						bufferedWriter.flush();
						
						
					}
					for (int j = 0; j < tabmsg.size(); j++) {
						System.out.println(tabmsg.elementAt(j));
						if (tabmsg.elementAt(0).equals("close")) {
							System.exit(0);
						}
						if (takeFile) {
							try {
								manage.addLigne(nameDossier, new String[] {tabmsg.elementAt(j)});
			          		 } catch (Exception e) {
			          			 System.err.println("ca marche pas");
			          		 } 
							msg = "";
							i = 255;
			    		  }
					}
					if (i!=0) {
						takeFile = false;
					}
					tabmsg.removeAllElements();
				} catch (Exception e) {
					System.err.println("listenForMessage");
					e.printStackTrace();
					System.exit(0);
				}
			}
			
		}
	});
	
	
	public void sendMessage() {
		try {
			bufferedWriter.write(userDir+ "�" +userName + "�" + id);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			String[] data;
			Scanner scanner = new Scanner(System.in);
			while (socket.isConnected()) {
				String messageToSend = scanner.nextLine();
				data = messageToSend.split(" ");
				if (data[0].equals("cp")) {
		          	  takeFile = true;
		          	  nameDossier = data[1];
		      	 }
				if (data[0].equals("add")) {
					FileManager manage = new FileManager();
					String[] content = manage.getLignes(data[1]);
					messageToSend += "\n";
					for (int i = 0; i < content.length; i++) {
						messageToSend += content[i] + "\n";
					}
				}
				if (data[0].equals("nano")) {
					nanoMode = true;
					nanoFile = data[1];
				}
				bufferedWriter.write(messageToSend);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			
		} catch (Exception e) {
			System.err.println("sendMessage");
			closeEverything(socket , bufferedReader, bufferedWriter);
		}
	}
	
	public void listenForMessage() {
		th.start();
	}
	
//	Partie 1 : to do liste
//	Partie 2 : demo
//	Partie 3 : presentation 
	
	private void closeEverything(Socket socket , BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {}

	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		FileManager manage = new FileManager();
		String[] Config = manage.getLignes("./clientConfig.txt");
		String[] lineName = Config[1].split(" ");
		String[] linePort = Config[2].split(" ");
		String[] lineIpServeur = Config[3].split(" ");
		String[] lineDossier = Config[4].split(" ");
		String[] lineId = Config[5].split(" ");
		String username = lineName[2];
		String userDir = lineDossier[2];
		String userID = lineId[2];
		Socket socket = new Socket(lineIpServeur[2],Integer.parseInt(linePort[2]));
		Client client = new Client(socket, username , userDir, userID);
		client.listenForMessage();
		client.sendMessage();
	}
	
	public void nano(String data) {
		
		nano.setTitle("Windows");
		nano.setSize(900, 400);
		nano.setDefaultCloseOperation(3);
		nano.setLocationRelativeTo(null);
		JPanel contentPane =  new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.setSize(new Dimension(nano.getWidth(),nano.getHeight()));
		area = new JTextArea(data);
		contentPane.add(area , BorderLayout.CENTER);
		JButton btn = new JButton("Save");
		btn.addActionListener(this);
		JPanel savePanel = new JPanel();
		savePanel.setLayout(new BorderLayout());
		savePanel.add(btn,BorderLayout.WEST);
		contentPane.add(savePanel,BorderLayout.NORTH);
		
		nano.add(contentPane);
		nano.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		nanoMessage += "datanano "+nanoFile +"\n" +  area.getText();
//		System.out.println(area.getText());
		nano.dispose();
	}
	
}
