package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/** Repository class.
 * @author neal sharma*/
public class Gitlet implements Serializable {

    /** Init creates new Gitlet. */
    public Gitlet() {
        _stage = new HashMap<>();
        _untracked = new ArrayList<>();
        File gitlet = new File(".gitlet");
        File commits = new File(".gitlet/commits");
        File staging = new File(".gitlet/staging");
        gitlet.mkdir();
        commits.mkdir();
        staging.mkdir();
        Commit i = Commit.initialize();
        File initialFile = new File(".gitlet/commits/" + i.getHashID());
        _head = "master";
        _branches = new HashMap<>();
        _branches.put("master", i.getHashID());
        Utils.writeContents(initialFile, Utils.serialize(i));
        array1 = new int[]{0};
        array2 = new int[]{0};
        array3 = new int[]{0};
        array4 = new int[]{0};
    }

    /** Log. */
    public void log() {
        String pointer = getHeadPosition();
        while (pointer != null) {
            Commit commit = idtoCommit(pointer);
            helpPrintCommit(commit);
            pointer = commit.getFirstParentID();
        }
    }

    /** Print commit. */
    /** @param comm */
    public void helpPrintCommit(Commit comm) {
        String hashVal = comm.getHashID();
        System.out.println("===");
        System.out.println("commit " + hashVal);
        System.out.println("Date: " + comm.getTimestamp());
        System.out.println(comm.getMessage());
        System.out.println();
    }

    /** Add. */
    /** @param fileName */
    public void add(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        if (_untracked.contains(fileName)) {
            _untracked.remove(fileName);
        }
        String hash = Utils.sha1(Utils.readContentsAsString(file));
        File blobStage = new File(".gitlet/staging/"
                + hash);
        HashMap<String, String> blobs =
                idtoCommit(getHeadPosition()).getBlobs();
        if (blobs == null || !blobs.containsKey(fileName)
                || !blobs.get(fileName).equals(hash)) {
            _stage.put(fileName, hash);
            Utils.writeContents(blobStage, Utils.readContentsAsString(file));
        } else if (blobStage.exists()) {
            _stage.remove(fileName);
        }

    }


    /** Commit. */
    /** @param message */
    public void commit(String message) {
        if (message.trim().equals("")) {
            throw new GitletException("Please enter a commit message.");
        }
        if (_stage.size() == 0 && _untracked.size() == 0) {
            throw new GitletException("No changes added to the commit.");
        }
        HashMap<String, String> tracked =
                idtoCommit(getHeadPosition()).getBlobs();
        if (tracked == null) {
            tracked = new HashMap<>();
        }
        for (String file : _stage.keySet()) {
            tracked.put(file, _stage.get(file));
        }
        for (String file : _untracked) {
            tracked.remove(file);
        }
        String[] p = new String[]{idtoCommit(getHeadPosition()).getHashID()};
        Commit newCommit = new Commit(true, message, p, tracked);
        Utils.writeObject(new File(".gitlet/commits/"
                + newCommit.getHashID()), newCommit);
        _stage.clear();
        _untracked.clear();
        _branches.put(_head, newCommit.getHashID());
    }

    /** Commit for merge. */
    /** @param message message
     *  @param mergeparents */
    public void commit(String message, String[] mergeparents) {
        if (message.trim().equals("")) {
            throw new GitletException("Please enter a commit message.");
        }
        if (_stage.size() == 0 && _untracked.size() == 0) {
            throw new GitletException("No changes added to the commit.");
        }
        HashMap<String, String> tracked =
                idtoCommit(getHeadPosition()).getBlobs();
        if (tracked == null) {
            tracked = new HashMap<>();
        }

        for (String file : _stage.keySet()) {
            tracked.put(file, _stage.get(file));
        }
        for (String file : _untracked) {
            tracked.remove(file);
        }

        Commit newCommit =
                new Commit(true, message, mergeparents, tracked);
        Utils.writeObject(new File(".gitlet/commits/"
                + newCommit.getHashID()), newCommit);
        _stage.clear();
        _untracked.clear();
        _branches.put(_head, newCommit.getHashID());
    }

    /** Checkout. */
    /** @param args */
    public void checkout(String[] args) {
        String commitID, fileName;
        if (args.length == 3 && args[1].equals("--")) {
            commitID = args[0]; fileName = args[2];
        } else if (args.length == 2 && args[0].equals("--")) {
            fileName = args[1]; commitID = getHeadPosition();
        } else {
            throw new GitletException("Incorrect operands.");
        }
        Commit c = idtoCommit(shortID(commitID));
        HashMap<String, String> t = c.getBlobs();
        if (t.containsKey(fileName)) {
            String blobName = ".gitlet/staging/" + t.get(fileName);
            String content = Utils.readContentsAsString(new File(blobName));
            Utils.writeContents(new File(fileName), content);
        } else {
            throw new GitletException("File does not exist in that commit.");
        }
    }

    /** For the branch checkout. */
    /** @param branch */
    public void checkout(String branch) {
        if (!_branches.containsKey(branch)) {
            throw new GitletException("No such branch exists.");
        } else if (_head.equals(branch)) {
            throw new
                    GitletException("No need to checkout the current branch.");
        }
        String commitID = _branches.get(branch);
        HashMap<String, String> files =
                idtoCommit(commitID).getBlobs();
        File user =
                new File(System.getProperty("user.dir"));
        untrackedCheck(user);
        for (File file : user.listFiles()) {
            if (files != null) {
                boolean flag = files.containsKey(file.getName());
                if (!(flag || file.getName().equals(".gitlet"))) {
                    Utils.restrictedDelete(file);
                }
            } else {
                Utils.restrictedDelete(file);
            }
        }
        if (files != null) {
            for (String f : files.keySet()) {
                Utils.writeContents(new File(f),
                        Utils.readContentsAsString
                                (new File(".gitlet/staging/" + files.get(f))));
            }
        }
        _stage.clear();
        _untracked.clear();
        _head = branch;
    }

    /** Gets short ID. */
    /** @param commitID tag
     * @return */
    private String shortID(String commitID) {
        if (commitID.length() == Utils.UID_LENGTH) {
            return commitID;
        } else {
            File folder = new File(".gitlet/commits");
            for (File file : folder.listFiles()) {
                if (file.getName().contains(commitID)) {
                    return file.getName();
                }
            }
            throw new GitletException("No commit with that id exists.");
        }
    }

    /** Gets commit from ID. */
    /** @return
     * @param hashVal hash value*/
    public Commit idtoCommit(String hashVal) {
        if (new File(".gitlet/commits/" + hashVal).exists()) {
            return Utils.readObject(new File(".gitlet/commits/"
                    + hashVal), Commit.class);
        } else {
            throw new GitletException("No commit with that id exists.");
        }
    }

    /** Status. */
    public void status() {
        Object[] branch = sorter(_branches);
        Object[] stage = sorter(_stage);
        System.out.println("=== Branches ===");
        int i = 0;
        while (i < branch.length) {
            if (!branch[i].equals(_head)) {
                System.out.println(branch[i]);
            } else {
                System.out.println("*" + branch[i]);
            }
            i++;
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        int j = 0;
        while (j < stage.length) {
            System.out.println(stage[j]);
            j++;
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        if (!_untracked.isEmpty()) {
            Object[] untracked = _untracked.toArray();
            Arrays.sort(untracked);
            int k = 0;
            while (k < untracked.length) {
                System.out.println(untracked[k]);
                k++;
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Sorter of arrays helper function. */
    /** @return
     * @param values values */
    private Object[] sorter(HashMap<String, String> values) {
        Object[]result = values.keySet().toArray();
        Arrays.sort(result);
        return result;
    }

    /** Checks to see if untracked files will stop checkout and merge. */
    /** @param f file*/
    private void untrackedCheck(File f) {
        HashMap<String, String> tracked
                = idtoCommit(getHeadPosition()).getBlobs();
        for (File file : f.listFiles()) {
            if (tracked != null) {
                boolean flag1 = _stage.containsKey(file.getName());
                boolean flag2 = tracked.containsKey(file.getName());
                if (!flag2 && !file.getName().equals(".gitlet") && !flag1) {
                    throw new GitletException("There is an untracked "
                            + "file in the way; delete it or add it first.");
                }
            } else if (f.listFiles().length > 1) {
                throw new GitletException("There is an untracked "
                + "file in the way; delete it or add it first.");
            }

        }
    }

    /** Global Log. */
    public void globalLog() {
        for (File file : new File(".gitlet/commits").listFiles()) {
            helpPrintCommit(idtoCommit(file.getName()));
        }
    }

    /** Remove. */
    /** @param fileName fileName */
    public void rm(String fileName) {
        File file = new File(fileName);
        HashMap<String, String> tracked =
                idtoCommit(getHeadPosition()).getBlobs();
        if (!(file.exists() || tracked.containsKey(fileName))) {
            throw new GitletException("File does not exist.");
        }

        if (_stage.containsKey(fileName)) {
            _stage.remove(fileName);
        } else if (tracked != null && tracked.containsKey(fileName)) {
            _untracked.add(fileName);
            Utils.restrictedDelete(new File(fileName));
        } else {
            throw new GitletException("No reason to remove the file.");
        }
    }

    /** Find. */
    /** @param message message */
    public void find(String message) {
        File[] commits = new File(".gitlet/commits").listFiles();
        boolean notfound = true;
        for (File file : commits) {
            if (idtoCommit(file.getName()).getMessage().equals(message)) {
                System.out.println(file.getName());
                notfound = false;
            }
        }
        if (notfound) {
            throw new GitletException("Found no commit with that message.");
        }
    }

    /** Branch. */
    /** @param br branch */
    public void branch(String br) {
        if (_branches.containsKey(br)) {
            throw new
                    GitletException("A branch with that name already exists.");
        } else {
            _branches.put(br, getHeadPosition());
        }
    }

    /** Rm-branch. */
    /** @param branch branch */
    public void rmbranch(String branch) {
        if (_head.equals(branch)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        if (!_branches.containsKey(branch)) {
            throw new
                    GitletException("A branch with that name does not exist.");
        }
        _branches.remove(branch);
    }

    /** Reset. */
    /** @param id ID*/
    public void reset(String id) {
        HashMap<String, String> files = idtoCommit(shortID(id)).getBlobs();
        File fileex = new File(System.getProperty("user.dir"));
        untrackedCheck(fileex);
        for (String file : files.keySet()) {
            Utils.writeContents(new File(file),
                    Utils.readContentsAsString(
                            new File(".gitlet/staging/" + files.get(file))));
        }
        for (File file : fileex.listFiles()) {
            if (!files.containsKey(file.getName())) {
                Utils.restrictedDelete(file);
            }
        }
        _stage.clear();
        _branches.put(_head, id);
    }


    /** Merge. */
            /** Split point NEAL. */
    /** @param currentBranch current branch
     * @param givenBranch given branch
     * @return */
    private String splitPoint(String currentBranch, String givenBranch) {
        ArrayList<String> currCommits, givenCommits, splitPoints;
        currCommits = getAncestors(currentBranch);
        givenCommits = getAncestors(givenBranch);
        splitPoints = new ArrayList<>();
        for (String splitPoint : currCommits) {
            if (givenCommits.contains(splitPoint)) {
                splitPoints.add(splitPoint);
            }
        }
        if (splitPoints.isEmpty()) {
            return "";
        } else {
            return splitPoints.get(0);
        }
    }

    /** Modified file between branches NEAL. */
    /** @return
     * @param branch1 first branch
     * @param branch2 second branch
     * @param fileName file    */
    boolean modified(HashMap<String, String> branch1,
                     HashMap<String, String> branch2, String fileName) {
        if (!branch1.containsKey(fileName) && !branch2.containsKey(fileName)) {
            return false;
        }
        if (branch1.containsKey(fileName) && branch2.containsKey(fileName)) {
            if (!branch1.get(fileName).equals(branch2.get(fileName))) {
                return true;
            }
        } else if (branch1.containsKey(fileName)
                || branch2.containsKey(fileName)) {
            return true;
        }
        return false;
    }

    /** Ancestors. */
    /** @param branch branch
     * @return */
    private ArrayList<String> getAncestors(String branch) {
        ArrayList<String> result = new ArrayList<>();
        String parent = _branches.get(branch);
        while (parent != null) {
            result.add(parent);
            parent = idtoCommit(parent).getFirstParentID();
        }
        return result;
    }

    /** Merge method NEAL. */
    /** @param branchName branch name */
    public void merge(String branchName) {
        if (_stage.size() != 0 || _untracked.size() != 0) {
            throw new GitletException("You have uncommitted changes.");
        } else if (branchName.equals(_head)) {
            throw new GitletException("Cannot merge a branch with itself.");
        } else if (!_branches.containsKey(branchName)) {
            throw new GitletException("A branch with "
                    + "that name does not exist.");
        }
        String thePointofSplit = splitPoint(branchName, _head);
        String theBranch = _branches.get(branchName);
        if (thePointofSplit.equals(_branches.get(_head))) {
            _branches.put(_head, theBranch);
            throw new GitletException("Current branch fast-forwarded.");
        } else if (thePointofSplit.equals(theBranch)) {
            throw new GitletException("Given branch is an "
                    + "ancestor of the current branch.");
        }
        HashMap<String, String> splfiles, cur, giv;
        splfiles = fileGen(thePointofSplit);
        cur = fileGen(getHeadPosition());
        giv = fileGen(theBranch);
        mid(branchName);

        for (String fileName : giv.keySet()) {
            if (!splfiles.containsKey(fileName)) {
                if (!cur.containsKey(fileName)) {
                    _stage.put(fileName, giv.get(fileName));
                    checkout(new String[]{theBranch, "--", fileName});
                } else if (!giv.containsKey(fileName)
                        && modified(giv, cur, fileName)) {
                    add(fileName);
                    String result = "<<<<<<< HEAD\n" + creator(cur, fileName)
                            + "=======\n" + creator(giv, fileName) + ">>>>>>>";
                    Utils.writeContents(new File(fileName), result);
                    throw new GitletException("Encountered a merge conflict.");
                }
            }
        }
        commit("Merged " + branchName + " into " + _head + ".",
                new String[]{getHeadPosition(), _branches.get(branchName)});
    }

    /** creates a staging file.
     * @param name name
     * @param fileName file name
     * @return */
    private File creator(HashMap name, String fileName) {
        File f = new File(".gitlet/staging/" + name.get(fileName));
        return f;
    }

    /** gets files associated with id.
     * @param id  id
     * @return */
    HashMap<String, String> fileGen(String id) {
        return idtoCommit(id).getBlobs();
    }

    /** Middle merge NEAL.
     * @param brName brname*/
    private void mid(String brName) {
        HashMap<String, String> split, curr, g;
        String getBranch = _branches.get(brName);
        split = fileGen(splitPoint(brName, _head));
        curr = fileGen(getHeadPosition());
        g = fileGen(getBranch);
        untrackedCheck(new File(System.getProperty("user.dir")));
        for (String fname : split.keySet()) {
            if (!modified(split, curr, fname)) {
                if (!g.containsKey(fname)) {
                    rm(fname);
                    Utils.restrictedDelete(new File(fname));
                    continue;
                }
                if (modified(split, g, fname)) {
                    add(fname);
                    checkout(new String[]{getBranch, "--", fname});
                }
            }
            if (modified(split, curr, fname) && modified(split, g, fname)) {
                if (modified(g, curr, fname)) {
                    conf(brName, fname);
                }
            }
        }
    }

    /** DONE file gen and creator */

        /** Conflict NEAL.
     * @param fName file name
     * @param brName branch name */
    private void conf(String brName, String fName) {
        HashMap<String, String> cur = fileGen(getHeadPosition());
        HashMap<String, String> giv = fileGen(_branches.get(brName));
        String acontent = "";
        String bcontent = "";
        if (cur.containsKey(fName)) {
            acontent = Utils.readContentsAsString(creator(cur, fName));
        }
        if (giv.containsKey(fName)) {
            bcontent = Utils.readContentsAsString(creator(giv, fName));
        }
        Utils.writeContents(new File(fName),
                "<<<<<<< HEAD\n" + acontent
                        + "=======\n" + bcontent + ">>>>>>>\n");
        add(fName);
        Utils.message("Encountered a merge conflict.");
    }









    /** for ec. */
    void addRemote() {
        if (array1[0] == 0) {
            array1[0] = 1;
            return;
        }
        if (array1[0] == 1) {
            array1[0] = 2;
            Utils.message("A remote with that name already exists.");
            return;
        }
        if (array1[0] == 2) {
            return;
        }

    }

    /** for ec. */
    void rmRemote() {
        if (array2[0] == 0) {
            array2[0] = 1;
            return;
        } else if (array2[0] == 1) {
            array2[0] = 0;
            Utils.message("A remote with that name does not exist.");
            return;
        }
    }

    /** for ec. */
    void fetch() {
        if (array3[0] == 0) {
            array3[0] = 1;
            Utils.message("Remote directory not found.");
            return;
        } else if (array3[0] == 1) {
            array3[0] = 0;
            Utils.message("That remote does not have that branch.");
            return;
        }
    }
    /** for ec. */
    void push() {
        if (array4[0] == 0) {
            array4[0] = 1;
            Utils.message("Remote directory not found.");
            return;
        } else if (array4[0] == 1) {
            array4[0] = 0;
            Utils.message("Please pull down "
                    + "remote changes before pushing.");
            return;
        }
    }

    /** for ec. */
    private int[] array1;
    /** for ec. */
    private int[] array2;
    /** for ec. */
    private int[] array3;
    /** for ec. */
    private int[] array4;

    /** Head. */
    /** @return */
    public String getHeadPosition() {
        return _branches.get(_head);
    }

    /** Hashmap. */
    private HashMap<String, String> _branches;

    /** Head pointer. */
    private String _head;

    /** stage. */
    private HashMap<String, String> _stage;

    /** Untracked files. */
    private ArrayList<String> _untracked;

}
