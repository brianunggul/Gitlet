package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** CommitTree class that stores all the commits in the .gitlet directory.
 * @author Brian Unggul */
public class CommitTree implements Serializable {

    /**The commit that will represent this committree's node. */
    private Commit node;

    /** The hashmap of committrees, which are the children. */
    private HashMap<String, CommitTree> children;

    /** The constructor for this committree with the node as
     *  the given commit, and the children initialized to an
     *  empty hashmap.
     * @param commit The commit that will become this committree's node
     */
    CommitTree(Commit commit) {
        node = commit;
        children = new HashMap<>();
    }

    /** Method to add a new commit node to the specified branch
     *  in this committree.
     * @param commit The commit to be added to this committree
     * @param branch The branch of this committree whose leaf the
     *               commit is to be appended to.
     * @return True or false whether or not a commit was added.
     */
    boolean addCommit(Commit commit, Commit branch) {
        if (node.getSha1().compareTo(branch.getSha1()) == 0) {
            children.put(commit.getSha1(), new CommitTree(commit));
            return true;
        } else {
            for (CommitTree ct : children.values()) {
                if (ct.addCommit(commit, branch)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Method to add a committree to this committree.
     * @param ct The committree to be added
     * @param branch The branch in which the committree is to be
     * added to
     * @return Whether or not a committree was successfully added
     */
    boolean addCommitTree(CommitTree ct, Commit branch) {
        if (node.getSha1().compareTo(branch.getSha1()) == 0) {
            String sha1 = ct.node.getSha1();
            children.put(sha1, ct);
            return true;
        } else {
            for (CommitTree child : children.values()) {
                if (child.addCommitTree(ct, branch)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Method to find the committree whose node is equal to commit.
     * @param commit The commit to be compared to
     * @return The committree with the node similar to commit
     */
    CommitTree findCT(Commit commit) {
        if (node.equals(commit)) {
            return this;
        }
        for (CommitTree ct : children.values()) {
            CommitTree foundCT = ct.findCT(commit);
            if (foundCT != null) {
                return foundCT;
            }
        }
        return null;
    }

    /** Method to check if this committree has any children
     *  committree's or not.
     * @return True or false whether or not this has any children
     */
    boolean hasChildren() {
        return children.size() > 0;
    }

    /** Method to print out the commit info of all commits
     *  in this tree.
     */
    void globalLog() {
        System.out.println("===");
        System.out.println("commit " + node.getSha1());
        System.out.println("Date: " + node.getDate());
        System.out.println(node.getLogMessage());
        System.out.println();
        for (CommitTree ct : children.values()) {
            ct.globalLog();
        }
    }

    /** Method to find the the commits that have a log message
     *  which is the same as the specified message, then prints
     *  the sha-1 value of the commit.
     * @param message The message to be looked for
     * @param found The arraylist of commits found so far
     * */
    void find(String message, ArrayList<String> found) {
        if (node.getLogMessage().compareTo(message) == 0) {
            System.out.println(node.getSha1());
            found.add(node.getSha1());
        }
        for (CommitTree ct : children.values()) {
            ct.find(message, found);
        }
    }

    /** Method to get the commit stored in this committree with
     *  the specified commit ID. Returns the Commit object if the
     *  commit with the commit ID exists; otherwise returns null.
     * @param commitID The commit ID (can be the original commit
     *                 ID, or a prefix of the original commit ID
     * @return The commit with the commit ID; otherwise returns null
     */
    Commit findCommit(String commitID) {
        if (node.getSha1().startsWith(commitID)) {
            return node;
        } else if (hasChildren()) {
            for (CommitTree ct : children.values()) {
                Commit cmt = ct.findCommit(commitID);
                if (cmt != null) {
                    return cmt;
                }
            }
        }
        return null;
    }

    /** Method to check if a file with the specified file name
     *  is being tracked by any commit in this committree.
     * @param fileName The name of the file
     * @return True or false whether or not the file is tracked
     */
    boolean fileTracked(String fileName) {
        for (String name : node.getBlobs().keySet()) {
            if (fileName.compareTo(name) == 0) {
                return true;
            }
        }
        if (hasChildren()) {
            for (CommitTree ct : children.values()) {
                if (ct.fileTracked(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

}
