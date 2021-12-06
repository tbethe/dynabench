#!/bin/bash

#
# Author: Timme Bethe
#


##############
# Constants  #
##############

# directory the script is in
SCRIPT_DIR=$(realpath $(dirname $0))
# relative directory the results should be put into
RESULT_DIR='results'
# relative path the android source projects are in
SOURCE_DIR='benchmark_src'
# relative path the compiled android apks should be placed upon compilation
APKS_DIR='benchmark_apks'

###########################
# Build apps using gradle #
###########################

# Filter directories with gradle wrapper
for dir in "$SOURCE_DIR"/*; do
    if [ -f "$dir/gradlew" ]; then
        APP_DIRS+=("$dir")
    fi
done

echo "Outputting apk's in $APKS_DIR"
if [ ! -d "$APKS_DIR" ]; then
    mkdir apks
elif [ ! -z "$(ls -A $APKS_DIR)"  ]; then
    echo "Removing old apks"
    rm -r "$APKS_DIR"/*
fi

# For each app, build it using Gradle and move the apk to /apks
NR_APPS_DECR=$(( ${#APP_DIRS[@]} - 1 ))
for i in $(
        seq 0 $NR_APPS_DECR
    )
do
    DIR="${APP_DIRS[i]}"
    echo -ne "\r $(( ${i} + 1))/$(( ${NR_APPS_DECR} + 1)) \t Building $DIR ... \t"
    cd "$DIR"
    # To obtain a new apk (different hash), we increment the version in the gradle script
    sed -i 's/\(.*versionCode \)\([0-9]\+\)/echo "\1$((\2 + 1))"/e' app/build.gradle
    "./gradlew" tasks build > /dev/null
    RC=$?
    RC=0
    cd $SCRIPT_DIR

    if [ $RC = 0 ]; then
        echo Done
        cp "${DIR}/app/build/outputs/apk/debug/app-debug.apk" "${APKS_DIR}/$(basename ${DIR}).apk"
    else
        echo Failed
    fi
done

#######################################################
# Invoke Python script to submit samples to analysers #
#######################################################

echo "Invoking python script ..."
date=$(date +%F__%H-%M)
mkdir "results/$date"
apks=./"$APKS_DIR"/*.apk
python3 -u submit_samples.py "results/$date" $apks
