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
  
  static def jsonString(obj, pretty = false) {
    def json = new JsonBuilder(obj)
    if (pretty) {
      return json.toPrettyString()
    } else {
      return json.toString()
    }
  }
}
