import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

import client_serveur.FileManager;

public class ClientListe implements Runnable {

	public static ArrayList<ClientListe> clientHandlers = new ArrayList<ClientListe>();
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private String clientUserNAme;
	private String clientUserDir;
	private String clientUserId;
	private int permi;
	private File myFile ;
	
	public ClientListe(Socket socket) {
		try {
			// initialisation
			this.socket = socket;
			this.out = new PrintWriter(socket.getOutputStream());
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String first = in.readLine();
			System.out.println(first);
			String[] data = first.split("�");
			this.clientUserNAme = data[1];
			this.clientUserDir = data[0];
			this.clientUserId = data[2];
			
			// check la permission du client
			FileManager dir = new FileManager();
			String[] tab = dir.getLignes("src/serveurAccess.txt");
			String[][] liste = new String[tab.length][3];
			Boolean bool = false;
			for (int i = 0; i < tab.length; i++) {
				liste[i] = tab[i].split(" ");
			}
			for (int i = 0; i < liste.length; i++) {
				if (liste[i][0].equals(data[1]+"/"+data[2])) {
					this.permi = Integer.parseInt(liste[i][2]);
					bool = true;
					break;
				}
			}
			if (!bool) {
				this.permi = 4;
				dir.addLigne("src/serveurAccess.txt" , new String[] {"\n"+data[1]+"/"+data[2]+" = 4"} );
			}
			
			clientHandlers.add(this);
			System.out.println("SERVER : "+clientUserNAme+" connected !!");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("clientHandler");
			closeEverything(socket , in, out);
		}
	}
	
	@Override
	public void run() {
		String messageFromClient;
		Vector<String> allLine = new Vector<String>();
		while (socket.isConnected()) {
			try {
				while (in.ready()) {
					messageFromClient = in.readLine();
					allLine.addElement(messageFromClient);
//					System.out.println(messageFromClient);
				}
				broadcastMessage(allLine);
			} catch (Exception e) {
				System.err.println("run");
				closeEverything(socket , in, out);
				break;
			}
		}
		
		
	}
	
	public void broadcastMessage(Vector<String> allLine) {
		String[] demande;
		try {
			demande = allLine.elementAt(0).split(" ");
		} catch (Exception e) {
			demande = new String[] {""};
		}
		
		if (demande[0]!="") {
			for(ClientListe clientHandler : clientHandlers) {
				try {
					if (clientHandler.clientUserNAme.equals(clientUserNAme)) {
						if (demande[0].equals("ls")) {
							ls(clientHandler,demande);
						}
						else if (demande[0].equals("cp")) {
							cp(clientHandler,demande);
						}
						else if (demande[0].equals("add")) {
							add(clientHandler,demande,allLine);
						}
						else if (demande[0].equals("close")) {
							clientHandler.out.println("close");
							clientHandler.out.flush();
						}
						else if (demande[0].equals("touch")) {
							touch(clientHandler,demande);
							clientHandler.out.flush();
						}
						else if (demande[0].equals("cd")) {
							cd(clientHandler,demande);
							clientHandler.out.flush();
						}
						else if (demande[0].equals("mkdir")) {
							mkdir(clientHandler,demande);
							clientHandler.out.flush();
						}
						else if (demande[0].equals("nano")) {
							cp(clientHandler,demande);
							clientHandler.out.flush();
						}
						else if (demande[0].equals("datanano")) {
							add(clientHandler,demande,allLine);
							clientHandler.out.flush();
						}
						else if (demande[0].equals("id")) {
							clientHandler.out.println(clientHandler.clientUserId);
							clientHandler.out.flush();
						}
						else if (demande[0].equals("permi")) {
							clientHandler.out.println(clientHandler.permi);
							clientHandler.out.flush();
						}
						else if (demande[0].equals("pwd")) {
							clientHandler.out.println(clientHandler.clientUserDir);
							clientHandler.out.flush();
						}
						else {
							clientHandler.out.println("Command not found");
							clientHandler.out.flush();
						}
						
					}
				} catch (Exception e) {
					System.err.println("broadcastMessage");
					closeEverything(socket , in, out);
				}
			}
			allLine.removeAllElements();
		}
		

	}

	private void cd(ClientListe clientHandler, String[] demande) {
		try {
			String[] tabDir = clientHandler.clientUserDir.split("/");
			if (demande[1].equals("..")) {
				String newDir = "";
				for (int i = 0; i < tabDir.length-1; i++) {
					newDir += tabDir[i];
				}
				clientHandler.clientUserDir = newDir;
			}
			else {
				File dir = new File(clientHandler.clientUserDir+"/"+demande[1]);
				if (dir.exists() && dir.isDirectory()) {
					clientHandler.clientUserDir += "/"+demande[1];
				}
			}
			  
		} catch (Exception e) { e.printStackTrace(); }
		
	}
	
	private void touch(ClientListe clientHandler, String[] demande) {
		try {
			File file = new File(clientHandler.clientUserDir+"/"+demande[1]);
			if (!file.exists()) {
				file.createNewFile();
			} clientHandler.myFile = new File(clientHandler.clientUserDir+"/"+demande[1]);
			  
		} catch (Exception e) { e.printStackTrace(); }
		
	}
	
	private void mkdir(ClientListe clientHandler, String[] demande) {
		try {
			File file = new File(clientHandler.clientUserDir+"/"+demande[1]);
			if (!file.exists()) {
				file.mkdir();
			} clientHandler.myFile = new File(clientHandler.clientUserDir+"/"+demande[1]);
			  
		} catch (Exception e) { e.printStackTrace(); }
		
	}

	public void add(ClientListe clientHandler, String[] demande , Vector<String> allLine) {
		for (int i = 0; i < demande.length; i++) {
			System.out.println(demande[i]);
		}
		String[] valeur = new String[allLine.size() - 1];
		for (int i = 0; i < allLine.size()-1; i++) {
			valeur[i] = allLine.elementAt(i+1);
			System.out.println(allLine.elementAt(i));
		}
		try {
			FileManager manage = new FileManager();
			File file = new File(clientHandler.clientUserDir+"/"+demande[1]);
			if (!file.exists()) {
				file.createNewFile();
			}
			else {
				file.delete();
				file.createNewFile();
			}
			manage.addLigne(clientHandler.clientUserDir+"/"+demande[1], valeur);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	public void cp(ClientListe clientHandler , String[] demande) {
    	 try {
    		  System.err.println("Demande de fichier");
 			  clientHandler.myFile = new File(clientHandler.clientUserDir+"/"+demande[1]);
 			  byte[] mybytearray = new byte[(int) clientHandler.myFile.length()];
 			  if (mybytearray.length != 0) {
 				 try (BufferedInputStream buffered = new BufferedInputStream(new FileInputStream(clientHandler.myFile))) {
 					buffered.read(mybytearray, 0, mybytearray.length);
 	 		      }
 	 		      OutputStream outp = clientHandler.socket.getOutputStream();
 	 		      outp.write(mybytearray, 0, mybytearray.length);
 	 		      outp.flush();
 			  }
 			  else {
 				  out.println("");
 				  out.flush();
 			  }
		} catch (Exception e) { e.printStackTrace(); }
     }
	
	public void ls(ClientListe clientHandler , String[] demande) {
		if (clientHandler.permi > 1) {
			FileManager dir = new FileManager();
			String[][] tab = dir.takeFromDirectory(clientHandler.clientUserDir);
			String liste = "";
			liste = liste + "ls\n";
			for (int i = 0; i < tab.length; i++) {
				liste = liste + tab[i][0] + " : " + tab[i][1] + "\n";
			}
			clientHandler.out.println(liste);
			clientHandler.out.flush();
		}
		else {
			clientHandler.out.println("permission non valide");
			clientHandler.out.flush();
		}
     }
	
	
	public void removeClientHandler() {
		clientHandlers.remove(this);
		System.err.println("SERVER : " + clientUserNAme + " exit !!!");
	}
	
	public void closeEverything(Socket socket , BufferedReader bufferedReader ,  PrintWriter printWriter) {
		removeClientHandler();
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (printWriter != null) {
				printWriter.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
