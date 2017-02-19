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
    def evars = [:]
    script.sh 'env > .env.tmp'
    script.sh 'cat .env.tmp'
    def evars_list = script.readFile('.env.tmp') =~ /([^={}]+)=((\(\)\s*\{[^\{\}]+\}\s*)|(.+)\s*|(\s*))/
    for (int index=0; index < evars_list.size(); index++) {
        def i = index
        evars[evars_list[i][1]] = evars_list[i][2]
    }
    return evars
  }
}
