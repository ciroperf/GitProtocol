import junit.framework.TestCase;
import it.unisa.p2p.gitprotocol.GitProtocolImpl;
import it.unisa.p2p.gitprotocol.operations.OperationMessages;
import it.unisa.p2p.gitprotocol.storage.StorageDHT;
import java.io.*;
import java.util.Arrays;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.logging.Logger;
//import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppTest {

    private StorageDHT storage;

    private GitProtocolImpl gitProtocol;
    private final static String INITIAL_STRING = "Initial string pushed master";
    private final static String SECOND_STRING = "Second string pushed master";
    private final static String SEC_INITIAL_STRING = "Initial string pushed peer1";
    private final static String SEC_SECOND_STRING = "Second string pushed peer1";
    private final static String BOOTSTRAP_HN = "127.0.0.1";
    private final static Logger log = Logger.getLogger(AppTest.class.getName());
    private final static Integer MASTER_PEER_ID = 0;
    private final static Integer PEER_ID_1 = 1;
    private final static Integer PEER_ID_2 = 2;
    private final static Integer PEER_ID_3 = 3;
    private final static String REPO_NAME = "REPO_TEST";
    private static final String DIRECTORY = "testfiles/";
    private static final String FAKE_REPO = "repository/";
    private static final File REPO = new File(DIRECTORY + FAKE_REPO);
    private static final File REPO_FILE = new File(DIRECTORY + FAKE_REPO + "file0.txt");
    private static final File SEC_REPO_FILE = new File(DIRECTORY + FAKE_REPO + "file1.txt");

    public void setUp() throws Exception {
        //super.setUp();
        log.info("Creating master node");
        storage = new StorageDHT(MASTER_PEER_ID, 4000, BOOTSTRAP_HN, 4000);
        gitProtocol = new GitProtocolImpl(storage);
        writeSingleLine(REPO_FILE, INITIAL_STRING);
    }

    @Test
    public void testStorage() throws Exception {
        setUp();
        assertNotNull(storage);
        //assertNotNull(storage.get("Nothing"));
    }

    @Test
    public void testCommitPullPush() throws Exception {
        setUp();
        assertNotNull(storage);                                 // storage not null
        assertNotNull(gitProtocol);                             // gitprotocol class not null
        assertEquals(readSingleLine(REPO_FILE), INITIAL_STRING);    // file gets correct text
        log.info("Creating first commit");
        assertTrue(gitProtocol.createRepository(REPO_NAME, REPO));  // repo creation ok
        assertEquals(1, gitProtocol.getCommits().size());
        log.info("Creating again repo");
        assertFalse(gitProtocol.createRepository(REPO_NAME, REPO)); // cannot create again repo
        assertEquals(1, gitProtocol.getCommits().size());
        log.info("Trying to make a pull: it should not work");
        assertEquals(gitProtocol.pull(REPO_NAME), OperationMessages.REPOSITORY_NOT_FOUND);  // i'm trying to pull a repo not in dht
        log.info("Making first push");
        assertEquals(gitProtocol.push(REPO_NAME), OperationMessages.PUSH_MESSAGE); // pushing repo
        log.info("Pulling repo");
        assertEquals(gitProtocol.pull(REPO_NAME), OperationMessages.NO_FILE_CHANGED); // pull repo (no changes)
        log.info("Now let's edit the file a little");
        assertEquals(1, gitProtocol.getCommits().size());
        writeSingleLine(REPO_FILE, SECOND_STRING);  // write a little edit in file
        assertEquals(readSingleLine(REPO_FILE), SECOND_STRING);  // was it written?
        log.info("Commit and pull");
        assertTrue(gitProtocol.commit(REPO_NAME, "A little edit")); // new commit
        assertEquals(2, gitProtocol.getCommits().size());
        assertEquals(readSingleLine(REPO_FILE), SECOND_STRING);  // unchanged
        log.info("Pulling repo: it should delete last commit");
        assertEquals(OperationMessages.PULL_CONFLICT, gitProtocol.pull(REPO_NAME));
        assertEquals(OperationMessages.PULL_MESSAGE, gitProtocol.pull(REPO_NAME));
        assertEquals(readSingleLine(REPO_FILE), INITIAL_STRING);
        gitProtocol.getCommits().forEach(System.out::println);
        assertEquals(1, gitProtocol.getCommits().size());
        gitProtocol.getFiles().forEach(System.out::println);
        assertEquals(1, gitProtocol.getFiles().size());

        // Let's put our hands on the second file
        log.info("Writing second file");
        writeSingleLine(SEC_REPO_FILE, SEC_INITIAL_STRING);
        assertEquals(SEC_INITIAL_STRING, readSingleLine(SEC_REPO_FILE));
        assertTrue(gitProtocol.commit(REPO_NAME, "Another commit"));
        log.info("Now I try to make a commit (it should not work)");
        assertEquals(1, gitProtocol.getFiles().size());
        assertFalse(gitProtocol.commit(REPO_NAME, "Not a valid commit, second file is not tracked"));
        log.info("Let's add second file to repo");
        assertTrue(gitProtocol.addFilesToRepository(REPO_NAME, Arrays.asList(SEC_REPO_FILE)));
        log.info("Let's do another commit");
        assertTrue(gitProtocol.commit(REPO_NAME, "Now I'll commit with new file"));
        log.info("Commit done");
        assertEquals(2, gitProtocol.getFiles().size());
        assertEquals(3, gitProtocol.getCommits().size());
    }


    @AfterEach
    public void tearDown() throws Exception {
        //super.tearDown();
        writeSingleLine(REPO_FILE, INITIAL_STRING);
        if (SEC_REPO_FILE.exists()) {
            SEC_REPO_FILE.delete();
        }
    }

    private String readSingleLine(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String toRet = br.readLine();
        br.close();
        return toRet;
    }

    private void writeSingleLine(File f, String line) throws IOException {
        BufferedWriter wr = new BufferedWriter(new FileWriter(f));
        wr.write(line);
        wr.close();
    }

}