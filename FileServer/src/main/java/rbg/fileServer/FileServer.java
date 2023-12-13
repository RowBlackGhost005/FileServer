package rbg.fileServer;

import rbg.fileServer.gui.SelectionScreen;

/**
 * Application for passing files between two computers using the internet.
 * 
 * @author Luis Marin
 */
public class FileServer {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new SelectionScreen().setVisible(true);
        });
    }
}
