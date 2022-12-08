import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.plaf.synth.SynthSeparatorUI;

import client_serveur.FileManager;


public class Serveur {
	
	private ServerSocket serverSocket;

	public Serveur(ServerSocket serverSocket) {
		super();
		this.serverSocket = serverSocket;
	}

	public void startServeur() {
		
		try {
			while (!serverSocket.isClosed()) {
				Socket socket = serverSocket.accept();
				System.out.println("A new Client has connected !");
				ClientListe client = new ClientListe(socket);
				
				Thread thread = new Thread(client);
				thread.start();
			}
			
		} catch (Exception e) {
			  e.printStackTrace();
		}
		
	}
	
	public void closeServerSocket() {
		
		try {
			if (serverSocket != null) { 
				serverSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public static void main(String[] args) throws IOException {
		FileManager manage = new FileManager();
		String[] Config = manage.getLignes("./serveurConfig.txt");
		String[] linePort = Config[2].split(" ");
		ServerSocket serverSocket = new ServerSocket(Integer.parseInt(linePort[2]));
		Serveur serveur = new Serveur(serverSocket);
		serveur.startServeur();
	}
}
