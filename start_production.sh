#!/usr/bin/bash

# Check if the .env file exists
if [ -f .env ]; then
	echo "Loading environment variables from .env file..."
	export $(cat .env | grep -v '#')

	# start the server
	./gradlew build

	echo "Related files: "
	ls build/libs/nametrek*

	echo "Launching..."
	java -jar build/libs/nametrek_backend-0.0.1-SNAPSHOT.jar
else 
	echo "No .env file found. Please create one"
fi
