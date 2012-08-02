/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.webui.popup;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.webui.BaseUIFAQForm;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.ks.common.image.ResizeImageService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.input.UIUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Apr 29, 2008 ,9:41:42 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIAttachmentForm.gtmpl", 
    events = {
        @EventConfig(listeners = UIAttachmentForm.SaveActionListener.class), 
        @EventConfig(listeners = UIAttachmentForm.CancelActionListener.class) 
    }
)
public class UIAttachmentForm extends BaseUIFAQForm implements UIPopupComponent {
  private static final String FILE_UPLOAD         = "FileUpload";

  final private static int    fixWidthImage       = 200;

  private boolean             isChangeAvatar      = false;
  
  private int                 numberOfUploadInput = 0;
  
  public int getNumberOfUploadInput() {
    return numberOfUploadInput;
  }

  public void setIsChangeAvatar(boolean changeAvatar) {
    this.isChangeAvatar = changeAvatar;
  }

  public void setNumberUpload(int number) {
    this.numberOfUploadInput = number;
    int sizeLimit = FAQUtils.getLimitUploadSize(isChangeAvatar);
    UIUploadInput uploadInput = new UIUploadInput(FILE_UPLOAD, FILE_UPLOAD, number, sizeLimit);
    addUIFormInput(uploadInput);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public UIAttachmentForm() {
    this.setRendered(false);
  }

  static public class SaveActionListener extends EventListener<UIAttachmentForm> {
    public void execute(Event<UIAttachmentForm> event) throws Exception {
      UIAttachmentForm attachMentForm = event.getSource();
      List<FileAttachment> listFileAttachment = new ArrayList<FileAttachment>();
      UploadService uploadService = attachMentForm.getApplicationComponent(UploadService.class);
      UIUploadInput input = (UIUploadInput) attachMentForm.getUIInput(FILE_UPLOAD);
      long size = 0;
      for (UploadResource uploadResource : input.getUploadResources()) {
        if (uploadResource == null) {
          continue;
        }
        String fileName = uploadResource.getFileName();
        if (fileName == null || fileName.equals(StringUtils.EMPTY)) {
          continue;
        }
        InputStream stream = new FileInputStream(new File(uploadResource.getStoreLocation()));
        if (attachMentForm.isChangeAvatar) {
          if (uploadResource.getMimeType().indexOf("image") < 0) {
            attachMentForm.warning("UIAttachmentForm.msg.fileIsNotImage");
            uploadService.removeUploadResource(uploadResource.getUploadId());
            return;
          }
          ResizeImageService resizeImgService = (ResizeImageService) attachMentForm.getApplicationComponent(ResizeImageService.class);
          stream = resizeImgService.resizeImageByWidth(fileName, stream, fixWidthImage);
        }
        size = (long) uploadResource.getUploadedSize();
        if (size > 0) {
          String fileExtenstion = StringUtils.EMPTY;
          int indexOfDot = uploadResource.getFileName().lastIndexOf(".");
          if (indexOfDot > -1) {
            fileExtenstion = uploadResource.getFileName().substring(indexOfDot);
          }
          FileAttachment fileAttachment = new FileAttachment();
          fileAttachment.setName(uploadResource.getFileName());
          fileAttachment.setInputStream(stream);
          fileAttachment.setMimeType((attachMentForm.isChangeAvatar) ? "image/png" : uploadResource.getMimeType());
          fileAttachment.setSize(size);
          fileAttachment.setId("file" + IdGenerator.generate());
          fileAttachment.setNodeName(IdGenerator.generate() + fileExtenstion);
          listFileAttachment.add(fileAttachment);
        } else {
          attachMentForm.warning("UIAttachmentForm.msg.size-of-file-is-0", new String[] { uploadResource.getFileName() });
          uploadService.removeUploadResource(uploadResource.getUploadId());
          return;
        }
        uploadService.removeUploadResource(uploadResource.getUploadId());
      }

      if (listFileAttachment.isEmpty()) {
        attachMentForm.warning("UIAttachmentForm.msg.file-not-found");
        return;
      }

      UIAnswersPortlet portlet = attachMentForm.getAncestorOfType(UIAnswersPortlet.class);
      if (attachMentForm.isChangeAvatar) {
        String currentUser = FAQUtils.getCurrentUser();
        attachMentForm.getFAQService().saveUserAvatar(currentUser, listFileAttachment.get(0));
        UISettingForm settingForm = portlet.findFirstComponentOfType(UISettingForm.class);
        settingForm.setAvatarUrl(FAQUtils.getUserAvatar(currentUser));
        event.getRequestContext().addUIComponentToUpdateByAjax(settingForm);
      } else {
        UIQuestionForm questionForm = portlet.findFirstComponentOfType(UIQuestionForm.class);
        questionForm.setListFileAttach(listFileAttachment);
        questionForm.refreshUploadFileList();
        event.getRequestContext().addUIComponentToUpdateByAjax(questionForm);
      }
      attachMentForm.cancelChildPopupAction();
    }
  }

  static public class CancelActionListener extends EventListener<UIAttachmentForm> {
    public void execute(Event<UIAttachmentForm> event) throws Exception {
      UIAttachmentForm attachMentForm = event.getSource();
      // remove temp file in upload service and server
      UploadService uploadService = attachMentForm.getApplicationComponent(UploadService.class);
      UIUploadInput input = (UIUploadInput) attachMentForm.getUIInput(FILE_UPLOAD);
      for (UploadResource uploadResource : input.getUploadResources()) {
        if (uploadResource == null) {
          continue;
        }
        uploadService.removeUploadResource(uploadResource.getUploadId());
      }
      attachMentForm.cancelChildPopupAction();
    }
  }
}
