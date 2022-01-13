package it.unisa.p2p;

import java.io.File;
import java.io.IOException;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class StartGitProtocol {

    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
	private static String master;

	@Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
	private static int id;

	public static void main(String[] args) throws Exception {
		StartGitProtocol gitProtocol = new StartGitProtocol();
        final CmdLineParser parser = new CmdLineParser(gitProtocol);  
		try  
		{  
			parser.parseArgument(args);  
			TextIO textIO = TextIoFactory.getTextIO();
			TextTerminal terminal = textIO.getTextTerminal();
			GitProtocol peer = new GitProtocolImpl(id, master);
			
			terminal.printf("\nStaring peer id: %d on master node: %s\n", id, master);
			while(true) {
				printMenu(terminal);
				
				int option = textIO.newIntInputReader().withMaxVal(6).withMinVal(1).read("Option");

				switch (option) {
				case 1:
					terminal.printf("\nENTER TOPIC NAME\n");
					String name = textIO.newStringInputReader().withDefaultValue("default-repository").read("Name:");
					if(peer.createRepository(name, new File(repositoryPath)));
						terminal.printf("\nTOPIC %s SUCCESSFULLY CREATED\n",name);
					else
						terminal.printf("\nERROR IN TOPIC CREATION\n");
					break;
				case 2:
					terminal.printf("\nENTER TOPIC NAME\n");
					String sname = textIO.newStringInputReader()
					        .withDefaultValue("default-topic")
					        .read("Name:");
					if(peer.subscribetoTopic(sname))
						terminal.printf("\n SUCCESSFULLY SUBSCRIBED TO %s\n",sname);
					else
						terminal.printf("\nERROR IN TOPIC SUBSCRIPTION\n");
					break;
				case 4:
					terminal.printf("\nENTER TOPIC NAME\n");
					String tname = textIO.newStringInputReader()
					        .withDefaultValue("default-topic")
					        .read(" Name:");
					terminal.printf("\nENTER MESSAGE\n");
					String message = textIO.newStringInputReader()
					        .withDefaultValue("default-message")
					        .read(" Message:");
					if(peer.publishToTopic(tname,message))
						terminal.printf("\n SUCCESSFULLY PUBLISH MESSAGE ON TOPIC %s\n",tname);
					else
						terminal.printf("\nERROR IN TOPIC PUBLISH\n");

					break;
				case 3:
					terminal.printf("\nENTER TOPIC NAME\n");
					String uname = textIO.newStringInputReader()
					        .withDefaultValue("default-topic")
					        .read("Name:");
					if(peer.unsubscribeFromTopic(uname))
						terminal.printf("\n SUCCESSFULLY UNSUBSCRIBED TO %s\n",uname);
					else
						terminal.printf("\nERROR IN TOPIC UN SUBSCRIPTION\n");
					break;
				case 6:
					terminal.printf("\nARE YOU SURE TO LEAVE THE NETWORK?\n");
					boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
					if(exit) {
						peer.leaveNetwork();
						System.exit(0);
					}
					break;

				default:
					break;
				}
			}

		}  catch (CmdLineException clEx) {  
			System.err.println("ERROR: Unable to parse command-line options: " + clEx);  
		}  


	}
	public static void printMenu(TextTerminal terminal) {
		terminal.printf("\n1 - CREATE REPOSITORY\n");
		terminal.printf("\n2 - ADD FILES TO REPOSITORY\n");
		terminal.printf("\n3 - COMMIT\n");
		terminal.printf("\n4 - PUSH\n");
		terminal.printf("\n5 - PULL\n");
		terminal.printf("\n6 - EXIT\n");

	}


    
    
}