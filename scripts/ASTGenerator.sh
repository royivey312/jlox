#!/usr/bin/env bash

function usage() {
  echo "Usage: ./AstGenerator.sh <descriptor-file> <output-file>"
}

function printGenerationDetails() {
  echo "${parentClassName}>${innerClassName}"
  # for ((i = 0; i < "${#elemArray[*]}"; i++)); do
  for elem in "${elemArray[@]}"; do
    echo "  ${elem}"
  done
}

function writeTop() {
  echo "package com.iind.lox;"
  echo 
  echo "import com.iind.lox.Token;"
  echo
  echo "public abstract class ${parentClassName} {"
  echo
}

function writeInnerStaticClass() {
  # Add class
  echo "  static class ${innerClassName} extends ${parentClassName} {"

  # Add Fields
  for elem in "${elemArray[@]}"; do 
    echo "    final ${elem};"
  done
  echo

  # Add Constructor
  echo -n "    ${innerClassName}("

  # Add Constructor Parameters
  for elem in "${elemArray[@]}"; do
    if [ "$elem" != "${elemArray[-1]}" ]; then
      echo -n "${elem}, "
    else
      echo "${elem}) {"
    fi
  done

  # Add Constructor body
  for elem in "${elemArray[@]}"; do
    name=${elem/* }
    echo "      this.${name} = ${name};"
  done
  echo "    }"
  echo "  }"
  echo
}

function writeBottom() {
  echo "}"
}

# MAINLINE
descriptor="${1?$(usage)}"
javaFileName="${2?${usage}}"

# Exit if provided descriptor is not a file
[ ! -f "${descriptor}" ] && exit 75

parentClassName=${javaFileName/.java}

# Start fresh file
writeTop > ${javaFileName}

# Read Descriptor File, skipping header row
while IFS=":" read -r innerClassName elements; do
  readarray -td , elemArray <<< "${elements}" # Generate Array from CSV
  for ((i = 0; i < ${#elemArray[@]}; i++)); do
    elemArray[$i]=$(echo -n "${elemArray[$i]}"|sed 's/\n//g')
  done

  printGenerationDetails
  writeInnerStaticClass >> "${javaFileName}"
done <<< "$(tail -n +2 "${descriptor}")"

writeBottom >> ${javaFileName}

#cat ${javaFileName}
