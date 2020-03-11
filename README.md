# Gitlet
## Version-control system like Git.
Modeled after Git, Gitlet works as a backup system for related collections of files with the following functionality:
1. Saving the contents of entire directories of files. In Gitlet, this is called committing, and the saved contents themselves are called commits.
2. Restoring a version of one or more files or entire commits. In Gitlet, this is called checking out those files or that commit.
3. Viewing the history of your backups. In Gitlet, you view this history in something called the log.
4. Maintaining related sequences of commits, called branches.
5. Merging changes made in one branch into another.


>The following are descriptions of the files:
- Commit.java
  - Creates and serializes a commit of files.
- Gitlet.java
  - Repository class with init function to create a new Gitlet and contains all functions.
- GitletException.java
  - General exception indicating a Gitlet error.
- Main.java
  - Driver class for Gitlet.
- UnitTest.java
  - The suite of all JUnit tests for the gitlet package.
- Utils.java
  - Assorted utilities.
