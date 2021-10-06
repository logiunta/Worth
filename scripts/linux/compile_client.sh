#!/bin/bash
mkdir ./bin
mkdir ./bin/Worth

javac -d ./bin/Worth -cp "lib/*" src/Client/*.java src/Common/*.java src/Exceptions/*.java
