package cs451.links.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import cs451.Message;
import cs451.Observer;

public class UDPReceiver implements Runnable {
    private final Observer observer;
    private static boolean isRunning;
    private DatagramSocket socket;

    public UDPReceiver(Observer observer, DatagramSocket socket) {
        this.observer = observer;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            byte[] receiveData = new byte[1024];
            isRunning = true;
            System.out.println(InetAddress.getLocalHost());
            while (isRunning) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                Message message = Message.fromBytes(receivePacket.getData());

                observer.deliver(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopReceiver() {
        isRunning = false;
    }
}