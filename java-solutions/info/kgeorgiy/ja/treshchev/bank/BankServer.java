package info.kgeorgiy.ja.treshchev.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Server application for {@link Bank}
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public final class BankServer {

    private final static int DEFAULT_PORT = 8080;

    /**
     * Method provides you to start the server-side of {@link Bank}.
     * @param args (optional) on what port should bank be binded, default value is {@code 8080}
     */
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Required not null arguments!");
            return;
        }
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Required integer value! " + e.getMessage());
                return;
            }
        }
        final Bank bank = new RemoteBank(port);
        try {
            UnicastRemoteObject.exportObject(bank, port);
            LocateRegistry.createRegistry(port);
            Naming.rebind("//localhost:" + port + "/bank", bank);
        } catch (RemoteException e) {
            System.err.println("Couldn't export object: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + e.getMessage());
        }
    }

}
