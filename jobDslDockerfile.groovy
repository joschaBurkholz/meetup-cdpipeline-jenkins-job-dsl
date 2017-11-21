def config = Config.getConfig(DOCKER_REGISTRY)
def jenkinsJobsApi = new JenkinsJobsApi()
def projects = jenkinsJobsApi.getDockerProjects()


println "############################################################################################################"
println "Reading project configuration from json"
println "############################################################################################################"
println "Iterating all projects"
println ""
projects.each {project ->

  def jobName = "${project.name}"
  // docker projects has name convention "docker-foo-bar" with "foo-bar" as imagename
  def imagename = project.name.split("-", 3)[2] // remove "cdpipeline-docker-" part from the name
  def gitCredentials = config.gitCredentials

  def gitUrl = "${project.ssh_url_to_repo}";

  println "############################################################################################################"
  println "Creating Docker Build Job ${jobName}"

  job(jobName) {
    scm {
      git {
        remote {
          url("${gitUrl}")
          credentials("${gitCredentials}")

        }
        branch("master")
        extensions {
          cleanBeforeCheckout()
          wipeOutWorkspace()
          gitTagMessageExtension()
        }
      }
    }
    if(config.enableBuildTrigger) {
      triggers {
        scm("H/5 * * * *")
      }
    }
    steps {
      shell("docker build -t ${imagename} ${config.httpProxyBuildArg} .")
      shell("docker tag ${imagename} \${DOCKER_REGISTRY}/${imagename}")
      shell("docker push \${DOCKER_REGISTRY}/${imagename}")
    }
    wrappers {
      maskPasswords()
      release {
        postSuccessfulBuildSteps {
          shell("docker tag ${imagename} \${DOCKER_REGISTRY}/${imagename}:\${RELEASE_VERSION}")
          shell("docker push \${DOCKER_REGISTRY}/${imagename}")
          shell("git tag \${RELEASE_VERSION}")
          shell("git push origin \${RELEASE_VERSION}")
        }
      }
    }
  }
}
