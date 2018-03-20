package WorkQueues;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Worker {
    //private final static String QUEUE_NAME = "hello";
    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv)
    {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setVirtualHost("/");
            factory.setHost("localhost");
            factory.setPort(5672);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            boolean durable = false;
            channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            int prefetchCount = 1;
            channel.basicQos(prefetchCount);
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                    try {
                        doWork(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println(" [x] Done");
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }
                }
            };
            boolean autoAck = false; // acknowledgment is covered below
            channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }
    private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
            if (ch == '.')
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException _ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
