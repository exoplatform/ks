/***************************************************************************
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.ks.common;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jan 6, 2010 - 7:17:11 AM  
 */
public class TestTransformHTML extends TestCase{

  private static List<String> bbcs = Arrays.asList(new String[] { "B", "I", "IMG", "CSS", "URL", "LINK", "GOTO", "QUOTE", "LEFT", "CODE"});
  public TestTransformHTML() throws Exception {
    super();
  }
  
  public void testCleanHtmlCode() {
    // for text is empty
    String str = TransformHTML.EMPTY_STR;
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.cleanHtmlCode(str, bbcs));
    
    // for text contain bbcode  
    str = "text [b]content[/b] has [I]bbcode[/I], [url]link[/url] and img: [img]http://host.com/abc.jpg[/img]";
    assertEquals("text content has bbcode, link and img: http://host.com/abc.jpg", 
                  TransformHTML.cleanHtmlCode(str, bbcs));
    
    // for text contain bbcode and tag html   
    str += " defaul data <a href='http://exoplatform.com'>link</a>. new <strong>data</strong>" +
                       " test<style>.css{color:blue;}</style>, <script> function a {alert('abc');}</script>tested.";
    assertEquals("text content has bbcode, link and img: http://host.com/abc.jpg defaul data link. new data test, tested.",
                    TransformHTML.cleanHtmlCode(str, bbcs));
  }
  
  public void testGetTitleInHTMLCode() {
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.getTitleInHTMLCode(TransformHTML.EMPTY_STR, bbcs));
    assertEquals("1 3", TransformHTML.getTitleInHTMLCode("1  3", bbcs));

    String title = " title [b]title[/b] <strong>title</strong>&nbsp;&nbsp; title<br/>title      title    title " + new String(Character.toChars(20)) + "title\t\ntitle";
    assertEquals("title title title title title title title title title", TransformHTML.getTitleInHTMLCode(title, bbcs));
  }
  
  public void testRemoveCharterStrange() {
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.removeCharterStrange(null));
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.removeCharterStrange(TransformHTML.EMPTY_STR));

    assertEquals("abc", TransformHTML.removeCharterStrange("abc\n\t" + new String(Character.toChars(30))));
    assertEquals("abc ", TransformHTML.removeCharterStrange("abc\n" + new String(Character.toChars(32))));
  }
  
  public void testEnCodeHTML() {
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.enCodeHTMLTitle(null));
    // clear space superfluous fix for FCKEditer
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.enCodeHTMLTitle("<p>&nbsp;&nbsp;&nbsp;</p>"));
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.enCodeHTMLTitle("<p>&nbsp;&nbsp;&nbsp;     </p>"));
    assertEquals("abc", TransformHTML.enCodeHTMLTitle("<br/><br/><br/><br/>abc"));
    assertEquals("abc", TransformHTML.enCodeHTMLTitle("abc<br/><br/><br/><br/>"));
    // encode: '<' --> '&lt;' ; '>' --> '&gt;' ; ''' --> &#39
    assertEquals("&lt;p&gt; test &lt;br/&gt; test &#39test&#39 &lt;div&gt;text&lt;/div&gt;&lt;/p&gt;", 
                TransformHTML.enCodeHTMLTitle("<p> test <br/> test 'test' <div>text</div></p>"));
  }
  
  public void testEnCodeViewSignature() {
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.enCodeViewSignature(null));
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.enCodeViewSignature(TransformHTML.EMPTY_STR));
    assertEquals("ABC hello<br/> test<div>hello</div><br/>Hello World.", TransformHTML.enCodeViewSignature("ABC hello\n test<div>hello</div>\nHello World."));
  }

  public void testEnCodeHTMLContent() {
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.enCodeViewSignature(null));
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.enCodeViewSignature(TransformHTML.EMPTY_STR));
    String s =   "Abc\txyz \nHelo everybody <div>xxx</div> BBB.\nStar is 'Ngoi Sao'";
    assertEquals("Abc&nbsp; &nbsp; xyz <br/>Helo everybody &lt;div&gt;xxx&lt;/div&gt; BBB.<br/>Star is &#39;Ngoi Sao&#39;", 
                 TransformHTML.enCodeHTMLContent(s));
    s = "[color=red\"onmouseover=\"(function(){document.write('I\'m evil');}).call(this)\"]hello world[/color]" +
        "[size=15\"onmouseover=\"(function(){document.write('I\'m evil');}).call(this)\"]hello world[/size]" +
        "[url=http://example.com\"onmouseover=\"(function(){document.write('I\'m evil');}).call(this)\"]Example[/url]";
    assertEquals("[color=red&quot;onmouseover=&quot;(function(){document.write(&#39;I&#39;m evil&#39;);}).call(this)&quot;]hello world[/color][size=15&quot;" +
    		"onmouseover=&quot;(function(){document.write(&#39;I&#39;m evil&#39;);}).call(this)&quot;]hello world[/size][url=http://example.com&quot;onmouseover=&quot;" +
    		"(function(){document.write(&#39;I&#39;m evil&#39;);}).call(this)&quot;]Example[/url]",
                 TransformHTML.enCodeHTMLContent(s));
  }

  public void testFixAddBBcodeAction() {
    // value input not null.
    assertEquals(TransformHTML.EMPTY_STR, TransformHTML.fixAddBBcodeAction(TransformHTML.EMPTY_STR));
    // check for tag <p>
    assertEquals("<p>[quote] test [/quote]</p>s<p>[code] text [/code]</p> <p>[QUOTE] QUOTE[/QUOTE]</p> <p>[CODE] CODE[/CODE]</p>", 
                  TransformHTML.fixAddBBcodeAction("<p>[quote]</p> test <p>[/quote]</p>s<p>[code]</p> text <p>[/code]</p> " +
                 "<p>[QUOTE]</p> QUOTE<p>[/QUOTE]</p> <p>[CODE]</p> CODE<p>[/CODE]</p>"));
    // check for tag <span>
    assertEquals("<span>[quote] test [/quote]</span>s<span>[code] text [/code]</span> <span>[QUOTE] QUOTE[/QUOTE]</span> <span>[CODE] CODE[/CODE]</span>", 
        TransformHTML.fixAddBBcodeAction("<span>[quote]</span> test <span>[/quote]</span>s<span>[code]</span> text <span>[/code]</span> " +
            "<span>[QUOTE]</span> QUOTE<span>[/QUOTE]</span> <span>[CODE]</span> CODE<span>[/CODE]</span>"));
  }





}
