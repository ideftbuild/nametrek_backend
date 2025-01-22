#!/usr/bin/bash

# Check if the .env file exists
if [ -f .env ]; then
	echo "Loading environment variables from .env file..."
	export $(cat .env | grep -v '#')

	# start the server
	./gradlew bootRun
else 
	echo "No .env file found. Please create one"
fi
