/* TweetTracker. Copyright (c) Arizona Board of Regents on behalf of Arizona State University
 * @author shamanth
 */
package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils
{
    //holds a list of stop words to be removed when generating word clouds etc.
    HashSet<String> STOPWORDS = new HashSet<String>();

    String SEPARATOR = " ";

    /**
     * Loads the stop words from a file onto a collection. for use by all methods in this class
     * @param filename
     */
    public void LoadStopWords(String filename)
      {
          if(!filename.isEmpty())
          {

                BufferedReader bread = null;
                try {
                    bread = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
                    String temp = "";
                    try {
                        while ((temp = bread.readLine()) != null) {
                            if (!temp.isEmpty()) {
                                String[] stwords = temp.split(",");
                                for (String t : stwords) {
                                    t = t.toLowerCase();
                                    if (!STOPWORDS.contains(t)) {
                                        STOPWORDS.add(t);
                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(TextUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(TextUtils.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TextUtils.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        bread.close();
                    } catch (IOException ex) {
                        Logger.getLogger(TextUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
          }
      }

   /**
     * Converts a tweet/text into individual words/tokens. All stopwords are removed and the list also does not contain hyperlinks.
     * Splitting is performed on space.
     * @param text
     * @param ignoreHashtags
     * @param ignoreUsernames
     * @return a list of words contained in text
     */
    public HashMap<String,Integer> TokenizeText(String text, boolean ignoreHashtags, boolean ignoreUsernames)
    {
        String[] tokens = text.split(SEPARATOR);
        HashMap<String,Integer> words = new HashMap<String,Integer>();
        for(String token:tokens)
        {
            token = token.replaceAll("\"|'|\\.||;|,", "");
            if(token.isEmpty()||token.length()<=2||STOPWORDS.contains(token)||token.startsWith("&")||token.startsWith("http"))
            {
               continue;
            }
            else
            {
                if(ignoreHashtags)
                {
                    if(token.startsWith("#"))
                    {
                        continue;
                    }
                }
                if(ignoreUsernames)
                {
                    if(token.startsWith("@"))
                    {
                        continue;
                    }
                }
                if(!words.containsKey(token))
                {
                    words.put(token,1);
                }
                else
                {
                    words.put(token, words.get(token)+1);
                }
            }
        }
        return words;
    }

    /**
     * Checks whether the tweet is a retweet based on the presence of the RT pattern as the start of the text. Expects the tweet text to be in lowercase.
     * @param text
     * @return
     */
    public static boolean IsTweetRT(String text)
    {
        Pattern p = Pattern.compile("^rt @[a-z_0-9]+");
        Matcher m = p.matcher(text);
        if(m.find())
        {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the text contains a hyperlink in the text
     * @param text
     * @return
     */
    public static boolean ContainsURL(String text)
    {
        Pattern urlpat = Pattern.compile("https?://[a-zA-Z0-9\\./]+");
        Matcher urlmat = urlpat.matcher(text);
        if(urlmat.find())
        {
            return true;
        }
        else
            return false;
    }

    /**
     * extracts and returns a list of hashtags from the text
     * @param text
     * @return
     */
    public static ArrayList<String> GetHashTags(String text)
    {
        Pattern p = Pattern.compile("#[a-zA-Z0-9]+");
        Matcher mat = p.matcher(text);
        ArrayList<String> tags = new ArrayList<String>();
        while(mat.find())
        {
            String tag = text.substring(mat.start(),mat.end());
            if(!tags.contains(tag.toLowerCase()))
            {
                tags.add(tag.toLowerCase());
            }
        }
        return tags;
    }

    /**
     * Removes LF and CR from the text as well as any quotes and backslashes
     * @param text
     * @return
     */
    public static String GetCleanText(String text)
    {
       text = text.replaceAll("'|\"|&quot;", "");
       text = text.replaceAll("\\\\", "");
       text = text.replaceAll("\r\n|\n|\r", " ");
       text = text.trim();
       return text;
    }

    /**
     * Removes all patterns that correspond to Retweeted status leaving only original text
     * @param tweet
     * @return
     */
    public static String RemoveRTElements(String tweet)
    {
        String text = tweet.replaceAll("rt @[a-z_A-Z0-9]+", " ");
        text = text.replaceAll("RT @[a-z_A-Z0-9]+", " ");
        text = text.replaceAll(":","");
        return text.trim();
    }

    /**
     * Removes all hashtags, URLs, and usernames from the tweet text
     * @param tweet
     * @return
     */
     public static String RemoveTwitterElements(String tweet)
     {
         String temptweet = tweet.replaceAll("#[a-zA-Z_0-9]+", "");
         temptweet = temptweet.replaceAll("https?://[a-zA-Z0-9\\./]+", "");
         temptweet = temptweet.replaceAll("@[a-zA-Z_0-9]+", "");
         temptweet = temptweet.replaceAll("[:?\\.;<>()]", "");
         return temptweet;
     }

}
