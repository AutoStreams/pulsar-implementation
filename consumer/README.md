## About The Consumer
Consumer is a Pulsar Consumer implementation intended to simulate multiple end points subscribing to data from a Pulsar stream.
## Getting Started
First acquire this project by cloning the repository. Cloning this repository can be done by downloading [Git](https://git-scm.com/) then executing the command:
```
git clone https://github.com/AutoStreams/pulsar-implementation.git
```
The next step is to change the working directory to be the root of the cloned repository, then init and update all submodules of this project recursively. This can be done by executing the commands:
```bash
cd pulsar-implementation
git submodule update --init --recursive
```
### Option 1: Build and run with Maven
**Prerequisites**
* Download the latest version of [Maven](https://maven.apache.org/).
* Download a Java JDK of version 17
* Set the working directory to the root of this consumer project i.e. **`pulsar-implementation/consumer/`**

To build the project with its dependencies to a single jar file, execute the command:
```
mvn package
```
To run the application, execute the command:
```
java -jar pulsar-consumer.jar
```
This will run the application and create the amount of workers specified in the config file.
It is also possible to specify a different amount of workers using the command line argument **`-w`**. The following
command executes the application with 6 workers.
```
java -jar pulsar-consumer.jar -w 6
```
### Option 2: Build and run with Docker
**Prerequisites**
* Make sure you have downloaded [Docker](https://www.docker.com/) on your system.
* Set the working directory to the root of this consumer project i.e. **`pulsar-implementation/consumer/`**

To build the docker image, execute the command:
```
docker build -t consumer .
```

To execute the built image, execute the command:
```
docker run consumer 
```