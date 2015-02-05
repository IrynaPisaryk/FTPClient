package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

public class FTPClientMav {

	private static FTPClientMav clientObj;
	private FTPClientMav(){         
	}
	public static FTPClientMav getInstance(){
		if(clientObj == null){
			clientObj = new FTPClientMav();
		}
		return clientObj;
	}
	public static void main(String[] args) throws MalformedURLException, IOException{
		FTPClientMav newClient = FTPClientMav.getInstance();
		newClient.StartFTPClient();
	}
	public void StartFTPClient() throws MalformedURLException, IOException{
		try{
			FTPClient client = FTPconnect();
			PrintListFile(client);
			FTPdisconnect(client);
		}catch(FTPConnectionClosedException e){
			System.out.println("Error. FTP connection is closed. Try to restart program");
		}
	}
	public static FTPClient FTPconnect() throws MalformedURLException, IOException{
		FTPClient client = new FTPClient();
		client.connect("ftp.mozilla.org");
		client.login("anonymous", "anonymous");
		client.enterLocalPassiveMode();
		client.setFileType(FTP.BINARY_FILE_TYPE);
		return client;
	}
	public static void FTPdisconnect(FTPClient client) throws IOException{
		try{
			client.logout();
			client.disconnect();
		}catch(FTPConnectionClosedException e){
			System.out.println("Error. FTP connection is closed. Try to restart program");
		}
	}
	public static void UserDialog(){
		System.out.println("What you want to do?");
		System.out.println("1. Download file(1))");
		System.out.println("2. Change current directory(2)");
		System.out.println("3. Change to parent directory(3)");
		System.out.println("4. Exit(4)");
	}
	public static void PrintListFile(FTPClient client) throws IOException{

		boolean stopProgram = false;
		do{
			FTPFile[] ftpFiles = client.listFiles();
			System.out.println(client.printWorkingDirectory());
			for(int i = 0; i < ftpFiles.length; i++){			
				System.out.println(ftpFiles[i].toString());			
			}
			int fileCount = 0;
			for(int i = 0; i < ftpFiles.length; i++){
				if(ftpFiles[i].isFile()==true)
					fileCount++;				
			}		
			String action = null;
			Scanner scan = new Scanner(System.in);
			if(fileCount == ftpFiles.length){

				System.out.println("What you want to do?");
				System.out.println("1. Download file(1))");
				System.out.println("2. Change to parent directory(2))");
				System.out.println("3. Exit(3)");				
				action = scan.nextLine();	
				int actionInt = Integer.parseInt(action);				
				switch(actionInt){
				case(1):
					FileDownload(client);
				break;
				case(2):
					ChangeToParentDirectory(client);
				break;
				case(3):
					stopProgram = true;
					System.out.println("Completed");				
				break;
				}
			}
			else{
				UserDialog();		
				action = scan.nextLine();
				try{

					int actionInt = Integer.parseInt(action);
					switch(actionInt){
					case(1):
						FileDownload(client);
					break;
					case(2):				
						ChangeCurrentDirectory(client, ftpFiles, scan);
					break;
					case(3):
						ChangeToParentDirectory(client);
					break;
					case(4):
						System.out.println("Completed");
					stopProgram=true;
					break;
					default:
						System.out.println("Incorrect input number");
						break;

					}	
				}catch(NumberFormatException e){
					System.out.println("Incorrect symbol");
				}catch(FTPConnectionClosedException e){
					System.out.println("Error. FTP connection is closed. Try to restart program");
				}catch(NoSuchElementException e){
					e.printStackTrace();
				}
			}

		}while(stopProgram == false);
	}
	public static void ChangeToParentDirectory(FTPClient client) throws IOException{
		try{
			if(client.printWorkingDirectory().equals("/")==true){

				System.out.println("This is root!");
			}else{
				client.changeToParentDirectory();
			}
		}catch(FTPConnectionClosedException e){
			System.out.println("Error. FTP connection is closed. Try to restart program");
		}
	}
	public static void ChangeCurrentDirectory(FTPClient client, FTPFile[] ftpFiles, Scanner scan) throws IOException{
		try{					
			String newDir = null;
			System.out.println("Enter directory name");
			String newName = scan.nextLine();
			for(int i = 0; i < ftpFiles.length; i++){
				if(ftpFiles[i].getName().equals(newName)==true && ftpFiles[i].isDirectory()==true)
					newDir = ftpFiles[i].getName();
			}										
			client.changeWorkingDirectory(newDir);

		}catch(FTPConnectionClosedException e){
			System.out.println("Error. FTP connection is closed. Try to restart program");
		}
	}
	public static void FileDownload(FTPClient client) throws IOException{
		try{	

			String filename = null;
			String path = null;
			System.out.println("Enter the file name");
			Scanner scan = new Scanner(System.in);
			filename = scan.nextLine();		
			FTPFile[] ftpFiles = client.listFiles();
			boolean fileExist = false;
			for(int i = 0; i < ftpFiles.length; i++){			
				if(filename.equalsIgnoreCase(ftpFiles[i].getName())== true)
					fileExist = true;
			}
			System.out.println("Enter the path to destination folder");
			path = scan.nextLine();		
			boolean canWrite = CanWrite(path);
			if(canWrite == true && fileExist == true){				
				FileOutputStream fos = null;
				try {				    
					fos = new FileOutputStream(path+"\\"+filename);
					client.retrieveFile(filename, fos);
					System.out.println("File "+filename+" has been downloaded successfully.");
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fos != null) {
							fos.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else System.out.println("Can't write file into current folder/file doesn't exist");
		}
		catch(NullPointerException e){
			e.printStackTrace();
		}
		catch(FileNotFoundException e){
			System.out.println("This path doesn't exist");
		}
		catch(FTPConnectionClosedException e){
			System.out.println("Error. FTP connection is closed. Try to restart program");
		}
	}
	public static boolean CanWrite(String path){
		int DiskCounter = 0;			
		for (File f:File.listRoots()) {
			if(path.toUpperCase().startsWith(f.getPath())== true && f.canWrite() == true){
				return true;			
			}
			else{
				DiskCounter++;
			}
		}
		if(DiskCounter !=0){
			System.out.println("This path doesn't exists or blocked");
			return false;
		}
		return false;
	}
}


