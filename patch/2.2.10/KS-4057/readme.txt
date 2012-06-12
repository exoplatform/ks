Summary
Impossible to open a wiki in a new tab from the wiki-tree 

CCP Issue: N/A
Product Jira Issue: KS-4057.
Complexity: N/A

Impacted Client(s): N/A 
Proposal
 

Problem description
What is the problem to fix?

When you are browsing your wiki content, it's impossible to open a page in a new tab/window of the browser from the wiki-tree.
Fix description
Problem analysis

This problem happened because the wiki tree doesn't support for opening wiki page on a new tab yet.
How is the problem fixed?

Fix in UITreeExplorer: change the action on each page of wiki tree to link. So user can right click to each page to open on new tab. 
Tests to perform
Reproduction test

cf. above
Tests performed at DevLevel

cf. above
Tests performed at Support Level

cf. above
Tests performed at QA

N/A
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
Data (template, node type) migration/upgrade: None
Is there a performance risk/cost?

No
Validation (PM/Support/QA)
PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

N/A
