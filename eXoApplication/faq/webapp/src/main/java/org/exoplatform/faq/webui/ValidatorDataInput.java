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
package org.exoplatform.faq.webui;


/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * May 3, 2008 ,1:48:11 AM
 */
public class ValidatorDataInput {

  public static boolean fckContentIsNotEmpty(String input) {
    if (input == null || input.trim().length() < 1)
      return false;
    input = input.replaceAll("<p>", "");
    input = input.replaceAll("</p>", "");
    input = input.replaceAll("&nbsp;", " ");
    if (input.trim().length() > 0) {
      return true;
    } else {
      return false;
    }
  }
}
