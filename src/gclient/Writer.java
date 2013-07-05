/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Damith
 */
public class Writer {
    
    public void writeToPort(int port, String msg){
        try {
            try (Socket s = new Socket(Props.URL,port)) {
                PrintWriter writer=new PrintWriter(s.getOutputStream());
                
                writer.write(msg);
                writer.flush();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
