package com.RHCCServer.Router;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

public class WorkerThread implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;

    public WorkerThread(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            
            output.write(("Madhur Kapoor").getBytes());

            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}