package KafkaExamplePackage;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class ControlSubThread implements Runnable {

    private Thread worker;
    private int interval;
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean stopped = new AtomicBoolean(true);
    private KafkaExample k;
    private Producer<String, String> kp;
    private int i = 0;

    public ControlSubThread(int sleepInterval, KafkaExample c) {
        interval = sleepInterval;
        k = c;
        kp = new KafkaProducer<>(k.props);
    }

    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        running.set(false);
    }

    boolean isRunning() {
        return running.get();
    }

    boolean isStopped() {
        return stopped.get();
    }

    public void run() {
        running.set(true);
        stopped.set(false);
        while (running.get()) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
            	Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted, Failed to complete operation");
    	    }
            System.out.println("Record "+Integer.toString(i)+" ready");
            Date d = new Date();
            RecordMetadata ack;
            try {
                ack = kp.send(new ProducerRecord<>(k.topic, Integer.toString(i), KafkaExample.os + " " + d.toString())).get();
                System.out.println("Offset = " + ack.offset());
            } catch (InterruptedException | ExecutionException e1) {
                System.out.println("Ack " + i + " not received");
                e1.printStackTrace();
            }
            i++;
        }
        stopped.set(true);
    }
}
