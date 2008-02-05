/*
 * Created on Aug 6, 2004
 *
 */
package net.sf.thingamablog.feed;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feed utilities
 */
public class FeedUtils
{
	private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=([.[^; ]]*)");

	/**
	 * Creates a reader with the appropriate char encoding for the URL
	 * 
	 * @param feedUrl the Feed url
	 * @return an appropriate reader
	 * @throws IOException If an error occurs
	 */
	public static Reader getFeedReader(URL feedUrl) throws IOException 
	{
		Reader reader;
		URLConnection conn = feedUrl.openConnection();

		if(feedUrl.getProtocol().equals("http") || feedUrl.getProtocol().equals("https")) 
		{
			// Finds out server charset encoding based on HTTP spec
			String contentTypeHeader = conn.getContentType();
			String encoding = "ISO-8859-1";
			if(contentTypeHeader!=null) 
			{
				Matcher matcher = CHARSET_PATTERN.matcher(contentTypeHeader);
				if (matcher.find()) 
				{
					encoding = matcher.group(1);
				}
			}
			reader = new InputStreamReader(conn.getInputStream(),encoding);
		}
		else 
		{
			// Goes with plartform's default charset encoding
			reader = new InputStreamReader(conn.getInputStream());
		}
        
		return reader;
	}
}
