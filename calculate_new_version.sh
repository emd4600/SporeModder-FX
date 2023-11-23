#!/bin/bash

# Get a string like 2.5.6
current_version=$(git describe --tags --abbrev=0 | cut -c2- )

# Separate the string into an array
IFS='.' read -ra numbers <<< "$current_version"

new_build_number=$(( numbers[2] + 1 ))
echo "${numbers[0]}.${numbers[1]}.$new_build_number"