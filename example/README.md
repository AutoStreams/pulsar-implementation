## Example
This is an example of a complete Pulsar system with its producer, broker, and consumer running in Docker containers.

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

### Build and run with Docker Compose

**Prerequisites**
* Make sure you have downloaded [Docker](https://www.docker.com/) on your system.
* Make sure [Docker Compose](https://docs.docker.com/compose/install/) is installed (Added by default with Docker Desktop for Windows)
* Set the working directory as the root of this example directory i.e. **`pulsar-implementation/example`**

To build the docker images, execute the command:
```bash
docker compose build
```

To start the built images, execute the command:
```bash
docker compose up
```
