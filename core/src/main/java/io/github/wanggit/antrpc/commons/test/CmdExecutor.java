package io.github.wanggit.antrpc.commons.test;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class CmdExecutor {

    public static String executeWindowsCmdReturnResult(String cmd) throws IOException {
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + cmd);
            bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
            StringBuilder builder = new StringBuilder();
            while (true) {
                String line = bufferedReader.readLine();
                if (null == line) {
                    break;
                }
                builder.append(line).append(System.lineSeparator());
            }
            return builder.toString();
        } finally {
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void executeWindowsCmdAtPathAndPrintResultToConsole(String cmd)
            throws IOException {
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + cmd);
            bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
            while (true) {
                String line = bufferedReader.readLine();
                if (null == line) {
                    break;
                }
                System.out.println(line);
            }
        } finally {
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
