package readWrite;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class readUsers {
	private String path;
	
	public readUsers(String file_path){
		path = file_path;
	}
	
	public String[] OpenFile() throws IOException{
		FileReader fr = new FileReader(path);
		BufferedReader textReader = new BufferedReader(fr);
		
		int numberOfLines = readLines();
		String[] textData = new String[numberOfLines];
		
		int i;
		
		for(i=0; i<numberOfLines; i++){
			textData[i]= textReader.readLine();
		}
		
		textReader.close();
		return textData;
	}
	
	int readLines() throws IOException{
		FileReader filetoread = new FileReader(path);
		BufferedReader bf = new BufferedReader(filetoread);
		
		String aLine;
		int numberoflines = 0;
		
		while((aLine = bf.readLine()) != null){
			numberoflines++;
		}
		bf.close();
		
		return numberoflines;
	}
}
