package org.exoplatform.wiki.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.TreeNode.TREETYPE;
import org.exoplatform.wiki.tree.utils.TreeUtils;
import org.exoplatform.wiki.webui.UIWikiEmptyAjaxBlock;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.tree.EventUIComponent;
import org.exoplatform.wiki.webui.tree.UITreeExplorer;
import org.exoplatform.wiki.webui.tree.EventUIComponent.EVENTTYPE;

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "/templates/wiki/webui/popup/UIWikiSelectPageForm.gtmpl",
    events = {
        @EventConfig(listeners = UIWikiSelectPageForm.SetCurrentPageActionListener.class),
        @EventConfig(listeners = UIWikiSelectPageForm.SelectPageActionListener.class),
        @EventConfig(listeners = UIWikiSelectPageForm.CancelActionListener.class)
    }
)
public class UIWikiSelectPageForm extends UIForm implements UIPopupComponent {
  private static final Log log                     = ExoLogger.getLogger(UIWikiSelectPageForm.class);
  
  public static final String FORM_ID = "UIWikiSelectPageForm";
  
  private String currentNodeValue = ""; 
  
  public static final String UI_TREE_ID = "UIPageTree";
  
  public UIWikiSelectPageForm() throws Exception {
    setId(FORM_ID);
    UITreeExplorer uiTree = addChild(UITreeExplorer.class, null, UI_TREE_ID);
    EventUIComponent eventComponent = new EventUIComponent(FORM_ID,
                                                           "SetCurrentPage",
                                                           EVENTTYPE.EVENT);
    StringBuilder initURLSb = new StringBuilder(Utils.getCurrentRestURL());
    initURLSb.append("/wiki/tree/").append(TREETYPE.ALL.toString());
    StringBuilder childrenURLSb = new StringBuilder(Utils.getCurrentRestURL());
    childrenURLSb.append("/wiki/tree/").append(TREETYPE.CHILDREN.toString());
    uiTree.init(initURLSb.toString(), childrenURLSb.toString(), getInitParam(), eventComponent, false);
  }
  /**
   * list of ui component needed to updated when form is submitted.
   */
  private List<UIComponent> updatedComponents = new ArrayList<UIComponent>();
  
  public void addUpdatedComponent(UIComponent component) {
    updatedComponents.add(component);
  }
  
  public void removeUpdatedComponent(UIComponent component) {
    updatedComponents.remove(component);
  }
  
  private String getInitParam() throws Exception {
    StringBuilder sb = new StringBuilder();
    String currentPath = Utils.getCurrentWikiPagePath();
    sb.append("?")
      .append(TreeNode.PATH)
      .append("=")
      .append(currentPath)
      .append("&")
      .append(TreeNode.CURRENT_PATH)
      .append("=")
      .append(currentPath);
    return sb.toString();
  }
  
  @Override
  public void activate() throws Exception {
    
  }
  
  
  
  @Override
  public void deActivate() throws Exception {

    
  }
  
  static public class SetCurrentPageActionListener extends EventListener<UIWikiSelectPageForm> {

    @Override
    public void execute(Event<UIWikiSelectPageForm> event) throws Exception {
      UIWikiSelectPageForm uiform = event.getSource();
      UIWikiEmptyAjaxBlock emptyBlock = uiform.getAncestorOfType(UIWikiPortlet.class).getChild(UIWikiEmptyAjaxBlock.class);
      String param = event.getRequestContext().getRequestParameter(OBJECTID);
      if (param != null) uiform.currentNodeValue = param;
      event.getRequestContext().addUIComponentToUpdateByAjax(emptyBlock);
    }
  }
  
  static public class SelectPageActionListener extends EventListener<UIWikiSelectPageForm> {

    @Override
    public void execute(Event<UIWikiSelectPageForm> event) throws Exception {
      UIWikiSelectPageForm uiform = event.getSource();
      UIWikiPortlet wikiPortlet = uiform.getAncestorOfType(UIWikiPortlet.class);
      try {
        if (uiform.currentNodeValue.length() > 0) {
          String currentNodeValue = TitleResolver.getId(uiform.currentNodeValue, false);
          WikiPageParams params = TreeUtils.getPageParamsFromPath(currentNodeValue);

          WikiService service = uiform.getApplicationComponent(WikiService.class);
          service.addRelatedPage(Utils.getCurrentWikiPageParams(), params);
        }
      } catch (Exception e) {
         if (log.isWarnEnabled()) log.warn("can not execute 'SelectPage' action", e);
      }
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.cancelPopupAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
      for (UIComponent c : uiform.updatedComponents) {
        event.getRequestContext().addUIComponentToUpdateByAjax(c);
      }
    }
  }
  
  static public class CancelActionListener extends EventListener<UIWikiSelectPageForm> {

    @Override
    public void execute(Event<UIWikiSelectPageForm> event) throws Exception {
      UIWikiSelectPageForm uiform = event.getSource();
      UIWikiPortlet wikiPortlet = uiform.getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.cancelPopupAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }
  
}
