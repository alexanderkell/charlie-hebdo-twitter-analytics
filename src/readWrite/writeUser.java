package readWrite;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import org.json.*;

public class writeUser {
	
	private String path;
	private boolean append_to_file = true;
	
	public writeUser(String file_path, boolean append_value){
		path = file_path;	
		append_to_file = append_value;
	}
	
	public writeUser(String file_path){
		path = file_path;	
		//JArrayinput = data;
	}

	public void writeToFile(JSONArray textLine) throws	IOException{
		FileWriter write = new FileWriter(path, append_to_file);
		PrintWriter print_line = new PrintWriter(write); //Writes in txt not bytes
		print_line.printf("%s" + "%n", textLine);
		print_line.close();
		//print_line.printf
	}
	public void writeToFile(JSONObject textLine) throws	IOException{
		FileWriter write = new FileWriter(path, append_to_file);
		PrintWriter print_line = new PrintWriter(write); //Writes in txt not bytes
		print_line.printf("%s" + "%n", textLine);
		print_line.close();
	}
	public void writeToFile(String textLine) throws	IOException{
		FileWriter write = new FileWriter(path, append_to_file);
		PrintWriter print_line = new PrintWriter(write); //Writes in txt not bytes
		print_line.printf("%s" + "%n", textLine);
		print_line.close();
	}
}
