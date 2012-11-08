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
package org.exoplatform.forum.webui;


import org.exoplatform.forum.base.AbstractUIComponentTestCase;
import org.exoplatform.forum.service.FakeForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.core.BBCodeRenderer;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.ks.bbcode.core.MemoryBBCodeService;
import org.exoplatform.ks.rendering.MarkupRenderingService;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestUITopicDetail extends AbstractUIComponentTestCase<UITopicDetail> {

  private FakeForumService  service;
  private MarkupRenderingService markupRenderingService;
  private MemoryBBCodeService bbcodeService;
  
  public TestUITopicDetail() throws Exception {
    super();
    service = new  FakeForumService();  
  }

  public void doSetUp() {  
    super.doSetUp();

    // init BBCodeRenderer with extended BBCode
    ExtendedBBCodeProvider provider = new ExtendedBBCodeProvider();
    bbcodeService = new MemoryBBCodeService();
    provider.setBBCodeService(bbcodeService);
    BBCodeRenderer bbcodeRenderer = new BBCodeRenderer();
    bbcodeRenderer.setBbCodeProvider(provider);
    
    //     
    // register renderign service
    
    this.markupRenderingService = new MarkupRenderingService();
    markupRenderingService.registerRenderer(bbcodeRenderer);

  }

  public void testReplaceByBBCode() throws Exception {
    registerBBCode("FOO", "BAR");
  }
  
  public void testProcessMarkup() throws Exception {
    registerBBCode("FOO", "BAR");
    String markup = "[FOO]sdsd[/FOO]";
    Post post = new Post();
    post.setMessage(markup);
    component.renderHelper.setMarkupRenderingService(this.markupRenderingService);
    assertEquals("BAR", component.renderPost(post));
  }

  private void registerBBCode(String tagName, String replacement) {
    service.addActiveBBCodes(tagName);
    BBCode foo = new BBCode();
    foo.setReplacement(replacement);
    foo.setId(tagName);
    foo.setTagName(tagName);  
    service.setBBCode(tagName, foo);

    bbcodeService.addBBCode(foo);
    
    
  }
  
  @Override
  protected UITopicDetail createComponent() throws Exception {
    UITopicDetail form =  new UITopicDetail();
    form.setForumService(service);
    return form;
  }




}
