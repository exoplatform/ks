package org.exoplatform.wiki.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.chromattic.api.ChromatticSession;
import org.chromattic.common.IO;
import org.chromattic.core.api.ChromatticSessionImpl;
import org.chromattic.core.jcr.SessionWrapper;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.Template;
import org.exoplatform.wiki.service.DataStorage;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.service.search.TemplateSearchData;
import org.exoplatform.wiki.service.search.TemplateSearchResult;
import org.exoplatform.wiki.service.search.TitleSearchResult;
import org.exoplatform.wiki.service.search.WikiSearchData;
import org.exoplatform.wiki.template.plugin.WikiTemplatePagePlugin;
import org.exoplatform.wiki.utils.Utils;

public class JCRDataStorage implements DataStorage{
  private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);
  
  private static final int searchSize = 10;
  
  private WikiTemplatePagePlugin templatePlugin; 
  
  public void setTemplatePagePlugin(WikiTemplatePagePlugin plugin) {
    this.templatePlugin = plugin;
  }
  
  public PageList<SearchResult> search(ChromatticSession session, WikiSearchData data) throws Exception {
    List<SearchResult> resultList = new ArrayList<SearchResult>();
    String statement = data.getStatement();
    Query q = ((ChromatticSessionImpl) session).getDomainSession().getSessionWrapper().createQuery(statement);
    QueryResult result = q.execute();
    RowIterator iter = result.getRows();
    while (iter.hasNext()) {
      SearchResult tempResult = getResult(iter.nextRow());
      // If contains, merges with the exist
      if (tempResult != null && !isContains(resultList, tempResult)) {
        resultList.add(tempResult);
      }
    }
    return new ObjectPageList<SearchResult>(resultList, searchSize);
  }
  
  public void initDefaultTemplatePage(ChromatticSession crmSession, ConfigurationManager configurationManager, String path) {
    if (templatePlugin != null) {
      try {
        Iterator<String> iterator = templatePlugin.getSourcePaths().iterator();
        Session session = crmSession.getJCRSession();
        InputStream is = null;
        while (iterator.hasNext()) {
          try {
            String sourcePath = iterator.next();
            is = configurationManager.getInputStream(sourcePath);
            int type = ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;
            if(((Node)session.getItem(path)).hasNode(WikiNodeType.WIKI_TEMPLATE_CONTAINER)) {
              type = ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;
            }
            session.importXML(path, is, type);
            session.save();
          } finally {
            if (is != null) {
              is.close();
            }
          }
        }
      } catch (Exception e) {
        log.info("Failed to init default template page because: " + e.getCause());
      }
    }
  }
  
  private SearchResult getResult(Row row) throws Exception {
    String type = row.getValue(WikiNodeType.Definition.PRIMARY_TYPE).getString();

    String path = row.getValue(WikiNodeType.Definition.PATH).getString();
    String title = (row.getValue(WikiNodeType.Definition.TITLE) == null ? null : row.getValue(WikiNodeType.Definition.TITLE).getString());
    String excerpt = null;
    Calendar updateDate = GregorianCalendar.getInstance();
    Calendar createdDate = GregorianCalendar.getInstance();
    PageImpl page = null;
    if (WikiNodeType.WIKI_ATTACHMENT_CONTENT.equals(type)) {
      // Transform to Attachment result
      type = WikiNodeType.WIKI_ATTACHMENT.toString();
      if (row.getValue("rep:excerpt(.)") != null) {
        excerpt = row.getValue("rep:excerpt(.)").getString();
      }
      path = path.substring(0, path.lastIndexOf("/"));
      if(!path.endsWith(WikiNodeType.Definition.CONTENT)){
        AttachmentImpl searchAtt = (AttachmentImpl) Utils.getObject(path, WikiNodeType.WIKI_ATTACHMENT);
        updateDate = searchAtt.getUpdatedDate();
        page = searchAtt.getParentPage();
        createdDate.setTime(page.getCreatedDate());
        title = page.getTitle();
      } else {
        String pagePath = path.substring(0, path.lastIndexOf("/" + WikiNodeType.Definition.CONTENT));
        type = WikiNodeType.WIKI_PAGE_CONTENT.toString();
        page = (PageImpl) Utils.getObject(pagePath, WikiNodeType.WIKI_PAGE);
        title = page.getTitle();
        updateDate.setTime(page.getUpdatedDate());
        createdDate.setTime(page.getCreatedDate());
      }
    } else if (WikiNodeType.WIKI_ATTACHMENT.equals(type)) {
      AttachmentImpl searchAtt = (AttachmentImpl) Utils.getObject(path, WikiNodeType.WIKI_ATTACHMENT);
      updateDate = searchAtt.getUpdatedDate();
      page = searchAtt.getParentPage();
      createdDate.setTime(page.getCreatedDate());
    } else if (WikiNodeType.WIKI_PAGE.equals(type)) {
      page = (PageImpl) Utils.getObject(path, type);
      updateDate.setTime(page.getUpdatedDate());
      createdDate.setTime(page.getCreatedDate());
    }
    if (page == null || !page.hasPermission(PermissionType.VIEWPAGE))
      return null;
    SearchResult result = new SearchResult(excerpt, title, path, type, updateDate, createdDate);
    return result;
  }
  
  private TitleSearchResult getTitleSearchResult(Row row) throws Exception {
    String type = row.getValue(WikiNodeType.Definition.PRIMARY_TYPE).getString();
    String path = row.getValue(WikiNodeType.Definition.PATH).getString();
    String fullTitle = row.getValue(WikiNodeType.Definition.TITLE).getString();
    boolean isFile = row.getValue(WikiNodeType.Definition.FILE_TYPE) != null;
    PageImpl page = null;
    if (!isFile){
      page = (PageImpl) Utils.getObject(path, WikiNodeType.WIKI_PAGE);      
    } else {
      fullTitle = fullTitle.concat(row.getValue(WikiNodeType.Definition.FILE_TYPE).getString());
      AttachmentImpl searchAtt = (AttachmentImpl) Utils.getObject(path, WikiNodeType.WIKI_ATTACHMENT);
      page = searchAtt.getParentPage();
    }
    if (page == null || !page.hasPermission(PermissionType.VIEWPAGE))
      return null;
    TitleSearchResult result = new TitleSearchResult(fullTitle, path, type);
    return result;
  }
  
  public List<SearchResult> searchRenamedPage(ChromatticSession session, WikiSearchData data) throws Exception {
    List<SearchResult> resultList = new ArrayList<SearchResult>() ;
    String statement = data.getStatementForRenamedPage() ;
    Query q = ((ChromatticSessionImpl)session).getDomainSession().getSessionWrapper().createQuery(statement);
    QueryResult result = q.execute();
    NodeIterator iter = result.getNodes() ;
    while(iter.hasNext()) {      
      try {
        resultList.add(getResult(iter.nextNode()));
      } catch (Exception e) {
        log.debug("Failed to add item search result", e);
      }
    }
    return resultList ;
  }
  
  private SearchResult getResult(Node node)throws Exception {
    SearchResult result = new SearchResult() ;
    result.setPageName(node.getName()) ;
    String title = node.getProperty(WikiNodeType.Definition.TITLE).getString();
    InputStream data = node.getNode(WikiNodeType.Definition.CONTENT).getNode(WikiNodeType.Definition.ATTACHMENT_CONTENT).getProperty(WikiNodeType.Definition.DATA).getStream();
    byte[] bytes = IO.getBytes(data);
    String content = new String(bytes, "UTF-8");
    if(content.length() > 100) content = content.substring(0, 100) + "...";
    result.setExcerpt(content) ;
    result.setTitle(title) ;
    return result ;
  }
  
  public InputStream getAttachmentAsStream(String path, ChromatticSession session) throws Exception {
    Node attContent = (Node)session.getJCRSession().getItem(path) ;
    return attContent.getProperty(WikiNodeType.Definition.DATA).getStream() ;    
  }
  
  public List<TitleSearchResult> searchDataByTitle(ChromatticSession session, WikiSearchData data) throws Exception {
    List<TitleSearchResult> resultList = new ArrayList<TitleSearchResult>();
    SessionWrapper sessionWrapper = ((ChromatticSessionImpl) session).getDomainSession().getSessionWrapper();
    int limit = data.getLimit();
    QueryImpl q = (QueryImpl) sessionWrapper.createQuery(data.getStatementForTitle(true));
    searchDataByTitle(q, resultList, limit);
    if (limit == 0 || (limit = limit - resultList.size()) > 0) {
      q = (QueryImpl) sessionWrapper.createQuery(data.getStatementForTitle(false));
      searchDataByTitle(q, resultList, limit);
    }
    Collections.sort(resultList, new SortComparatorDESC());
    return resultList;
  }

  private void searchDataByTitle(QueryImpl q, List<TitleSearchResult> resultList, int numberItem) throws Exception {
    RowIterator iter;
    // no limit
    if (numberItem == 0) {
      iter = q.execute().getRows();
      while (iter.hasNext()) {
        addTitleSearchResult(iter.nextRow(), resultList);
      }
    } else {
      // limit numberItem
      int offset = 0, count = 0, limit = 0;
      while (count < numberItem) {
        q.setOffset(offset);
        limit = numberItem + offset;
        q.setLimit(limit);
        iter = q.execute().getRows();
        if (iter.getSize() <= 0) {
          break;
        }
        while (iter.hasNext()) {
          count += addTitleSearchResult(iter.nextRow(), resultList);
          if (count == numberItem) {
            break;
          }
        }
        offset = limit;
      }
    }
  }

  private int addTitleSearchResult(Row row, List<TitleSearchResult> resultList) throws Exception {
    try {
      TitleSearchResult iterResult = getTitleSearchResult(row);
      if (iterResult != null) {
        resultList.add(iterResult);
        return 1;
      }
    } catch (Exception e) {
      log.warn(String.format("Failed to add item %s ", row.toString()), e);
    }
    return 0;
  }
 
  private boolean isContains(List<SearchResult> list, SearchResult result) throws Exception {
    AttachmentImpl att = null;
    PageImpl page = null;
    if (WikiNodeType.WIKI_ATTACHMENT.equals(result.getType())) {
      att = (AttachmentImpl) Utils.getObject(result.getPath(), WikiNodeType.WIKI_ATTACHMENT);
    } else if (WikiNodeType.WIKI_ATTACHMENT_CONTENT.equals(result.getType())) {
      String attPath = result.getPath().substring(0, result.getPath().lastIndexOf("/"));
      att = (AttachmentImpl) Utils.getObject(attPath, WikiNodeType.WIKI_ATTACHMENT);
    } else if(WikiNodeType.WIKI_PAGE.equals(result.getType()) || WikiNodeType.WIKI_HOME.equals(result.getType())){
      page = (PageImpl) Utils.getObject(result.getPath(), WikiNodeType.WIKI_PAGE);
    } else if (WikiNodeType.WIKI_PAGE_CONTENT.equals(result.getType())) {
      att = (AttachmentImpl) Utils.getObject(result.getPath(), WikiNodeType.WIKI_ATTACHMENT);
      page = att.getParentPage();
    }
    if (att != null || page != null) {
      Iterator<SearchResult> iter = list.iterator();
      while (iter.hasNext()) {
        SearchResult child = iter.next();
        if (WikiNodeType.WIKI_ATTACHMENT.equals(child.getType()) || WikiNodeType.WIKI_PAGE_CONTENT.equals(child.getType())) {
          AttachmentImpl tempAtt = (AttachmentImpl) Utils.getObject(child.getPath(),
                                                                    WikiNodeType.WIKI_ATTACHMENT);
          if (att != null && att.equals(tempAtt)) {
            // Merge data
            if (child.getExcerpt()==null && result.getExcerpt()!=null ){
              child.setExcerpt(result.getExcerpt());
            }
            return true;
          }               
          if (page != null && page.getName().equals(tempAtt.getParentPage())) {
            return true;
          }     
        }
        else if (WikiNodeType.WIKI_PAGE.equals(child.getType())) {
          if (page != null && page.getPath().equals(child.getPath())) {
            iter.remove();
            return false;
          }
        }
      }
    }
    return false;
  }

  @Override
  public List<TemplateSearchResult> searchTemplate(ChromatticSession session,
                                                       TemplateSearchData data) throws Exception {

    List<TemplateSearchResult> resultList = new ArrayList<TemplateSearchResult>();
    String statement = data.getStatement();
    Query q = ((ChromatticSessionImpl)session).getDomainSession().getSessionWrapper().createQuery(statement);
    QueryResult result = q.execute();
    RowIterator iter = result.getRows();
    while (iter.hasNext()) {
      TemplateSearchResult tempResult = getTemplateResult(iter.nextRow());
      resultList.add(tempResult);
    }
   return resultList;
  }

  private TemplateSearchResult getTemplateResult(Row row) throws Exception {
    String type = row.getValue(WikiNodeType.Definition.PRIMARY_TYPE).getString();

    String path = row.getValue(WikiNodeType.Definition.PATH).getString();
    String title = (row.getValue(WikiNodeType.Definition.TITLE) == null ? null : row.getValue(WikiNodeType.Definition.TITLE).getString());
    
    Template template = (Template) Utils.getObject(path, WikiNodeType.WIKI_PAGE);
    String description = template.getDescription();
    TemplateSearchResult result = new TemplateSearchResult(template.getName(),
                                                           title,
                                                           path,
                                                           type,
                                                           null,
                                                           null,
                                                           description);
    return result;
  }
  
  static private class SortComparatorDESC implements Comparator<TitleSearchResult> {
    public int compare(TitleSearchResult titleSearchResult1, TitleSearchResult titleSearchResult2) {
      return titleSearchResult2.getType().compareTo(titleSearchResult1.getType());
    }
  }
}
