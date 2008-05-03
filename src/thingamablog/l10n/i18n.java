/*
 * i18n.java
 *
 * Created on 21 avril 2008, 17:17
 */

package thingamablog.l10n;

import java.lang.reflect.Method;
import java.util.Locale;
import thingamablog.l10n.L10n;

/**
 * Extends the L10n class, to keep the same name and interface as before
 * @author dieppe
 */
public class i18n extends L10n{
    
    i18n(String selected){
        super(selected);
    }
    
    public static void setLocale(String selected){
        int index = selected.indexOf('_');
        if (index != -1)
            selected = selected.substring(0,index);
        setLanguage(selected);
    }
    
    public static String str(String key){
        return getString(key);
    }
    
    public static char mnem(String MnemoKey){
        return getMnemonic(MnemoKey);
    }
    
    public static Locale getLocale(){
        String loc = getSelectedLanguage();
        return new Locale(loc);
    }
    
    public static Locale[] getAvailableLanguagePackLocales() {
        int Length = AVAILABLE_LANGUAGES.length;
        Locale result[] = new Locale[Length];
        for(int i = 0; i<Length; i++) {
            String loc = AVAILABLE_LANGUAGES[i];
            result[i]= new Locale(loc);
        }
        return result;
    }
}
