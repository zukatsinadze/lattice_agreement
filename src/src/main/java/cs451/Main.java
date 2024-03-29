package cs451;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Main {
  static HashMap<Byte, Host> hostMap = new HashMap<>();
  static int p;
  static int vs;
  static int ds;
  static Process process;
  static Parser parser;
  static boolean active = true;

  private static void handleSignal() {
    System.out.println("Immediately stopping network packet processing.");
    process.stopProcessing();
    active = false;
    System.out.println("Writing output.");
  }

  private static void initSignalHandlers() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        handleSignal();
      }
    });
  }

  public static void main(String[] args) throws InterruptedException {
    parser = new Parser(args);
    parser.parse();

    // example
    long pid = ProcessHandle.current().pid();
    System.out.println("My PID: " + pid + "\n");
    System.out.println("From a new terminal type `kill -SIGINT " + pid +
        "` or `kill -SIGTERM " + pid +
        "` to stop processing packets\n");

    System.out.println("My ID: " + parser.myId() + "\n");
    System.out.println("List of resolved hosts is:");
    System.out.println("==========================");
    for (Host host : parser.hosts()) {
      System.out.println(host.getId());
      System.out.println("Human-readable IP: " + host.getIp());
      System.out.println("Human-readable Port: " + host.getPort());
      System.out.println();
      hostMap.put((byte) (host.getId() - 1), host);
    }
    System.out.println();

    System.out.println("Path to output:");
    System.out.println("===============");
    System.out.println(parser.output() + "\n");

    System.out.println("Path to config:");
    System.out.println("===============");
    System.out.println(parser.config() + "\n");

    System.out.println("Doing some initialization\n");
    BufferedReader br = null;
    String[] parts = null;
    try {
      br = new BufferedReader(new FileReader(parser.config()));
      parts = br.readLine().split(" ");

      p = Integer.parseInt(parts[0]);
      vs = Integer.parseInt(parts[1]);
      ds = Integer.parseInt(parts[2]);

    } catch (IOException e) {
      System.err.println("Error reading the file: " + e.getMessage());
    }

    process = new Process((byte) (parser.myId() - 1), hostMap, parser.output(), p, vs, ds);
    process.startProcessing();

    initSignalHandlers();

    System.out.println("Broadcasting and delivering messages...\n");

    try {
      for (int currentRound = 0; currentRound < p && active; currentRound++) {
        System.out.println("Starting Round " + currentRound);
        parts = br.readLine().split(" ");
        Set<Integer> set = new HashSet<>();
        for (String number : parts) {
          set.add(Integer.parseInt(number));
        }
        process.send(set);
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // After a process finishes broadcasting,
    // it waits forever for the delivery of messages.
    while (true) {
      // Sleep for 1 hour
      Thread.sleep(60 * 60 * 1000);
    }
  }
}
