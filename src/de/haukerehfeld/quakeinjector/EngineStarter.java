package de.haukerehfeld.quakeinjector;

import java.io.File;
import java.util.ArrayList;

public class EngineStarter {
	private File quakeDir;
	private File quakeExe;
	private String quakeCmdline;

	public EngineStarter(File quakeDir, File quakeExe, String quakeCmdline) {
		this.quakeDir = quakeDir;
		this.quakeExe = quakeExe;
		this.quakeCmdline = quakeCmdline;
	}

	public void start(String mapCmdline, String startmap) throws java.io.IOException {
		ArrayList<String> cmd = new ArrayList<String>(5);

		cmd.add(quakeExe.getAbsolutePath());
		//processbuilder doesn't like arguments with spaces
		if (quakeCmdline != null) {
			for (String s: quakeCmdline.split(" ")) { cmd.add(s); }
		}
		if (mapCmdline != null) {
			for (String s: mapCmdline.split(" ")) { cmd.add(s); }
		}
		cmd.add("+map");
		cmd.add(startmap);
		
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(quakeDir);

		System.out.println(cmd);
		
		Process p = pb.start();		
	}

	public void setQuakeDirectory(File dir) {
		this.quakeDir = dir;
	}

	public void setQuakeExecutable(File exe) {
		this.quakeExe = exe;
	}

	public void setQuakeCommandline(String cmdline) {
		this.quakeCmdline = cmdline;
	}
}