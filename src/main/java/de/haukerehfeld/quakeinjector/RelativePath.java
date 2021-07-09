package de.haukerehfeld.quakeinjector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * this class provides functions used to generate a relative path
 * from two absolute paths
 * @see http://www.devx.com/tips/Tip/13737
 */
public class RelativePath {
	/**
	 * break a path down into individual elements and add to a list.
	 * example : if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
	 * @param f input file
	 * @return a List collection with the individual elements of the path in
reverse order
	 */
	private static List<File> getPathList(File f) {
		List<File> list = new ArrayList<File>();
		File parent;
		try {
			parent = f.getCanonicalFile();
			while (parent != null) {
				list.add(parent);
				parent = parent.getParentFile();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			list = null;
		}
		return list;
	}

	private static int getFirstRootPosition(List<File> file) {
		File[] roots = File.listRoots();
		for (File root: roots) {
//			System.out.println("Root: " + root);
		}
		int i = 0;
	done: while (i < file.size() - 1) {
			for (File root: roots) {
				if (file.get(i).equals(root)) {
					break done;
				}
//				System.out.println(root + " isn't " + file.get(i));
			}
			i++;
		}
//		System.out.println("Root is " + file.get(i));
		return i;
	}
	/**
	 * figure out a string representing the relative path of
	 * 'f' with respect to 'r'
	 * @param home home path list
	 * @param file path of file list
	 */
	private static File matchPathLists(List<File> home, List<File> file) {
		if (file.size() <= 0) {
			return new File("");
		}
		if (home.size() <= 0) {
			return file.get(0);
		}
		
		// start at the beginning of the lists
		// iterate while both lists are equal
		final int homeLast = home.size() - 1;
		final int fileLast = file.size() - 1;

		

		
		int i = getFirstRootPosition(home);
		int j = getFirstRootPosition(file);

		//the root isn't identical (common on win32) - just return an absolute path
		if (!home.get(i).equals(file.get(j))) {
			// System.out.println(home);
			// System.out.println(file);
			// System.out.println(home.get(i) + " != " + file.get(j));
			return file.get(0);
		}

		// first eliminate common root
		while((i >= 0) && (j >= 0) && (home.get(i).equals(file.get(j)))) {
			i--;
			j--;
		}

		StringBuilder result = new StringBuilder();
		// for each remaining level in the home path, add a ..
		for(; i >= 0; i--) {
			result.append(".." + File.separator);
		}

		// for each level in the file path, add the path
		for(; j >= 1; j--) {
			result.append(file.get(j).getName() + File.separator);
		}

		if (j >= 0) {
			// file name doesn't need separator at the end
			result.append(file.get(j).getName());
		}
		return new File(result.toString());
	}

	/**
	 * get relative path of File 'f' with respect to 'home' directory
	 * example : home = /a/b/c
	 *           f    = /a/d/e/x.txt
	 *           s = getRelativePath(home,f) = ../../d/e/x.txt
	 * @param home base path, should be a directory, not a file, or it doesn't
make sense
	 * @param file file to generate path for
	 * @return path from home to f as a string
	 */
	public static File getRelativePath(File home, File file){
		List<File> homelist;
		List<File> filelist;

		//System.out.println("home: " + home);
		//System.out.println("file: " + file);
		homelist = getPathList(home);
		filelist = getPathList(file);

		File result = matchPathLists(homelist,filelist);
		//System.out.println("getName(): " + result.getName());
		//System.out.println("getToString(): " + result.toString());

		return result;
	}
}