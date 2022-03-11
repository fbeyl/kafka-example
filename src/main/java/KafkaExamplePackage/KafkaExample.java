package KafkaExamplePackage;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;


public class KafkaExample {

    public static final String os = System.getProperty("os.name");
    private AtomicBoolean go = new AtomicBoolean(true);
    private static final String HALT_PRODUCER = "HALT " + os.toUpperCase();
    private static final String STOP = "STOP";
    private static final String GO_PRODUCER = "GO " + os.toUpperCase();
    public final String topic;
    public final Properties props;

    public KafkaExample(String brokers, String username, String password) {
        this.topic = username + "-default";
//        this.topic = username + "-1replica";
        String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, username, password);

        String serializer = StringSerializer.class.getName();
        String deserializer = StringDeserializer.class.getName();
        props = new Properties();
        props.put("bootstrap.servers", brokers);
//        props.put("group.id", username + "-consumer");
        props.put("group.id", username + "-" + os);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "5000");
        props.put("auto.offset.reset", "latest");
        props.put("session.timeout.ms", "60000");
        props.put("key.deserializer", deserializer);
        props.put("value.deserializer", deserializer);
        props.put("key.serializer", serializer);
        props.put("value.serializer", serializer);
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-256");
        props.put("sasl.jaas.config", jaasCfg);
        props.put("acks","all");
        props.put("batch.size","10");
    }

    public void consume(ControlSubThread t) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
        System.out.println("Consumer subscribed on " + os);
        while (go.get()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
/*
                System.out.printf("%s [%d] offset=%d, key=%s, value=\"%s\"\n",
                               record.topic(), record.partition(),
                               record.offset(), record.key(), record.value());
*/
                System.out.printf("[%d] offset=%d, key=%s, value=\"%s\"\n",
                               record.partition(), record.offset(),
                               record.key(), record.value());
                Date d = new Date();
                if (record.value().toUpperCase().equals(GO_PRODUCER)) {
                    if (t.isStopped()) {
                        System.out.println(d.toString() + " Starting producer on " + os);
                        t.start();
                    }
                    else {
                        System.out.println(d.toString() + " Producer already running on " + os);
                    };
                };
                if (record.value().toUpperCase().equals(HALT_PRODUCER)) {
                    if (t.isRunning()) {
                        System.out.println(d.toString() + " Halting producer on " + os);
                        t.stop();
                    }
                    else {
                        System.out.println(d.toString() + " Producer already halted on " + os);
                    };
                };
                if (record.value().toUpperCase().equals(STOP)) {
                    System.out.println(d.toString() + " Stopping on " + os);
                    if (t.isRunning()) {
                        t.stop();
                        System.out.println(d.toString() + " Consumer closed on " + os);
                    };
                    go.set(false);
                };
			};
        };
        consumer.close();
        System.out.println("Consumer closed");
    }

    public static void main(String[] args) {
		String brokers = System.getenv("CLOUDKARAFKA_BROKERS");
		String username = System.getenv("CLOUDKARAFKA_USERNAME");
		String password = System.getenv("CLOUDKARAFKA_PASSWORD");
		KafkaExample c = new KafkaExample(brokers, username, password);

        ControlSubThread t = new ControlSubThread(1000, c);

        t.start();
        c.consume(t);

    }
}
