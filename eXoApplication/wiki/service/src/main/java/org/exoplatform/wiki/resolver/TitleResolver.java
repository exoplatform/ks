/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.resolver;

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.AccessController;
import java.util.BitSet;
import java.util.StringTokenizer;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import sun.security.action.GetPropertyAction;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 7, 2010  
 */
public class TitleResolver {
  
  private static final Log      log               = ExoLogger.getLogger(TitleResolver.class);
  
  public static String getId(String title, boolean isEncoded) {
    if (title == null) {
      return null;
    }
    String id = title;
    if (isEncoded) {
      try {
        id = URLDecoder.decode(title, "UTF-8");
      } catch (UnsupportedEncodingException e1) {
        if (log.isWarnEnabled()) 
          log.warn(String.format("Getting Page Id from %s failed because of UnspportedEncodingException. Using page title(%s) instead (Not recommended. Fix it if possible!!!)", title), e1);
      }
    }
    return replaceSpacebyUnderscore(id);
  }

  private static String replaceSpacebyUnderscore(String s) {
    StringTokenizer st = new StringTokenizer(s, " ", false);
    StringBuilder sb = new StringBuilder();
    if (st.hasMoreElements()) {
      sb.append(st.nextElement());
    }
    while (st.hasMoreElements())
      sb.append("_").append(st.nextElement());
    return sb.toString();
  }
  public static String encodeSpecialCharacters(String s) throws UnsupportedEncodingException{
  	return encode(s, "UTF-8");
  }
  
  static BitSet dontNeedEncoding;
	static final int caseDiff = ('a' - 'A');
	static String dfltEncName = null;
	
	static{
		dontNeedEncoding = new BitSet(256);
		int i;
		for(i = 'a'; i <= 'z'; i++){
			dontNeedEncoding.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++){
			dontNeedEncoding.set(i);
		}
		 for (i = '0'; i <= '9'; i++){
			dontNeedEncoding.set(i);
		}
		 dontNeedEncoding.set('-');
		 dontNeedEncoding.set('_');
		 dontNeedEncoding.set('.');
		 
		 dfltEncName = (String) AccessController.doPrivileged(
				 new GetPropertyAction("file.encoding")
			);
	}
	
	public static String encode(String s){
		 String str = null;
		 
		 try{
			 str = encode(s, dfltEncName);
			 
		 }catch(UnsupportedEncodingException e){
			 //LOG
		 }
		 return str;
	}
	
	public static String encode (String s, String enc) throws UnsupportedEncodingException{
		boolean needToChange = false;
		StringBuffer out = new StringBuffer(s.length());
		Charset charset;
		CharArrayWriter charArrayWriter = new CharArrayWriter();
		
		if (enc == null){
			throw new NullPointerException("charsetName");
		}
		
		try{
			charset = Charset.forName(enc);
		}catch(IllegalCharsetNameException e){
			throw new UnsupportedEncodingException(enc);
		}catch (UnsupportedCharsetException e){
			throw new UnsupportedEncodingException(enc);
		}
		
		for(int i =0; i< s.length();){
			int c = (int) s.charAt(i);
			if (dontNeedEncoding.get(c)){
				out.append((char)c);
				i++;
			}else{
				do{
					charArrayWriter.write(c);
					
					if (c >= 0xD800 && c <= 0xDBFF){
						if ( (i+1) < s.length()){
							int d = (int) s.charAt(i+1);
							if (d >= 0xDC00 && d <= 0xDFFF){
								charArrayWriter.write(d);
								i++;
							}
						}
					}
					i++;
					
				} while (i < s.length() && !dontNeedEncoding.get((c = (int) s.charAt(i))));
				
				charArrayWriter.flush();
				String str = new String(charArrayWriter.toCharArray());
				byte[] ba = str.getBytes(charset);
				for (int j = 0; j < ba.length; j++){
					out.append('%');
					char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
					if (Character.isLetter(ch)){
						ch -= caseDiff;
					}
					out.append(ch);
					ch = Character.forDigit(ba[j] & 0xF, 16);
					if (Character.isLetter(ch)){
						 ch -= caseDiff;
					}
					out.append(ch);
				}
				charArrayWriter.reset();
				needToChange = true;
			}
		}
		return (needToChange? out.toString() : s);
	 }
}
