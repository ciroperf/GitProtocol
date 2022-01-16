package it.unisa.p2p.gitprotocol.storage;

import java.io.IOException;

public interface StorageInterface <K,V>{
    
    boolean put(K key, V data) throws IOException;

    V get(K key) throws ClassNotFoundException, IOException;
}
