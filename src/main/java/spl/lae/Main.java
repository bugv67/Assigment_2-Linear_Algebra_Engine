package spl.lae;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

import parser.*;

public class Main {
  public static void main(String[] args) throws IOException {
    // TODO: maiN
    // System.out.println(" Starting main.. "); // SpecialPrint
    String arg1 = args[0];
    System.out.println(" args 1:  " + arg1); // SpecialPrint
    String arg2 = args[1];
    System.out.println(" args 2:  " + arg2); // SpecialPrint
    String arg3 = args[2];
    System.out.println(" args 3:  " + arg3); // SpecialPrint

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
      System.out.println(" Finished main.. "); // SpecialPrint
      // lae.executor.shutdown();
    }

  }
}
