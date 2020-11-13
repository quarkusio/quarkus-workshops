#!/bin/sh

MYDIR="$( cd "$(dirname "$0")" ; pwd -P )"
function usage() {
    echo "usage: $(basename $0) [-c/--count usercount -s/--api-server api-server --start 1 --end usercount]"
}

# Defaults
OPERATOR_NAME=postgresql-operator-dev4devs-com
OPERATOR_CHANNEL=alpha
OPERATOR_INSTALL_MODE=OwnNamespace
START=1

POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -c|--count)
    USER_COUNT="$2"
    shift # past argument
    shift # past value
    ;;
    -s|--api-server)
    API_SERVER="$2"
    shift # past argument
    shift # past value
    ;;
    --start)
    START="$2"
    shift # past argument
    shift # past value
    ;;
    --end)
    END="$2"
    shift # past argument
    shift # past value
    ;;
    *)    # unknown option
    echo "Unknown option: $key"
    usage
    exit 1
    ;;
esac
done

if [ -z "$USER_COUNT" ]
then
  echo "-c/--count cannot be empty"
  usage
  exit 1;
fi

if [ -z "$API_SERVER" ]
then
  echo "-s/--api-server cannot be empty"
  usage
  exit 1;
fi

if [ -z "$END" ]
then
  END=$((${START} + ${USER_COUNT}))
fi

TOKEN=$(oc whoami -t)

echo "====> seq $START $END"

for i in $(seq $START $END); 
do 
  PROJECT_NAME=user$i-heroes
  OCP_USERNAME=user$i
  OCP_PASSWORD=openshift
  oc login -u user$i -p openshift --server=${API_SERVER}
  oc new-project ${PROJECT_NAME}
done

oc login --token=${TOKEN}

for i in $(seq $START $END); 
do 
  PROJECT_NAME=user$i-heroes
  OCP_USERNAME=user$i
  kubectl operator install ${OPERATOR_NAME} -a Automatic -c ${OPERATOR_CHANNEL} --create-operator-group --install-mode ${OPERATOR_INSTALL_MODE} -n ${PROJECT_NAME}
done
