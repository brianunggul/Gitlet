package gitlet;

import java.io.Serializable;

/** Blob class that stores original name of file (in working
 *  directory) and also the sha-1 of the file.
 *  Blobs only stores data of files in the .gitlet directory.
 *  @author Brian Unggul
 */
public class Blob implements Serializable {

    /** The original file name of the file pointed to
     *  by this blob.
     */
    private String fileName;

    /** The sha-1 of the file pointed to by this blob. */
    private String fileSha1;

    /** Blob constructor.
     * @param name The name of the original file
     * @param sha1 The sha1 of the original file
     */
    Blob(String name, String sha1) {
        fileName = name;
        fileSha1 = sha1;
    }

    /** Method to get the original name of file.
     * @return name of the file */
    public String getFileName() {
        return fileName;
    }

    /** Method to get the sha-1 of the file in the
     * .gitlet directory.
     * @return sha-1 of the file
     */
    public String getFileSha1() {
        return fileSha1;
    }

}
