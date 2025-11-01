#!/bin/bash

# Set the number of iterations (default to 20 if not provided)
N=${1:-20}

GAMEID_URL="https://swe1.wst.univie.ac.at/games?enableDummyCompetition=true"
JAR_PATH="build/libs/ExampleClient.jar"
SERVER_URL="http://swe1.wst.univie.ac.at:18235"


echo "Fetching GameID..."

response=$(curl -s "$GAMEID_URL")
game_id=$(echo "$response" | sed -n 's:.*<uniqueGameID>\(.*\)</uniqueGameID>.*:\1:p')

if [[ -z "$game_id" ]]; then
echo "Error: Unable to extract GameID from the response."
exit 1
fi

echo "Iteration $i: Retrieved GameID: $game_id"
echo "Iteration $i: Launching Java program..."

# delete old logs
if [ -f "map_log.txt" ]; then
rm "map_log.txt"
fi

java -jar "$JAR_PATH" dummy "$SERVER_URL" "$game_id"
# java -Xmx200M -Xms96M -Djava.security.manager -Djava.security.policy==http://swe1.wst.univie.ac.at:18235/policy -Dsun.misc.URLClassPath.disableJarChecking=true -jar "$JAR_PATH" TR "$SERVER_URL" "$game_id"

if [ $? -ne 0 ]; then
echo "Iteration $i: Java program encountered an error."
fi

echo "Game finished."

