#!/bin/bash
if [[ $1 == "sctp" ]] ; then java -jar rnp_sctp.jar ; fi
if [[ $1 == "tcp" ]] ; then java -jar rnp_tcp.jar ; fi
