package Network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class DiscoveryService {

    private static final int DISCOVERY_PORT = 8888;
    private static final int CHAT_PORT = 8889;

    private String deviceName;

    public DiscoveryService(String deviceName) {
        this.deviceName = deviceName;
    }

    public void start() {

        try {

            DatagramSocket socket =
                    new DatagramSocket(DISCOVERY_PORT);

            socket.setBroadcast(true);

            System.out.println("LAN Device Discovery");
            System.out.println("--------------------");
            System.out.println("Device: " + deviceName);
            System.out.println("Discovery Port: " + DISCOVERY_PORT);
            System.out.println("Chat Port: " + CHAT_PORT);

            // Start listening in another thread
            Thread listenerThread = new Thread(() -> {

                byte[] buffer = new byte[1024];

                while (true) {

                    try {

                        DatagramPacket packet =
                                new DatagramPacket(
                                        buffer,
                                        buffer.length
                                );

                        socket.receive(packet);

                        String message =
                                new String(
                                        packet.getData(),
                                        0,
                                        packet.getLength(),
                                        StandardCharsets.UTF_8
                                );

                        InetAddress sender =
                                packet.getAddress();

                        String senderIP =
                                sender.getHostAddress();

                        System.out.println(
                                "Received: " + message
                        );

                        // Someone is looking for devices
                        if (message.startsWith("DISCOVER:")) {

                            String response =
                                    "DISCOVER_RESPONSE:"
                                    + deviceName
                                    + ":"
                                    + CHAT_PORT;

                            byte[] responseData =
                                    response.getBytes(
                                            StandardCharsets.UTF_8
                                    );

                            DatagramPacket responsePacket =
                                    new DatagramPacket(
                                            responseData,
                                            responseData.length,
                                            sender,
                                            DISCOVERY_PORT
                                    );

                            socket.send(responsePacket);

                            System.out.println(
                                    "Response sent to: "
                                    + senderIP
                            );
                        }

                        // Someone responded to our discovery
                        else if (
                                message.startsWith(
                                        "DISCOVER_RESPONSE:"
                                )
                        ) {

                            System.out.println(
                                    "Device found!"
                            );

                            System.out.println(
                                    "IP Address: "
                                    + senderIP
                            );

                            System.out.println(
                                    "Details: "
                                    + message
                            );

                            System.out.println();
                        }

                    } catch (Exception e) {

                        System.out.println(
                                "Listener Error: "
                                + e.getMessage()
                        );
                    }
                }

            });

            listenerThread.start();

            // Give listener time to start
            Thread.sleep(500);

            // Send discovery broadcast
            sendDiscovery(socket);

        } catch (Exception e) {

            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }

    private void sendDiscovery(DatagramSocket socket)
            throws Exception {

        String message =
                "DISCOVER:"
                + deviceName
                + ":"
                + CHAT_PORT;

        byte[] data =
                message.getBytes(
                        StandardCharsets.UTF_8
                );

        InetAddress broadcastAddress =
                InetAddress.getByName(
                        "255.255.255.255"
                );

        DatagramPacket packet =
                new DatagramPacket(
                        data,
                        data.length,
                        broadcastAddress,
                        DISCOVERY_PORT
                );

        socket.send(packet);

        System.out.println();
        System.out.println(
                "Broadcast sent..."
        );
        System.out.println(
                "Looking for LAN devices..."
        );
    }

    public static void main(String[] args) {

        DiscoveryService discovery =
                new DiscoveryService("James-PC");

        discovery.start();
    }
}