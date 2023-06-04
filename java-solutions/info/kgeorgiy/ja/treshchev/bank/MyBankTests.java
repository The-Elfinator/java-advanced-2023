package info.kgeorgiy.ja.treshchev.bank;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 * Tests for {@link Bank} application
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public class MyBankTests {

    private static final int PORT = 8090;
    private static final String bankURL = "//localhost:" + PORT + "/bank";
    private static final boolean log = false;
    private static final List<Boolean> ok = new ArrayList<>();

    private static Bank bank;

    /**
     * Method creating new registry before running tests.
     * @throws RemoteException if some RMI error occurred.
     */
    @BeforeClass
    public static void beforeClass() throws RemoteException {
        bank = new RemoteBank(PORT);
        try {
            LocateRegistry.createRegistry(PORT);
        } catch (RemoteException e) {
            throw new RemoteException("Couldn't create registry due to error: " + e.getMessage());
        }
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
        } catch (RemoteException e) {
            throw new RemoteException("Export bank fails: " + e.getMessage());
        }
        try {
            Naming.rebind(bankURL, bank);
        } catch (MalformedURLException ignored) {
            // nothing
        }
    }

    private static synchronized void creatingPerson(final String name,
                                                   final String surname,
                                                   final String passport) throws RemoteException {
        if (log)
            System.err.printf("\tCreating a person name=%s surname=%s passport=%s%n", name, surname, passport);
        bank.createPerson(name, surname, passport);
    }

    private static synchronized void creatingAccount(final String id) throws RemoteException {
        if (log)
            System.err.printf("\tCreating a remote account with id=\"%s\"%n", id);
        bank.createAccount(id);
    }

    /**
     * Testing that local person don't see changes
     * @throws RemoteException if some RMI error occurred
     */
    @Test
    public void test1() throws RemoteException {
        final long startTime = System.currentTimeMillis();
        System.err.println("============Running test1============");
        System.err.println("Testing that local person don't see changes in remote bank");
        final String name = "User1";
        final String surname = "Test1";
        final String passport = "1";
        creatingPerson(name, surname, passport);

        if (log)
            System.err.println("\tGetting a local person");
        Person local = bank.getPerson(passport, Mode.LOCAL);
        Assert.assertNotNull(local);
        if (log)
            System.err.println("\tGetting a remote person");
        Person remote = bank.getPerson(passport, Mode.REMOTE);
        Assert.assertNotNull(remote);

        final String subId = "1";
        final String accountId = remote.getPassport() + ":" + subId;
        creatingAccount(accountId);

        Assert.assertNull(bank.getAccount(local, subId));
        Assert.assertNotNull(bank.getAccount(remote, subId));
        Assert.assertEquals(1, remote.getAccountsOfPerson().size());
        Assert.assertNotEquals(local.getAccountsOfPerson().size(), remote.getAccountsOfPerson().size());
        final long finishTime = System.currentTimeMillis();
        System.err.println("Test 1 successfully passed in " + (finishTime - startTime) + " ms");
        ok.add(true);
    }

    /**
     * Testing that local person don't see new {@link RemoteAccount}
     * @throws RemoteException if some RMI error has occurred
     */
    @Test
    public void test2() throws RemoteException {
        final long startTime = System.currentTimeMillis();
        System.err.println("============Running test2============");
        System.err.println("Testing that local person don't see if remote one creates an extra account");
        final String name = "User1";
        final String surname = "Test2";
        final String passport = "abacaba";
        creatingPerson(name, surname, passport);

        final String subId1 = "1";
        final String accountId1 = passport + ":" + subId1;
        creatingAccount(accountId1);

        if (log)
            System.err.println("\tGetting a local person");
        final Person local = bank.getPerson(passport, Mode.LOCAL);
        Assert.assertNotNull(local);
        if (log)
            System.err.println("\tGetting a remote person");
        final Person remote = bank.getPerson(passport, Mode.REMOTE);
        Assert.assertNotNull(remote);

        if (log)
            System.err.println("\tChecking if remote and local persons has only 1 account");
        Assert.assertNotNull(bank.getAccount(local, subId1));
        Assert.assertNotNull(bank.getAccount(remote, subId1));
        Assert.assertEquals(1, local.getAccountsOfPerson().size());
        Assert.assertEquals(local.getAccountsOfPerson().size(), remote.getAccountsOfPerson().size());

        final String subId2 = "2";
        final String accountId2 = passport + ":" + subId2;
        creatingAccount(accountId2);

        if (log)
            System.err.println("\tChecking if remote person has 2 accounts while local person has only 1 account");
        Assert.assertEquals(2, remote.getAccountsOfPerson().size());
        Assert.assertNotEquals(2, local.getAccountsOfPerson().size());
        Assert.assertEquals(1, local.getAccountsOfPerson().size());
        final long finishTime = System.currentTimeMillis();
        System.err.println("Test2 successfully passed in " + (finishTime - startTime) + " ms");
        ok.add(true);
    }

    /**
     * Testing that local person don't see if amount of money on remote account changes
     * @throws RemoteException if some RMI error occurred
     */
    @Test
    public void test3() throws RemoteException {
        final long startTime = System.currentTimeMillis();
        System.err.println("============Running test3============");
        System.err.println("Testing that local person don't see changes that remote person does");
        final String name = "User1";
        final String surname = "Test3";
        final String passport = "hello";

        creatingPerson(name, surname, passport);

        if (log)
            System.err.println("\tGetting a remote person");
        Person remote = bank.getPerson(passport, Mode.REMOTE);
        Assert.assertNotNull(remote);

        final String subId1 = "1";
        final String accountId1 = passport + ":" + subId1;
        creatingAccount(accountId1);

        if (log)
            System.err.println("\tGetting a local person");
        Person local = bank.getPerson(passport, Mode.LOCAL);
        Assert.assertNotNull(local);

        if (log)
            System.err.println("\tChecking if account with subId=\"wrong\" does not exist");
        Account nonExist = bank.getAccount(remote, "wrong");
        Assert.assertNull(nonExist);

        if (log)
            System.err.println("\tGetting remote and local persons account");
        Account remoteAccount = bank.getAccount(remote, subId1);
        Account localAccount = bank.getAccount(local, subId1);
        Assert.assertNotNull(remoteAccount);
        Assert.assertNotNull(localAccount);

        if (log)
            System.err.println("\tSetting remote account new amount");
        remoteAccount.setAmount(100);

        if (log)
            System.err.println("\tChecking amount");
        Assert.assertEquals(100, remoteAccount.getAmount());
        Assert.assertNotEquals(bank.getAccount(remote, subId1).getAmount(),
                bank.getAccount(local, subId1).getAmount());
        Assert.assertEquals(0, localAccount.getAmount());
        final long finishTime = System.currentTimeMillis();
        System.err.println("Test3 successfully passed in " + (finishTime - startTime) + " ms");
        ok.add(true);
    }

    /**
     * Testing a lot of clients and accounts
     * @throws RemoteException if some RMI error occurred
     */
    @Test
    public void test4() throws RemoteException {
        System.err.println("============Running test4============");
        System.err.println("Testing that each of 10 persons could contain exactly 10 accounts");
        final long startTime = System.currentTimeMillis();
        final List<String> names = new ArrayList<>();
        final List<String> surnames = new ArrayList<>();
        final List<String> passports = new ArrayList<>();
        final int personsCount = 10;
        for (int i = 1; i <= personsCount; i++) {
            names.add("User" + i);
            surnames.add("Test4");
            passports.add("passport4_" + i);
        }
        final List<Person> persons = new ArrayList<>();
        for (int i = 0; i < personsCount; i++) {
            creatingPerson(names.get(i), surnames.get(i), passports.get(i));
            final Person person = bank.getPerson(passports.get(i), Mode.REMOTE);
            Assert.assertNotNull(person);
            persons.add(person);
        }
        final int accountsCount = 10;
        final List<List<Account>> accounts = new ArrayList<>();
        for (int i = 0; i < personsCount; i++) {
            accounts.add(new ArrayList<>());
            for (int j = 0; j < accountsCount; j++) {
                creatingAccount(passports.get(i) + ":" + j);
                final Account account = bank.getAccount(persons.get(i), String.valueOf(j));
                Assert.assertNotNull(account);
                accounts.get(i).add(account);
            }
        }
        if (log)
            System.err.println("\tChecking...");
        for (int i = 0; i < personsCount; i++) {
            Assert.assertEquals(accountsCount, persons.get(i).getAccountsOfPerson().size());
        }
        final long finishTime = System.currentTimeMillis();
        System.err.println("Test4 successfully passed in " + (finishTime - startTime) + " ms");
        ok.add(true);
    }

    /**
     * Testing a lot of persons and accounts using MultiThreading
     * @throws RemoteException if some RMI error occurred
     */
    @Test
    public void test5() throws RemoteException {
        System.err.println("============Running test5============");
        System.err.println("Running multithreaded test");
        final long startTime = System.currentTimeMillis();
        multiThread(5, false);
        final long finishTime = System.currentTimeMillis();
        System.err.println("Test5 successfully passed in " + (finishTime - startTime) + " ms");
        ok.add(true);
    }

    /**
     * Testing a lot of persons and accounts and checking that local persons don't see remote changes
     * using MultiThreading
     * @throws RemoteException if some RMI error occurred
     */
    @Test
    public void test6() throws RemoteException {
        System.err.println("============Running test6============");
        System.err.println("Running multithreaded test with local persons");
        final long startTime = System.currentTimeMillis();
        multiThread(6, true);
        final long finishTime = System.currentTimeMillis();
        System.err.println("Test6 successfully passed in " + (finishTime - startTime) + " ms");
        ok.add(true);
    }

    private void multiThread(final int ind, final boolean flag) throws RemoteException {
        final ExecutorService creators = Executors.newFixedThreadPool(10);
        final int countPersons = 100;
        final int countAccounts = 100;
        final List<Person> persons = new ArrayList<>(Collections.nCopies(countPersons, null));
        final List<Person> localPersons = new ArrayList<>(Collections.nCopies(countPersons, null));
        final Phaser phaser = new Phaser();
        phaser.register();
        if (log)
            System.err.println("\tCreating 100 persons in 10 threads");
        for (int i = 0; i < countPersons; i++) {
            int finalI = i;
            phaser.register();
            creators.submit(() -> {
                try {
                    final Person person = bank.createPerson("User" + finalI, "Test" + ind,
                            "passport" + ind + "_" + finalI);
                    Assert.assertNotNull(person);
                    if (flag) {
                        final Person local = bank.getPerson("passport" + ind + "_" + finalI, Mode.LOCAL);
                        Assert.assertNotNull(local);
                        localPersons.set(finalI, local);
                    }
                    persons.set(finalI, person);
                } catch (RemoteException ignored) {

                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }
        phaser.arriveAndAwaitAdvance();
        final Phaser phaser1 = new Phaser();
        phaser1.register();
        if (log)
            System.err.println("\tFor each of the 100 persons creating 100 accounts and set them all different amounts");
        for (int i = 0; i < countPersons; i++) {
            for (int j = 0; j < countAccounts; j++) {
                int finalI = i;
                int finalJ = j;
                phaser1.register();
                creators.submit(() -> {
                    try {
                        final Account account = bank.createAccount("passport" + ind + "_" + finalI + ":" + finalJ);
                        Assert.assertNotNull(account);
                        account.setAmount(finalI * countPersons + finalJ);
                        if (flag) {
                            Assert.assertEquals(0, localPersons.get(finalI).getAccountsOfPerson().size());
                        }
                    } catch (RemoteException ignored) {

                    } finally {
                        phaser1.arriveAndDeregister();
                    }
                });
            }
        }
        phaser1.arriveAndAwaitAdvance();
        if (log)
            System.err.println("\tChecking...");
        for (int i = 0; i < countPersons; i++) {
            for (int j = 0; j < countAccounts; j++) {
                final Person owner = persons.get(i);
                Assert.assertEquals(i * countPersons + j,
                        owner.getAccountsOfPerson().get(owner.getPassport() + ":" + j).getAmount());
            }
        }

    }

    /**
     * What to do after all tests completed (successfully or not)
     */
    @AfterClass
    public static void afterClass() {
        if (ok.size() == 6) {
            System.out.println("=============================================");
            System.out.println("Successfully passed all the tests!");
            System.out.println("Final verdict: OK, but I don't recommend you this bank with such methods " +
                    "like \"Account.setAmount()\"");
        } else {
            System.err.println("=============================================");
            System.err.println("Tests not passed!");
        }
    }
}
