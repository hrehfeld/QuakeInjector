package de.haukerehfeld.quakeinjector;

import java.io.IOException;
import java.util.Properties;

public final class BuildCommit {
      private static String buildCommit = "";

      public static String getBuildCommit(){
            if (buildCommit == null || buildCommit.isEmpty()){
                  Properties properties = new Properties();
                  try {
                        System.out.println("No build commit found, attempting to read from props");
                        properties.load(BuildCommit.class.getClassLoader().getResourceAsStream(
                                "build-info.properties"));

                  } catch (IOException e) {
                        e.printStackTrace();
                  }

                  buildCommit = properties.getProperty("quake-injector.build-commit");
                  System.out.println("Build commit is " + buildCommit);
            }
            return buildCommit;
      }
}
