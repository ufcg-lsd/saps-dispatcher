/* (C)2020 */
package saps.dispatcher.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

import saps.dispatcher.interfaces.*;

public class DigestUtil {

  private static final Logger LOGGER = Logger.getLogger(DigestUtil.class);

  /**
   * This function gets immutable identifier based in repository and tag
   *
   * @param imageDockerInfo image docker information
   * @return immutable identifier that match with repository and tag passed
   */
  public static String getDigest(ExecutionScriptTag imageDockerInfo) throws Exception {

    String dockerRepository = imageDockerInfo.getDockerRepository();
    String dockerTag = imageDockerInfo.getDockerTag();

    String result = null;

    try {
      Process builder =
          new ProcessBuilder("bash", "./scripts/get_digest", dockerRepository, dockerTag).start();

      LOGGER.debug("Waiting for the process for execute command: " + builder.toString());
      builder.waitFor();

      if (builder.exitValue() != 0)
        throw new Exception("Process output exit code: " + builder.exitValue());

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(builder.getInputStream()))) {
        result = reader.readLine();
      }
    } catch (Exception e) {
      LOGGER.error(
          "Error while trying get digest from Docker image ["
              + dockerRepository
              + "] with tag ["
              + dockerTag
              + "].",
          e);
      throw new Exception(e);
    }

    return result;
  }
}
