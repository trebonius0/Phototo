package photato.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandLineHelper {

    public static class CommandLineResult {

        public final String outlog;
        public final String errlog;

        public CommandLineResult(String outlog, String errlog) {
            this.outlog = outlog;
            this.errlog = errlog;
        }

    }

    public static CommandLineResult runCommandLine(String commandLine) throws IOException, InterruptedException {
        Process p;
        if (OsHelper.isWindows()) {
            p = Runtime.getRuntime().exec(commandLine);
        } else {
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", commandLine);
            p = pb.start();
        }

        String outlog;
        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"))) {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = input.readLine()) != null) {
                builder.append(line).append("\n");
            }
            outlog = builder.toString();
        }

        String errlog;
        try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream(), "UTF-8"))) {
            String line;
            StringBuilder errBuilder = new StringBuilder();
            while ((line = err.readLine()) != null) {
                errBuilder.append(line).append("\n");
            }
            errlog = errBuilder.toString();
        }
        p.waitFor();

        return new CommandLineResult(outlog, errlog);
    }
}
