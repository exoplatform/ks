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
package org.exoplatform.wiki.rendering.render.confluence;

import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.xwiki.rendering.internal.parser.plain.PlainTextStreamParser;
import org.xwiki.rendering.internal.renderer.ParametersPrinter;
import org.xwiki.rendering.listener.QueueListener.Event;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.reference.ResourceReferenceSerializer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 2, 2010  
 */

/**
 * Logic to render a Confluence Link into Confluence syntax.
 */
public class ConfluenceSyntaxLinkRenderer {

  private ParametersPrinter parametersPrinter = new ParametersPrinter();

  private Stack<Boolean> forceFullSyntax = new Stack<Boolean>();

  private ConfluenceSyntaxListenerChain listenerChain;

  private ResourceReferenceSerializer linkReferenceSerializer;

  public ConfluenceSyntaxLinkRenderer(ConfluenceSyntaxListenerChain listenerChain, ResourceReferenceSerializer linkReferenceSerializer) {
    this.listenerChain = listenerChain;
    this.linkReferenceSerializer = linkReferenceSerializer;
    this.forceFullSyntax.push(false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see DefaultLinkReferenceSerializer#serialize(org.xwiki.rendering.listener.Link)
   */
  public String serialize(ResourceReference link) {
    return this.linkReferenceSerializer.serialize(link);
  }

  public void beginRenderLink(ConfluenceSyntaxEscapeWikiPrinter printer, ResourceReference link, boolean isFreeStandingURI,
                              Map<String, String> parameters) {
    // find if the last printed char is part of a syntax (i.e. consumed by the
    // parser before starting to parse the link)
    boolean isLastSyntax = printer.getBuffer().length() == 0;

    printer.flush();

    if (forceFullSyntax(printer, isLastSyntax, isFreeStandingURI, parameters)) {
      this.forceFullSyntax.push(true);

      printer.print("[");
    } else {
      this.forceFullSyntax.push(false);
    }
  }

  public boolean forceFullSyntax(ConfluenceSyntaxEscapeWikiPrinter printer, boolean isFreeStandingURI,
                                 Map<String, String> parameters) {
    return forceFullSyntax(printer, true, isFreeStandingURI, parameters);
  }

  public boolean forceFullSyntax(ConfluenceSyntaxEscapeWikiPrinter printer, boolean isLastSyntax, boolean isFreeStandingURI,
                                 Map<String, String> parameters) {
    Event nextEvent = this.listenerChain.getLookaheadChainingListener().getNextEvent();

    // force full syntax if
    // 1: it's not a free standing URI
    // 2: there is parameters
    // 3: it follows a character which is not a white space (newline/space) and
    // is not consumed by the parser (like a another link)
    // 4: it's followed by a character which is not a white space
    // (TODO: find a better way than this endless list of EventType test but it probably need
    // some big refactoring of the printer and ConfluenceSyntaxLinkRenderer)
    return !isFreeStandingURI
        || !parameters.isEmpty()
        || (!isLastSyntax && !printer.isAfterWhiteSpace() && (!PlainTextStreamParser.SPECIALSYMBOL_PATTERN.matcher(String.valueOf(printer.getLastPrinted()
                                                                                                                                         .charAt(printer.getLastPrinted()
                                                                                                                                                        .length() - 1)))
                                                                                                          .matches()))
        || (nextEvent != null && nextEvent.eventType != EventType.ON_SPACE
            && nextEvent.eventType != EventType.ON_NEW_LINE
            && nextEvent.eventType != EventType.END_PARAGRAPH
            && nextEvent.eventType != EventType.END_LINK
            && nextEvent.eventType != EventType.END_LIST_ITEM
            && nextEvent.eventType != EventType.END_DEFINITION_DESCRIPTION
            && nextEvent.eventType != EventType.END_DEFINITION_TERM
            && nextEvent.eventType != EventType.END_QUOTATION_LINE && nextEvent.eventType != EventType.END_SECTION);
  }

  public void renderLinkContent(ConfluenceSyntaxEscapeWikiPrinter printer, String label) {
    // If there was some link content specified then output the character separator "|".
    if (!StringUtils.isEmpty(label)) {
      printer.print(label);
      printer.print("|");
    }
  }

  public void endRenderLink(ConfluenceSyntaxEscapeWikiPrinter printer, ResourceReference link, boolean isFreeStandingURI,
                            Map<String, String> parameters) {
    printer.print(serialize(link));

    // If there were parameters specified, output them separated by the "|" characters
    if (!parameters.isEmpty()) {
      printer.print("|");
      printer.print(this.parametersPrinter.print(parameters, '~'));
    }

    if (this.forceFullSyntax.peek() || !isFreeStandingURI) {
      printer.print("]");
    }

    this.forceFullSyntax.pop();
  }
}
