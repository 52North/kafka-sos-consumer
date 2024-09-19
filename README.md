# ARCHIVED

This project is no longer maintained and will not receive any further updates. If you plan to continue using it, please be aware that future security issues will not be addressed.

# Kafka SOS Consumer

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
