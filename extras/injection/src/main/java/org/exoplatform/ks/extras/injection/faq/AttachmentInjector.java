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
package org.exoplatform.ks.extras.injection.faq;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.forum.service.Utils;

public class AttachmentInjector extends AbstractFAQInjector {
  
  /** . */
  private static final String NUMBER = "number";
  
  /** . */
  private static final String FROM_QUES = "fromQues";
  
  /** . */
  private static final String TO_QUES = "toQues";
  
  /** . */
  private static final String BYTE_SIZE = "byteSize";

  /** . */
  private static final String QUESTION_PREFIX = "quesPrefix";

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    //
    int number = param(params, NUMBER);
    int fromQues = param(params, FROM_QUES);
    int toQues = param(params, TO_QUES);
    String questionPrefix = params.get(QUESTION_PREFIX);
    init(null, null, questionPrefix, null, null, 0);
    
    //
    int byteSize = param(params, BYTE_SIZE);
    if (byteSize < 0 || byteSize > 99) {
      getLog().info("ByteSize is invalid with '" + byteSize + "' wrong. Please set it exactly in range 0 - 99 (words). Aborting injection ..." );
      return;
    }
    
    //
    String questionName = null;
    Question question = null;
    
    for (int i = fromQues; i <= toQues; ++i) {
      //
      questionName = questionBase + i;
      question = getQuestionByName(questionName);
      if (question == null) {
        getLog().info("Question name is '" + questionName + "' wrong. Aborting injection ..." );
        return;
      }
      
      //
      generateAttachments(question, QUESTION_PREFIX, number, byteSize);
      faqService.saveQuestion(question, false, faqSetting);
      
      //
      getLog().info("Uploads " + number + " attachments into '" + questionName + "' with each attachment's " + byteSize + " byte(s)");
    }
  }
  
  private void generateAttachments(Question question, String prefix, int number, int byteSize) throws Exception {
    //
    if (question.getAttachMent() == null || question.getAttachMent().size() == 0) {
      question.setAttachMent(new ArrayList<FileAttachment>());
    }
    int baseNumber = question.getAttachMent().size();
    
    //
    String rs = createTextResource(byteSize);
    String attId = null;
    FileAttachment att = null;
    
    for (int i = 0; i < number; i++) {
      //
      attId = generateId(prefix + baseNumber, Utils.ATTACHMENT, byteSize, i);
      att = new FileAttachment();
      att.setId(attId);
      att.setName(attId);
      att.setNodeName(Utils.ATTACHMENT + baseNumber);
      att.setInputStream(new ByteArrayInputStream(rs.getBytes("UTF-8")));
      att.setMimeType("text/plain");
      long fileSize = (long) byteSize * 1024;
      att.setSize(fileSize);
      question.getAttachMent().add(att);
      
      //
      baseNumber++;
    }
  }
  
  private String generateId(String prefix, String entity, int byteSize, int order) {
    StringBuilder sb = new StringBuilder();
    sb.append(entity)
      .append("-")
      .append(prefix)
      .append("_")
      .append(lorem.getCharacters(byteSize))
      .append("_")
      .append(order);
    return sb.toString();
  }
}
