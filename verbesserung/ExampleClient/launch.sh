#!/bin/bash

# Set the number of iterations (default to 20 if not provided)
N=${1:-20}

GAMEID_URL="https://swe1.wst.univie.ac.at/games?enableDummyCompetition=true"
JAR_PATH="build/libs/ExampleClient.jar"
SERVER_URL="http://swe1.wst.univie.ac.at:18235"
LOGS_DIR="logs"

# Remove existing logs directory if it exists
if [ -d "$LOGS_DIR" ]; then
  rm -rf "$LOGS_DIR"
fi

mkdir "$LOGS_DIR"

# Loop N times
for ((i=1; i<=N; i++)); do
  echo "Iteration $i: Fetching GameID..."

  response=$(curl -s "$GAMEID_URL")
  game_id=$(echo "$response" | sed -n 's:.*<uniqueGameID>\(.*\)</uniqueGameID>.*:\1:p')

  if [[ -z "$game_id" ]]; then
    echo "Error: Unable to extract GameID from the response."
    continue
  fi

  echo "Iteration $i: Retrieved GameID: $game_id"

  # Define the output log file name
  output_log="output_${i}.log"

  echo "Iteration $i: Launching Java program..."

  # delete old logs
  if [ -f "map_log.txt" ]; then
    rm "map_log.txt"
  fi

  java -jar "$JAR_PATH" dummy "$SERVER_URL" "$game_id" > "$output_log" 2>&1

  if [ $? -ne 0 ]; then
    echo "Iteration $i: Java program encountered an error. Check $output_log for details."
    continue
  fi

  # Define the new names for the logs
  new_output_log="${LOGS_DIR}/output_${i}.log"
  new_map_log="${LOGS_DIR}/map_log_${i}.txt"

  # Move and rename the output log
  mv "$output_log" "$new_output_log"

  # Move and rename the map_log.txt if it exists
  if [ -f "map_log.txt" ]; then
    mv "map_log.txt" "$new_map_log"
  else
    echo "Iteration $i: Warning: map_log.txt not found."
  fi

  echo "Iteration $i: Logs saved to $LOGS_DIR."
done

echo "All iterations completed."
