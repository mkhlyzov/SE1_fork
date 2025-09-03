#!/bin/bash

# Set the number of iterations (default to 20 if not provided)
N=${1:-20}

JAR_PATH="build/libs/ExampleClient.jar"
LOGS_DIR="logs/logs_algo"

# Print start time
start_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "Script started at: $start_time"

# Remove existing logs directory if it exists
if [ -d "$LOGS_DIR" ]; then
  rm -rf "$LOGS_DIR"
fi

mkdir -p "$LOGS_DIR"

# Loop N times
for ((i=1; i<=N; i++)); do
  echo "Iteration $i"

  # Define the output log file name
  output_log="${LOGS_DIR}/output_${i}.txt"

  echo "Iteration $i: Launching Java program..."
  java -jar "$JAR_PATH" > "$output_log" 2>&1

  if [ $? -ne 0 ]; then
    echo "Iteration $i: Java program encountered an error. Check $output_log for details."
    continue
  fi

  echo "Iteration $i: Logs saved to $LOGS_DIR."
done

# Print end time
end_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "Script finished at: $end_time"