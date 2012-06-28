package org.exoplatform.faq.cache;

import org.exoplatform.ks.common.cache.CachedData;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class FAQMarkupData implements CachedData<String> {

  private final String markup;

  public FAQMarkupData(String markup) {
    this.markup = markup;
  }

  public String build() {
    return markup;
  }
  
}
