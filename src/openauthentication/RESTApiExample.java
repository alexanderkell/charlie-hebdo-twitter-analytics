package openauthentication;

import support.APIType;
import support.OAuthTokenSecret;
import openauthentication.OAuthExample;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RESTApiExample
{
	private OAuthConsumer Consumer;
    public RESTApiExample(OAuthConsumer one){
    	Consumer = one;
    	
    }
    public RESTApiExample(){
    		
    }
	//file handlers to store the collected user information
    BufferedWriter OutFileWriter;
    OAuthTokenSecret OAuthTokens;
    
    ArrayList<String> Usernames = new ArrayList<String>();

    /**
     * Creates a OAuthConsumer with the current consumer & user access tokens and secrets
     * @return consumer
     */
    public OAuthConsumer GetConsumer(String one, String two)
    {
        OAuthConsumer consumer = new DefaultOAuthConsumer(utils.OAuthUtils.CONSUMER_KEY,utils.OAuthUtils.CONSUMER_SECRET);
        //consumer.setTokenWithSecret(OAuthTokens.getAccessToken(),OAuthTokens.getAccessSecret());
        consumer.setTokenWithSecret(one,two);
        return consumer;
    }
    
    /**
     * Reads the file and loads the users in the file to be crawled
     * @param filename
     */
    public void ReadUsers(String filename)
    {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            String temp = "";
            while((temp = br.readLine())!=null)
            {
                if(!temp.isEmpty())
                {
                    Usernames.add(temp);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        finally{
            try {
                br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Load the User Access Token, and the User Access Secret
     */
    public void LoadTwitterToken()
    {
        //Un-comment before release
//        OAuthExample oae = new OAuthExample();
//        OAuthTokens =  oae.GetUserAccessKeySecret();
        //Remove before release
        OAuthTokens = OAuthExample.DEBUGUserAccessSecret();
    }

    /**
     * Retrieves the rate limit status of the application
     * @return
     */
   public JSONObject GetRateLimitStatus()
   {
     try{
            URL url = new URL("https://api.twitter.com/1.1/application/rate_limit_status.json");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setReadTimeout(5000);           
            Consumer.sign(huc);
            huc.connect();
            BufferedReader bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
            StringBuffer page = new StringBuffer();
            String temp= "";
            while((temp = bRead.readLine())!=null)
            {
                page.append(temp);
            }
            bRead.close();
            return (new JSONObject(page.toString()));
        } catch (JSONException ex) {
            Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthCommunicationException ex) {
            Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (OAuthMessageSignerException ex) {
            Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthExpectationFailedException ex) {
            Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IOException ex)
        {
            Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
        }
     return null;
   }

   /**
    * Initialize the file writer
    * @param path of the file
    * @param outFilename name of the file
    */
   public void InitializeWriters(String outFilename) {
        try {
            File fl = new File(outFilename);
            if(!fl.exists())
            {
                fl.createNewFile();
            }
            /**
             * Use UTF-8 encoding when saving files to avoid
             * losing Unicode characters in the data
             */
            OutFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilename,true),"UTF-8"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

   /**
    * Close the opened filewriter to save the data
    */
   public void CleanupAfterFinish()
   {
        try {
            OutFileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
        }
   }

   /**
    * Writes the retrieved data to the output file
    * @param data containing the retrived information in JSON
    * @param user name of the user currently being written
    */
    public void WriteToFile(String user, String data)
    {
        try
        {
            OutFileWriter.write(data);
            OutFileWriter.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrives the profile information of the user
     * @param username of the user whose profile needs to be retrieved
     * @return the profile information as a JSONObject
     */
    public JSONObject GetProfile(String username)
    {
        BufferedReader bRead = null;
        JSONObject profile = null;
        try {
            System.out.println("Processing profile of "+username);
            boolean flag = true;
            URL url = new URL("https://api.twitter.com/1.1/users/show.json?screen_name="+username);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setReadTimeout(5000);
            // Step 2: Sign the request using the OAuth Secret
            Consumer.sign(huc);
            huc.connect();
            if(huc.getResponseCode()==404||huc.getResponseCode()==401)
            {
               System.out.println(huc.getResponseMessage());
            }           
            else
            if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
            {
                try {
                    huc.disconnect();
                    System.out.println(huc.getResponseMessage());
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            else
                // Step 3: If the requests have been exhausted, then wait until the quota is renewed
            if(huc.getResponseCode()==429)
            {
                try {
                    huc.disconnect();
                    Thread.sleep(this.GetWaitTime("/users/show/:id"));
                    flag = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            if(!flag)
            {
                //recreate the connection because something went wrong the first time.
                huc.connect();
            }
            StringBuilder content=new StringBuilder();
            if(flag)
            {
                bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
                String temp= "";
                while((temp = bRead.readLine())!=null)
                {
                    content.append(temp);
                }
            }
            huc.disconnect();
            try {
                profile = new JSONObject(content.toString());
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        } catch (OAuthCommunicationException ex) {
            ex.printStackTrace();
        } catch (OAuthMessageSignerException ex) {
            ex.printStackTrace();
        } catch (OAuthExpectationFailedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Worked!");
        return profile;
    }

    /**
     * Retrieves the followers of a user
     * @param username the name of the user whose followers need to be retrieved
     * @return a list of user objects corresponding to the followers of the user
     */
    public JSONArray GetFollowers(String username)
    {
        BufferedReader bRead = null;
        JSONArray followers = new JSONArray();
        try {
            System.out.println(" followers user = "+username);
            long cursor = -1;
            while(true)
            {
                if(cursor==0)
                {
                    break;
                }
                // Step 1: Create the APi request using the supplied username
                URL url = new URL("https://api.twitter.com/1.1/followers/list.json?screen_name="+username+"&cursor=" + cursor);
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setReadTimeout(5000);
                // Step 2: Sign the request using the OAuth Secret
                Consumer.sign(huc);
                huc.connect();
                if(huc.getResponseCode()==400||huc.getResponseCode()==404)
                {
                    System.out.println(huc.getResponseMessage());
                    break;
                }
                else
                if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503||huc.getResponseCode()==504)
                {
                    try{
                        System.out.println(huc.getResponseMessage());
                        huc.disconnect();
                        Thread.sleep(3000);
                        continue;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else
                    // Step 3: If the requests have been exhausted, then wait until the quota is renewed
                if(huc.getResponseCode()==429)
                {
                    try {
                        huc.disconnect();
                        Thread.sleep(this.GetWaitTime("/followers/list"));
                        continue;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // Step 4: Retrieve the followers list from Twitter
                bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
                StringBuilder content = new StringBuilder();
                String temp = "";
                while((temp = bRead.readLine())!=null)
                {
                    content.append(temp);
                }
                try {
                    JSONObject jobj = new JSONObject(content.toString());
                    // Step 5: Retrieve the token for the next request
                    cursor = jobj.getLong("next_cursor");
                    JSONArray idlist = jobj.getJSONArray("users");
                    if(idlist.length()==0)
                    {
                        break;
                    }
                    for(int i=0;i<idlist.length();i++)
                    {
                        followers.put(idlist.getJSONObject(i));
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (OAuthCommunicationException ex) {
            ex.printStackTrace();
        } catch (OAuthMessageSignerException ex) {
            ex.printStackTrace();
        } catch (OAuthExpectationFailedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {              
            ex.printStackTrace();
        }
        return followers;
    }

    /**
     * Retrieved the status messages of a user
     * @param username the name of the user whose status messages need to be retrieved
     * @return a list of status messages
     */
    public JSONArray GetStatuses(String username)
        {
            BufferedReader bRead = null;
            //Get the maximum number of tweets possible in a single page 200
            int tweetcount = 200;
            //Include include_rts because it is counted towards the limit anyway.
            boolean include_rts = true;
            JSONArray statuses = new JSONArray();
            try {
                System.out.println("Processing status messages of "+username);
                long maxid = 0;
                while(true)
                {
                    URL url = null;                    
                    if(maxid==0)
                    {
                        url = new URL("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + username+"&include_rts="+include_rts+"&count="+tweetcount);
                    }
                    else
                    {
                        //use max_id to get the tweets in the next page. Use max_id-1 to avoid getting redundant tweets.
                        url = new URL("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + username+"&include_rts="+include_rts+"&count="+tweetcount+"&max_id="+(maxid-1));
                    }
                    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                    huc.setReadTimeout(5000);
                    Consumer.sign(huc);
                    huc.connect();
                    if(huc.getResponseCode()==400||huc.getResponseCode()==404)
                    {
                        System.out.println(huc.getResponseCode());
                        break;
                    }
                    else
                    if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
                    {
                        try {System.out.println(huc.getResponseCode());
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(RESTApiExample.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else
                        // Step 3: If the requests have been exhausted, then wait until the quota is renewed
                    if(huc.getResponseCode()==429)
                    {
                        try {
                            huc.disconnect();
                            Thread.sleep(this.GetWaitTime("/statuses/user_timeline"));
                            continue;
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String temp = "";
                    while((temp = bRead.readLine())!=null)
                    {
                        content.append(temp);
                    }
                    try {
                        JSONArray statusarr = new JSONArray(content.toString());
                        if(statusarr.length()==0)
                        {
                            break;
                        }
                        for(int i=0;i<statusarr.length();i++)
                        {
                            JSONObject jobj = statusarr.getJSONObject(i);
                            statuses.put(jobj);
                            //Get the max_id to get the next batch of tweets
                            if(!jobj.isNull("id"))
                            {
                                maxid = jobj.getLong("id");
                            }
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
                System.out.println(statuses.length());
        } catch (OAuthCommunicationException ex) {
            ex.printStackTrace();
        } catch (OAuthMessageSignerException ex) {
            ex.printStackTrace();
        } catch (OAuthExpectationFailedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return statuses;
    }

    /**
     * Retrieves the friends of a user
     * @param username the name of the user whose friends need to be fetched
     * @return a list of user objects who are friends of the user
     */
    public JSONArray GetFriends(String username)
    {
        BufferedReader bRead = null;
        JSONArray friends = new JSONArray();
        try {
            System.out.println("Processing friends of "+username);
            long cursor = -1;
            while(true)
            {
                if(cursor==0)
                {
                    break;
                }
                // Step 1: Create the APi request using the supplied username
                URL url = new URL("https://api.twitter.com/1.1/friends/list.json?screen_name="+username+"&cursor="+cursor);
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setReadTimeout(5000);
                //Step 2: Sign the request using the OAuth Secret
                Consumer.sign(huc);
                huc.connect();
                if(huc.getResponseCode()==400||huc.getResponseCode()==401)
                {
                    System.out.println(huc.getResponseMessage());
                    break;
                }
                else
                if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
                {
                    try {
                        System.out.println(huc.getResponseMessage());
                        Thread.sleep(3000);
                        continue;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                else
                    // Step 3: If the requests have been exhausted, then wait until the quota is renewed
                if(huc.getResponseCode()==429)
                {
                    try {
                        huc.disconnect();
                        Thread.sleep(this.GetWaitTime("/friends/list"));
                        continue;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                // Step 4: Retrieve the friends list from Twitter
                bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
                StringBuilder content = new StringBuilder();
                String temp = "";
                while((temp = bRead.readLine())!=null)
                {
                    content.append(temp);
                }
                try {
                    JSONObject jobj = new JSONObject(content.toString());
                    // Step 5: Retrieve the token for the next request
                    cursor = jobj.getLong("next_cursor");
                    JSONArray userlist = jobj.getJSONArray("users");
                    if(userlist.length()==0)
                    {
                        break;
                    }
                    for(int i=0;i<userlist.length();i++)
                    {
                        friends.put(userlist.get(i));
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }               
                huc.disconnect();
            }
         } catch (OAuthCommunicationException ex) {
            ex.printStackTrace();
         } catch (OAuthMessageSignerException ex) {
            ex.printStackTrace();
         } catch (OAuthExpectationFailedException ex) {
            ex.printStackTrace();
         } catch (IOException ex) {
            ex.printStackTrace();
         }
        return friends;
    }

    /**
     * Retrieves the wait time if the API Rate Limit has been hit
     * @param api the name of the API currently being used
     * @return the number of milliseconds to wait before initiating a new request
     */
    public long GetWaitTime(String api)
    {
        JSONObject jobj = this.GetRateLimitStatus();
        if(jobj!=null)
        {
            try {                
                if(!jobj.isNull("resources"))
                {
                    JSONObject resourcesobj = jobj.getJSONObject("resources");
                    JSONObject apilimit = null;
                    if(api.equals(APIType.USER_TIMELINE))
                    {
                        JSONObject statusobj = resourcesobj.getJSONObject("statuses");
                        apilimit = statusobj.getJSONObject(api);
                    }
                    else
                    if(api.equals(APIType.FOLLOWERS))
                    {
                        JSONObject followersobj = resourcesobj.getJSONObject("followers");
                        apilimit = followersobj.getJSONObject(api);
                    }
                    else
                    if(api.equals(APIType.FRIENDS))
                    {
                        JSONObject friendsobj = resourcesobj.getJSONObject("friends");
                        apilimit = friendsobj.getJSONObject(api);
                    }
                    else
                    if(api.equals(APIType.USER_PROFILE))
                    {
                        JSONObject userobj = resourcesobj.getJSONObject("users");
                        apilimit = userobj.getJSONObject(api);
                    }
                    int numremhits = apilimit.getInt("remaining");
                    if(numremhits<=1)
                    {
                        long resettime = apilimit.getInt("reset");
                        resettime = resettime*1000; //convert to milliseconds
                        return resettime;
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }
    public static void main(String[] args)
    {
        //RESTApiExample rae = new RESTApiExample();
        //rae.LoadTwitterToken();
        //rae.Consumer = rae.GetConsumer();
//        System.out.println(rae.GetStatuses("twtanalyticsbk"));
        //System.out.println(rae.GetRateLimitStatus());
//        int apicode = InfoType.PROFILE_INFO;
//        String infilename = rae.DEF_FILENAME;
//        String outfilename = rae.DEF_OUTFILENAME;
//        if(args!=null)
//        {
//            if(args.length>2)
//            {
//                apicode = Integer.parseInt(args[2]);
//                outfilename = args[1];
//                infilename = args[0];
//            }
//            if(args.length>1)
//            {
//                outfilename = args[1];
//                infilename = args[0];
//            }
//            else
//            if(args.length>0)
//            {
//                infilename = args[0];
//            }
//        }
//        rae.InitializeWriters(outfilename);
//        rae.ReadUsers(infilename);
//        if(apicode!=InfoType.PROFILE_INFO&&apicode!=InfoType.FOLLOWER_INFO&&apicode!=InfoType.FRIEND_INFO&&apicode!=InfoType.STATUSES_INFO)
//        {
//          System.out.println("Invalid API type: Use 0 for Profile, 1 for Followers, 2 for Friends, and 3 for Statuses");
//          System.exit(0);
//        }
//        if(rae.Usernames.size()>0)
//        {
//            //TO-DO: Print the possible API types and get user selection to crawl the users.
//            rae.LoadTwitterToken();
//            for(String user:rae.Usernames)
//            {
//                if(apicode==InfoType.PROFILE_INFO)
//                {
//                    JSONObject jobj = rae.GetProfile(user);
//                    if(jobj!=null&&jobj.length()==0)
//                    {
//                        rae.WriteToFile(user, jobj.toString());
//                    }
//                }
//                else
//                if(apicode==InfoType.FRIEND_INFO)
//                {
//                    JSONArray statusarr = rae.GetFriends(user);
//                    if(statusarr.length()>0)
//                    {
//                        rae.WriteToFile(user, statusarr.toString());
//                    }
//                }
//                else
//                if(apicode == InfoType.FOLLOWER_INFO)
//                {
//                    JSONArray statusarr = rae.GetFollowers(user);
//                    if(statusarr.length()>0)
//                    {
//                        rae.WriteToFile(user, statusarr.toString());
//                    }
//                }
//                else
//                if(apicode == InfoType.STATUSES_INFO)
//                {
//                    JSONArray statusarr = rae.GetStatuses(user);
//                    if(statusarr.length()>0)
//                    {
//                        rae.GetStatuses(user);
//                    }
//                }
//            }
//        }
////        now you can close the files as all the threads have finished
//        rae.CleanupAfterFinish();
   }
}
