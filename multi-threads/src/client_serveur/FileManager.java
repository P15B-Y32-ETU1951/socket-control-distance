package client_serveur;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileManager {

	public FileManager() {}

	public void addLigne(String nomFichier, String[] lValeur) {
		try {
			File fichier = new File(nomFichier);
			if(!fichier.exists()) {
				fichier.createNewFile();
			}

			FileWriter writer = new FileWriter(nomFichier, true);
			for(int iRow = 0; iRow < lValeur.length; iRow++) {
				writer.write(lValeur[iRow]);
				writer.write("\n");
			}
			writer.close();
		}
		catch(Exception e) {}
	}

	int countLignes(String nomFichier) {
		int result = 0;
		try {
			File file = new File(nomFichier);
			if(!file.exists()) {
				file.createNewFile();
			}

			BufferedReader reader = new BufferedReader(new FileReader(file));
			String ligne = reader.readLine();
			while(ligne != null) {
				result ++;
				ligne = reader.readLine();
			}
			reader.close();
		}
		catch(Exception e) {}

		return result;
	}

	public String[] getLignes(String nomFichier) {
		String[] result = new String[countLignes(nomFichier)];
		
		try {
			File file = new File(nomFichier);
			if(!file.exists()) {
				file.createNewFile();
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String ligne = reader.readLine();
			int indice = 0;
			while(ligne != null) {
				result[indice] = ligne;
				indice ++;
				ligne = reader.readLine();
			}
			reader.close();
		}
		catch(Exception e) {}

		return result;
	}
	
	public String[][] takeFromDirectory(String chemin){
		File folder = new File(chemin);
		File[] liste = folder.listFiles();
		String[][] tab = new String[liste.length][2];
		int i = 0;
		for(File item : liste){
			if(item.isFile()){ 
				tab[i][0] = "fichier";
				tab[i][1] = item.getName();
			} 
	        else if(item.isDirectory()){
	        	tab[i][0] = "dossier";
				tab[i][1] = item.getName();
	        }
			i++;
		}
		return tab;
	}
	
	public void copyAndPaste(String fichier , String destination) {
		File source = new File(fichier);
		File dest = new File(destination + source.getName());
		try {
			Files.copy(source.toPath(), dest.toPath() ,StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}