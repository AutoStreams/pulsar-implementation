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

### Build and run docker-compose

**Prerequisites**
* Make sure you have downloaded [Docker](https://www.docker.com/) on your system.
* Make sure [Docker Compose](https://docs.docker.com/compose/install/) is installed (Added by default with Docker Desktop for Windows)
* Set the working directory as the root of this collection directory i.e. **`pulsar-implementation/`**

To build the docker images, execute the command:
```bash
docker compose -f docker-compose.yml -f broker/docker-compose.yml build
```

To execute the built images, execute the command:
```bash
docker compose -f docker-compose.yml -f broker/docker-compose.yml up
```
