import openauthentication.*;
import support.*;
import utils.*;

import java.io.*;

import oauth.*;
import oauth.signpost.*;
import readWrite.*;
import org.json.*;

public class main {
	public static void main(String args[]) throws IOException{
		
		//addUser();
		try{
			readUsers readU = new readUsers("data/userAccess.txt");
			String users[] = readU.OpenFile();
			//System.out.println(users.toString());
			int i;
			for ( i=0; i < users.length; i++ ) {
				System.out.println(users[i]) ;
			}

			String access[]= {"223959690-6pKR8RCtufdQhcxEs0m3kEk3Cpkb7kh94LTcuG6K","3FNmDw9MpRMGl62WRjthGPll3ldmJGuEjSduIer06uWPi"};
			
			RESTApiExample obj1 = new RESTApiExample();
			OAuthConsumer one = obj1.GetConsumer(access[0],access[1]);
			RESTApiExample obj = new RESTApiExample(one);
			
			JSONArray profi = obj.GetFollowers("luckysori");
			System.out.println(profi.toString());
			
			writeUser printed= new writeUser("data/Followers.txt", false);
			printed.writeToFile(profi);
			
		}catch (IOException e){
			System.out.println(e.getMessage());
		}

	}
	
	public static void addUser(){
		OAuthExample aue = new OAuthExample();
        OAuthTokenSecret tokensecret = aue.GetUserAccessKeySecret();
		String access[] = tokensecret.toArray();
		String out = tokensecret.toString();
		System.out.println(out);
		
		String dir = "data/userAccess.txt";
		try{
			writeUser save = new writeUser(dir, true);
			save.writeToFile(out);
			System.out.println("Printed");
		}catch(IOException e){
			System.out.println("Didn't print");
		}
	}
}
