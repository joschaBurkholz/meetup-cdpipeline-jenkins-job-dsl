def config = Config.getConfig(DOCKER_REGISTRY)
def gitBaseUrl = config.gitBaseUrl
def gitCredentials = config.gitCredentials

job("meetup-deployment-for-promote") {
    parameters {
        stringParam("INVENTORY", "meetup-hello-world-infrastructure", "Der Name des Ansible Inventories")
        stringParam("ENVIRONMENT", "dev", "Der Name der Umgebung (Unterverzeichnis im Inventory-Repository).")
        stringParam("APPLICATION", "meetup-hello-world", "meetup-hello-world")
        stringParam("BUILD_ID", "1", "...")
    }
    wrappers {
        colorizeOutput('xterm')
    }
    multiscm {
        git {
            remote {
                url("${gitBaseUrl}/\${INVENTORY}")
                credentials(gitCredentials)
            }
            branch('master')
        }
        git {
            remote {
                url("${gitBaseUrl}/meetup-cdpipeline-ansible-roles")
                credentials(gitCredentials)
            }
            branch('master')
            extensions {
                relativeTargetDirectory('roles')
            }
        }
    }
    steps {
        // look up APPLICATION and VERSION
        copyArtifacts("\${JOB_NAME}") {
            includePatterns('build.properties')
            buildSelector {
                buildNumber("\${BUILD_ID}")
            }
        }
        environmentVariables {
            propertiesFile('build.properties')
        }
        ansiblePlaybook("deploy-\${APPLICATION}.yml") {
            ansibleName('ansible')
            credentialsId('ansible_ssh_keys')
            inventoryPath("\${ENVIRONMENT}")
            sudo(true)
            additionalParameters("--extra-vars \"version=\${VERSION}\" --private-key=~/.ssh/ansible")
            colorizedOutput(true)
        }
        publishers {
            groovyPostBuild {
                script("manager.addShortText(manager.build.getEnvironment(manager.listener)[\'ENVIRONMENT\'] + ' - ' + manager.build.getEnvironment(manager.listener)['VERSION'])")
                sandbox(false)
            }
        }
    }
}
