/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.internal.parser.confluence;

import javax.inject.Inject;
import javax.inject.Named;

import org.exoplatform.wiki.rendering.internal.parser.DefaultXWikiConfluenceGeneratorListener;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.confluence.ConfluenceParser;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.confluence.ConfluenceWikiParser;

/**
 * Created by The eXo Platform SAS
 * Author : haidd 
 *          haidd@exoplatform.com
 * Jan 14, 2012  
 */
@Component("confluence/1.0")
public class DefaultWikiModelConfluenceParser extends ConfluenceParser {
  
  /**
   * @see #getLinkReferenceParser()
   */  
  @Inject
  @Named("confluence/1.0/link")
  private ResourceReferenceParser referenceParser;

  /**
   * @see #getImageReferenceParser()
   */
  @Inject
  @Named("default/image")
  private ResourceReferenceParser imageReferenceParser;

  @Override
  public ResourceReferenceParser getLinkReferenceParser() {
    return this.referenceParser;
  }

  @Override
  public ResourceReferenceParser getImageReferenceParser() {
    return this.imageReferenceParser;
  }
  
  public IWikiParser createWikiModelParser() {
    return new ConfluenceWikiParser();
  }

  @Override
  public XWikiGeneratorListener createXWikiGeneratorListener(Listener listener,
                                                             IdGenerator idGenerator) {
    return new DefaultXWikiConfluenceGeneratorListener(this,
                                                       listener,
                                                       getLinkReferenceParser(),
                                                       getImageReferenceParser(),
                                                       this.plainRendererFactory,
                                                       idGenerator,
                                                       Syntax.CONFLUENCE_1_0);
  }
}
