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
package org.exoplatform.faq.service.ws;

import javax.ws.rs.core.MediaType;

import org.exoplatform.services.rest.impl.ContainerResponse;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Oct 15, 2012  
 */
public class FAQWebServiceTestCase extends AbstractResourceTest {
  
  private static final String  BASE_URL     = "http://localhost:8080";

  private static final String  REST_CONTEXT = "/ks/faq";
  
  private static FAQWebservice faqWebservice;

  public FAQWebServiceTestCase() throws Exception {
  }
  
  public void setUp() throws Exception {
    super.setUp();
    
    faqWebservice = new FAQWebservice();
    registry(faqWebservice);
  }
  
  public void tearDown() throws Exception {
    super.tearDown();

    removeResource(faqWebservice.getClass());
  }
  
  public void testViewRss() throws Exception {
    ContainerResponse response = service("GET", BASE_URL + REST_CONTEXT + "/rss/categories", "", null, null);
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_XML, response.getContentType().toString());
  }
}
