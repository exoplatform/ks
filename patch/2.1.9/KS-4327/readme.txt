Summary
answers portlet - linking questions - non-member groups appear

CCP Issue:  CCP-1228 
Product Jira Issue: KS-4327.
Complexity: N/A

Impacted Client(s): SPFF
Proposal
 

Problem description
What is the problem to fix?

When linking a question in the answers portlet, ALL groups appear, even the ones the user is not a member of. This is very confusing and unuserfriendly.
To reproduce:
1- Try to create space with a moderator (jhon for exemple) and invite specific group or members( mary for exemple).
2- create another space with another moderator and other group or members ( for exemple (root and james)
3- in answers portlet try to submit questions ans response for each space.
4- when you submit a question with the moderator of one group and you try to link a question (the + button), all groups appear and their questions even ones the user is 
not member of and he can reponse on their questions.
expected behaviour: Expected behaviour : only groups of which the user is part of AND only these groups that contain questions are presented to the user.

Fix description
Problem analysis

The form UIAddRelationForm & UIGroupSelector can not check permission of user when use it when the AnswerPortlet & ForumPortlet in space. So, when open the form in space: 
+ The form UIAddRelationForm: All questions displayed in this from for select relation question => error. 
+ The UIGroupSelector all group displayed for in this form for select group/membership => error.
How is the problem fixed?

 For UIAddRelationForm: Check persimmon of user when display list questions for select relation question.
// old logic
     listCategory_.addAll(getFAQService().listingCategoryTree()) ;
// new logic
    List<String> listOfUser = UserHelper.getAllGroupAndMembershipOfUser(null);
   for (Cate cat : getFAQService().listingCategoryTree()) {
     if (hasPermission(cat, listOfUser)) {
        listCategory_.add(cat);
      }
     }
For UIGroupSelector : Set new property SpaceGroupId when open this form in portlet of space and display the list of groups is group and children group of space.
  public void setSpaceGroupId(String groupId) throws Exception {
   if (!ForumUtils.isEmpty(groupId)) {
      Group group = service.getGroupHandler().findGroupById(groupId);
     if (group != null) {
        this.spaceId = groupId;
        selectedGroup_ = new ArrayList<Group>();
        selectedGroup_.add(group);
        spaceParentId = group.getParentId();
        changeGroup(spaceParentId);
      }
    } else {
      setSelectedGroups(null);
    }
  }
Tests to perform
Reproduction test

cf. above
Tests performed at DevLevel

cf. above
Tests performed at Support Level

cf. above
Tests performed at QA
*

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

No
Changes in Selenium scripts 

No
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

No
Configuration changes
Configuration changes:

No 
Will previous configuration continue to work?

Yes 
Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: None
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?

No
Validation (PM/Support/QA) 
PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

N/A
