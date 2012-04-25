/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.ks.bench;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 2, 2011  
 */
public class AnswerDataInjector extends DataInjector {
  private static Log          log           = ExoLogger.getLogger(AnswerDataInjector.class);

  private Map<String, String> fullNameData  = new HashMap<String, String>();

  private int[]               infoIject     = new int[] { 0, 0, 0, 0 };

  private Category            categoryRoot  = null;

  private FAQService          faqService;

  private FAQSetting          faqSetting    = new FAQSetting();

  private List<String>        categoryIds   = new ArrayList<String>();
  
  public static final String SLASH         = "/".intern();
  public static final String  ARRAY_SPLIT   = ",";

  enum CONSTANTS {
    TYPE("type"), DATA("data"), PERM("perm"), Q("q"), PRE("pre"), ANY("any"),
    EDIT("edit"), VIEW("view"), ATT("att"), ATTCP("attCp"), TXTCP("txtCp");
    private final String name;

    CONSTANTS(String name) {
      this.name = name;
    }
    
    public String getName() {
      return name;
    }
  };

  public AnswerDataInjector(FAQService faqService) {
    this.faqService = faqService;
    initDatas();
  }

  private void initDatas() {
    faqSetting.setDisplayMode("");
    faqSetting.setEmailSettingSubject("eXo Answers Notification");
    faqSetting.setEmailSettingContent("<p>We have a new question or answer by injector datas in category " + 
        "<strong>&categoryName_</strong></p><p><em>&questionContent_</em></p>");
    List<String> users = Arrays.asList(new String[] { "root", "demo", "mary", "john" });
    List<String> userFullNames = Arrays.asList(new String[] { "Root Root", "Demo", "Mary Kelly", "John Anthony" });
    for (int i = 0; i < users.size(); ++i) {
      fullNameData.put(users.get(i), userFullNames.get(i));
    }
  }

  @Override
  public Log getLog() {
    return log;
  }

  private Category getCategoryRoot(boolean isUpdate) {
    try {
      if (isUpdate || categoryRoot == null) {
        categoryRoot = faqService.getCategoryById(KSDataLocation.Locations.FAQ_CATEGORIES_HOME);
      }
      return categoryRoot;
    } catch (Exception e) {
      return null;
    }
  }  
  
  private String makeName(String prefix, int leve, int order) {
    return prefix + "_" + leve + "_" + order;
  }

  private String makeId(String prefix, String type, int leve, int order) {
    return type + prefix + leve + order;
  }


  private List<Category> findCategories(int leve, InjectInfo info) {
    List<Category> categories = new ArrayList<Category>();
    if (info.getCategories() > 0) {
      Category cat;
      String catName = "", catId = "";
      for (int i = 0; i < info.getCategories(); i++) {
        catId = makeId(info.getPreCategories(), Category.CATEGORY_ID, leve, i + 1);
        catName = makeName(info.getPreCategories(), leve, i + 1);
        cat = newCategory(catId, catName, i + 1, info);
        categories.add(cat);
      }
    }
    return categories;
  }

  private List<Question> findQuestion(String catId, int leve, InjectInfo info) {
    List<Question> questions = new ArrayList<Question>();
    if (info.getQuestions() > 0) {
      String queName = "", queId = "";
      for (int i = 0; i < info.getQuestions(); i++) {
        queId = makeId(info.getPreQuestions(), Question.QUESTION_ID, leve, i + 1);
        queName = makeName(info.getPreQuestions(), leve, i + 1);
        questions.add(newQuestion(catId, queId, queName, info));
      }
    }
    return questions;
  }

  private List<Answer> findAnswers(InjectInfo info) {
    List<Answer> answers = new ArrayList<Answer>();
    if (info.getAnswers() > 0) {
      String asId = "";
      for (int i = 0; i < info.getAnswers(); i++) {
        asId = makeId(info.getPreAnswers(), Answer.ANSWER_ID, 1, i);
        answers.add(newAnswer(asId, info));
      }
    }
    return answers;
  }

  private List<Comment> findComments(InjectInfo info) {
    List<Comment> comments = new ArrayList<Comment>();
    if (info.getComments() > 0) {
      String cmId = "";
      for (int i = 0; i < info.getComments(); i++) {
        cmId = makeId(info.getPreComments(), Answer.ANSWER_ID, 1, i);
        comments.add(newComment(cmId, info));
      }
    }
    return comments;
  }

  private String getTabs(int currentDepth) {
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < currentDepth; i++) {
      s.append("    ");
    }
    return s.toString();
  }
  
  private void initDataForOneCategory(String parentId, Category cat, int currentDepth, int index, int size, InjectInfo info) throws Exception {
    String catId = parentId + SLASH + cat.getId();
    boolean isExist = faqService.isExisting(catId);
    String questionId, stt = "Update";
    String s = getTabs(currentDepth);
    List<Question> questions = findQuestion(catId, currentDepth, info);
    int index_ = 0, size_ = questions.size(), as = 0, cm = 0;
    if (!isExist) {
      stt = "Add new";
      faqService.saveCategory(parentId, cat, true);
      infoIject[0]++;
    }
    log.info(String.format(" %s %s Category %s/%s with %s questions...", s, stt, index, size, size_));
    long t1;
    for (Question question : questions) {
      t1 = System.currentTimeMillis();
      questionId = catId + SLASH + Utils.QUESTION_HOME + SLASH + question.getId();
      isExist = faqService.isExisting(questionId);
      stt = "Update";
      if (!isExist) {
        faqService.saveQuestion(question, true, faqSetting);
        infoIject[1] += 1;
        stt = "Add new";
      } else {
        try {
          if(info.getMaxAtt() > faqService.getQuestionById(questionId).getAttachMent().size()){
            question.setPath(questionId);
            faqService.saveQuestion(question, false, faqSetting);
          }
        } catch (Exception e) {
          log.info("Failed to get attachments.");
        }
      }
      List<Answer> answers = findAnswers(info);
      for (Answer answer : answers) {
        isExist = faqService.isExisting(questionId + SLASH + Utils.ANSWER_HOME + SLASH + answer.getId());
        if (!isExist) {
          faqService.saveAnswer(questionId, answer, true);
          infoIject[2] += 1;
          as += 1;
        }
      }
      List<Comment> comments = findComments(info);
      for (Comment comment : comments) {
        isExist = faqService.isExisting(questionId + SLASH + Utils.COMMENT_HOME + SLASH + comment.getId());
        if (!isExist) {
          faqService.saveComment(questionId, comment, true);
          infoIject[3] += 1;
          cm += 1;
        }
      }
      log.info(String.format(" %s %s Question %s/%s  with %s new answer(s) and %s new comment(s) in %sms",
                             s, stt,++index_, size_, as, cm, (System.currentTimeMillis() - t1)));
    }
  }

  private void createCategory(String parentId, Category me, int currentDepth,int index, int size, InjectInfo info) throws Exception {
    initDataForOneCategory(parentId, me, currentDepth, index, size, info);
    List<Category> cats = findCategories(currentDepth+1, info);
    int index_ = 1, size_ = cats.size();
    for (Category cat : cats) {
      if (currentDepth + 1 < info.getDepth()) {
        createCategory(parentId + SLASH + me.getId(), cat, currentDepth + 1, index_, size_, info);
        index_++;
      }
    }
  }

  private void injectData(InjectInfo info) throws Exception {
    String parentId = KSDataLocation.Locations.FAQ_CATEGORIES_HOME;
    infoIject = new int[] { 0, 0, 0, 0 };
    log.info("Start inject data for answer ....");
    long time = System.currentTimeMillis();
    List<Category> cats = findCategories(0, info);
    int size = cats.size(), index = 1;
    for (Category cat : cats) {
      categoryIds.add(parentId + SLASH + cat.getId());
      createCategory(parentId, cat, 0, index, size, info);
      index++;
    }
    time = System.currentTimeMillis() - time;
    log.info(String.format("INJECTED : new categories=%s / new questions=%s / new answers=%s / new comments=%s / time=%sms", 
                           infoIject[0], infoIject[1], infoIject[2], infoIject[3], time));
    saveHistoryInject();
  }
  
  @Override
  public void inject(HashMap<String, String> queryParams) throws Exception {
    InjectInfo info = new InjectInfo(queryParams, getCategoryRoot(false));
    String type = info.getType();
    if (CONSTANTS.DATA.getName().equalsIgnoreCase(type) || 
        CONSTANTS.PERM.toString().equalsIgnoreCase(type)) {
      log.info(String.format("Injecting by type: %s ...", type));
      injectData(info);
    } else {
      log.info(String.format("Do not support type %s for injector...", type));
    }
  }

  private void removeData() throws Exception {
    try {
      if (categoryIds.isEmpty()) {
        categoryIds.addAll(getHistoryInject());
      }
      for (String categoryId : categoryIds) {
        faqService.removeCategory(categoryId);
      }
      log.info("Completely remove the datas inject !");
      categoryIds.clear();
      Category category = getCategoryRoot(false);
      category.setDescription("");
      faqService.saveCategory(null, category, false);
    } catch (Exception e) {
      log.warn("Failed to remove data injected....");
    }
  }

  @Override
  public void reject(HashMap<String, String> queryParams) throws Exception {
    log.info("Start remove data injected....");
    removeData();
  }

  private List<String> getHistoryInject() {
    try {
      return convertStringToList(getCategoryRoot(true).getDescription());
    } catch (Exception e) {
      return new ArrayList<String>();
    }
  }

  private void saveHistoryInject() {
    String s = getCategoryRoot(true).getDescription();
    if (s != null && s.trim().length() > 0) {
      categoryIds.addAll(convertStringToList(s));
    }
    Category category = getCategoryRoot(false);
    category.setDescription(categoryIds.toString());
    faqService.saveCategory(null, category, false);
  }

  public static List<String> convertStringToList(String s) {
    s = s.replace("[", "").replace("]", "");
    s = s.trim().replaceAll("(,\\s*)", ",").replaceAll("(\\s*,)", ",");
    String[] strs = s.split(",");
    return new ArrayList<String>(Arrays.asList(strs));
  }

  private Category newCategory(String catId, String catName, int order, InjectInfo info) {
    Category category = new Category();
    category.setId(catId);
    category.setName(catName);
    category.setDescription(randomWords(20));
    category.setIndex(order);
    category.setModerators(info.getPerCanEdit());
    category.setUserPrivate(info.getPerCanView());
    return category;
  }

  private Question newQuestion(String catId, String id, String name, InjectInfo info) {
    Question question = new Question();
    question.setId(id);
    question.setQuestion(name);
    question.setAuthor(randomUser());
    question.setCategoryId(catId);
    question.setCategoryPath(catId);
    question.setDetail(getStringResource(info));
    question.setEmail("noreply@exoplatform.com");
    question.setLanguage("English");
    question.setLink("");
    question.setTopicIdDiscuss("");
    question.setMarkVote(0.0);
    question.setRelations(new String[] { "" });
    question.setUsersWatch(new String[] { "" });
    question.setEmailsWatch(new String[] { "" });
    if (info.getMaxAtt() > 0) {
      try {
        question.setAttachMent(getFileAttachment(info));
      } catch (Exception e) {
        log.warn("Failed to set attachment in question.");
      }
    }
    return question;
  }

  private Answer newAnswer(String id, InjectInfo info) {
    String other = randomUser();
    Answer answer = new Answer(other, true);
    answer.setId(id);
    answer.setFullName(getFullName(other));
    answer.setLanguage("English");
    answer.setMarksVoteAnswer(0.0);
    answer.setMarkVotes(0);
    answer.setResponses(getStringResource(info));
    answer.setNew(true);
    return answer;
  }

  private Comment newComment(String id, InjectInfo info) {
    String other = randomUser();
    Comment comment = new Comment();
    comment.setId(id);
    comment.setCommentBy(other);
    comment.setComments(getStringResource(info));
    comment.setFullName(getFullName(other));
    comment.setPostId("");
    comment.setNew(true);
    return comment;
  }

  private String getFullName(String userName) {
    try {
      return fullNameData.get(userName);
    } catch (Exception e) {
      return "No Name";
    }
  }

  @Override
  public Object execute(HashMap<String, String> arg0) throws Exception {
    return new Object();
  }
  
  private String getStringResource(InjectInfo info) {
    if(info.getTxtCp() > 0) {
      return createTextResource(info.getTxtCp());
    }
    return randomParagraphs(3);
  }

  private List<FileAttachment> getFileAttachment(InjectInfo info) throws Exception {
    List<FileAttachment> listAttachments = new ArrayList<FileAttachment>();
    String rs = createTextResource(info.getAttCp());
    for (int i = 0; i < info.getMaxAtt(); i++) {
      InputStream stream = new ByteArrayInputStream(rs.getBytes("UTF-8"));
      FileAttachment fileAttachment = new FileAttachment();
      fileAttachment.setInputStream(stream);
      fileAttachment.setMimeType("text/plain");
      fileAttachment.setName("Attch_" + (i + 1) + ".txt");
      fileAttachment.setNodeName("Attch" + (i + 1) + ".txt");
      listAttachments.add(fileAttachment);
      stream.close();
    }
    return listAttachments;
  }
}
