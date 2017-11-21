def config = Config.getConfig(DOCKER_REGISTRY)
def gitBaseUrl = config.gitBaseUrl
def gitCredentials = config.gitCredentials

job("ansible-deployment") {
    parameters {
        stringParam("APPLICATION", "meetup-hello-world", "Der Name der Applikation")
        stringParam("INVENTORY", "meetup-hello-world-infrastructure", "Der Name des Ansible Inventories")
        stringParam("ENVIRONMENT", "dev", "Der Name der Umgebung (Unterverzeichnis im Inventory-Repository).")
        stringParam("VERSION", "1.0.0-SNAPSHOT", "Die Version der Lieferpakets.")
    }
    wrappers {
        colorizeOutput('xterm')
    }
    logRotator {
        numToKeep(30)
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
        ansiblePlaybook("deploy-\${APPLICATION}.yml") {
            ansibleName('ansible')
            credentialsId('ansible_ssh_keys')
            inventoryPath("\${ENVIRONMENT}")
            sudo(true)
            additionalParameters("--extra-vars \"version=\${VERSION}\" --private-key=~/.ssh/ansible")
            colorizedOutput(true)
        }
    }
}
job("ansible-run-playbook") {
    parameters {
        stringParam("APPLICATION", "site", "Der Name des Playbooks (site fuer alle)")
        stringParam("INVENTORY", "meetup-hello-world-infrastructure", "Der Name des Ansible Inventories")
        stringParam("ENVIRONMENT", "dev", "Der Name der Umgebung (Unterverzeichnis im Inventory-Repository).")
    }
    wrappers {
        colorizeOutput('xterm')
    }
    logRotator {
        numToKeep(30)
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
        ansiblePlaybook("\${APPLICATION}.yml") {
            ansibleName('ansible')
            credentialsId('ansible_ssh_keys')
            inventoryPath("\${ENVIRONMENT}")
            sudo(true)
            additionalParameters("--private-key=~/.ssh/ansible")
            colorizedOutput(true)
        }
    }
}
