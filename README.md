# Snapshots in causal system
Project for class "Concurrent and Distributed programming" at Računarski fakultet.\
The project uses a framework created by TA , at mentioned class, [Branislav Milojkovic](https://github.com/bmilojkovic).

## 1 Overview

Implement a distributed system that supports the following functionalities:\
     • Implemented in Java or go programming language. The use of auxiliary libraries for communication between nodes is allowed.\
     • Fully asynchronous non-FIFO communication.\
     • Arbitrary number of nodes connected in any way (graph is not complete). If any node at any time needs to send a message to all nodes in the system, it must function exclusively through the connections given in the system configuration.\
     • Each node has its own port where it accepts messages from its neighbors, and everyone listens on the localhost.\
     • One snapshot is done at one time, at the level of the entire system. The system should support the creation of a new snapshot on an arbitrary node, after the previous snapshot has been completed.\
     • Communication with the user via CLI or text (script) files.
    
## 2  Functional requirements

Each node in the system starts with a predetermined amount of bitcakes.\
It is necessary to be able to snapshot the system from any node in the system. The strategy for making snapshots should be one of the two algorithms for recording the state in systems with causal delivery, and they are:

**1. Acharya-Badrinath algorithm**\
**2. Alagar-Venkatesan algorithm**
    
The system should support the operation of both of these algorithms, and the one used in a particular startup should be selected based on the settings in the configuration file. The new values for the snapshot attribute should be: ab (Acharya-Badrinath) and av (Alagar-Venkatesan).
All nodes, based on commands from users, will very often exchange their bitcake stocks. The result of the snapshot algorithm should be the current bitcake state in the system. With the Alagar-Venkatesan algorithm, there is no need to display the state in the channels on the node that initiated the snapshot algorithm, but it is enough for each node to print the state for its own channels.
All nodes are allowed to start on the same machine and listen on different ports on the localhost. It is necessary to have an artificially introduced random delay when sending each message, in order to simulate the delay online.
Two nodes are allowed to exchange messages only if they are listed as neighbors in the configuration file.
It is necessary for the system to support "scripted" startup of multiple nodes, where the commands for each node are read from a text file, and the outputs for each node are written in separate files.

## 3 Non-functional requirements

It is necessary to have support for the following situations, with the listed solutions. There is no need to solve problems that are not listed.

1. Problem: The user is looking for a snapshot on a node that has already started snapshot creation, but that snapshot is not complete. \
   Solution: Report a bug on the console and resume normal operation.
   
2. Problem: If the user starts making snapshots on multiple nodes concurrently, the system is allowed to behave unpredictably.\
   Solution: It is not resolved.
   
It is necessary that the number of messages in these algorithms be the same as in the description of the algorithm - 2n for Acharya-Badrinath and 3n for Alagar-Venkatesan.

The configuration file specifies:\
     • How many nodes are in the system.\
     • The port on which each node listens.\
     • Neighbor list for each node.
     
There should be print messages on all nodes that provide information on how the communication is going, so that the history of the system can be reconstructed by observing the printout.
