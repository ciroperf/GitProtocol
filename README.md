# GitProtocol

Progetto accademico di [**Ciro Perfetto**](https://github.com/ciroperf)

Corso di Laurea Magistrale in informatica: **Architetture distribuite per il Cloud**

Professori: **Alberto Negro** **Carmine Spagnuolo** **Gennaro Cordasco**

[Dipartimento di Informatica](http://www.di.unisa.it) - [Università degli Studi di Salerno](https://www.unisa.it/) (Italia)

## Definizione del problema

In questo progetto è stata creata un'implementazione del protocollo Git, utilzzando una rete P2P. Ciascun peer di questa rete può gestire i suoi progetti(un insieme di file) usando il protocollo Git, in una sua versione basilare. Il sistema permette all'utente di creare una nuova repository in una cartella specifica, aggiungere nuovi file per essere tracciati dal sistema, applicare cambiamenti sulla repository locale (funzione di commit), fare il push dei cambiamenti della rete, e fare il pull dei cambiamenti dalla rete. Il protocollo git ha un comportamento specifico per gestire i conflitti che verranno poi gestiti manualmente.

![](picture1.png)

##Soluzione del progetto

Il perno principale su cui si basa il progetto è l'uso di una DHT
