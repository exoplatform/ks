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
package org.exoplatform.ks.rendering;

import junit.framework.TestCase;

import org.exoplatform.ks.rendering.api.Renderer;
import org.exoplatform.ks.rendering.api.UnsupportedSyntaxException;
import org.exoplatform.ks.rendering.base.AssertUtils;
import org.exoplatform.ks.rendering.base.Closure;
import org.exoplatform.ks.rendering.spi.MarkupRenderDelegate;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestMarkupRenderingService extends TestCase {

  public void testRegisterRenderer() throws UnsupportedSyntaxException {
    final MarkupRenderingService service = new MarkupRenderingService();
    Renderer renderer = new SampleRenderer();
    service.registerRenderer(renderer);
    Renderer actual = service.getRenderer(renderer.getSyntax());
    assertEquals(renderer, actual);
    AssertUtils.assertException(UnsupportedSyntaxException.class, new Closure() {
      public void dothis() {
        service.getRenderer("");
      }
    });
    AssertUtils.assertException(UnsupportedSyntaxException.class, new Closure() {
      public void dothis() {
        service.getRenderer("");
      }
    });
  }

  public void testDelegateRendering() {
    final MarkupRenderingService service = new MarkupRenderingService();
    Renderer renderer = new SampleRenderer();
    service.registerRenderer(renderer);
    SampleRenderingDelegate delegate = new SampleRenderingDelegate();
    String actual = service.delegateRendering(delegate, "foo");
    assertEquals("foo//processed", actual);

  }

  class SampleRenderingDelegate implements MarkupRenderDelegate<String> {

    public String getMarkup(String target) {
      return target;
    }

    public String getSyntax(String target) {
      return "sample"; // made to use SampleRenderer
    }

  }

  class SampleRenderer implements Renderer {

    public String getSyntax() {
      return "sample";
    }

    public String render(String markup) {
      return markup + "//processed";
    }

  }

}
