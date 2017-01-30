package edu.ucsb.cs.anacapa.pipeline

import groovy.json.JsonSlurperClassic

class Lib implements Serializable {
  static def mysh(script, args) {
    script.sh "${args}"
  }

  def parseJSON(text) {
    final slurper = new JsonSlurper()
    return new HashMap<>(slurper.parseText(text))
  }
}
