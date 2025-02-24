#!/usr/bin/env bash

# Check if the .env file exists
if [ -f .env ]; then
	echo "Loading environment variables from .env file..."
	export $(cat .env | grep -v '#')

	echo "Variables: "
	echo $GMAIL_USERNAME
	echo $GMAIL_PASSWORD
	# start the server
	./gradlew bootRun
else 
	echo "No .env file found. Please create one"
fi
