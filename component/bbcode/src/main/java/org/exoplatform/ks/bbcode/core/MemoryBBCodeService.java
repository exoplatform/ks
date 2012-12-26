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
package org.exoplatform.ks.bbcode.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.api.BBCodeService;
import org.exoplatform.ks.bbcode.spi.BBCodePlugin;

/**
 * Implementation of BBCodeService that manages BBCodes in memory
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MemoryBBCodeService implements BBCodeService {

  private Map<String, BBCode> bbcodes;

  public MemoryBBCodeService() {
    bbcodes = new HashMap<String, BBCode>();
  }

  public void delete(String bbcodeId) throws Exception {
    bbcodes.remove(bbcodeId);
  }

  public BBCode findById(String bbcodeId) throws Exception {
    return bbcodes.get(bbcodeId);
  }

  public List<String> getActive() throws Exception {
    List<String> result = new ArrayList<String>();
    Iterator<BBCode> it = bbcodes.values().iterator();
    while (it.hasNext()) {
      BBCode bbCode = (BBCode) it.next();
      if (bbCode.isActive()) {
        result.add(bbCode.getId());
      }
    }
    return result;
  }

  public List<BBCode> getAll() throws Exception {
    return new ArrayList<BBCode>(bbcodes.values());
  }

  public void registerBBCodePlugin(BBCodePlugin plugin) throws Exception {
    throw new UnsupportedOperationException("registering a BBCode plugin is not supported by " + getClass());
  }

  public void save(List<BBCode> bbcodes) throws Exception {
    for (BBCode bbCode : bbcodes) {
      addBBCode(bbCode);
    }
  }

  public void addBBCode(BBCode code) {
    code.setId(code.getTagName() + ((code.isOption()) ? "=" : ""));
    this.bbcodes.put(code.getId(), code);
  }

  @Override
  public List<BBCode> getBBCodeActive() throws Exception {
    List<BBCode> result = new ArrayList<BBCode>();
    Iterator<BBCode> it = bbcodes.values().iterator();
    while (it.hasNext()) {
      BBCode bbCode = (BBCode) it.next();
      if (bbCode.isActive()) {
        result.add(bbCode);
      }
    }
    return result;
  }

}
