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

  static def slugify(str) {
    return str.replaceAll("[\\W]+", "-")
  }

  static def get_envvars(script) {
    script.sh 'env > .env.tmp'
    script.sh 'cat .env.tmp'
    def evars_list = script.readFile('.env.tmp').split("\\r?\\n")
    script.sh 'rm .env.tmp'
    def evars = [:]
    for (int index = 0; index < evars.size(); index++) {
        def i = index
        def keys = evars_list[i].split("=")
        evars[keys[0]] = keys[1]
    }
    return evars
  }
}
