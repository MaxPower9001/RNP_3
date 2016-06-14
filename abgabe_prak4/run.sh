#!/bin/bash
if [[ $1 == "tcp" ]] ; then java -jar rnp_tcp.jar ; fi
if [[ $1 == "sctp" ]] ; then 
  echo -n "Hearbeat Intervall: "
  sysctl net.sctp.hb_interval
  [[ $2 ]] && ( MILLIS=$2; sudo sysctl net.sctp.hb_interval=$MILLIS )
  java -jar rnp_sctp.jar 
fi
