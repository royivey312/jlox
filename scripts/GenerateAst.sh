#!/usr/bin/env bash
#
# GenerateAst.sh
#
# This script will generate a Abstract Syntax Tree class in java based on a descriptor
#

function usage() {
  echo "Usage: ${0##*/} <descriptor-file> <output-file>"
}

function printGenerationDetails() {
  echo "${parentClassName}>${innerClassName}"
  for elem in "${elemArray[@]}"; do
    echo "  ${elem}"
  done
}

function writeTop() {
  echo "package com.iind.lox;"
  echo 
  echo "public abstract class ${parentClassName} {"
  echo
}

function writeVisitorInterface() {
  echo "  interface Visitor<R> {"

# Generate Visitor methods
  while IFS=":" read -r innerClassName other; do
    echo "    R visit${innerClassName}${parentClassName}(${innerClassName} ${innerClassName,*});"
  done <<< $(tail -n +2 ${descriptor})
  echo "  }"
  echo

  # Generate abstract template function for Visitor pattern
  echo "  abstract <R> R accept(Visitor<R> visitor);"
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

  # Add Visitor pattern
  echo
  echo "    <R> R accept(Visitor<R> visitor) {"
  echo "      return visitor.visit${innerClassName}${parentClassName}(this);"
  echo "    }"
  echo "  }"
  echo
}

function writeBottom() {
  echo "}"
}

function main() {
  descriptor="${1?$(usage)}"
  javaFileName="${2?${usage}}"

  # Exit if provided descriptor is not a file
  [ ! -f "${descriptor}" ] && exit 75

  parentClassName=${javaFileName/.java}

  # Start fresh file
  writeTop > ${javaFileName}

  # Write Visitor Interface
  writeVisitorInterface >> ${javaFileName}

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

  cat ${javaFileName}
}

main "$@"
