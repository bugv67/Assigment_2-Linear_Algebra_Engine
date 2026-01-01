package spl.lae;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

import parser.*;

public class Main {
  public static void main(String[] args) throws IOException {
    // TODO: maiN
    String arg1 = args[0];
    String arg2 = args[1];
    String arg3 = args[2];

    InputParser parser = new InputParser();
    try {
      ComputationNode root = parser.parse(args[1]);
      LinearAlgebraEngine lae = new LinearAlgebraEngine(Integer.parseInt(args[0]));
      ComputationNode result = lae.run(root);
      OutputWriter.write(result.getMatrix(), args[2]);
      System.out.println(lae.getWorkerReport());

    } catch (ParseException e) {
      OutputWriter.write(e.getMessage(), args[2]);
      // throw new IOException(e.getMessage());
    } catch (Exception e) {
      OutputWriter.write(e.getMessage(), args[2]);
      // throw new IOException("Something went wrong: " + e.getMessage());
    } finally {
      System.out.println(" Finished main.. ");
      // lae.executor.shutdown();
    }

  }
}
