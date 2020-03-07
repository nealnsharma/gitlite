package gitlet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Date;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/** Commit class.
 * @author neal sharma */
public class Commit implements Serializable {
    /** A commit format. */
    /**@param initialCommit - sdfsdf
     * @param message - sdfsdf
     * @param allParents - sdsdfsd
     * @param blobs - as;dfjasdf
     * */
    public Commit(boolean initialCommit, String message, String[] allParents,
                  HashMap<String, String> blobs) {
        _message = message;
        _blobs = blobs;
        _allParents = allParents;
        if (initialCommit) {
            _timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            _timestamp = date.format(new Date()) + "-0800";
        }
        _hashID = hashFunc();
    }

    /** Initial commit. */
    /** @return */
    public static Commit initialize() {
        return new Commit(true, "initial commit", null, null);
    }

    /** Hash func. */
    /** @return */
    public String hashFunc() {
        String files;
        if (_blobs == null) {
            files = "";
        } else {
            files = _blobs.toString();
        }
        String parents = Arrays.toString(_allParents);
        return Utils.sha1(_message, files, _timestamp, parents);
    }
    /** Blobs. */
    /** @return */
    public HashMap<String, String> getBlobs() {
        return _blobs;
    }
    /** Get commit message. */
    /** @return */
    public String getMessage() {
        return _message;
    }
    /** Timestamp.  */
    /** @return */
    public String getTimestamp() {
        return _timestamp;
    }
    /** All parents. */
    /** @return */
    public String[] getAllParents() {
        return _allParents;
    }
    /** First parent ID of commit. */
    /** @return */
    public String getFirstParentID() {
        if (_allParents == null) {
            return null;
        }
        return _allParents[0];
    }
    /** Get hash. */
    /** @return */
    public String getHashID() {
        return _hashID;
    }
    /** The date.*/
    private static SimpleDateFormat date =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
    /** The commit message.*/
    private String _message;
    /** Blobs.*/
    private HashMap<String, String> _blobs;
    /** Hash. */
    private String _hashID;
    /** The timestamp.*/
    private String _timestamp;
    /** Parents. */
    private String[] _allParents;
    /** The date format. */

}
