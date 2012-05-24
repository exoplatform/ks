package org.exoplatform.faq.cache;

import org.exoplatform.ks.common.cache.ScopeCacheKey;

import java.util.Arrays;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class FAQMarkupKey extends ScopeCacheKey {

  private String componentId;
  private String interactionstate;
  private String type;

  public FAQMarkupKey(Map<String, String[]> context) {

    if (context == null) {
      throw new NullPointerException();
    }

    String[] pcomponentId = context.get("portal:componentId");
    String[] pinteractionstate = context.get("interactionstate");
    String[] ptype = context.get("portal:type");

    if (pcomponentId != null) {
      componentId = Arrays.asList(pcomponentId).toString();
    }

    if (pinteractionstate != null) {
      interactionstate = Arrays.asList(pinteractionstate).toString();
    }

    if (ptype != null) {
      type = Arrays.asList(ptype).toString();
    }
    
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FAQMarkupKey)) return false;
    if (!super.equals(o)) return false;

    FAQMarkupKey that = (FAQMarkupKey) o;

    if (componentId != null ? !componentId.equals(that.componentId) : that.componentId != null) return false;
    if (interactionstate != null ? !interactionstate.equals(that.interactionstate) : that.interactionstate != null)
      return false;
    if (type != null ? !type.equals(that.type) : that.type != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (componentId != null ? componentId.hashCode() : 0);
    result = 31 * result + (interactionstate != null ? interactionstate.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

}
