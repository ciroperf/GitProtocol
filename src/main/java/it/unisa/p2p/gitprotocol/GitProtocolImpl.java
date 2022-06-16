package it.unisa.p2p.gitprotocol;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import it.unisa.p2p.gitprotocol.operations.CommitOperation;
import it.unisa.p2p.gitprotocol.operations.OperationMessages;
import it.unisa.p2p.gitprotocol.repository.GitRepository;
import it.unisa.p2p.gitprotocol.storage.StorageDHT;


public class GitProtocolImpl implements GitProtocol {

    final private StorageDHT storage;
    private GitRepository repository;
    private boolean fetch;
    
    /**
     * Implementation of GitProtocol
     * @param storage Storage to store data
     */
    public GitProtocolImpl(StorageDHT storage) {
		this.storage = storage;
        this.repository = null;
        this. fetch = false;
    }

    /**
     * Create a repository in the directory
     * @param _repo_name String with the name of the repository
     * @param _directory File containing the directory of the repository
     * @return boolean true if the repostory creates succesfully, false otherwise
     */
    public boolean createRepository(String _repo_name, File _directory) {
        if (this.repository != null) {
            return false;
        }

        try {
            this.repository = new GitRepository(_repo_name, _directory.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Adds new files to the repository
     * @param _repo_name String, name of the repository where to store file
     * @param files List<File>, list of files to be added to the repository
     * @return boolean, true if the files are added, false otherwise
     */
    @Override
    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        try {
            if (this.repository != null && this.repository.getRepositoryName().equals(_repo_name)) {
                return this.repository.addFiles(files);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a commit to the list of commit
     * @param _repo_name String, name of the repository
     * @param _message String, message included in commit
     * @return boolean, true if the commit is added, false otherwise
     */
    @Override
    public boolean commit(String _repo_name, String _message) {
        if (this.repository == null) {
            return false;
        }

        try {
            return this.repository.addCommit(_repo_name, _message);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * executes push of the files on storage
     * @param _repo_name String, name of the repository
     * @return String with the result of the operation
     */
    @Override
    public String push(String _repo_name) {
        if (this.repository == null) {
            return OperationMessages.REPOSITORY_NOT_FOUND;
        }

        try {
            GitRepository remoteRepository = ((StorageDHT)storage).get(_repo_name);
            if (remoteRepository == null || this.repository.getDigests().contains(remoteRepository.getDigest())) {
                ((StorageDHT)this.storage).put(_repo_name, this.repository);
                return OperationMessages.PUSH_MESSAGE;
            } else {
                return OperationMessages.PULL_REQUIRED;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return OperationMessages.ERROR + ": " + e.getMessage();
        } catch (ClassNotFoundException e) {
            return OperationMessages.ERROR + ": " + e.getMessage();
        }
    }

    /**
     * @param _repo_name String name of the repository
     * @return String with the result of the operation
     */
    @Override
    public String pull(String _repo_name) {
        try {
            GitRepository repositoryPulled = ((StorageDHT)storage).get(_repo_name);
            if (repositoryPulled == null) {
                return OperationMessages.REPOSITORY_NOT_FOUND;
            }
            
            if (repositoryPulled.getDigest().equals(this.repository.getDigest())) {
                return OperationMessages.NO_FILE_CHANGED;
            }

            if (!repositoryPulled.getDigests().contains(this.repository.getDigest()) && !this.fetch) {
                this.fetch = true;
                return OperationMessages.PULL_CONFLICT;
            }

            this.repository.replaceFileFromHashmap(repositoryPulled.getFileHashmap());
            String repositoryDirectory = this.repository.getRepositoryDirectory();
            if (!fetch) {
                for (CommitOperation commitOperation: repositoryPulled.getCommitList()) {
                    if (!this.repository.getCommitList().contains(commitOperation)) {
                        this.repository.addCommit(commitOperation);
                    }
                }
            } else {
                this.repository.setCommitList(repositoryPulled.getCommitList());
            }

            this.repository.setRepositoryDirectory(repositoryDirectory);
            this.fetch = false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return OperationMessages.PULL_MESSAGE;
    }

    public String getLastDigest() {
        return this.repository.getDigest();
    }

    public List<CommitOperation> getCommits() {
        assert this.repository != null;
        return this.repository.getCommitList();
    }

    public List<File> getFiles() {
        return this.repository.getFileList();
    }
    
}