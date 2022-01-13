package it.unisa.p2p.gitprotocol;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;


public class GitProtocolImpl implements GitProtocol {

    final private Peer peer;
    final private PeerDHT _dht;
    final private int DEFAULT_MASTER_PORT=4000;
    final private ArrayList<String> repositories = new ArrayList<String>();

    public GitProtocolImpl(int _id, String _master_peer) throws Exception {
        peer= new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
		_dht = new PeerBuilderDHT(peer).start();	
		
		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}else {
			throw new Exception("Error in master peer bootstrap.");
		}
		
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean createRepository(String _repo_name, File _directory) {
        try {
			FutureGet futureGet = _dht.get(Number160.createHash(_repo_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				HashSet<PeerAddress> peers_on_repository;
				peers_on_repository = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
				for(PeerAddress peer:peers_on_repository)
				{
					FutureDirect futureDirect = _dht.peer().sendDirect(peer).object(_directory).start();
					futureDirect.awaitUninterruptibly();
				}
				
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
    }

    
    @Override
    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean commit(String _repo_name, String _message) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public String push(String _repo_name) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public String pull(String _repo_name) {
        // TODO Auto-generated method stub
        return null;
    }
    public boolean leaveNetwork() {
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
		return true;
	}
    
    
}