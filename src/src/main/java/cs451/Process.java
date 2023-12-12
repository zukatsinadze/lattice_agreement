package cs451;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import cs451.lattice.LatticeAgreement;

public class Process implements Observer {
  private final byte id;
  private Host me;
  private LatticeAgreement lattice;
  static Logger outputWriter;

  public Process(byte id, HashMap<Byte, Host> hostMap, String output, int p, int vs, int ds) {
    this.id = id;
    this.me = hostMap.get(id);
    this.lattice = new LatticeAgreement(id, me.getPort(), hostMap, p, vs, ds, this);

    try {
      outputWriter = new Logger(output);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void send(Set<Integer> set) {
    this.lattice.propose(set);
  }

  public byte getId() {
    return id;
  }

  public void stopProcessing() {
    lattice.stop();
    try {
      outputWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void startProcessing() {
    lattice.start();
  }

  @Override
  public void deliver(Message message) {
  }

  public void deliver(Set<Integer> numbers) {
    outputWriter.decided(numbers);
  }


}
