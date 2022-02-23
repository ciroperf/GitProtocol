package it.unisa.p2p.gitprotocol.storage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import it.unisa.p2p.gitprotocol.repository.GitRepository;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;



import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.dht.FutureGet;

/**
 * class that represents a storage on dht
 */
public class StorageDHT implements StorageInterface<String, GitRepository>{
    
    final private PeerDHT peer;

    public StorageDHT(int peerId, int port, String bootStrapHostName, int bootStrapPort) throws IOException, Exception {

        peer = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(peerId)).ports(port).start()).start();

        FutureBootstrap bs = peer.peer().bootstrap().inetAddress(InetAddress.getByName(bootStrapHostName)).ports(bootStrapPort).start();
        bs.awaitUninterruptibly();
        if (bs.isSuccess()) {
            peer.peer().discover().peerAddress(bs.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        } else {
			throw new Exception("Error in master peer bootstrap.\n");
		}
    }

    /** 
     * Put a repository in the DHT with a key String.
     * @param key Sting, of the data to store
     * @param data Repository data to store
     * @return boolean, true if ok, false otherwise
     * @throws IOException 
     */
    public boolean put(String key, GitRepository data) throws IOException {
        try {
			FutureGet futureGet = peer.get(Number160.createHash(key)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty()) 
				peer.put(Number160.createHash(key)).data(new Data(data)).start().awaitUninterruptibly();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
    }


    /** 
     * Retrieve a GitRepository with String key
     * @param key String, key of the data to find
     * @return GitRepository, returns the repository by key
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
    public GitRepository get(String key) throws ClassNotFoundException, IOException {
        FutureGet futureGet = peer.get(Number160.createHash(key)).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            Collection<Data> dataMapValues = futureGet.dataMap().values();
            if (dataMapValues.isEmpty()) {
                return null;
            }
            return (GitRepository) futureGet.dataMap().values().iterator().next().object();
        }
        return null;
    }
}
