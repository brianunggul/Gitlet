package gitlet;

import org.junit.Test;
import ucb.junit.textui;

import java.io.File;
import java.io.IOException;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Brian Unggul
 */
public class UnitTest {

    /** Run the JUnit tests in the load package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

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

    @Test
    public void mergeConflictTest() throws IOException {
        File workFile = new File("wug1.txt");
        workFile.createNewFile();
        File currFile = new File("curr.txt");
        currFile.createNewFile();
        File givenFile = new File("given.txt");
        String str1 = "Lorem ipsum dolor sit amet, consectetur";
        String str2 = System.lineSeparator();
        String str3 = "adipiscing elit, sed do eiusmod tempor ";
        Utils.writeContents(currFile, str1, str2, str3);
        Utils.writeContents(givenFile, "");
        mergeConflict(workFile, currFile, givenFile);
    }

}


