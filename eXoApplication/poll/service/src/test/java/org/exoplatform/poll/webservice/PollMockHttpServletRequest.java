package org.exoplatform.poll.webservice;

import org.exoplatform.services.test.mock.MockHttpServletRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class PollMockHttpServletRequest extends MockHttpServletRequest {

  /**
   * Instantiates a new mock http servlet request.
   *
   * @param url     the url
   * @param data    the data
   * @param length  the length
   * @param method  the method
   * @param headers the headers
   */
  public PollMockHttpServletRequest(String url, InputStream data, int length, String method, Map<String, List<String>> headers) {
    super(url, data, length, method, headers);
  }

   /**
    * {@inheritDoc}
    */
   public String getServerName() {
     try {
      return super.getServerName();
     } catch (Exception e) {

     }
     return "localhost";
   }

     /**
    * {@inheritDoc}
    */
   public int getServerPort() {
     try {
      return super.getServerPort();
     } catch (Exception e) {

     }
     return 8080;
   }

}