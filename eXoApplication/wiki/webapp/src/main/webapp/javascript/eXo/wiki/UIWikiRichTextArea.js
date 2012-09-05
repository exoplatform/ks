/**
 * Copyright (C) 2011 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

function UIWikiRichTextArea() {
};

UIWikiRichTextArea.prototype.init = function(hookId, inputURL, syntax, wiki,
    space, page) {  
  var me = eXo.wiki.UIWikiRichTextArea;
  me.hookId = hookId;
  me.inputURL = inputURL;
  me.syntax = syntax;
  me.wiki = wiki;
  me.space = space;
  me.page = page;
  if(eXo.wiki.WysiwygEditor){
    try{
      eXo.wiki.WysiwygEditor.release();
    }catch(e){
      if(window.console){
        window.console.error(e);
      }
    }
  };
  
  try{
    if(WysiwygEditor){
      me.createWysiwygEditor();
    }
  }catch(e){
    eXo.wiki.Wysiwyg.onModuleLoad(me.createWysiwygEditor);
  }
};

UIWikiRichTextArea.prototype.createWysiwygEditor = function() {
  var me = eXo.wiki.UIWikiRichTextArea;
  eXo.wiki.WysiwygEditor = new WysiwygEditor({
    hookId: me.hookId,
    syntax: me.syntax,
    convertInput : 'true',
    inputURL: me.inputURL,
    defaultEditor : 'wysiwyg',
    plugins: 'submit readonly line separator embed text valign justify list indent history format font color symbol table link image macro import',
    menu : '[{\"feature\": \"link\", \"subMenu\":[\"linkEdit\", \"linkRemove\", \"linkWikiPage\", \"linkAttachment\", \"|\", \"linkWebPage\", \"linkEmail\"]}, {\"feature\":\"image\", \"subMenu\":[\"imageInsertAttached\", \"imageInsertURL\", \"imageEdit\", \"imageRemove\"]}, {\"feature\":\"table\", \"subMenu\":[\"inserttable\", \"insertcolbefore\", \"insertcolafter\", \"deletecol\", \"|\", \"insertrowbefore\", \"insertrowafter\", \"deleterow\", \"|\", \"deletetable\"]}, {\"feature\":\"macro\", \"subMenu\":[\"macroInsert\", \"macroEdit\", \"|\", \"macroRefresh\", \"|\", \"macroCollapse\", \"macroExpand\"]}, {\"feature\":\"import\", \"subMenu\":[\"importOffice\"]}]',
    toolbar: 'bold italic underline strikethrough teletype | subscript superscript | justifyleft justifycenter justifyright justifyfull | unorderedlist orderedlist | outdent indent | undo redo | format | fontname fontsize forecolor backcolor | hr removeformat symbol',
    allowExternalImages : 'true',
    colors : '#000000, #444444, #666666, #999999, #CCCCCC, #EEEEEE, #F3F3F3, #FFFFFF,\n#FF0000, #FF9900, #FFFF00, #00FF00, #00FFFF, #0000FF, #9900FF, #FF00FF,\n#F4CCCC, #FCE5CD, #FFF2CC, #D9EAD3, #D0E0E3, #CFE2F3, #D9D2E9, #EAD1DC,\n#EA9999, #F9CB9C, #FFE599, #B6D7A8, #A2C4C9, #9FC5E8, #B4A7D6, #D5A6BD,\n#E06666, #F6B26B, #FFD966, #93C47D, #76A5AF, #6FA8DC, #8E7CC3, #C27BA0,\n#CC0000, #E69138, #F1C232, #6AA84F, #45818E, #3D85C6, #674EA7, #A64D79,\n#990000, #B45F06, #BF9000, #38761D, #134F5C, #0B5394, #351C75, #741B47,\n#660000, #783F04, #7F6000, #274E13, #0C343D, #073763, #20124D, #4C1130',
    colorsPerRow : '8',
    fontNames : 'Andale Mono, Arial, Arial Black, Book Antiqua, Comic Sans MS, Courier New, Georgia, Helvetica, Impact, Symbol, Tahoma, Terminal, Times New Roman, Trebuchet MS, Verdana, Webdings, Wingdings',
    fontSizes : '8px 10px 12px 14px 18px 24px 36px',
    styleNames : '[]',
    wiki: me.wiki,
    space: me.space,
    page: me.page,
    cacheId: 'wysiwygCache0Na5'
  });
};

eXo.wiki.UIWikiRichTextArea = new UIWikiRichTextArea();