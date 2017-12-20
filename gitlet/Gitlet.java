package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/** Gitlet class that parses the commands and executes them.
 * @author Brian Unggul
 */
public class Gitlet implements Serializable {

    /** The committree pointed to by this Gitlet object. */
    private CommitTree commitTree;

    /** The hashmap of branches (commits) pointed to by this
     * Gitlet object. */
    private HashMap<String, Commit> branches;

    /** The name of the current branch pointed to by this
     * Gitlet object. */
    private String currBranchName;

    /** The current branch (commit) pointed to by this
     * Gitlet object. */
    private Commit currBranch;

    /** The hashmap of blobs pointed to by this Gitlet object
     * that are removed. */
    private HashMap<String, Blob> removed;

    /** The arraylist of blobs that are in the staging area. */
    private ArrayList<Blob> stageBlobs;

    /** The arraylist of names of the files that are removed by
     * the rm command, which are not to be included in the next
     * or succeeding commits. */
    private ArrayList<String> nextRem;

    /** The hashmap of remotes pointed to by this Gitlet object;
     * stores the name and the path of the remotes. */
    private HashMap<String, String> remotes;


    /** Method to parse the commands and run the commands.
     * @param args The arguments given by the person */
    void process(String... args) throws IOException {
        if (args[0].compareTo("add") == 0) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                add(args[1]);
            }
        } else if (args[0].compareTo("commit") == 0) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else if (args[1].compareTo("") == 0) {
                System.out.println("Please enter a commit message.");
            } else {
                commit(args[1]);
            }
        } else if (args[0].compareTo("rm") == 0) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                rm(args[1]);
            }
        } else if (args[0].compareTo("log") == 0) {
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
            } else {
                log();
            }
        } else if (args[0].compareTo("global-log") == 0) {
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
            } else {
                globalLog();
            }
        } else if (args[0].compareTo("find") == 0) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                find(args[1]);
            }
        } else if (args[0].compareTo("status") == 0) {
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
            } else {
                status();
            }
        } else if (args[0].compareTo("checkout") == 0) {
            if (args.length == 3 && args[1].compareTo("--") == 0) {
                checkout1(args[2]);
            } else if (args.length == 4 && args[2].compareTo("--") == 0) {
                checkout2(args[1], args[3]);
            } else if (args.length == 2) {
                checkout3(args[1]);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else {
            process2(args);
        }
    }

    /** Helper method to continue the original process() method;
     * this is literally just to make the stylechecker happy.
     * @param args The arguments from the user
     * @throws IOException e
     */
    void process2(String... args) throws IOException {
        if (args[0].compareTo("branch") == 0) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                branch(args[1]);
            }
        } else if (args[0].compareTo("rm-branch") == 0) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                rmBranch(args[1]);
            }
        } else if (args[0].compareTo("reset") == 0) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                reset(args[1]);
            }
        } else if (args[0].compareTo("merge") == 0) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                merge(args[1]);
            }
        } else if (args[0].compareTo("add-remote") == 0) {
            if (args.length != 3 || !args[2].endsWith("/.gitlet")) {
                System.out.println("Incorrect operands.");
            } else {
                addRemote(args[1], args[2]);
            }
        } else if (args[0].compareTo("rm-remote") == 0) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                rmRemote(args[1]);
            }
        } else if (args[0].compareTo("push") == 0) {
            if (args.length != 3) {
                System.out.println("Incorrect operands.");
            } else {
                push(args[1], args[2]);
            }
        } else if (args[0].compareTo("fetch") == 0) {
            if (args.length != 3) {
                System.out.println("Incorrect operands.");
            } else {
                fetch(args[1], args[2]);
            }
        } else if (args[0].compareTo("pull") == 0) {
            if (args.length != 3) {
                System.out.println("Incorrect operands.");
            } else {
                pull(args[1], args[2]);
            }
        } else {
            System.out.println("No command with that name exists.");
        }
    }

    /** The init command. */
    void init() throws IOException {
        try {
            if (new File(".gitlet").mkdir()) {
                new File(".gitlet/stage").mkdir();
                new File(".gitlet/data").createNewFile();
                Commit commit = new Commit();
                commitTree = new CommitTree(commit);
                branches = new HashMap<>();
                currBranchName = "master";
                branches.put(currBranchName, commit);
                currBranch = branches.get(currBranchName);
                stageBlobs = new ArrayList<>();
                removed = new HashMap<>();
                nextRem = new ArrayList<>();
                remotes = new HashMap<>();
            } else {
                System.out.println("A Gitlet version-control"
                        + " system already exists in the current directory.");
            }
        } catch (IOException e) {
            throw new IOException();
        }
    }

    /** The add command.
     * @param fileName The name of the file */
    void add(String fileName) throws IOException {
        try {
            if (fileName.compareTo("-A") == 0) {
                addAll();
                return;
            }
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("File does not exist.");
                return;
            }
            boolean tracked = currBranch.blobExists(fileName);
            String newSha1 = Utils.sha1(Utils.readFullContents(file));
            if (nextRem.contains(fileName)) {
                nextRem.remove(fileName);
                return;
            }
            if (tracked) {
                String oldSha1 = currBranch.getBlob(fileName).getFileSha1();
                if (oldSha1.compareTo(newSha1) == 0) {
                    return;
                }
            }
            File stageFile = new File(".gitlet/stage/" + newSha1);
            if (!stageFile.exists()) {
                stageFile.createNewFile();
                byte[] contents = Utils.readContents(file);
                Utils.writeContents(stageFile, contents);
                Blob blob = new Blob(fileName, newSha1);
                stageBlobs.add(blob);
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /** Method that is invoked by "add -A" command; used for
     * personal purposes. */
    void addAll() throws IOException {
        File curDir = new File(".");
        File[] files = curDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                add(file.getName());
            }
        }
    }

    /** Method that is used only by the merge command;
     * this method is to checkout and stage a specified file.
     * @param commit The parent commit of the current branch
     * @param file The merged file
     * @param name The name of the merged file */
    void add(Commit commit, File file, String name) throws IOException {
        try {
            String sha1 = file.getName();
            File newFile = new File(".gitlet/stage/" + sha1);
            newFile.createNewFile();
            byte[] contents = Utils.readContents(file);
            Utils.writeContents(newFile, contents);
            Blob blob = commit.getBlob(name);
            stageBlobs.add(blob);
        } catch (IOException e) {
            throw new IOException();
        }
    }

    /** The commit command.
     * @param message The log message of the commit */
    void commit(String message) throws IOException {
        try {
            File stage = new File(".gitlet/stage");
            boolean noUntrack = (nextRem.size() == 0);
            boolean isDir = stage.isDirectory();
            boolean noStaged = (stage.listFiles().length == 0);
            if (noStaged && isDir && noUntrack) {
                System.out.println("No changes added to the commit.");
                return;
            }
            for (File file : stage.listFiles()) {
                File newFile = new File(".gitlet/" + file.getName());
                newFile.createNewFile();
                byte[] content = Utils.readContents(file);
                Utils.writeContents(newFile, content);
                file.delete();
            }
            ArrayList<Blob> blobs = new ArrayList<>();
            for (Blob blob : stageBlobs) {
                if (!nextRem.contains(blob.getFileName())) {
                    blobs.add(blob);
                }
            }
            Commit commit = new Commit(message, currBranch, blobs, nextRem);
            nextRem = new ArrayList<>();
            stageBlobs = new ArrayList<>();
            commitTree.addCommit(commit, currBranch);
            currBranch = commit;
            branches.put(currBranchName, currBranch);
        } catch (IOException e) {
            throw new IOException();
        }
    }

    /** Method to make a merge commit.
     * @param cBranch The current branch (commit)
     * @param mBranch The merged branch (commit)
     * @param branchName The name of the current branch
     * @param toTrack The arraylist of file names that are to be
     * tracked in the following merge commit */
    void commit(Commit cBranch, Commit mBranch, String branchName,
                ArrayList<String> toTrack) {
        String message = "Merged " + branchName + " into "
                + currBranchName + ".";
        Commit commit = new Commit(message, cBranch, mBranch,
                nextRem, stageBlobs, toTrack);
        stageBlobs = new ArrayList<>();
        nextRem = new ArrayList<>();
        commitTree.addCommit(commit, currBranch);
        currBranch = commit;
        branches.put(currBranchName, currBranch);
    }

    /** The rm command.
     * @param fileName The name of the file */
    void rm(String fileName) {
        File stagedFile = null;
        String fileSha1 = null;
        boolean addNextRem = true;
        boolean stageContains = false;
        for (Blob blob : stageBlobs) {
            if (blob.getFileName().compareTo(fileName) == 0) {
                stageBlobs.remove(blob);
                addNextRem = false;
                fileSha1 = blob.getFileSha1();
                stagedFile = new File(".gitlet/stage/" + fileSha1);
                stageContains = true;
                if (stagedFile.exists()) {
                    stagedFile.delete();
                }
                break;
            }
        }
        boolean tracked = currBranch.blobExists(fileName);
        if (!stageContains && !tracked) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (currBranch.blobExists(fileName)) {
            File file = new File(fileName);
            if (file.exists()) {
                Utils.restrictedDelete(file);
            }
            if (addNextRem) {
                nextRem.add(fileName);
            }
        }
    }

    /** The log command. */
    void log() {
        Commit tCommit = currBranch;
        while (tCommit != null) {
            if (!tCommit.hasParent2()) {
                System.out.println("===");
                System.out.println("commit " + tCommit.getSha1());
                System.out.println("Date: " + tCommit.getDate());
                System.out.println(tCommit.getLogMessage());
                System.out.println();
            } else {
                String id1 = tCommit.getParent1().getSha1()
                        .substring(0, 7);
                String id2 = tCommit.getParent2().getSha1()
                        .substring(0, 7);
                System.out.println("===");
                System.out.println("commit " + tCommit.getSha1());
                System.out.println("Merge: " + id1 + " " + id2);
                System.out.println("Date: " + tCommit.getDate());
                System.out.println(tCommit.getLogMessage());
                System.out.println();
            }
            tCommit = tCommit.getParent1();
        }
    }

    /** The global-log command. */
    void globalLog() {
        commitTree.globalLog();
    }

    /** The find command.
     * @param message The log message of the commits to
     * be searched for */
    void find(String message) {
        ArrayList<String> found = new ArrayList<>();
        commitTree.find(message, found);
        if (found.size() == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** The status command. */
    void status() {
        String[] names = new String[branches.size()];
        names = branches.keySet().toArray(names);
        Arrays.sort(names);
        System.out.println("=== Branches ===");
        for (String name : names) {
            if (name.compareTo(currBranchName) == 0) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();
        ArrayList<String> staged = new ArrayList<>();
        for (Blob blob : stageBlobs) {
            staged.add(blob.getFileName());
        }
        Collections.sort(staged);
        System.out.println("=== Staged Files ===");
        for (String name : staged) {
            System.out.println(name);
        }
        System.out.println();
        Collections.sort(nextRem);
        System.out.println("=== Removed Files ===");
        for (String rem : nextRem) {
            System.out.println(rem);
        }
        System.out.println();
        status2(staged);
    }

    /** Helper method for original status() method;
     * this is literally just to satisfy the stylechecker.
     * @param staged The name of the staged files */
    void status2(ArrayList<String> staged) {
        ArrayList<String> mbns = new ArrayList<>();
        File curDir = new File(".");
        File[] files = curDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                modButNotStaged(file, mbns);
            }
        }
        for (String name : staged) {
            File temp = new File(name);
            if (!temp.exists() && !mbns.contains(name)) {
                mbns.add(name + " (deleted)");
            }
        }
        for (String name : currBranch.getBlobs().keySet()) {
            File tempCurr = new File(name);
            boolean inNextRem = nextRem.contains(name);
            if (!tempCurr.exists() && !inNextRem) {
                if (!mbns.contains(name)) {
                    mbns.add(name + " (deleted)");
                }
            }
        }
        Collections.sort(mbns);
        System.out.println("=== Modifications Not "
                + "Staged For Commit ===");
        for (String name : mbns) {
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        ArrayList<String> notTracked = untracked(nextRem);
        for (String name : notTracked) {
            System.out.println(name);
        }
        System.out.println();
    }

    /** Helper method to get the names of files in the working
     * directory that were modified but not staged.
     * @param file The file to be checked
     * @param mbns The arraylist of file names that so far have been
     * checked to be modified but not staged */
    void modButNotStaged(File file, ArrayList<String> mbns) {
        ArrayList<String> inStage = new ArrayList<>();
        for (Blob blob : stageBlobs) {
            inStage.add(blob.getFileName());
        }
        String name = file.getName();
        String currSha1 = Utils.sha1(Utils.readFullContents(file));
        boolean staged = inStage.contains(name);
        boolean tracked = currBranch.blobExists(name);
        if (tracked) {
            Blob blob = currBranch.getBlob(name);
            String oldSha1 = blob.getFileSha1();
            if (!staged) {
                if (currSha1.compareTo(oldSha1) != 0) {
                    mbns.add(name + " (modified)");
                    return;
                }
            }
        }
        if (staged) {
            Blob stageBlob = null;
            for (Blob blob : stageBlobs) {
                if (blob.getFileName().compareTo(name) == 0) {
                    stageBlob = blob;
                    break;
                }
            }
            if (currSha1.compareTo(stageBlob.getFileSha1()) != 0) {
                mbns.add(name + " (modified)");
            }
        }
    }

    /** Helper method to get an arraylist of names of files
     * that are untracked.
     * @param rem The String[] of names of removed files
     * @return The arraylist of untracked file names
     */
    ArrayList<String> untracked(ArrayList<String> rem) {
        ArrayList<String> list = new ArrayList<>();
        File curDir = new File(".");
        File[] files = curDir.listFiles();
        ArrayList<String> stage = new ArrayList<>();
        for (Blob blob : stageBlobs) {
            stage.add(blob.getFileName());
        }
        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                boolean untracked1 = !currBranch.blobExists(name);
                boolean untracked2 = rem.contains(name);
                boolean notStaged = !stage.contains(name);

                if ((untracked1 && notStaged) || untracked2) {
                    list.add(name);
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    /** The checkout command (1st case).
     * @param fileName The name of the file */
    void checkout1(String fileName) {
        boolean tracked = currBranch.blobExists(fileName);
        if (!tracked) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Blob blob = currBranch.getBlob(fileName);
        File oldFile = new File(
                ".gitlet/" + blob.getFileSha1());
        byte[] data = Utils.readContents(oldFile);
        File curFile = new File(fileName);
        Utils.writeContents(curFile, data);
    }

    /** The checkout command (2nd case).
     * @param fileName The name of the file
     * @param commitID The commit ID */
    void checkout2(String commitID, String fileName) {
        Commit commit = commitTree.findCommit(commitID);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        boolean tracked = commit.blobExists(fileName);
        if (!tracked) {
            System.out.println("File does not "
                    + "exist in that commit.");
            return;
        }
        Blob blob = commit.getBlob(fileName);
        File oldFile = new File(
                ".gitlet/" + blob.getFileSha1());
        byte[] data = Utils.readContents(oldFile);
        File curFile = new File(fileName);
        Utils.writeContents(curFile, data);
    }

    /** The checkout command (3rd case).
     * @param branchName The name of the branch */
    @SuppressWarnings("unchecked")
    void checkout3(String branchName) throws  IOException {
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchName.compareTo(currBranchName) == 0) {
            System.out.println("No need to checkout"
                    + " the current branch.");
            return;
        }
        File curDir = new File(".");
        File[] files = curDir.listFiles();
        Commit newBranch = branches.get(branchName);
        ArrayList<String> newBlobNames = new ArrayList(
                newBranch.getBlobs().keySet());
        ArrayList<String> oldBlobNames = new ArrayList<>(
                currBranch.getBlobs().keySet());
        for (File file : files) {
            if (!file.isDirectory()) {
                String name = file.getName();
                String fileSha1 = Utils.sha1(Utils.readFullContents(file));
                if (!oldBlobNames.contains(name)) {
                    if (newBranch.blobExists(name)
                            && fileSha1.compareTo(newBranch.getBlob(
                            name).getFileSha1()) != 0) {
                        System.out.println("There is an untracked file "
                                + "in the way; delete it or add it first.");
                        System.exit(0);
                    }
                }
            }
        }
        for (String name : oldBlobNames) {
            if (!newBlobNames.contains(name)) {
                File file = new File(name);
                file.delete();
            }
        }
        for (Blob blob : newBranch.getBlobs().values()) {
            File newFile = new File(
                    ".gitlet/" + blob.getFileSha1());
            File curFile = new File(blob.getFileName());
            if (!curFile.exists()) {
                curFile.createNewFile();
            }
            byte[] data = Utils.readContents(newFile);
            Utils.writeContents(curFile, data);
        }
        currBranchName = branchName;
        currBranch = branches.get(currBranchName);
        File stage = new File(".gitlet/stage");
        assert stage.isDirectory();
        for (File file : stage.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
        stageBlobs = new ArrayList<>();
    }

    /** The branch command.
     * @param branchName The name of the branch */
    void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with "
                    + "that name already exists.");
            return;
        }
        branches.put(branchName, currBranch);
    }

    /** The rm-branch command.
     * @param branchName The name of the branch */
    void rmBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that "
                    + "name does not exist.");
            return;
        }
        if (branchName.compareTo(currBranchName) == 0) {
            System.out.println("Cannot remove the "
                    + "current branch.");
            return;
        }
        branches.remove(branchName);
    }

    /** The reset command.
     * @param commitID The commit ID*/
    @SuppressWarnings("unchecked")
    void reset(String commitID) throws IOException {
        Commit newBranch = commitTree.findCommit(commitID);
        if (newBranch == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        ArrayList<String> newBlobNames = new ArrayList(
                newBranch.getBlobs().keySet());
        ArrayList<String> oldBlobNames = new ArrayList<>(
                currBranch.getBlobs().keySet());
        File curDir = new File(".");
        File[] files = curDir.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                String name = file.getName();
                String fileSha1 = Utils.sha1(Utils.readFullContents(file));
                if (!oldBlobNames.contains(name)) {
                    if (newBranch.blobExists(name)
                            && fileSha1.compareTo(newBranch.getBlob(
                            name).getFileSha1()) != 0) {
                        System.out.println("There is an untracked file "
                                + "in the way; delete it or add it first.");
                        System.exit(0);
                    }
                }
            }
        }
        for (Blob blob : newBranch.getBlobs().values()) {
            File oldFile = new File(
                    ".gitlet/" + blob.getFileSha1());
            byte[] data = Utils.readContents(oldFile);
            File curFile = new File(blob.getFileName());
            Utils.writeContents(curFile, data);
        }
        for (String name : currBranch.getBlobs().keySet()) {
            if (!newBranch.getBlobs().keySet().contains(name)) {
                File temp = new File(name);
                if (temp.exists() && temp.isFile()) {
                    temp.delete();
                }
            }
        }
        currBranch = newBranch;
        branches.put(currBranchName, currBranch);
        File stage = new File(".gitlet/stage");
        assert stage.isDirectory();
        for (File file : stage.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
        stageBlobs = new ArrayList<>();
    }

    /** The merge command.
     * @param branchName The name of the branch */
    void merge(String branchName) throws IOException {
        File stage = new File(".gitlet/stage");
        if (stage.listFiles().length > 0 || nextRem.size() > 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!branches.keySet().contains(branchName)) {
            System.out.println("A branch with that"
                    + " name does not exist.");
            return;
        }
        if (branchName.compareTo(currBranchName) == 0) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        ArrayList<String> currBranchPar = new ArrayList<>();
        currBranch.getParentHistory(currBranchPar);
        Commit given = branches.get(branchName);
        Commit tempPar = given;
        Commit splitPoint = null;
        while (tempPar != null) {
            if (currBranchPar.contains(tempPar.getSha1())) {
                splitPoint = tempPar;
                break;
            }
            tempPar = tempPar.getParent1();
        }
        assert splitPoint != null;
        if (splitPoint.equals(given)) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return;
        }
        if (splitPoint.equals(currBranch)) {
            currBranch = given;
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        if (checkMergeUntracked(splitPoint, given)) {
            System.out.println("There is an untracked file in "
                    + "the way; delete it or add it first.");
            System.exit(0);
        }
        merge2(splitPoint, given, branchName);
    }

    /** Helper method for original merge() method;
     * this is literally just to satisfy the stylechecker.
     * @param branchName The name of the branch
     * @param given The given commit
     * @param splitPoint The split point (commit) */
    void merge2(Commit splitPoint, Commit given, String branchName)
            throws IOException {
        ArrayList<String> toTrack = new ArrayList<>();
        boolean printMC = false;
        for (Blob blob : splitPoint.getBlobs().values()) {
            String name = blob.getFileName();
            File splitFile = new File(".gitlet/" + blob.getFileSha1());
            File workFile = new File(name);
            String splitSha1 = blob.getFileSha1();
            boolean currAbsent = !currBranch.blobExists(name);
            boolean givenAbsent = !given.blobExists(name);
            if (!currAbsent && givenAbsent) {
                String currSha1 = currBranch.getBlob(name).getFileSha1();
                boolean modCurr = splitSha1.compareTo(currSha1) != 0;
                if (!modCurr) {
                    rm(name);
                } else if (modCurr) {
                    File currFile = new File(".gitlet/" + currSha1);
                    File givenFile = new File("blank.txt");
                    givenFile.createNewFile();
                    mergeConflict(workFile, currFile, givenFile);
                    givenFile.delete();
                    add(name);
                    toTrack.add(name);
                    printMC = true;
                }
            } else if (currAbsent && !givenAbsent) {
                String givenSha1 = given.getBlob(name).getFileSha1();
                if (splitSha1.compareTo(givenSha1) != 0) {
                    File currFile = new File("blank.txt");
                    File givenFile = new File(".gitlet/" + givenSha1);
                    currFile.createNewFile();
                    mergeConflict(workFile, currFile, givenFile);
                    currFile.delete();
                    add(name);
                    toTrack.add(name);
                    printMC = true;
                }
            } else if (!currAbsent && !givenAbsent) {
                String givenSha1 = given.getBlob(name).getFileSha1();
                File givenFile = new File(".gitlet/" + givenSha1);
                boolean modGiven = splitSha1.compareTo(givenSha1) != 0;
                String currSha1 = currBranch.getBlob(name).getFileSha1();
                File currFile = new File(".gitlet/" + currSha1);
                boolean modCurr = splitSha1.compareTo(currSha1) != 0;
                if (modGiven && modCurr) {
                    if (currSha1.compareTo(givenSha1) != 0) {
                        mergeConflict(workFile, currFile, givenFile);
                        add(name);
                        toTrack.add(name);
                        printMC = true;
                    }
                } else if (modGiven && !modCurr) {
                    byte[] contents = Utils.readContents(givenFile);
                    Utils.writeContents(workFile, contents);
                    toTrack.add(name);
                }
            }
        }
        merge3(splitPoint, given, branchName, printMC, toTrack);
    }

    /** Helper method for merge() and merge2();
     * literally just to satisfy stylechecker.
     * @param given The given branch (commit)
     * @param branchName The name of the given branch
     * @param splitPoint The split point (commit)
     * @param printMC Whether or not to print merge conflict message
     * @param toTrack The arraylist of file names that are to be tracked
     * in the following merge commit */
    void merge3(Commit splitPoint, Commit given, String branchName,
                boolean printMC, ArrayList<String> toTrack)
            throws IOException {
        for (Blob blob : given.getBlobs().values()) {
            String name = blob.getFileName();
            boolean splitAbsent = !splitPoint.blobExists(name);
            boolean currAbsent = !currBranch.blobExists(name);
            if (splitAbsent && currAbsent) {
                File file = new File(name);
                checkout2(given.getSha1(), name);
                add(given, file, name);
                toTrack.add(name);
            }
        }
        for (Blob blob : currBranch.getBlobs().values()) {
            String name = blob.getFileName();
            boolean splitAbsent = !splitPoint.blobExists(name);
            boolean givenAbsent = !given.blobExists(name);
            File curr = new File(name);
            if (splitAbsent && givenAbsent && curr.exists()) {
                toTrack.add(name);
            }
        }
        if (printMC) {
            System.out.println("Encountered a merge conflict.");
        }
        commit(currBranch, given, branchName, toTrack);
    }

    /** Method to check if there is an untracked file when merging.
     * @param split The splitpoint (commit)
     * @param given The given branch (commit)
     * @return Whether or not there is an untracked file
     */
    @SuppressWarnings("unchecked")
    boolean checkMergeUntracked(Commit split, Commit given) {
        ArrayList<String> currNames = new ArrayList(
                currBranch.getBlobs().keySet());
        ArrayList<String> splitNames = new ArrayList<>(
                split.getBlobs().keySet());
        ArrayList<String> givenNames = new ArrayList<>(
                given.getBlobs().keySet());
        File curDir = new File(".");
        File[] files = curDir.listFiles();
        for (File workFile : files) {
            String name = workFile.getName();
            if (!currNames.contains(name)) {
                if (!splitNames.contains(name) && givenNames.contains(name)) {
                    return true;
                } else if (splitNames.contains(name)) {
                    Blob splitBlob = split.getBlob(name);
                    String splitSha1 = splitBlob.getFileSha1();
                    Blob givenBlob = given.getBlob(name);
                    String givenSha1 = givenBlob.getFileSha1();
                    if (splitSha1.compareTo(givenSha1) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Helper method for changing and adding contents to a specified
     * file in the working directory when a merge conflict is encountered.
     * @param currFile The file in the current branch
     * @param givenFile The file in the given branch
     * @param workFile The file in the working directory */
    void mergeConflict(File workFile, File currFile, File givenFile) {
        String str1 = "<<<<<<< HEAD";
        String sep = System.lineSeparator();
        String str2 = "=======";
        String str3 = ">>>>>>>";
        byte[] currData = Utils.readContents(currFile);
        byte[] givenData = Utils.readContents(givenFile);
        Utils.writeContents(workFile, str1, sep, currData,
                str2, sep, givenData, str3, sep);
    }

    /** Method to get the branch with the name.
     * @param name The name of the branch
     * @return The branch with the name */
    Commit getBranch(String name) {
        if (!branches.keySet().contains(name)) {
            System.out.println("Given branch does not exist.");
            return null;
        }
        return branches.get(name);
    }

    /** The add-remote command.
     * @param dir The path of the remote
     * @param remName The name of the remote */
    void addRemote(String remName, String dir) {
        if (remotes.keySet().contains(remName)) {
            System.out.println("A remote with that name already exists.");
            return;
        }
        dir.replace("/", "\\");
        remotes.put(remName, dir);
    }

    /** The rm-remote command.
     * @param remName The name of the remote */
    void rmRemote(String remName) {
        if (!remotes.keySet().contains(remName)) {
            System.out.println("A remote with that name does not exist.");
            return;
        }
        remotes.remove(remName);
    }

    /** The push command.
     * @param branchName The name of the branch
     * @param remName The name of the remote*/
    void push(String remName, String branchName) throws IOException {
        try {
            String dir = remotes.get(remName);
            File rem = new File(dir + "/data");
            if (!rem.exists() || !remotes.keySet().contains(remName)) {
                System.out.println("Remote directory not found.");
                return;
            }
            byte[] data = Utils.readObject(rem, byte[].class);
            Gitlet remGitlet = (Gitlet) Utils.deserialize(data);
            String remBranchName = remName + "/" + branchName;
            Commit branch = remGitlet.currBranch;
            if (!remGitlet.branches.containsKey(remBranchName)) {
                remGitlet.branches.put(remBranchName, branch);
            }
            if (!inHistory(branch)) {
                System.out.println("Please pull down remote "
                        + "changes before pushing.");
                return;
            }
            Commit tCommit = currBranch;
            ArrayList<String> remBranchPar = new ArrayList<>();
            branch.getParentHistory(remBranchPar);
            Commit splitPoint = null;
            while (tCommit != null && !tCommit.equals(branch)) {
                for (Blob blob : tCommit.getBlobs().values()) {
                    String sha1 = blob.getFileSha1();
                    File remFile = new File(dir + "/" + sha1);
                    if (!remFile.exists()) {
                        remFile.createNewFile();
                        File curFile = new File(".gitlet/" + sha1);
                        byte[] contents = Utils.readContents(curFile);
                        Utils.writeContents(remFile, contents);
                    }
                }
                if (remBranchPar.contains(tCommit.getSha1())) {
                    splitPoint = tCommit;
                    break;
                }
                tCommit = tCommit.getParent1();
            }
            CommitTree ct = commitTree.findCT(splitPoint);
            remGitlet.commitTree.addCommitTree(ct, splitPoint);
            String commitID = currBranch.getSha1();
            remGitlet.reset(commitID);
            byte[] contents = Utils.serialize(remGitlet);
            Utils.writeObject(new File(dir + "/data"), contents);
        } catch (IOException e) {
            throw new IOException();
        }
    }

    /** The fetch command.
     * @param branchName The name of the branch
     * @param remName The name of the remote */
    void fetch(String remName, String branchName) throws IOException {
        String dir = remotes.get(remName);
        File rem = new File(dir + "/data");
        if (!rem.exists() || !remotes.keySet().contains(remName)) {
            System.out.println("Remote directory not found.");
            return;
        }
        byte[] data = Utils.readObject(rem, byte[].class);
        Gitlet remGitlet = (Gitlet) Utils.deserialize(data);
        if (!remGitlet.branches.containsKey(branchName)) {
            System.out.println("That remote does not have that branch.");
            return;
        }
        Commit remCommit = remGitlet.getBranch(branchName);
        Commit tCommit = remCommit;
        ArrayList<String> currBranchPar = new ArrayList<>();
        currBranch.getParentHistory(currBranchPar);
        Commit splitPoint = null;
        while (tCommit != null && !tCommit.equals(currBranch)) {
            for (Blob blob : tCommit.getBlobs().values()) {
                String sha1 = blob.getFileSha1();
                File curFile = new File(".gitlet/" + sha1);
                if (!curFile.exists()) {
                    curFile.createNewFile();
                    File remFile = new File(dir + "/" + sha1);
                    byte[] contents = Utils.readContents(remFile);
                    Utils.writeContents(curFile, contents);
                }
            }
            if (currBranchPar.contains(tCommit.getSha1())) {
                splitPoint = tCommit;
                break;
            }
            tCommit = tCommit.getParent1();
        }
        String local = remName + "/" + branchName;
        branches.put(local, remCommit);
        CommitTree ct = remGitlet.commitTree.findCT(splitPoint);
        commitTree.addCommitTree(ct, splitPoint);
        byte[] contents = Utils.serialize(remGitlet);
        Utils.writeObject(new File(dir + "/data"), contents);
    }

    /** The pull command.
     * @param branchName The name of the branch
     * @param remName The name of the remote */
    void pull(String remName, String branchName) throws IOException {
        fetch(remName, branchName);
        merge(remName + "/" + branchName);
    }

    /** Method to check if the branch is in the history of
     *  this current branch.
     * @param branch The branch to be compared to
     * @return Whether or not it exists in the history
     */
    boolean inHistory(Commit branch) {
        Commit localBranch = currBranch;
        while (localBranch != null) {
            if (localBranch.getSha1().compareTo(branch.getSha1()) == 0) {
                return true;
            }
            localBranch = localBranch.getParent1();
        }
        return false;
    }

}
