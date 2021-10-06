#!/bin/bash
mkdir ./bin
mkdir ./bin/Worth

javac -d ./bin/Worth -cp "lib/*" src/Server/*.java src/Common/*.java src/Exceptions/*.java
