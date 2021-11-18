#!/bin/bash

#
# Author: Timme Bethe
#
#
#
#
#
#


### Main script starts here


################################
# Build apps using gradle      #
################################

# Filter directories with gradle wrapper
for dir in *; do
    if [ -f "$dir/gradlew" ]; then
        APP_DIRS+=("$dir")
    fi
done

echo "Outputting apk's in ./apks"
if [ ! -d "apks" ]; then
    mkdir apks
elif [ ! -z "$(ls -A apks)"  ]; then
    rm -r apks/*
fi


# For each app, build it using Gradle and move the apk to /apks
NR_APPS_DECR=$(( ${#APP_DIRS[@]} - 1 ))
for i in $(
        seq 0 $NR_APPS_DECR
    )
do
    DIR="${APP_DIRS[i]}"
    echo -ne "\r ${i}/${NR_APPS_DECR} \t Building $DIR ... \t"
    cd "$DIR"
    "./gradlew" --quiet tasks build > /dev/null
    RC=$?
    cd ..

    if [ $RC = 0 ]; then
        echo Done
        cp "${DIR}/app/build/outputs/apk/debug/app-debug.apk" "apks/${DIR}.apk"
    else
        echo Failed
    fi
done

########################################################
# Invoke Python script to submit samples to analysers. #
########################################################

echo "Invoking python script ..."
date=$(date +%F__%H-%M)
mkdir "results/$date"
apks=./apks/*.apk
python3 -u submit_samples.py "results/$date" $apks
