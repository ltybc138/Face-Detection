package com.denis.detection.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

public class Log {
    private PrintWriter saver = null;
    public Log() {
        // TODO fix problem with year
        String fileName = String.valueOf(Calendar.getInstance().getTime().getYear()) + "-" +
                String.valueOf(Calendar.getInstance().getTime().getMonth()) + "-" +
                String.valueOf(Calendar.getInstance().getTime().getDate()) + "-" +
                String.valueOf(Calendar.getInstance().getTime().getHours()) + "-" +
                String.valueOf(Calendar.getInstance().getTime().getMinutes());
        try {
            saver = new PrintWriter(new FileWriter("src/" + fileName + ".log"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void simpleLog(String message) {
        saver.println("[" + String.valueOf(Calendar.getInstance().getTime().getHours()) + "-" +
                String.valueOf(Calendar.getInstance().getTime().getMinutes()) + "-" +
                String.valueOf(Calendar.getInstance().getTime().getSeconds()) + "]" + "  Log: " + message);
    }

    public void errorLog(String errorMessage) {
        saver.println("[" + String.valueOf(Calendar.getInstance().getTime().getHours()) + "-" +
                String.valueOf(Calendar.getInstance().getTime().getMinutes()) + "-" +
                String.valueOf(Calendar.getInstance().getTime().getSeconds()) + "]" + "  Error log: " + errorMessage);
    }

    public void closeLogger() {
        saver.close();
    }
}
