/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Damith
 */
public class GServer extends Observable implements Runnable {

    String stream; ServerSocket ss; 
    
    public GServer(){
        try {
            ss = new ServerSocket(7000);
        } catch (IOException ex) {
            System.out.println("Error Establishing New Server Connection");        }
    }
    @Override
    public void run() {
        while(true){
            try {
                try (Socket s = ss.accept()) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    stream = r.readLine();
                    if(stream != null){
                        this.setChanged();
                        notifyObservers();
                        this.clearChanged();
                    }
                   
                }
            } catch (IOException ex) {
                System.out.println("Input Output Error at Server Connection");            }

        }
    }
    
    public String getInboundString(){
        return stream;
    }
    
}
