# Kafka SOS Consumer [![Build Status](https://travis-ci.org/52North/kafka-sos-consumer.svg)](https://travis-ci.org/52North/kafka-sos-consumer)

**Work in Progress**

This project aims at developing a Kafka consumer/stream processor that
catched changes to a connect [SOS](https://github.com/52North/SOS) database.
The database has to be in the default SOS schema and have the
[Debezium CDC plugin](https://github.com/debezium/postgres-decoderbufs)
installed.

## Example Setup

A set of Docker images is available that demonstrated the required services
and their composition: https://github.com/52North/postgis-kafka-cdc

## License

This project is licensed under the [Apache License 2.0](LICENSE).