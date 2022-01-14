package it.unisa.p2p.gitprotocol.repository.exception;

import java.io.IOException;

public class NotADirectoryException extends IOException{
    public NotADirectoryException(String message) {
        super(message);
    }
}
