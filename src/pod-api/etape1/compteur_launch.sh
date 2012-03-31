#!/bin/bash

java Compteur -1 C
echo "Lancement de $1 clients qui incrémente $2 fois"

for ((i = 0; i < $1; i += 1)) do
    java Compteur $2 $i &
  done

  echo "sleeping 10s"
  sleep 180
  echo "end sleep"
  java CompteurRead
  killall java
