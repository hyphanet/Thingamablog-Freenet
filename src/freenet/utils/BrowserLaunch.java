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
        public static void main(String arg[]) {
		launch(arg[0]);
	}
	
	public static void launch(URL url) {
		launch(url.toString());
	}
	
	public static void launch(String url) {
                String osName = System.getProperty("os.name");
                try {
                        if (osName.startsWith("Mac OS")) {
                                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });

                                openURL.invoke(null, new Object[] { url });
                        }
                        else if (osName.startsWith("Windows")) Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                        else {
                                //assume Unix or Linux
                                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape", "seamonkey" };
                                String browser = null;

                                for (int count = 0; count < browsers.length && browser == null; count++)
                                        if (Runtime.getRuntime().exec( new String[] { "which", browsers[count]} ).waitFor() == 0)
                                                browser = browsers[count];
                                if (browser == null)
                                        throw new Exception("Could not find web browser");
                                else 
                                        Runtime.getRuntime().exec(new String[] { browser, url});
                        }
                }
                catch (Exception e) {
                        System.out.println("Unable to detect/startup your browser... please go to " + url + " for futher instructions");
                }
        }
}