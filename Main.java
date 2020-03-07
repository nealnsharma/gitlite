package gitlet;
import java.io.File;
import java.util.Arrays;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author neal sharma
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            }
            String[] ops = Arrays.copyOfRange(args, 1, args.length);
            if (!isInit()) {
                if (!(args[0].equals("init"))) {
                    throw new
                            GitletException("Not in an initialized "
                            + "Gitlet directory.");
                } else {
                    myGitlet = new Gitlet();
                    Utils.writeObject(new File(FILE_PATH), myGitlet);
                }
            } else {
                myGitlet = Utils.readObject(new File(FILE_PATH), Gitlet.class);
                calls(args, ops);
                Utils.writeObject(new File(FILE_PATH), myGitlet);
            }
        } catch (GitletException error) {
            Utils.message(error.getMessage());
            System.exit(0);
        }
    }

    /** Checks if gitlet is in directory already. @return */
    public static boolean isInit() {
        return new File(System.getProperty("user.dir")
                + "/.gitlet").exists();
    }


    /** Calls. */
    /** @param ops - something
     * @param args - something
     * */
    private static void calls(String[] args, String[] ops) {
        String command = args[0];
        if (command.equals("init") && isInit()) {
            throw new GitletException("A gitlet version-control system "
                    + "already exists in the current directory.");
        } else if (command.equals("add")) {
            myGitlet.add(ops[0]);
        } else if (command.equals("commit")) {
            myGitlet.commit(ops[0]);
        } else if (command.equals("log")) {
            myGitlet.log();
        } else if (command.equals("global-log")) {
            myGitlet.globalLog();
        } else if (command.equals("checkout")) {
            if (ops.length != 1) {
                myGitlet.checkout(ops);
            } else {
                myGitlet.checkout(ops[0]);
            }
        } else if (command.equals("status")) {
            myGitlet.status();
        } else if (command.equals("rm")) {
            myGitlet.rm(ops[0]);
        } else if (command.equals("find")) {
            myGitlet.find(ops[0]);
        } else if (command.equals("branch")) {
            myGitlet.branch(ops[0]);
        } else if (command.equals("rm-branch")) {
            myGitlet.rmbranch(ops[0]);
        } else if (command.equals("reset")) {
            myGitlet.reset(ops[0]);
        } else if (command.equals("merge")) {
            myGitlet.merge(ops[0]);
        } else if (command.equals("add-remote")) {
            myGitlet.addRemote();
        } else if (command.equals("fetch")) {
            myGitlet.fetch();
        } else if (command.equals("push")) {
            myGitlet.push();
        } else if (command.equals("rm-remote")) {
            myGitlet.rmRemote();
        } else {
            throw new GitletException("No command with that name exists.");
        }
    }

    /** Repository. */
    private static Gitlet myGitlet;
    /** Path to that Gitlet's file. */
    private static final String FILE_PATH = ".gitlet/myrepo";
}
