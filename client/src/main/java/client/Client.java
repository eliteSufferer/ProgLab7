package client;

import client.utils.AufHandler;
import client.utils.UserHandler;
import common.functional.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Objects;

public class Client {
    private String host;
    private int port;

    private UserHandler userHandler;
    private DatagramChannel datagramChannel = DatagramChannel.open();

    private AufHandler authHandler;
    private User user;

    public Client(String host, int port, UserHandler userHandler, AufHandler authHandler) throws IOException {
        this.host = host;
        this.port = port;
        this.userHandler = userHandler;
        datagramChannel.configureBlocking(false);
        this.authHandler = authHandler;
    }

    private boolean processRequestToServer() {
        Request requestToServer = null;
        Response serverResponse = null;
        do {
            try {
                requestToServer = serverResponse != null ? userHandler.handle(serverResponse.getResponseCode(), user) :
                        userHandler.handle(null, user);
                if (requestToServer.isEmpty()) continue;
                ByteArrayOutputStream serverWriter = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverWriter);
                objectOutputStream.writeObject(requestToServer);
                byte[] bytes;
                bytes = serverWriter.toByteArray();
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                buffer.put(bytes);
                buffer.flip();
                InetSocketAddress address = new InetSocketAddress(host, port);
                datagramChannel.send(buffer, address);
                if (requestToServer.getCommandName().equals("exit")) {
                    System.exit(0);
                }
                ByteBuffer receiveBuffer = ByteBuffer.allocate(4096);

                long timeout = 5000;
                long start = System.currentTimeMillis();
                while (datagramChannel.receive(receiveBuffer) == null && System.currentTimeMillis() - start < timeout) {

                    Thread.sleep(100);
                }
                if (System.currentTimeMillis() - start >= timeout) {
                    System.out.println("Превышено время ожидания ответа от сервера");
                    continue;
                }

                receiveBuffer.flip();
                byte[] data = new byte[receiveBuffer.limit()];
                receiveBuffer.get(data);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                Object deserializedObject = objectInputStream.readObject();
                serverResponse = (Response) deserializedObject;
                Printer.print(serverResponse.getResponseBody(), serverResponse.getResponseCode());
            } catch (NullPointerException e) {
                System.out.println("Недопустимый ввод");
                assert serverResponse != null;
                requestToServer = userHandler.handle(serverResponse.getResponseCode(), user);
            } catch (ClassNotFoundException e) {
                System.out.println("Ошибка при чтении пакета");
            } catch (IOException e) {
                System.out.println("Непредвиденная ошибка при отправке данных");
            } catch (InterruptedException e) {
                System.out.println("Прервано ожидание ответа от сервера");
            }
        } while (!requestToServer.getCommandName().equals("exit"));

        return false;
    }

    public void run() {
        try {
            boolean processingStatus = true;
            while (processingStatus) {
                try {
                    processAuthentication();
                    processingStatus = processRequestToServer();
                } catch (Exception exception) {
                    System.out.println("Фатальная ошибка при работе клиента");
                }
                if (datagramChannel != null) datagramChannel.close();
                Printer.println("Работа клиента завершена.");
                System.exit(0);
            }
        } catch (IOException e) {
            System.out.println("Возникла ошибочка!");
        }

    }

    private void processAuthentication() {
        Request requestToServer = null;
        Response serverResponse = null;
        do {
            try {
                requestToServer = authHandler.handle();
                if (requestToServer.isEmpty()) continue;

                // Serialize request to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(requestToServer);
                byte[] requestData = bos.toByteArray();

                // Send request to the server
                ByteBuffer requestBuffer = ByteBuffer.wrap(requestData);
                InetSocketAddress address = new InetSocketAddress(host, port);
                datagramChannel.send(requestBuffer, address);

                // Receive response from the server
                ByteBuffer responseBuffer = ByteBuffer.allocate(4096);
                datagramChannel.receive(responseBuffer);
                byte[] responseData = responseBuffer.array();

                // Deserialize response
                ByteArrayInputStream bis = new ByteArrayInputStream(responseData);
                ObjectInputStream ois = new ObjectInputStream(bis);
                serverResponse = (Response) ois.readObject();
                Printer.print(serverResponse.getResponseBody(), serverResponse.getResponseCode());

            } catch (InvalidClassException | NotSerializableException exception) {
                Printer.printerror("Произошла ошибка при отправке данных на сервер!");
            } catch (ClassNotFoundException exception) {
                Printer.printerror("Произошла ошибка при чтении полученных данных!");
            } catch (IOException exception) {
                Printer.printerror("Соединение с сервером разорвано!");
            }
        } while (serverResponse == null || !serverResponse.getResponseCode().equals(ServerResponseCode.OK));
        user = requestToServer.getUser();
    }


}




