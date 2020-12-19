import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends JFrame {

    public ServerSocket server;

    public List<Client> list = new ArrayList<>();

    public JTextArea messagesArea, onlineClientList;

    public Server(int port) throws IOException {

        server = new ServerSocket(port);

    }

    public void start() throws IOException {

        setTitle("Chatting Room Server");

        GUI();

        //noinspection InfiniteLoopStatement
        while (true) {

            Socket socket = server.accept();

            String ip = socket.getInetAddress().getHostAddress();

            Client client = new Client(socket);

            messagesArea.append(ip + "(" + client.name + ")" + " is connected，" + (list.size() + 1) + " online user(s)" + "\r\n");

            list.add(client);

            new Thread(client).start();

            client.update();

        }
    }

    private void GUI() {
        setSize(500, 500);

        messagesArea = new JTextArea(" ---Server is running--- " + "\r\n");

        messagesArea.setEditable(false);

        onlineClientList = new JTextArea("--Online Users--" + "\r\n");

        onlineClientList.setEditable(false);

        JPanel clientPanel = new JPanel(new FlowLayout());

        clientPanel.setBorder(new LineBorder(Color.BLACK));

        clientPanel.add(onlineClientList);

        add(clientPanel, BorderLayout.EAST);

        add(new JScrollPane(messagesArea), BorderLayout.CENTER);

        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    class Client implements Runnable {

        String name;

        Socket socket;

        private final PrintWriter out;

        private final BufferedReader in;

        public Client(Socket socket) throws IOException {

            this.socket = socket;

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(socket.getOutputStream());

            name = in.readLine();

            onlineClientList.append(name+"\r\n");

        }

        public void send(String str) {

            out.println(str);

            out.flush();
        }

        public void update() {

            StringBuffer clientList = new StringBuffer("update:");

            onlineClientList.setText("--Online Users--\r\n");

            for (Client client : list) {

                onlineClientList.append(client.name + "\r\n");

                clientList.append(client.name).append(" ");
            }

            for (Client client : list) {

                client.out.println(clientList);

                client.out.flush();
            }

        }

        @Override
        public void run() {

            try {

                String msg = in.readLine();

                boolean flag = true;

                while (flag && msg != null) {

                    String str = this.name + "：" + msg;

                    messagesArea.append(str + "\r\n");

                    for (int i = 0; i < list.size(); i++) {

                        Client client = list.get(i);

                        if (client != this) {

                            client.send(str);

                            if (msg.equals("quit")) {

                                client.send(this.name + " left room");

                                messagesArea.append(this.name + " left room, " + (list.size() - 1) + " online user(s)" + "\r\n");

                                flag = false;

                            }
                        }
                    }

                    msg = in.readLine();
                }
            } catch (Exception ignored) {

            } finally {
                try {

                    list.remove(this);

                    update();

                    socket.shutdownOutput();

                    socket.shutdownInput();

                    socket.close();

                } catch (Exception e) {

                    System.out.println(this.name + " has an error");

                }
            }
        }

    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(8000);

        String serverIP = server.server.getInetAddress().getHostAddress();

        System.out.println("Server IP Address: " + serverIP + "\r\n" + "Port: " + server.server.getLocalPort() + "\r\n");

        server.start();
    }

}