package spl.lae;

import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
  public static void main(String[] args) throws IOException {
    // TODO: main
    LinearAlgebraEngine lae = new LinearAlgebraEngine(Integer.parseInt(args[0]));
    InputParser parser = new InputParser();
    try {
      ComputationNode root = parser.parse(args[1]);
      ComputationNode result = lae.run(root);

      OutputWriter.write(result.getMatrix(), args[2]);

    } catch (ParseException e) {
      OutputWriter.write(e.getMessage(), args[2]);
      // throw new IOException(e.getMessage());
    } catch (Exception e) {
      OutputWriter.write(e.getMessage(), args[2]);
      // throw new IOException("Something went wrong: " + e.getMessage());
    }

  }
}
