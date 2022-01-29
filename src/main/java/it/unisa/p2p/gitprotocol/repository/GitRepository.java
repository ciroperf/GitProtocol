package it.unisa.p2p.gitprotocol.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.codec.digest.DigestUtils;
import it.unisa.p2p.gitprotocol.operations.CommitOperation;
import it.unisa.p2p.gitprotocol.operations.OperationMessages;
import it.unisa.p2p.gitprotocol.repository.exception.NotADirectoryException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class GitRepository implements Serializable {

    private static final long SerialVersionUID = 1L;
    
    private final ArrayList<File> fileList;
    private final Map<File, byte[]> fileHashmap;
    private ArrayList<CommitOperation> commitList;
    private String repositoryName;
    private String repositoryDirectory;
    private String digest;

    /**
     * create a repository and execute the first commit
     * @param repositoryName name of repository
     * @param repositoryDirectory name of directory
     * @throws NoSuchAlgorithmException 
     * @throws IOException
     * @throws NotADirectoryException thrown if repositoryDirectory is not a directory
     */
    public GitRepository(String repositoryName, String repositoryDirectory) throws NoSuchAlgorithmException, IOException, NotADirectoryException {
        this.repositoryName = repositoryName;
        this.repositoryDirectory = repositoryDirectory;
        this.fileList = getAllFiles();
        this.commitList = new ArrayList<CommitOperation>();
        this.digest = getFolderDigest();
        
        commitList.add(new CommitOperation(repositoryName, OperationMessages.FIRST_COMMIT_MESSAGE, this.digest));
        this.fileHashmap = new HashMap<>();
        for (File f: this.fileList) {
            this.fileHashmap.put(f, Files.readAllBytes(f.toPath()));
        }
    }

    /**
     * sets the directory for the repository
     * @param repositoryDirectory String
     */
    public void setRepositoryDirectory(String repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory;
    }

    /**
     * returns the name of the repository
     * @return String
     */
    public String getRepositoryName() {
        return this.repositoryName;
    }

    /**
     * return the directory of the repository
     * @return String
     */
    public String getRepositoryDirectory() {
        return this.repositoryDirectory;
    }

    /**
     * return the list of file in the repository
     * @return ArrayList<File>
     */
    public ArrayList<File> getFileList() {
        return (ArrayList<File>) this.fileList.clone();
    }

    /**
     * return the HashMap of file of the repository
     * @return HashMap<File, byte[]>
     */
    public Map<File, byte[]> getFileHashmap() {
        return this.fileHashmap;
    }

    /**
     * return the digest
     * @return String 
     */
    public String getDigest() {
        return this.digest;
    }

    /**
     * returns the list of commits
     * @return ArrayList of CommitOperation
     */
    public ArrayList<CommitOperation> getCommitList() {
        if (this.commitList != null) {
            return (ArrayList<CommitOperation>) this.commitList.clone();
        } else {
            return null;
        }
    }

    /**
     * sets a list of commits
     * @param commitList ArrayList of commits to be set
     */
    public void setCommitList(ArrayList<CommitOperation> commitList) {
        if (commitList != null) {
            this.commitList = commitList;
        }
    }

    /**
     * Adds new file to repository
     * @param files ArrayList of files to be added
     * @return true if file are added, false otherwise
     * @throws IOException
     */
    public boolean addFiles(List<File> files) throws IOException{
        boolean bool[] = {true};

        if (files.size() == 0) {
            return false;
        }

        files.parallelStream().filter(f -> f.getAbsolutePath().startsWith(this.repositoryDirectory)).forEach(f -> {
            this.fileList.add(f);
            try {
                this.fileHashmap.put(f, Files.readAllBytes(Paths.get(f.toURI())));
            } catch (IOException e) {
                bool[0] = false;
                e.printStackTrace();
            }
        });
        
        return bool[0];
    }

    /**
     * adds a commit to commitList
     * @param repositoryName String, name of the repository
     * @param message String, message contained in the commit
     * @return boolean, true if commit succeded, false otherwise
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public boolean addCommit(String repositoryName, String message) throws IOException, NoSuchAlgorithmException {
        String digest = getFolderDigest();
        if (digest.equals(this.digest) && commitList.size()> 0) {
            return false;
        }
        this.digest = digest;
        CommitOperation commitOperation = new CommitOperation(repositoryName, message, digest);
        return addCommit(commitOperation);
    }

    /**
     * Adds a commit to commitList
     * @param commit CommitOperation to add
     * @return true if the commit is added, false otherwise
     */
    public boolean addCommit(CommitOperation commit) {
        return this.commitList.add(commit);
    
    }

    /**
     * Replace all the old edited files with the new one
     * @param fileHashMap HashMap with files that have to be updated locally
     * @return true if the function succeds, false otherwise
     */
    public boolean replaceFileFromHashmap(Map<File, byte[]> fileHashMap) {

        for (File file:fileHashMap.keySet()) {
            OutputStream os = null;
            File localFile;
            try {
                localFile = new File(getRepositoryDirectory() + File.pathSeparator + file.getName());
                os = new FileOutputStream(file);
                os.write(fileHashMap.get(file));
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            try {
                this.fileHashmap.put(localFile, Files.readAllBytes(localFile.toPath()));
                if (!this.fileList.contains(localFile)) {
                    this.fileList.add(localFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * Recursively search for files in the repository directory and and adds them to fileList
     * @param repositoryName repositoryName
     * @param fileList ArrayList of File
     */
    private void setFileList(String repositoryName, ArrayList<File> fileList) {
        File directory = new File(repositoryName);

        File[] _fileList = directory.listFiles();
        if (_fileList != null) {
            for (File file: _fileList) {
                if (file.isFile() && !file.getName().equals(".DS_Store")) {
                    fileList.add(file);
                } else if(file.isDirectory()) {
                    setFileList(file.getAbsolutePath(), fileList);
                }
            }
        }
    }

    /**
     * returns the ArrayList of all the files in a repository
     * @return ArrayList of FIle
     * @throws NotADirectoryException error thrown when the repositoryDirectory is a file
     */
    private ArrayList<File> getAllFiles() throws NotADirectoryException {
        ArrayList<File> fileList = new ArrayList<>();
        File repositoryDirectory = new File(this.repositoryDirectory);
        if (!repositoryDirectory.isDirectory()) {
            throw new NotADirectoryException("Base Path is not a directory");
        }

        setFileList(repositoryDirectory.getAbsolutePath(), fileList);
        return fileList;    
    }

    /**
     * returns the digest of all files committed
     * @return ArrayList of digest
     */
    public ArrayList<String> getDigests() {
        ArrayList<String> digestList = new ArrayList<>(commitList.size());
        commitList.parallelStream().sorted((c1, c2) -> c1.getTimestamp().compareTo(c2.getTimestamp())).forEach(c -> digestList.add(c.getDigest()));
        return digestList;
    }

    /**
     * Recursive method to list get all the inputStreams from a folder and its subfolders
     * @param directory repository directory
     * @param fileStreamList ArrayList of files
     * @param includeHiddenFiles set this parameter at true if you want to include hidden files, false otherwise
     */
    private void getInputStreams(File directory, List<FileInputStream> fileStreamList, boolean includeHiddenFiles) {
        File[] fileList = directory.listFiles();

        assert (fileList != null);

        Arrays.sort(fileList, (f1, f2) -> f1.getName().compareTo(f2.getName()));

        for (File f: getFileList()) {
            if (includeHiddenFiles || !f.getName().startsWith(".")) {
                if (f.isDirectory()) {
                    getInputStreams(f, fileStreamList, includeHiddenFiles);
                } else {
                    try {
                        fileStreamList.add(new FileInputStream(f));
                    } catch (FileNotFoundException e) {
                        throw new AssertionError(e.getMessage());
                    }
                }
            }
        }
        
    }

    /**
     * returns the digest of a folder, encrypted with md5
     * @return digest of the directory
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public String getFolderDigest() throws NoSuchAlgorithmException, IOException {
        File directory = new File(repositoryDirectory);
        assert(directory.isDirectory());
        Vector<FileInputStream> fileStreams = new Vector<>();
        getInputStreams(directory, fileStreams, false);
        SequenceInputStream sequenceInputStream = new SequenceInputStream(fileStreams.elements());
        try {
            String md5Hash = DigestUtils.md5Hex(sequenceInputStream);
            sequenceInputStream.close();
            return md5Hash;
        } catch (IOException e) {
            throw new RuntimeException("Error in" + directory.getAbsolutePath(), e);
        }
    }
}
