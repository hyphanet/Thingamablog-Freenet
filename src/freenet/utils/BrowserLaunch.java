/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */

package freenet.utils;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * A simple class to load an URL in a browser
 * 
 * @author Florent Daigni&egrave;re &lt;nextgens@freenetproject.org&gt;
 */
public class BrowserLaunch {
	private final static boolean isMacos;
	private final static boolean isWindows;
	private static String BROWSER = null;
	
	static {
		String osName = System.getProperty("os.name");
		isMacos = osName.startsWith("Mac OS");
		isWindows = osName.startsWith("Windows");
		if(!isMacos && !isWindows) {
			//assume Unix or Linux
			String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape", "seamonkey"};

			try {
				for(int count = 0; count < browsers.length && BROWSER == null; count++)
					if(Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0)
						BROWSER = browsers[count];
			} catch (Exception e) {}
		}
	}
	
        public static void main(String arg[]) {
		launch(arg[0]);
	}
	
	public static void launch(URL url) {
		launch(url.toString());
	}
	
	public static void launch(String url) {
                try {
                        if (isMacos) {
                                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });

                                openURL.invoke(null, new Object[] { url });
                        }
                        else if (isWindows)
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                        else {
                                if (BROWSER == null)
                                        throw new Exception("Could not find web browser");
                                else 
                                        Runtime.getRuntime().exec(new String[] { BROWSER, url });
                        }
                }
                catch (Exception e) {
                        System.out.println("Unable to detect/startup your browser... please go to " + url + " for futher instructions");
                }
        }
}