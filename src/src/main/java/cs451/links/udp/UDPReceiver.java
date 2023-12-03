package cs451.links.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cs451.Message;
import cs451.Observer;

public class UDPReceiver implements Runnable {
    private final Observer observer;
    private static boolean isRunning;
    private DatagramSocket socket;
    private ExecutorService deliverer = Executors.newFixedThreadPool(1);

    public UDPReceiver(Observer observer, int port) {
        this.observer = observer;
        try {
            this.socket = new DatagramSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            byte[] receiveData = new byte[8];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            isRunning = true;
            while (isRunning) {
                socket.receive(receivePacket);
                Message message = Message.fromBytes(receivePacket.getData());
                deliverer.submit(() -> observer.deliver(message));
                // observer.deliver(message);
            }
            deliverer.shutdown();
            this.socket.close();
            System.out.println("UDPReceiver stopped");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopReceiver() {
        isRunning = false;
    }
}
