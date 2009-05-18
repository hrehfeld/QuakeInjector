package de.haukerehfeld.quakeinjector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * this class provides functions used to generate a relative path
 * from two absolute paths
 * @author David M. Howard
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
	private static List<String> getPathList(File f) {
		List<String> list = new ArrayList<String>();
		File parent;
		try {
			parent = f.getCanonicalFile();
			while (parent != null) {
				list.add(parent.getName());
				parent = parent.getParentFile();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			list = null;
		}
		return list;
	}

	/**
	 * figure out a string representing the relative path of
	 * 'f' with respect to 'r'
	 * @param home home path list
	 * @param file path of file list
	 */
	private static String matchPathLists(List<String> home, List<String> file) {
		int i;
		int j;
		String result = "";
		// start at the beginning of the lists
		// iterate while both lists are equal
		i = home.size()-1;
		j = file.size()-1;

		// first eliminate common root
		while((i >= 0) && (j >= 0) && (home.get(i).equals(file.get(j)))) {
			i--;
			j--;
		}

		// for each remaining level in the home path, add a ..
		for(; i>=0; i--) {
			result += ".." + File.separator;
		}

		// for each level in the file path, add the path
		for(; j>=1; j--) {
			result += file.get(j) + File.separator;
		}

		if (j >= 0) {
			// file name doesn't need separator at the end
			result += file.get(j);
		}
		return result;
	}

	/**
	 * get relative path of File 'f' with respect to 'home' directory
	 * example : home = /a/b/c
	 *           f    = /a/d/e/x.txt
	 *           s = getRelativePath(home,f) = ../../d/e/x.txt
	 * @param home base path, should be a directory, not a file, or it doesn't
make sense
	 * @param f file to generate path for
	 * @return path from home to f as a string
	 */
	public static String getRelativePath(File home, File f){
		File r;
		List<String> homelist;
		List<String> filelist;
		String s;

		homelist = getPathList(home);
		filelist = getPathList(f);

		s = matchPathLists(homelist,filelist);

		return s;
	}
}