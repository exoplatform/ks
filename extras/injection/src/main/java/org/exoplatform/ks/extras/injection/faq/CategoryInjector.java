package org.exoplatform.ks.extras.injection.faq;

import java.util.HashMap;

import org.exoplatform.faq.service.Category;

public class CategoryInjector extends AbstractFAQInjector {

  /** . */
  private static final String NUMBER = "number";
  
  /** . */
  private static final String CATEGORY_PREFIX = "catPrefix";

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    //
    int number = param(params, NUMBER);
    String categoryPrefix = params.get(CATEGORY_PREFIX);
    init(null, categoryPrefix, null, null, null, 0);
    
    //
    Category rootCategory = getCategoryRoot(true);
    String categoryName = null;
    Category cat = null;
    
    for(int i = 0; i < number; i++) {
      //
      categoryName = categoryName();
      
      //
      cat = new Category();
      cat.setIndex(i);
      cat.setName(categoryName);
      cat.setDescription(lorem.getParagraphs(1));
      cat.setModerators(new String[] {""});
      cat.setUserPrivate(new String[] {""});
      
      //
      faqService.saveCategory(rootCategory.getPath(), cat, true);
      categoryNumber++;
      
      //
      getLog().info("Category '" + categoryName + "' created with parent is '" + rootCategory.getId() + "'");
    }
  }
}
