/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.extras.injection;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.exoplatform.forum.extras.injection.utils.LoremIpsum4J;
import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
public class AttachmentInjector extends AbstractForumInjector {

  /** . */
  private static final String NUMBER = "number";

  /** . */
  private static final String FROM_POST = "fromPost";

  /** . */
  private static final String TO_POST = "toPost";
  
  /** . */
  private static final String POST_PREFIX = "postPrefix";

  /** . */
  private static final String BYTE_SIZE = "byteSize";
  
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    //
    int number = param(params, NUMBER);
    String postPrefix = params.get(POST_PREFIX);
    
    //
    int fromPost = param(params, FROM_POST);
    int toPost = param(params, TO_POST);
    int byteSize = param(params, BYTE_SIZE);
    
    lorem = new LoremIpsum4J();

    init(null, null, null, null, postPrefix, byteSize);

    //
    for (int i = fromPost; i <= toPost; ++i) {

      //
      String postName = postBase + i;
      Post post = getPostByName(postName);
      if (post == null) {
        getLog().info("post name is '" + postName + "' wrong. Please set it exactly. Aborting injection ..." );
        return;
      }
      Topic topic = getTopicByPostName(postName);
      Forum forum = getForumByTopicName(topic.getTopicName());
      Category cat = getCategoryByForumName(forum.getForumName());
      
      //
      
      generateAttachments(post, POST_PREFIX, number, byteSize);
      forumService.savePost(cat.getId(), forum.getId(), topic.getId(), post, false, new MessageBuilder());

      //
      getLog().info("Uploads " + number + " attachments into '" + postName + "' with each attachment's " + byteSize + " byte(s)");
    }
  }
  
  
  private void generateAttachments(Post post, String prefix, int number, int byteSize) throws Exception {
    if (post.getNumberAttach() == 0) {
      post.setAttachments(new ArrayList<ForumAttachment>());
    }
    int baseNumber = (int) post.getNumberAttach() ;
    
    //
    String rs = createTextResource(byteSize);    
    for (int i = 0; i < number; i++) {
      String attId = generateId(prefix + baseNumber, Utils.ATTACHMENT, byteSize, i);
      BufferAttachment att = new BufferAttachment();
      att.setId(attId);
      att.setName(attId);
      att.setInputStream(new ByteArrayInputStream(rs.getBytes("UTF-8")));
      att.setMimeType("text/plain");
      long fileSize = (long) byteSize * 1024;
      att.setSize(fileSize);
      post.getAttachments().add(att);
      baseNumber++;
    }
    
    //
    post.setNumberAttach(baseNumber);
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
