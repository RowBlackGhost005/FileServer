# FileServer
File Transfer Application.

WORK IN PROGRESS - ALPHA VERSION 0.1

This application works for hosting and connecting into a computer to transfer files that had been allowed by the host.

How it works:
- Creates a TCP Sockets for inbound connections (Host) and thread that connection to accomodate multiple connections at once.
- For every connection a TCP & UDP Socket are created for system info and file transfer respectively.
- Once connection is established, host sends a list of all files white listed for transfer.
- Client selects the file to transfer and select its destination and name (A queue of files can be done).
- Transfer files goes through UDP Sockets and checks for integrity at the end.

Things to know:
- Default Host port is 4444.

Tests:
- No test has been done yet.
Target test is 3 connections at once each with 1GB file.

Requirements:
- Java 17.