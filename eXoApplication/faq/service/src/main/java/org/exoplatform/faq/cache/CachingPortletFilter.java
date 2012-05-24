package org.exoplatform.faq.cache;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.common.cache.CacheType;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import javax.portlet.*;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class CachingPortletFilter implements RenderFilter {

  public void doFilter(RenderRequest renderRequest, RenderResponse renderResponse, FilterChain filterChain) throws IOException, PortletException {

    //
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    CacheService service = (CacheService) container.getComponentInstanceOfType(CacheService.class);
    PortalRequestContext ctx = (PortalRequestContext) PortalRequestContext.getCurrentInstance();

    //
    Map<String, String[]> map = ctx.getRequest().getParameterMap();
    FAQMarkupKey key = new FAQMarkupKey(map);
    ExoCache<FAQMarkupKey, FAQMarkupData> faqMarkup = CacheType.FAQ_MARKUP.getFromService(service);

    //
    FAQMarkupData data = faqMarkup.get(key);
    if (data != null) {
      renderResponse.getWriter().write(data.build());
    } else {
      MarkupResponseWraper responseWraper = new MarkupResponseWraper(renderResponse, key, faqMarkup);
      filterChain.doFilter(renderRequest, responseWraper);
    }

  }

  public void init(FilterConfig filterConfig) throws PortletException {
  }

  public void destroy() {
  }
  
}
