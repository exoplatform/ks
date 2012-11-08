/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.faq.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.exoplatform.commons.utils.ISO8601;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class FAQEventQueryTestCase extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testQuickSearch() throws Exception {
    FAQEventQuery queryObject = new FAQEventQuery();

    /*
     * eventQuery.setAdmin(uiQuickSearch.faqSetting_.isAdmin()) ; eventQuery.setUserMembers(UserHelper.getAllGroupAndMembershipOfUser(FAQUtils.getCurrentUser())); eventQuery.setUserId(FAQUtils.getCurrentUser()) ; eventQuery.setText(text); eventQuery.setType("categoryAndQuestion");
     */

    queryObject.setPath("/foo");
    queryObject.setType(FAQEventQuery.CATEGORY_AND_QUESTION);
    queryObject.setAdmin(true);
    String selector = "/jcr:root/foo//*";

    assertEquals(selector + "[]", queryObject.getQuery());
    queryObject.setText("bar");
    assertEquals(selector + "[ jcr:contains(., 'bar')]", queryObject.getQuery());

    queryObject.setAdmin(false);
    String predicate = "jcr:contains(., 'bar') and ( not(@exo:isApproved) or @exo:isApproved='true' )";
    assertEquals(selector + "[ " + predicate + " ]", queryObject.getQuery());

    queryObject.setUserId("zed");
    predicate = "jcr:contains(., 'bar') and ( not(@exo:isApproved) or @exo:isApproved='true' or exo:author='zed' )";
    assertEquals(selector + "[ " + predicate + " ]", queryObject.getQuery());

    queryObject.setViewingCategories(Arrays.asList("cat1"));

    predicate += "  and (@exo:categoryId='cat1' or @exo:id='cat1' and ( @exo:userPrivate='' ) )";
    assertEquals(selector + "[ " + predicate + "]", queryObject.getQuery());

  }

  public void testBuildCategoryQuery() throws Exception {
    final FAQEventQuery queryObject = new FAQEventQuery();
    queryObject.setPath("/foo");
    queryObject.setType(FAQEventQuery.FAQ_CATEGORY);
    queryObject.setAdmin(true);
    String selector = "/jcr:root/foo//element(*,exo:faqCategory)[(@exo:isView='true') ";
    assertEquals(selector + "]", queryObject.getQuery());

    queryObject.setText("bar");
    String predicate = " and (jcr:contains(., 'bar'))";
    assertEquals(selector + predicate + "]", queryObject.getQuery());

    queryObject.setName("zed");
    predicate += " and (jcr:contains(@exo:name, 'zed'))";
    assertEquals(selector + predicate + "]", queryObject.getQuery());

    queryObject.setIsModeQuestion("blah");
    predicate += " and (@exo:isModerateQuestions='blah')";
    assertEquals(selector + predicate + "]", queryObject.getQuery());

    queryObject.setModerator("john");
    predicate += " and (jcr:contains(@exo:moderators, 'john'))";
    assertEquals(selector + predicate + "]", queryObject.getQuery());

    queryObject.setAdmin(false);
    queryObject.setUserMembers(Arrays.asList("jack", "jerry"));
    predicate += " and (not(@exo:userPrivate) or @exo:userPrivate='' or @exo:userPrivate='jack' or @exo:moderators='jack' or @exo:userPrivate='jerry' or @exo:moderators='jerry')";
    assertEquals(selector + predicate + "]", queryObject.getQuery());
  }

  public void testBuildQuestionQuery() throws Exception {
    String selector = "/jcr:root/foo//* [(";
    String predicate;
    final FAQEventQuery eventQuery = new FAQEventQuery();
    eventQuery.setType(FAQEventQuery.FAQ_QUESTION);
    eventQuery.setPath("/foo");
    eventQuery.setAuthor("root");
    predicate = "jcr:contains(@exo:author, 'root')";
    assertEquals(selector + predicate + ")]".trim(), eventQuery.getQuery().trim());

    eventQuery.setEmail("root@exoplatform");
    predicate += " and jcr:contains(@exo:email, 'root@exoplatform')";
    assertEquals(selector + predicate + ")]".trim(), eventQuery.getQuery().trim());

    Calendar calendar = GregorianCalendar.getInstance();
    eventQuery.setFromDate(calendar);
    predicate += " and ((@exo:createdDate >= xs:dateTime('" + ISO8601.format(calendar) + "')) " + "or (@exo:dateResponse >= xs:dateTime('" + ISO8601.format(calendar) + "')) " + "or (@exo:dateComment >= xs:dateTime('" + ISO8601.format(calendar) + "')))";
    assertEquals(selector + predicate + ")]".trim(), eventQuery.getQuery().trim());

    calendar = GregorianCalendar.getInstance();
    eventQuery.setToDate(calendar);
    predicate += " and ((@exo:createdDate <= xs:dateTime('" + ISO8601.format(calendar) + "')) " + "or (@exo:dateResponse <= xs:dateTime('" + ISO8601.format(calendar) + "')) " + "or (@exo:dateComment <= xs:dateTime('" + ISO8601.format(calendar) + "')))";
    assertEquals(selector + predicate + ")]".trim(), eventQuery.getQuery().trim());

    eventQuery.setLanguage("English");
    eventQuery.setResponse("response");
    predicate += ") and (( exo:responseLanguage='English' and jcr:contains(@exo:responses,'response'))";
    assertEquals(selector + predicate + ")]".trim(), eventQuery.getQuery().trim());

    eventQuery.setComment("comment");
    predicate += " or ( exo:commentLanguage='English' and jcr:contains(@exo:comments,'comment'))";
    assertEquals(selector + predicate + ")]".trim(), eventQuery.getQuery().trim());

    /*
     * String tempPredicate = predicate; eventQuery.setText("") ; //tempPredicate += ") and (exo:language='English')"; assertEquals(selector + tempPredicate + "]", eventQuery.getQuery());
     */

    eventQuery.setText("text");

    predicate += " or (jcr:contains(., 'text') and (  exo:language='English' or exo:commentLanguage='English' or exo:responseLanguage='English'))";
    assertEquals(selector + predicate + " )]".trim(), eventQuery.getQuery().trim());

    eventQuery.setViewingCategories(Arrays.asList("categoryId1", "categoryId2"));
    predicate += ") and (exo:categoryId='categoryId1' or exo:categoryId='categoryId2')";

    assertEquals((selector + predicate + "]").trim(), eventQuery.getQuery().trim());

    eventQuery.setUserId("root");
    eventQuery.setAdmin(true);
  }

}
