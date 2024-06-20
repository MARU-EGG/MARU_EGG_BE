#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu/app"

# Stop the currently running application
/home/ubuntu/app/stop.sh

# Copy the new jar file
cp $PROJECT_ROOT/build/libs/*.jar $PROJECT_ROOT/MARU_EGG_BE.jar

# Start the application
/home/ubuntu/app/start.sh