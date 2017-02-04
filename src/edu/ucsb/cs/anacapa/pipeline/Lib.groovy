package edu.ucsb.cs.anacapa.pipeline

import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic

class Lib implements Serializable {
  static def mysh(script, args) {
    script.sh "${args}"
  }

  static def parseJSON(text) {
    final slurper = new JsonSlurperClassic()
    return new HashMap<>(slurper.parseText(text))
  }
  
  static def jsonString(obj) {
    return new JsonBuilder(obj).toPrettyString()
  }
}
