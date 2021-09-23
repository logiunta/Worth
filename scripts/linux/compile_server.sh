#!/bin/bash
echo Compilazione del server in corso. Attendere...

javac -d ./bin/Worth/Server -cp "lib/*" src/Server/*.java src/Common/*.java src/Exceptions/*.java