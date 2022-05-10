## About The Project
This is a collection for a Pulsar producer and consumer implementation using the Java client API.

## Getting Started
First acquire this project by cloning the repository. Cloning this repository can be done by downloading [Git](https://git-scm.com/) then executing the command:
```bash
git clone https://github.com/AutoStreams/pulsar-implementation.git
```
The next step is to change the working directory to be the root of the cloned repository, then init and update all submodules of this project recursively. This can be done by executing the commands:
```bash
cd pulsar-implementation
git submodule update --init --recursive
```
All code and modules should now be available in your local repository.

## Building all modules
To build the project, it is possible to use Maven to build from the root **`pulsar-implementation/`** folder.
```bash
mvn package
```
Runnable jar files will then be available in the target folder for each module. The jars can be ran from the root with the following command
```bash
java -jar <DIRECTORY-NAME>/target/<MODULE-NAME>.jar 
```
Where <DIRECTORY-NAME> and <MODULE-NAME> are replaced by the name of the desired module.

Alternatively, each module have their own readme that can be referred to in order to build and run
the modules.