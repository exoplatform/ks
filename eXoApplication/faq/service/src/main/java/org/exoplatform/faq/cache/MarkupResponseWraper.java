package org.exoplatform.faq.cache;

import org.apache.tika.io.ByteArrayOutputStream;
import org.exoplatform.services.cache.ExoCache;

import javax.portlet.RenderResponse;
import javax.portlet.filter.RenderResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class MarkupResponseWraper extends RenderResponseWrapper {

  private final FAQMarkupKey key;
  private final ExoCache<FAQMarkupKey, FAQMarkupData> faqMarkup;

  public MarkupResponseWraper(RenderResponse response, FAQMarkupKey key, ExoCache<FAQMarkupKey, FAQMarkupData> faqMarkup) {
    super(response);
    this.key = key;
    this.faqMarkup = faqMarkup;
  }

  @Override
  public OutputStream getPortletOutputStream() throws IOException {
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        super.close();
        faqMarkup.put(key, new FAQMarkupData(toString()));
        writeTo(getResponse().getPortletOutputStream());
      }
    };
  }
}
