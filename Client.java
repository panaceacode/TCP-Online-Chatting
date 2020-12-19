import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends JFrame implements Runnable {

    private final Socket socket;

    private final PrintWriter out;

    private final BufferedReader in;

    Thread receiveThread;

    private JTextArea messagesArea, inputTextArea, onlineClientList;

    private JButton send;

    public Client(String name, Socket socket) throws IOException {

        this.socket = socket;

        System.out.println(socket.getLocalPort());

        System.out.println(InetAddress.getLocalHost().getHostAddress());

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out = new PrintWriter(socket.getOutputStream());

        out.println(name);

        out.flush();

        setTitle(name);

        GUI();

        receiveThread = new Thread(this);

        receiveThread.start();

        msgListener();

    }

    private void GUI() {
        setSize(500, 500);

        messagesArea = new JTextArea(" ---Connected--- " + "\r\n");

        messagesArea.setEditable(false);

        onlineClientList = new JTextArea("--Online Users--" + "\r\n");

        onlineClientList.setEditable(false);

        onlineClientList.setBorder(new LineBorder(Color.BLACK));

        JPanel centerPanel = new JPanel(new BorderLayout());

        centerPanel.add(new JScrollPane(messagesArea), BorderLayout.CENTER);

        centerPanel.add(onlineClientList, BorderLayout.EAST);

        send = new JButton("Send");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        buttonPanel.add(send);

        JPanel inputPanel = new JPanel(new BorderLayout());

        inputTextArea = new JTextArea();

        inputTextArea = new JTextArea(7, 20);

        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private void msgListener() {

        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                try {
                    out.println("quit");

                    out.flush();

                    receiveThread.interrupt();

                    socket.shutdownOutput();

                    socket.shutdownInput();

                    socket.close();
                } catch (Exception e2) {

                    System.out.println("Client Error");
                }
            }

        });

        send.addActionListener(e -> {

            String msg = inputTextArea.getText();

            inputTextArea.setText("");

            messagesArea.append("Me： " + msg + "\r\n");

            try {

                out.println(msg);

                out.flush();

            } catch (Exception e1) {

                System.out.println("Send Error");
            }
        });

        inputTextArea.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    send.doClick();
                }
            }
        });


    }

    @Override
    public void run() {

        try {

            String msg = in.readLine();

            while (msg != null) {

                if (!msg.split(":")[0].equals("update")) {

                    messagesArea.append(msg + "\r\n");
                } else {
                    String[] strings = (msg.split(":")[1]).split(" ");

                    onlineClientList.setText("--Online Users--\r\n");

                    for (String s : strings) {

                        onlineClientList.append(s + "\r\n");
                    }
                }

                msg = in.readLine();
            }

        } catch (Exception e) {

            System.out.println("Client Error");

        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please insert username");
        String name = br.readLine();
        new Client(name, new Socket("100.64.14.103", 8000));
    }

}
