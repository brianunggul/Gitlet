package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Brian Unggul
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        ArrayList<String> remotes = new ArrayList<>();
        remotes.add("add-remote");
        remotes.add("rm-remote");
        remotes.add("push");
        remotes.add("fetch");
        remotes.add("pull");
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].compareTo("init") == 0) {
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
            } else {
                Gitlet gitlet = new Gitlet();
                gitlet.init();
                byte[] data = Utils.serialize(gitlet);
                Utils.writeObject(new File(".gitlet/data"), data);
            }
        } else {
            File gitDir = new File(".gitlet");
            if (!gitDir.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                return;
            }
            byte[] data = Utils.readObject(
                    new File(".gitlet/data"), byte[].class);
            Gitlet gitlet = (Gitlet) Utils.deserialize(data);
            gitlet.process(args);
            byte[] contents = Utils.serialize(gitlet);
            Utils.writeObject(new File(".gitlet/data"), contents);
        }
    }

}
