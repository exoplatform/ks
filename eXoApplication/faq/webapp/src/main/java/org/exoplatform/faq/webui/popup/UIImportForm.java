package org.exoplatform.faq.webui.popup;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.upload.UploadService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormUploadInput;

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIImportForm.gtmpl", 
    events = {
        @EventConfig(listeners = UIImportForm.SaveActionListener.class), 
        @EventConfig(listeners = UIImportForm.CancelActionListener.class) 
    }
)
public class UIImportForm extends BaseUIForm implements UIPopupComponent {
  private final String FILE_UPLOAD = "FileUpload";

  private String       categoryId_;

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public UIImportForm() {
    this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD, true));
  }

  public void setCategoryId(String categoryId) {
    this.categoryId_ = categoryId;
  }

  static public class SaveActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm importForm = event.getSource();
      FAQService service = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);
      UIAnswersPortlet portlet = importForm.getAncestorOfType(UIAnswersPortlet.class);
      try {
        service.getCategoryById(importForm.categoryId_);

        UIFormUploadInput uploadInput = (UIFormUploadInput) importForm.getChildById(importForm.FILE_UPLOAD);

        if (uploadInput.getUploadResource() == null) {
          importForm.warning("UIAttachmentForm.msg.file-not-found");
          return;
        }
        String fileName = uploadInput.getUploadResource().getFileName();
        MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
        String mimeType = mimeTypeResolver.getMimeType(fileName);
        boolean isZip = false;
        if ("application/zip".equals(mimeType)) {
          isZip = true;
        } else if ("text/xml".equals(mimeType)) {
          isZip = false;
        } else {
          importForm.warning("UIImportForm.msg.mimetype-invalid");
          return;
        }
        try {
          service.importData(importForm.categoryId_, uploadInput.getUploadDataAsStream(), isZip);
          importForm.info("UIImportForm.msg.import-successful", false);
        } catch (AccessDeniedException ace) {
          importForm.warning("UIImportForm.msg.access-denied", false);
        } catch (ConstraintViolationException con) {
          importForm.warning("UIImportForm.msg.constraint-violation-exception", false);
        } catch (ItemExistsException ise) {
          importForm.warning("UIImportForm.msg.CategoryIsExist", false);
        } catch (Exception e) {
          importForm.warning("UIImportForm.msg.import-fail", false);
        }

        UploadService uploadService = importForm.getApplicationComponent(UploadService.class);
        uploadService.removeUploadResource(uploadInput.getUploadId());
      } catch (Exception e) {
        FAQUtils.findCateExist(service, portlet.findFirstComponentOfType(UIAnswersContainer.class));
        importForm.warning("UIQuestions.msg.admin-moderator-removed-action", false);
        event.getRequestContext().addUIComponentToUpdateByAjax(portlet);
      }

      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIAnswersContainer faqContainer = portlet.getChild(UIAnswersContainer.class);
      faqContainer.getChild(UIQuestions.class).setDefaultLanguage();
      event.getRequestContext().addUIComponentToUpdateByAjax(faqContainer);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm importForm = event.getSource();
      UIAnswersPortlet portlet = importForm.getAncestorOfType(UIAnswersPortlet.class);
      // remove temp file in upload service and server
      UploadService uploadService = importForm.getApplicationComponent(UploadService.class);
      UIFormUploadInput uploadInput = (UIFormUploadInput) importForm.getChildById(importForm.FILE_UPLOAD);
      uploadService.removeUploadResource(uploadInput.getUploadId());
      portlet.cancelAction();
    }
  }
}
