import it.unisa.p2p.gitprotocol.GitProtocolImpl;
import it.unisa.p2p.gitprotocol.operations.OperationMessages;
import it.unisa.p2p.gitprotocol.storage.StorageDHT;
import java.io.*;
import java.util.Arrays;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.logging.Logger;

public class AppTest {

    private StorageDHT storage;

    private GitProtocolImpl gitProtocol;
    private GitProtocolImpl gitProtocol1;
    private GitProtocolImpl gitProtocol2;
    private GitProtocolImpl gitProtocol3;
    private final static String INITIAL_STRING = "String 1";
    private final static String SECOND_STRING = "String 2";
    private final static String SEC_INITIAL_STRING = "String 3";
    private final static String BOOTSTRAP_HN = "127.0.0.1";

    private final static Logger log = Logger.getLogger(AppTest.class.getName());
    private final static Integer MASTER_PEER_ID = 0;
    private final static Integer PEER_ID_1 = 1;
    private final static Integer PEER_ID_2 = 2;
    private final static Integer PEER_ID_3 = 3;
    private final static String REPO_NAME = "REPO_TEST";
    private static final String DIRECTORY = "testfiles/";

    private static final String REPO_DIR_MASTER = "repositoryMaster/";
    private static final File REPO_MASTER = new File(DIRECTORY + REPO_DIR_MASTER);
    private static final File REPO_FILE_MASTER = new File(DIRECTORY + REPO_DIR_MASTER + "file0.txt");
    private static final File SEC_REPO_FILE_MASTER = new File(DIRECTORY + REPO_DIR_MASTER + "file1.txt");

    private static final String REPO_DIR_PEER1 = "repositoryPeer1/";
    private static final File REPO_PEER1 = new File(DIRECTORY + REPO_DIR_PEER1);
    private static final File REPO_FILE_PEER1 = new File(DIRECTORY + REPO_DIR_PEER1 + "file0.txt");
    private static final File SEC_REPO_FILE_PEER1 = new File(DIRECTORY + REPO_DIR_PEER1 + "file1.txt");

    private static final String REPO_DIR_PEER2 = "repositoryPeer2/";
    private static final File REPO_PEER2 = new File(DIRECTORY + REPO_DIR_PEER2);
    private static final File REPO_FILE_PEER2 = new File(DIRECTORY + REPO_DIR_PEER2 + "file0.txt");
    private static final File SEC_REPO_FILE_PEER2 = new File(DIRECTORY + REPO_DIR_PEER2 + "file1.txt");

    private static final String REPO_DIR_PEER3 = "repositoryPeer3/";
    private static final File REPO_PEER3 = new File(DIRECTORY + REPO_DIR_PEER3);
    private static final File REPO_FILE_PEER3 = new File(DIRECTORY + REPO_DIR_PEER3 + "file0.txt");
    private static final File SEC_REPO_FILE_PEER3 = new File(DIRECTORY + REPO_DIR_PEER3 + "file1.txt");

    public void setUpMaster() throws Exception {
        log.info("Creating master node");
        storage = new StorageDHT(MASTER_PEER_ID, 4000, BOOTSTRAP_HN, 4000);
        gitProtocol = new GitProtocolImpl(storage);

        //Gitprotocol class not null                                
        assertNotNull(gitProtocol);

        writeSingleLine(REPO_FILE_MASTER, INITIAL_STRING);
    }

    public void setUpPeer1() throws Exception {
        log.info("Creating peer 1 node");
        storage = new StorageDHT(PEER_ID_1, 4001, BOOTSTRAP_HN, 4000);

        gitProtocol1 = new GitProtocolImpl(storage);
        //Gitprotocol class not null                                
        assertNotNull(gitProtocol1);  
    }
    public void setUpPeer2() throws Exception {
        log.info("Creating peer 2 node");
        storage = new StorageDHT(PEER_ID_2, 4002, BOOTSTRAP_HN, 4000);
        gitProtocol2 = new GitProtocolImpl(storage);

        //Gitprotocol class not null 
        assertNotNull(gitProtocol2);  
        //writeSingleLine(REPO_FILE_PEER2, INITIAL_STRING);
    }
    public void setUpPeer3() throws Exception {
        log.info("Creating peer 3 node");
        storage = new StorageDHT(PEER_ID_3, 4003, BOOTSTRAP_HN, 4000);
        gitProtocol3 = new GitProtocolImpl(storage);

        //Gitprotocol class not null 
        assertNotNull(gitProtocol3);  
        //writeSingleLine(REPO_FILE_PEER3, INITIAL_STRING);
    }

    @Test
    public void testCommitPullPush() throws Exception {

        //Set up of environment
        setUpMaster();
        
        //File gets correct text
        assertEquals(readSingleLine(REPO_FILE_MASTER), INITIAL_STRING);    

        log.info("Creating first commit");

        //Repository created correctly
        assertTrue(gitProtocol.createRepository(REPO_NAME, REPO_MASTER));
        assertEquals(1, gitProtocol.getCommits().size());

        log.info("Creating again repo");
        //Cannot create again repository
        assertFalse(gitProtocol.createRepository(REPO_NAME, REPO_MASTER));
        assertEquals(1, gitProtocol.getCommits().size());

        log.info("Trying to make a pull: it should not work");
        //Trying to pull a repo not present in dht
        assertEquals(gitProtocol.pull(REPO_NAME), OperationMessages.REPOSITORY_NOT_FOUND);

        log.info("Making first push");
        //Pushing repository to save it on dht
        assertEquals(gitProtocol.push(REPO_NAME), OperationMessages.PUSH_MESSAGE);

        log.info("Pulling repo");
        //Pulling repository (no changes)
        assertEquals(gitProtocol.pull(REPO_NAME), OperationMessages.NO_FILE_CHANGED);

        log.info("Now let's edit the file a little");
        assertEquals(1, gitProtocol.getCommits().size());
        //Writing a line in file 0
        writeSingleLine(REPO_FILE_MASTER, SECOND_STRING);  

        //Check on file to verify the string was written correctly
        assertEquals(readSingleLine(REPO_FILE_MASTER), SECOND_STRING);  

        log.info("Commit and pull");
        //New commit
        assertTrue(gitProtocol.commit(REPO_NAME, "First edit")); 
        assertEquals(2, gitProtocol.getCommits().size());

        //Check againg on same string
        assertEquals(readSingleLine(REPO_FILE_MASTER), SECOND_STRING);  

        //Pull to delete last commit
        log.info("Pulling repo: it should delete last commit");
        assertEquals(OperationMessages.PULL_CONFLICT, gitProtocol.pull(REPO_NAME));
        assertEquals(OperationMessages.PULL_MESSAGE, gitProtocol.pull(REPO_NAME));
        assertEquals(readSingleLine(REPO_FILE_MASTER), INITIAL_STRING);
        gitProtocol.getCommits().forEach(System.out::println);
        assertEquals(1, gitProtocol.getCommits().size());
        gitProtocol.getFiles().forEach(System.out::println);
        assertEquals(1, gitProtocol.getFiles().size());

        //Test putting a second file in repository
        log.info("Writing second file");
        writeSingleLine(SEC_REPO_FILE_MASTER, SEC_INITIAL_STRING);
        assertEquals(SEC_INITIAL_STRING, readSingleLine(SEC_REPO_FILE_MASTER));

        assertTrue(gitProtocol.commit(REPO_NAME, "Another commit"));
        log.info("Now I try to make a commit (it should not work)");
        assertEquals(1, gitProtocol.getFiles().size());

        assertFalse(gitProtocol.commit(REPO_NAME, "Not a valid commit, second file is not tracked"));

        log.info("Adding second file to repo");
        assertTrue(gitProtocol.addFilesToRepository(REPO_NAME, Arrays.asList(SEC_REPO_FILE_MASTER)));

        log.info("Doing another commit");
        assertTrue(gitProtocol.commit(REPO_NAME, "Commit with new file"));

        log.info("Commit done");
        assertEquals(2, gitProtocol.getFiles().size());
        assertEquals(3, gitProtocol.getCommits().size());

        log.info("Making second push");
        //Pushing repository to save it on dht
        assertEquals(gitProtocol.push(REPO_NAME), OperationMessages.PUSH_MESSAGE);

        //Creating repository in Peer1
        log.info("Creating Repository on peer 1");
        setUpPeer1();

        log.info("Creating first commit on peer1");

        //Repository created correctly
        assertTrue(gitProtocol1.createRepository(REPO_NAME, REPO_PEER1));
        assertEquals(1, gitProtocol1.getCommits().size());

        //Trying to push without pulling first
        log.info("Pushing without pulling first(error)");
        assertEquals(OperationMessages.PULL_REQUIRED, gitProtocol1.push(REPO_NAME));

        log.info("Pulling repo created on master to peer1");
        //Pulling repository
        assertEquals(OperationMessages.PULL_CONFLICT, gitProtocol1.pull(REPO_NAME));
        assertEquals(OperationMessages.PULL_MESSAGE, gitProtocol1.pull(REPO_NAME));
        assertEquals(readSingleLine(REPO_FILE_PEER1), INITIAL_STRING);

        //Checking commits and files pulled
        log.info("Checking pulled commits and files on peer1");
        assertEquals(3, gitProtocol1.getCommits().size());
        gitProtocol1.getFiles().forEach(System.out::println);
        assertEquals(2, gitProtocol1.getFiles().size());
        assertEquals(readSingleLine(REPO_FILE_PEER1), INITIAL_STRING);
        assertEquals(readSingleLine(SEC_REPO_FILE_PEER1), SEC_INITIAL_STRING);
        log.info("Checking commit to verify the version is the latest one");
        assertTrue(checkCommit(gitProtocol, gitProtocol1));

        //Creating repository in Peer2
        log.info("Creating Repository on peer2");
        setUpPeer2();

        log.info("Creating first commit on peer2");

        //Repository created correctly
        assertTrue(gitProtocol2.createRepository(REPO_NAME, REPO_PEER2));
        assertEquals(1, gitProtocol2.getCommits().size());

        //Trying to push without pulling first
        log.info("Pushing without pulling first(error)");
        assertEquals(OperationMessages.PULL_REQUIRED, gitProtocol2.push(REPO_NAME));

        log.info("Pulling repo created on master to peer2");
        //Pulling repository
        assertEquals(OperationMessages.PULL_CONFLICT, gitProtocol2.pull(REPO_NAME));
        assertEquals(OperationMessages.PULL_MESSAGE, gitProtocol2.pull(REPO_NAME));
        assertEquals(readSingleLine(REPO_FILE_PEER2), INITIAL_STRING);

        //Checking commits and files pulled
        log.info("Checking pulled commits and files on peer2");
        assertEquals(3, gitProtocol2.getCommits().size());
        gitProtocol2.getFiles().forEach(System.out::println);
        assertEquals(2, gitProtocol2.getFiles().size());
        assertEquals(readSingleLine(REPO_FILE_PEER2), INITIAL_STRING);
        assertEquals(readSingleLine(SEC_REPO_FILE_PEER2), SEC_INITIAL_STRING);
        log.info("Checking commit to verify the version is the latest one");
        assertTrue(checkCommit(gitProtocol, gitProtocol2));

        //Creating repository in Peer3
        log.info("Creating Repository on peer3");
        setUpPeer3();

        log.info("Creating first commit on peer3");

        //Repository created correctly
        assertTrue(gitProtocol3.createRepository(REPO_NAME, REPO_PEER3));
        assertEquals(1, gitProtocol3.getCommits().size());

        //Trying to push without pulling first
        log.info("Pushing without pulling first(error)");
        assertEquals(OperationMessages.PULL_REQUIRED, gitProtocol3.push(REPO_NAME));

        log.info("Pulling repo created on master to peer3");
        //Pulling repository
        assertEquals(OperationMessages.PULL_CONFLICT, gitProtocol3.pull(REPO_NAME));
        assertEquals(OperationMessages.PULL_MESSAGE, gitProtocol3.pull(REPO_NAME));
        assertEquals(readSingleLine(REPO_FILE_PEER3), INITIAL_STRING);

        //Checking commits and files pulled
        log.info("Checking pulled commits and files on peer3");
        assertEquals(3, gitProtocol3.getCommits().size());
        gitProtocol3.getFiles().forEach(System.out::println);
        assertEquals(2, gitProtocol3.getFiles().size());
        assertEquals(readSingleLine(REPO_FILE_PEER3), INITIAL_STRING);
        assertEquals(readSingleLine(SEC_REPO_FILE_PEER3), SEC_INITIAL_STRING);
        log.info("Checking commit to verify the version is the latest one");
        assertTrue(checkCommit(gitProtocol, gitProtocol3)); 
    }

    boolean checkCommit(GitProtocolImpl g1, GitProtocolImpl g2) {

        boolean equal = true;

        if (g1.getCommits().size() != g2.getCommits().size())
            return false;

        for (int i = 0; i < g1.getCommits().size(); i++) {
            if (!g1.getCommits().get(i).equals(g2.getCommits().get(i)))
                equal = false;
        }

        return equal;
    }


    @AfterEach
    public void tearDown() throws Exception {

        log.info("Deleting files created on each peer repository");
        if (REPO_FILE_MASTER.exists() && SEC_REPO_FILE_MASTER.exists()) {
            REPO_FILE_MASTER.delete();
            SEC_REPO_FILE_MASTER.delete();
        }

        if (REPO_FILE_PEER1.exists() && SEC_REPO_FILE_PEER1.exists()) {
            REPO_FILE_PEER1.delete();
            SEC_REPO_FILE_PEER1.delete();
        }

        if (REPO_FILE_PEER2.exists() && SEC_REPO_FILE_PEER2.exists()) {
            REPO_FILE_PEER2.delete();
            SEC_REPO_FILE_PEER2.delete();
        }

        if (REPO_FILE_PEER3.exists() && SEC_REPO_FILE_PEER3.exists()) {
            REPO_FILE_PEER3.delete();
            SEC_REPO_FILE_PEER3.delete();
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