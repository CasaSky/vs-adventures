package de.haw.vsadventures.utils;

import java.net.*;

public class UDPClient extends Thread {

    private final int PORT = 24000;

    private String msg = null;

    public void run() {
        try {

            // Create a socket to listen on the port.
            DatagramSocket dsocket = new DatagramSocket(PORT, InetAddress.getByName("127.0.0.1"));

            // Create a buffer to read datagrams into. If a
            // packet is larger than this buffer, the
            // excess will simply be discarded!
            byte[] buffer = new byte[2048];

            // Create a packet to receive data into the buffer
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Now loop forever, waiting to receive packets and printing them.
            while (msg == null) {
                // Wait to receive a datagram
                dsocket.receive(packet);

                // Convert the contents to a string, and display them
                String msg = new String(buffer, 0, packet.getLength());
                System.out.println(packet.getAddress().getHostName() + ": "
                        + msg);

                // Reset the length of the packet before reusing it.
                packet.setLength(buffer.length);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public String getMsg() {
        return msg;
    }
}
