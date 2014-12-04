/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 */
package org.exoplatform.ks.bbcode.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.spi.BBCodeData;
import org.exoplatform.ks.bbcode.spi.BBCodeProvider;
import org.exoplatform.ks.common.TransformHTML;
import org.exoplatform.ks.rendering.api.Renderer;
import org.exoplatform.ks.rendering.core.SupportedSyntaxes;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.rendering.RenderingService;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

/**
 * Renderer for BBCode markup. 
 * BBCode lookup is delegated to {@link BBCodeProvider}. By default, this implementation will use a {@link BuiltinBBCodeProvider}
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class BBCodeRenderer implements Renderer {

  public static final String BBCODE_SYNTAX_ID = "bbcode";

  protected BBCodeProvider   bbCodeProvider;

  private static final Log   log              = ExoLogger.getLogger(BBCodeRenderer.class);

  public BBCodeRenderer() {
    bbCodeProvider = new BuiltinBBCodeProvider();
  }

  public String getSyntax() {
    return SupportedSyntaxes.bbcode.name();
  }

  public String render(String s) {
    for (BBCode bbcode : getBbcodes()) {
      s = processReplace(s, bbcode);
    }
    return s;
  }

  public String processReplace(String s, BBCode bbcode) {
    String bbc = bbcode.getTagName();
    if (bbc.equals("URL")) {
      s = StringUtils.replace(s, "[link", "[URL");
      s = StringUtils.replace(s, "[/link]", "[/URL]");
      s = StringUtils.replace(s, "[LINK", "[URL");
      s = StringUtils.replace(s, "[/LINK]", "[/URL]");
    }
    if (!bbc.equals("LIST")) {
      if (Boolean.valueOf(bbcode.isOption())) {
        s = processOptionedTag(s, bbcode);
      } else {
        s = processTag(s, bbcode);
      }
    } else {
      s = processList(s);
    }
    return s;
  }

  String processTag(String s, BBCode bbcode) {
    String bbc = bbcode.getTagName();
    int clsIndex;
    String start;
    String end;
    String str;
    String param;
    int lastIndex = 0, tagIndex = 0;
    start = "[" + bbc + "]";
    end = "[/" + bbc + "]";
    s = StringUtils.replace(s, start.toLowerCase(), start);
    s = StringUtils.replace(s, end.toLowerCase(), end);
    while ((tagIndex = s.indexOf(start, lastIndex)) != -1) {
      lastIndex = tagIndex + 1;
      try {
        clsIndex = s.indexOf(end, tagIndex);
        str = s.substring(tagIndex + start.length(), clsIndex);
        if ("WIKI".equals(bbc)) {
          String sourceSyntax = Syntax.CONFLUENCE_1_0.toIdString();
          RenderingService renderingService = (RenderingService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RenderingService.class);
          param = TransformHTML.getPlainText(str);
          param = renderingService.render(param, sourceSyntax, Syntax.XHTML_1_0.toIdString(), false);
          param = new StringBuffer("<div class=\"UIWikiPortlet\">").append(param).append("</div>").toString();
        } else {
          param = StringUtils.replace(bbcode.getReplacement(), "{param}", str);
        }
        s = StringUtils.replace(s, start + str + end, param);
      } catch (Exception e) {
        continue;
      }
    }
    return s;
  }

  String processOptionedTag(String markup, BBCode bbcode) {
    String bbc = bbcode.getTagName();
    int clsIndex;
    String start;
    String end;
    String str;
    String param;
    String option;
    int lastIndex = 0, tagIndex = 0;
    start = "[" + bbc + "=";
    end = "[/" + bbc + "]";
    markup = StringUtils.replace(markup, start.toLowerCase(), start);
    markup = StringUtils.replace(markup, end.toLowerCase(), end);
    while ((tagIndex = markup.indexOf(start, lastIndex)) != -1) {
      lastIndex = tagIndex + 1;
      try {
        clsIndex = markup.indexOf(end, tagIndex);
        str = markup.substring(tagIndex + start.length(), clsIndex);
        option = str.substring(0, str.indexOf("]"));
        if (option.indexOf("+") == 0)
          option = option.replaceFirst("\\+", "");
        if (option.indexOf("\"") == 0)
          option = option.replaceAll("\"", "");
        if (option.indexOf("'") == 0)
          option = option.replaceAll("'", "");
        if (option.indexOf("&quot;") == 0)
          option = option.replaceAll("&quot;", "");
        option = option.trim();
        param = str.substring(str.indexOf("]") + 1);
        while (bbc.equals("CODE") && (param.indexOf("<br") >= 0)) {
          param = param.replaceAll("<br\\s*\\/?>", "\n");
        }
        while (bbc.equals("CODE") && param.indexOf("<p>") >= 0 && param.indexOf("</p>") >= 0) {
          param = StringUtils.replace(param, "<p>", "");
          param = StringUtils.replace(param, "</p>", "\n");
        }
        if ("WIKI".equals(bbc)) {
          String sourceSyntax = Syntax.CONFLUENCE_1_0.toIdString();
          option = option.toLowerCase();
          if (SyntaxType.XWIKI.getId().equals(option)) {
            sourceSyntax = Syntax.XWIKI_2_0.toIdString();
          } else if (SyntaxType.CREOLE.getId().equals(option)) {
            sourceSyntax = Syntax.CREOLE_1_0.toIdString();
          } else if (SyntaxType.MEDIAWIKI.getId().equals(option)) {
            sourceSyntax = Syntax.MEDIAWIKI_1_0.toIdString();
          }
          RenderingService renderingService = (RenderingService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RenderingService.class);
          param = TransformHTML.getPlainText(param);
          param = renderingService.render(param, sourceSyntax, Syntax.XHTML_1_0.toIdString(), false);
          param = new StringBuffer("<div class=\"UIWikiPortlet\">").append(param).append("</div>").toString();
        } else {
          param = StringUtils.replace(bbcode.getReplacement(), "{param}", param);
          param = StringUtils.replace(param, "{option}", option);
        }
        markup = StringUtils.replace(markup, start + str + end, param);
      } catch (Exception e) {
        continue;
      }
    }
    return markup;
  }

  String processList(String s) {
    int lastIndex;
    int tagIndex;
    int clsIndex;
    String str;
    lastIndex = 0;
    tagIndex = 0;
    s = StringUtils.replace(s, "[LIST", "[list");
    s = StringUtils.replace(s, "[/LIST]", "[/list]");
    while ((tagIndex = s.indexOf("[list]", lastIndex)) != -1) {
      lastIndex = tagIndex + 1;
      try {
        clsIndex = s.indexOf("[/list]", tagIndex);
        str = s.substring(tagIndex + 6, clsIndex);
        String str_ = "";
        str_ = StringUtils.replaceOnce(str, "[*]", "<li>");
        str_ = StringUtils.replace(str_, "[*]", "</li><li>");
        if (str_.lastIndexOf("</li><li>") > 0) {
          str_ = str_ + "</li>";
        }
        if (str_.indexOf("<br/>") >= 0) {
          str_ = StringUtils.replace(str_, "<br/>", "");
        }
        if (str_.indexOf("<p>") >= 0) {
          str_ = StringUtils.replace(str_, "<p>", "");
          str_ = StringUtils.replace(str_, "</p>", "");
        }
        s = StringUtils.replace(s, "[list]" + str + "[/list]", "<ul>" + str_ + "</ul>");
      } catch (Exception e) {
        continue;
      }
    }

    lastIndex = 0;
    tagIndex = 0;
    while ((tagIndex = s.indexOf("[list=", lastIndex)) != -1) {
      lastIndex = tagIndex + 1;

      try {
        clsIndex = s.indexOf("[/list]", tagIndex);
        String content = s.substring(tagIndex + 6, clsIndex);
        int clsType = content.indexOf("]");
        String type = content.substring(0, clsType);
        type = type.replaceAll("\"", "").replaceAll("'", "");
        str = content.substring(clsType + 1);
        String str_ = "";
        str_ = StringUtils.replaceOnce(str, "[*]", "<li>");
        str_ = StringUtils.replace(str_, "[*]", "</li><li>");
        if (str_.lastIndexOf("</li><li>") > 0) {
          str_ = str_ + "</li>";
        }
        if (str_.indexOf("<br/>") >= 0) {
          str_ = StringUtils.replace(str_, "<br/>", "");
        }
        if (str_.indexOf("<p>") >= 0) {
          str_ = StringUtils.replace(str_, "<p>", "");
          str_ = StringUtils.replace(str_, "</p>", "");
        }
        if (" 1 i I a A ".indexOf(type) > 0) {
          s = StringUtils.replace(s, "[list=" + content + "[/list]", "<ol type=\"" + type + "\">" + str_ + "</ol>");
        } else {
          str_ = StringUtils.replace(str_, "<li>", "<li type=\"" + type + "\">");
          s = StringUtils.replace(s, "[list=" + content + "[/list]", "<ul>" + str_ + "</ul>");
        }
      } catch (Exception e) {
        continue;
      }
    }
    return s;
  }

  public List<BBCode> getBbcodes() {
    List<BBCode> result = new CopyOnWriteArrayList<BBCode>();
    Collection<String> supported = getBbCodeProvider().getSupportedBBCodes();

    if (supported == null) {
      log.warn("No BBCode supported by this renderer");
      return result;
    }
    Iterator<String> itor = supported.iterator();
    while(itor.hasNext()) {
      result.add(getBbCodeProvider().getBBCode(itor.next()));
    }

    return result;
  }

  protected List<BBCode> convert(List<BBCodeData> bbc) {
    List<BBCode> bbcodes = new ArrayList<BBCode>();
    for (BBCodeData bbCodeData : bbc) {
      BBCode bbCode = new BBCode();
      bbCode.setActive(Boolean.valueOf(bbCodeData.getIsActive()));
      bbCode.setDescription(bbCodeData.getDescription());
      bbCode.setExample(bbCodeData.getExample());
      bbCode.setOption(Boolean.valueOf(bbCodeData.getIsOption()));
      bbCode.setReplacement(bbCodeData.getReplacement());
      bbCode.setTagName(bbCodeData.getTagName());
      bbcodes.add(bbCode);
    }
    return bbcodes;
  }

  public BBCodeProvider getBbCodeProvider() {
    return bbCodeProvider;
  }

  public void setBbCodeProvider(BBCodeProvider bbCodeProvider) {
    this.bbCodeProvider = bbCodeProvider;
  }

  public String renderExample(String s, BBCode bbco) {
    List<BBCode> bbcodes = new ArrayList<BBCode>();
    bbcodes.add(bbco);
    bbcodes.addAll(getBbcodes());
    for (BBCode bbcode : bbcodes) {
      if (bbcode.getId().equals(bbco.getId())) {
        s = processReplace(s, bbco);
      } else {
        s = processReplace(s, bbcode);
      }
    }
    return s;
  }
}
