## About The Project
This is a collection for a Pulsar producer and consumer implementation using the Java client API.

## Getting Started
First acquire this project by cloning the repository. Cloning this repository can be done by downloading [Git](https://git-scm.com/) then executing the command:
```bash
git clone https://github.com/AutoStreams/pulsar-implementation.git
```
The next step is to change the working directory to be the root of the cloned repository, then init and update all submodules of this project recursively. This can be done by executing the commands:

```bash
cd prototype-pulsar
git submodule update --init --recursive
```

All code and modules should now be available in your local repository.

### Build and run with Docker

**Prerequisites**
* Make sure you have downloaded [Docker](https://www.docker.com/) on your system.
* Set the working directory as the root of this collection directory i.e. **`pulsar-implementation/`**

To execute the Docker images, they must first be built then ran. The producer and consumer must be built separately.

**Producer**
* Navigate to the producer folder
```bash
cd producer
```
* Build the producer. It is recommended to tag it for ease of use, forexample with the tag "producer"
```bash
Docker build -t producer .
```
* Run the producer
```bash
Docker run producer
```
**Consumer**
* Navigate to the consumer folder
```bash
cd consumer
```
* Build the consumer. It is recommended to tag it for ease of use, forexample with the tag "consumer"
```bash
Docker build -t consumer .
```
* Run the consumer
```bash
Docker run consumer
```
