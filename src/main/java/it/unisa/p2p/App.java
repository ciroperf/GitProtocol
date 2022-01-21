package it.unisa.p2p;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import it.unisa.p2p.gitprotocol.GitProtocol;
import it.unisa.p2p.gitprotocol.GitProtocolImpl;
import it.unisa.p2p.gitprotocol.storage.StorageDHT;

public class App {

    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
	private static String master;

	@Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
	private static int id;

	private static Logger logger = Logger.getLogger(App.class.getName());

	public static void main(String[] args) throws Exception {
		
		GitProtocol gitProtocol = null;
		App app = new App();
		final CmdLineParser parser = new CmdLineParser(app);  
		
		try {  
			parser.parseArgument(args);
			gitProtocol = new GitProtocolImpl(new StorageDHT(id, 4000, "127.0.0.1", 4000));
			TextIO textIO = TextIoFactory.getTextIO();
			TextTerminal terminal = textIO.getTextTerminal();
			terminal.printf("\nStaring peer id: %d on master node: %s\n", id, master);
			boolean running = true;

			while(running) {
				printMenu(terminal);
				int option = textIO.newIntInputReader()
						.withMaxVal(6)
						.withMinVal(1)
						.read("> Option");
					
				logger.info("option " + option);
				switch (option) {
					case 1:
						terminal.printf("\nENTER REPOSITORY NAME\n");
						logger.info("creating Repository");
						String repositoryName = textIO.newStringInputReader()
					        .withDefaultValue("default-repository")
					        .read(" Repository Name:");
						terminal.printf("\nENTER DIRECTORY NAME\n");
						String directoryName = textIO.newStringInputReader()
					        .withDefaultValue("default-directory")
					        .read(" Directory name:");
						logger.info("directory name: " + directoryName);
						
						File directory = new File(directoryName);
						logger.info("path:" + directory.getAbsolutePath());
						if (gitProtocol.createRepository(repositoryName, directory)) {
							terminal.printf("\nRepository created succesfully\n");
						} else {
							terminal.printf("\nError during the creation of repository\n");
						}
						break;
					case 2:
						terminal.printf("\nENTER REPOSITORY NAME\n");
						logger.info("adding files to repository");
						repositoryName = textIO.newStringInputReader()
					        .withDefaultValue("default-repository")
					        .read(" Repository Name:");
						terminal.printf("\nENTER FILE NAMES\n");
						String fileNames = textIO.newStringInputReader()
						.withDefaultValue("default-file")
						.read(" File Names:");
						String fileList[] = fileNames.split(" ");
						logger.info("input splitted: " + Arrays.toString(fileList));
						ArrayList<File> fileArrayList = new ArrayList<>();
						for (String filename: fileList) {
							File file = new File(filename);
							fileArrayList.add(file);
						}
						if (gitProtocol.addFilesToRepository(repositoryName, fileArrayList)) {
							terminal.printf("\nFiles added to repository\n");
						} else {
							terminal.printf("\nError adding files to repository\n");
						}
						break;
					case 3:
						terminal.printf("\nENTER REPOSITORY NAME\n");
						logger.info("commit");
						repositoryName = textIO.newStringInputReader()
							.withDefaultValue("default-repository")
							.read(" Repository Name:");
						terminal.printf("\nENTER MESSAGE\n");
						String message = textIO.newStringInputReader()
								.withDefaultValue("default-message")
								.read(" Commit Message:");
						logger.info("message: " + message);
						if (gitProtocol.commit(repositoryName, message)) {
							terminal.printf("\nCommit executed succesfully\n");
						} else {
							terminal.printf("\nError during the commit\n");
						}
						break;
					case 4:
						terminal.printf("\nENTER REPOSITORY NAME\n");
						logger.info("push");
						repositoryName = textIO.newStringInputReader()
							.withDefaultValue("default-repository")
							.read(" Repository Name:");
						terminal.printf("\n" + gitProtocol.push(repositoryName) + "\n");
						break;
					case 5:
						terminal.printf("\nENTER REPOSITORY NAME\n");
						logger.info("pull");
						repositoryName = textIO.newStringInputReader()
							.withDefaultValue("default-repository")
							.read(" Repository Name:");
						terminal.printf("\n" + gitProtocol.pull(repositoryName) + "\n");
						break;
					case 6:
						logger.info("exit");
						running = false;
						break;
					
				}
			}
	
		} catch (CmdLineException clEx) {  
			System.err.println("ERROR: Unable to parse command-line options: " + clEx);  
		}  
	}

	public static void printMenu(TextTerminal terminal) {
		terminal.printf("\n1 - CREATE REPOSITORY\n");
		terminal.printf("\n2 - ADD FILE TO REPOSITORY\n");
		terminal.printf("\n3 - COMMIT\n");
		terminal.printf("\n4 - PUSH\n");
		terminal.printf("\n5 - PULL\n");
		terminal.printf("\n6 - EXIT\n");

	}
			
}
