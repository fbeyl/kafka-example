# Apache Kafka example for java to run local + remote on z/OS + send halt & stop message from cloudkarafka browser page

Example code for connecting to a Apache Kafka cluster and authenticate with SSL_SASL and SCRAM.

### Prerequisites

To easily test this code you can create a free Apacha Kafka instance at https://www.cloudkarafka.com
To easily perform actions with z/OS install zowe-cli from https://www.zowe.org, vscode + zowe explorer extension.

### Configure

All of the authentication settings can be found in the Details page for your CloudKarafka instance.

```
export CLOUDKARAFKA_BROKERS=broker1:9094,broker2:9094,broker3:9094
export CLOUDKARAFKA_USERNAME=<username>
export CLOUDKARAFKA_PASSWORD=<password>
export CLOUDKARAFKA_TOPIC_PREFIX=<topicprefix>
```
Move launch.json and tasks.json from /resources/.vscode to .vscode folder and update contents with appropriate settings (environment variables + path) to make vscode tasks available.

### Run local

```
git clone
cd kafka-example
java -jar target/kafkaExample-1.0-SNAPSHOT-jar-with-dependencies.jar
```
or use vscode run

### Run on mainframe

- upload jar using zowe-cli

zowe zos-files upload file-to-uss \"target/kafkaExample-1.0-SNAPSHOT-jar-with-dependencies.jar\" \"<path to jar+kafka-stdparm>/kafkaExample-1.0-SNAPSHOT-jar-with-dependencies.jar\"

or use vscode defined run task 'Deploy to z/OS'

- update /resources/uss/stdparm with CLOUDKARAFKA info

- upload stdparm using zowe-cli (once)

zowe zos-files upload file-to-uss \"resources/uss/stdparm\" \"<path to jar+kafka-stdparm>/stdparm


- update /resources/job/runRemoteOnzOS.jcl with <path to jar+kafka-stdparm> and jobcard with z/OS userid

- submit jcl using zowe-cli

zowe zos-jobs submit local-file \"resources/runRemoteOnzOS.jcl\"

or use vscode defined run task 'Submit to z/OS'

This will start job on mainframe that pushes messages to Kafka topic prefix-default in one Thread and read messages in the main Thread.
The output you will see in STDOUT jes2 file is the messages received in the consumer + action taken by consumer to halt or stop.
Producer will halt on halt z/OS message and start once and then on go z/OS message. Halt z/OS or go z/OS or stop must be produced in topic default using cloudkarafka browser pages. Job will end when stop message is consumed.
