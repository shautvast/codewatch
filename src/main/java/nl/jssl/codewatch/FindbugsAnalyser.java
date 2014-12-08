package nl.jssl.codewatch;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class FindbugsAnalyser implements CodeAnalyser {
    static {
        // System.setErr(new PrintStream(new ByteArrayOutputStream()));// ignore findbugs errors
    }

    @Override
    public String analyse(String classFile) {
        PrintStream console = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            edu.umd.cs.findbugs.LaunchAppropriateUI.main(new String[] { "-textui", classFile });
            String result = out.toString();
            return result;
        } catch (Exception e) {
            throw new AnalysisException(e);
        } finally {
            System.setOut(console);
        }

    }
}
