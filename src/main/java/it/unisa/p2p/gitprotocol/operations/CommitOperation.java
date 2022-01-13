package it.unisa.p2p.gitprotocol.operations;

import java.io.Serializable;

public class CommitOperation implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private String message;
    private String repositoryName;
    private final Long timestamp;
    private final String digest;

    
    /**
     * Execute commit
     * @param message message that will be showed in commit
     * @param reopositoryName name of the repository to execute commit on
     * @param digest digest
     */
    public CommitOperation(String message, String reopositoryName, String digest) {
        this.message = message;
        this.repositoryName = reopositoryName;
        this.timestamp = System.currentTimeMillis();
        this.digest = digest;
    }

    /**
     * Returns the commit message
     * @return String
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns the repository name
     * @return String
     */
    public String getRepositoryName() {
        return this.repositoryName;
    }

    /**
     * Returns the timestamp of the commit
     * @return String
     */
    public Long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Returns the commit digest
     * @return String
     */
    public String getDigest() {
        return this.digest;
    }

    /**
     * Returns a String containing commit informations
     * @return String
     */
    @Override
    public String toString() {
        return String.format("[Repository Name: %s\nCommit Message: %s at timestamp: %d\nDigest: %s]",
         getRepositoryName(), getMessage(), getTimestamp(), getDigest());
    }

    /**
     * Returns true if the object passed by parameter is equal to the the CommitOperation on which this method is called upon, false otherwise
     * @return boolean
     */
    public boolean equals(Object object) {

        if (object == null) {
            return false;
        }

        if (object.getClass() != this.getClass()) {
            return false;
        }

        CommitOperation commitOperation = (CommitOperation)object;

        if (commitOperation.getMessage().equals(this.getMessage())
         && commitOperation.getRepositoryName().equals(this.getRepositoryName())
         && commitOperation.getTimestamp().equals(this.timestamp)
         && commitOperation.getDigest().equals(this.digest))
         return true;
        
        return false;
    }

}
