package client;

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
		newClient.startFTPClient();
	}
	public void startFTPClient() throws MalformedURLException, IOException{
		boolean ftpError = false;
		try{
			FTPClient client = ftpConnect();
			printListFile(client, ftpError);
			ftpDisconnect(client, ftpError);
		}catch(FTPConnectionClosedException e){
			ftpError = true;
		}
		if(ftpError){
			System.out.println("Error. FTP connection is closed. Try to restart program");
		}

	}
	public static FTPClient ftpConnect() throws MalformedURLException, IOException{
		FTPClient client = new FTPClient();
		client.connect("ftp.mozilla.org");
		client.login("anonymous", "anonymous");
		client.enterLocalPassiveMode();
		client.setFileType(FTP.BINARY_FILE_TYPE);
		return client;
	}
	public static void ftpDisconnect(FTPClient client, boolean ftpError) throws IOException{
		try{
			client.logout();
			client.disconnect();
		}catch(FTPConnectionClosedException e){
			ftpError = true;
		}
	}
	public static void userDialog(){
		System.out.println("What you want to do?");
		System.out.println("1. Download file(1)");
		System.out.println("2. Change current directory(2)");
		System.out.println("3. Change to parent directory(3)");
		System.out.println("4. Exit(4)");
	}
	public static void printListFile(FTPClient client, boolean ftpError) throws IOException{

		boolean stopProgram = false;
		do{
			FTPFile[] ftpFiles = client.listFiles();
			System.out.println(client.printWorkingDirectory());
			for(int i = 0; i < ftpFiles.length; i++){			
				System.out.println(ftpFiles[i].toString());			
			}
			int fileCount = 0;
			for(int i = 0; i < ftpFiles.length; i++){
				if(ftpFiles[i].isFile())
					fileCount++;				
			}		
			String action = null;
			Scanner scan = new Scanner(System.in);
			if(fileCount == ftpFiles.length){

				System.out.println("What you want to do?");
				System.out.println("1. Download file(1)");
				System.out.println("2. Change to parent directory(2)");
				System.out.println("3. Exit(3)");				
				action = scan.nextLine();	
				int actionInt = Integer.parseInt(action);				
				switch(actionInt){
				case(1):
					fileDownload(client, ftpError);
				break;
				case(2):
					changeToParentDirectory(client, ftpError);
				break;
				case(3):
					stopProgram = true;
				System.out.println("Completed");				
				break;
				}
			}
			else{
				userDialog();		
				action = scan.nextLine();
				try{

					int actionInt = Integer.parseInt(action);
					switch(actionInt){
					case(1):
						fileDownload(client, ftpError);
					break;
					case(2):				
						changeCurrentDirectory(client, ftpFiles, scan, ftpError);
					break;
					case(3):
						changeToParentDirectory(client, ftpError);
					break;
					case(4):
						System.out.println("Completed");
					stopProgram=true;
					break;
					default:
						System.out.println("Incorrect input number");
						break;

					}	
				} catch (NumberFormatException e) {
					System.out.println("Incorrect symbol");
				} catch (FTPConnectionClosedException e) {
					ftpError = true;
				} catch (NoSuchElementException e) {
					e.printStackTrace();
				}
			}

		}while(stopProgram == false);
	}
	public static void changeToParentDirectory(FTPClient client, boolean ftpError) throws IOException{
		try {
			if (client.printWorkingDirectory().equals("/")) {
				System.out.println("This is root!");
			} else {
				client.changeToParentDirectory();
			}
		} catch (FTPConnectionClosedException e) {
			ftpError = true;
		}
	}
	public static void changeCurrentDirectory(FTPClient client, FTPFile[] ftpFiles, Scanner scan, boolean ftpError) throws IOException{
		try{					
			String newDir = null;
			System.out.println("Enter directory name");
			String newName = scan.nextLine();
			for(int i = 0; i < ftpFiles.length; i++){
				if(ftpFiles[i].getName().equals(newName)==true && ftpFiles[i].isDirectory()==true)
					newDir = ftpFiles[i].getName();
			}	
			if(newDir == null){
				System.out.println("Incorrect directory name");
			}
			client.changeWorkingDirectory(newDir);

		}catch(FTPConnectionClosedException e){
			ftpError = true;
		}
	}
	public static void fileDownload(FTPClient client, boolean ftpError) throws IOException{
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
			if(fileExist){				
				FileOutputStream fos = null;
				try {
					try{
						fos = new FileOutputStream(path+"\\"+filename);
						client.retrieveFile(filename, fos);
						System.out.println("File "+filename+" has been downloaded successfully.");
					}catch(FileNotFoundException e){
						System.out.println("Bad path. Try again");
					}					
				} catch (IOException e) {
					e.printStackTrace();
				}finally {
					try {
						if (fos != null) {
							fos.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else System.out.println("Can't write file into current directory/file doesn't exist");
		}
		catch(NullPointerException e){
			e.printStackTrace();
		}
		catch(FileNotFoundException e){
			System.out.println("This path doesn't exist");
		}
		catch(FTPConnectionClosedException e){
			ftpError = true;
		}
	}
}


