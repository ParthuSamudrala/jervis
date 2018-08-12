/*
   Copyright 2014-2018 Sam Gleske - https://github.com/samrocketman/jervis

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   */
/*
   This variable is intended to build a project on a node in a non-matrix
   build.  It assumes a node for building is already provisioned for it.
 */

import net.gleske.jervis.lang.lifecycleGenerator
import net.gleske.jervis.lang.pipelineGenerator

def call(def global_scm, lifecycleGenerator generator, pipelineGenerator pipeline_generator, List jervisEnvList, String script_header, String script_footer) {
    Map stashMap = pipeline_generator.stashMap
    stage("Build Project") {
        checkout global_scm
        withEnvSecretWrapper(pipeline_generator, jervisEnvList) {
            String environment_string = sh(script: 'env | LC_ALL=C sort', returnStdout: true).split('\n').join('\n    ')
            echo "ENVIRONMENT:\n    ${environment_string}"
            sh(script: [
                script_header,
                generator.generateAll(),
                script_footer
            ].join('\n').toString())
        }
        for(String name : stashMap.keySet()) {
            stash allowEmpty: stashMap[name]['allow_empty'], includes: stashMap[name]['includes'], name: name, useDefaultExcludes: stashMap[name]['use_default_excludes']
        }
    }
}
