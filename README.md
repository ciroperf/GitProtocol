# GitProtocol

Progetto accademico di [**Ciro Perfetto**](https://github.com/ciroperf)

Corso di Laurea Magistrale in informatica: **Architetture distribuite per il Cloud**

Professori: **Alberto Negro** **Carmine Spagnuolo** **Gennaro Cordasco**

[Dipartimento di Informatica](http://www.di.unisa.it) - [Università degli Studi di Salerno](https://www.unisa.it/) (Italia)

## Definizione del problema

Design and develop the Git protocol, distributed versioning control on a P2P network. Each peer can manage its projects (a set of files) using the Git protocol (a minimal version). The system allows the users to create a new repository in a specific folder, add new files to be tracked by the system, apply the changing on the local repository (commit function), push the network’s changes, and pull the changing from the network. The git protocol has lot-specific behavior to manage the conflicts; in this version, it is only required that if there are some conflicts, the systems can download the remote copy, and the merge is manually done.
