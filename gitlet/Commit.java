package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** Commit class that stores data about file(s) that
 *  are committed.
 *  @author Brian Unggul*/
public class Commit implements Serializable {

    /** The date when this commit was created. */
    private String date;

    /** The log message of this commit. */
    private String logMessage;

    /** The hashmap that stores all the blobs this commit
     *  is pointing to.
     */
    private HashMap<String, Blob> blobs;

    /** The parent1 of this commit. */
    private Commit parent1;

    /** The parent2 of this commit. */
    private Commit parent2;

    /** The sha1 of this commit. */
    private String sha1;

    /** Constructor for the initial commit. */
    Commit() {
        date = "Thu Jan 1 00:00:00 1970 -0800";
        logMessage = "initial commit";
        blobs = new HashMap<>();
        parent1 = null;
        parent2 = null;
        sha1 = compSha1();
    }

    /** Constructor for an ordinary commit.
     * @param message The log message
     * @param parent The parent1
     * @param stageBlobs The arraylist of staged blobs
     * @param nextRem The arraylist of rm'ed blobs
     */
    @SuppressWarnings("unchecked")
    Commit(String message, Commit parent, ArrayList<Blob> stageBlobs,
           ArrayList<String> nextRem) {
        date = Utils.getDate();
        logMessage = message;
        blobs = new HashMap<>();
        for (Blob blob : parent.getBlobs().values()) {
            String name = blob.getFileName();
            if (!nextRem.contains(name)) {
                blobs.put(name, blob);
            }
        }
        for (Blob blob : stageBlobs) {
            String name = blob.getFileName();
            blobs.put(name, blob);
        }
        parent1 = parent;
        parent2 = null;
        sha1 = compSha1();
    }

    /** Constructor for a merge commit.
     * @param message The log message
     * @param p1 The parent1
     * @param p2 The parent2
     * @param nextRem The arraylist of blobs that
     * are staged to be removed
     * @param stage The stage containing blobs
     * @param toTrack The arraylist of file names that are
     * to be tracked in this commit
     */
    Commit(String message, Commit p1, Commit p2, ArrayList<String>
            nextRem, ArrayList<Blob> stage, ArrayList<String> toTrack) {
        date = Utils.getDate();
        logMessage = message;
        blobs = new HashMap<>();
        for (Blob blob : p1.getBlobs().values()) {
            String name = blob.getFileName();
            if (!nextRem.contains(name) && toTrack.contains(name)) {
                blobs.put(name, blob);
            }
        }
        ArrayList<String> blobSha1 = new ArrayList<>();
        for (Blob blob : blobs.values()) {
            blobSha1.add(blob.getFileSha1());
        }
        for (Blob blob : p2.getBlobs().values()) {
            String name = blob.getFileName();
            if (!nextRem.contains(name) && !blobSha1.contains(
                    blob.getFileSha1()) && toTrack.contains(name)) {
                blobs.put(name, blob);
            }
        }
        for (Blob blob : stage) {
            String name = blob.getFileName();
            blobs.put(name, blob);
        }
        parent1 = p1;
        parent2 = p2;
        sha1 = compSha1();
    }

    /** Method to compute the sha1 of this commit.
     * @return The computed sha1 of this commit */
    String compSha1() {
        return Utils.sha1(Utils.serialize(this));
    }

    /** Method to check if this commit points to a blob with the
     *  specified name.
     * @param name the name of the original file
     * @return true or false whether the blob exists
     */
    boolean blobExists(String name) {
        return blobs.containsKey(name);
    }

    /** Method to get the blob pointed to by this commit that has
     *  the specified name of the original file.
     * @param name the name of the original file
     * @return the blob being pointed to
     */
    Blob getBlob(String name) {
        assert blobExists(name);
        return blobs.get(name);
    }

    /** Method to check if this commit has a non-null parent1.
     * @return Whether this commit has a parent1 */
    boolean hasParent1() {
        return parent1 != null;
    }

    /** Method to check if this commit has a non-null parent2.
     * @return Whether this commit has a parent2 */
    boolean hasParent2() {
        return parent2 != null;
    }

    /** Method to get parent1.
     * @return This commit's parent1 */
    Commit getParent1() {
        return parent1;
    }

    /** Method to get parent2.
     * @return This commit's parent2 */
    Commit getParent2() {
        return parent2;
    }

    /** Method to get the number of parents of this commit that
     *  are not null.
     * @return the number of non-null parents
     */
    int numParents() {
        int num = 0;
        if (hasParent1()) {
            num += 1;
        }
        if (hasParent2()) {
            num += 1;
        }
        return num;
    }

    /** Method to get the date when this commit was created.
     * @return The date when this commit was created */
    String getDate() {
        return date;
    }

    /** Method to get the log message of this commit.
     * @return The log message of this commit */
    String getLogMessage() {
        return logMessage;
    }

    /** Method to get the hashmap of blobs of this commit.
     * @return THe hashmap of this commit's blobs */
    HashMap<String, Blob> getBlobs() {
        return blobs;
    }

    /** Method to get the sha1 of this commit.
     * @return The sha1 of this commit */
    String getSha1() {
        return sha1;
    }

    /** Method to get the HashSet of the parent commits.
     * @param history The hashset of parent commits */
    void getParentHistory(ArrayList<String> history) {
        history.add(this.getSha1());
        if (hasParent1()) {
            getParent1().getParentHistory(history);
        }
    }

}
